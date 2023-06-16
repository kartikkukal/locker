package com.kevlar.locker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.kevlar.locker.adapters.AccountsViewAdapter
import com.kevlar.locker.adapters.SearchViewAdapter
import com.kevlar.locker.dialogs.createImportDatabasePrompt
import com.kevlar.locker.dialogs.createMasterPasswordPrompt
import com.kevlar.locker.fragments.AddAccountDialog
import com.kevlar.locker.fragments.EditAccountDialog
import com.kevlar.locker.manager.Account
import com.kevlar.locker.manager.PasswordManager
import com.kevlar.locker.manager.createAccount
import com.kevlar.locker.manager.createDatabase
import com.kevlar.locker.manager.decryptString
import com.kevlar.locker.manager.encryptString
import com.kevlar.locker.manager.getMasterKey
import com.kevlar.locker.manager.shared_preference_name
import com.kevlar.locker.snackbar.errorWhileSaving
import com.kevlar.locker.snackbar.invalidDatabaseError
import com.kevlar.locker.snackbar.passwordIncorrectSnackBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class PasswordManager: AppCompatActivity() {

    @Serializable
    private data class Database(
        val cipher: String,
        val salt: String,
        val iv: String,
    )

    private lateinit var searchViewAdapter: SearchViewAdapter
    lateinit var accountsListAdapter: AccountsViewAdapter
    lateinit var passwordManager: PasswordManager

    private lateinit var writeBuffer: ByteArray

    private val saveExportedDatabase = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {
        if(it == null) {
            return@registerForActivityResult
        }

        val outputStream = this.contentResolver.openOutputStream(it)
            ?: throw Exception("Error while writing file: OutputStream is null")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                outputStream.write(writeBuffer)
                outputStream.flush()
                outputStream.close()

            } catch(_: Exception) {
                runOnUiThread {
                    errorWhileSaving(findViewById(R.id.main_layout), resources)
                }
                return@launch
            }
        }
    }

    private val readExportedDatabase = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        if(it == null) {
            return@registerForActivityResult
        }

        val inputStream = this.contentResolver.openInputStream(it) ?: return@registerForActivityResult
        importDatabase(inputStream.bufferedReader().use { buffer -> buffer.readText() })
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        if(!this.getSharedPreferences(shared_preference_name, Context.MODE_PRIVATE).getBoolean("setup", false)) {
            val introductionActivityIntent = Intent(applicationContext, IntroductionActivity::class.java)
            startActivity(introductionActivityIntent)

            finish()
        }

        installSplashScreen()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            setupDatabase(applicationContext)
        }

        setContentView(R.layout.password_manager)
        setOnClickListeners()
        startSearchView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.password_manager_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.export_database -> {
                exportAndSaveDatabase()
                true
            }
            R.id.import_database -> {
                readExportedDatabase.launch(arrayOf("application/json"))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportAndSaveDatabase() {

        fun exportDatabase(accounts: List<Account>, masterKey: String, masterPassword: String): ByteArray {
            val mutableList = accounts.toMutableList()
            mutableList.forEachIndexed { index, account ->
                val password = decryptString(account.password, masterKey, account.iv, account.salt)
                mutableList[index] = Account(account.id, account.service, account.identity, password, "", "")
            }

            val exported = Json.encodeToString(mutableList)
            val (cipher, iv, salt) = encryptString(exported, masterPassword)

            return Json.encodeToString(Database(cipher, salt, iv)).toByteArray()
        }

        fun getAllAccounts(masterPassword: String) {

            var masterKey: String
            try {
                masterKey = getMasterKey(applicationContext, masterPassword)
            } catch(_: Exception) {
                passwordIncorrectSnackBar(findViewById(R.id.main_layout), this.resources)
                return
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val accounts = passwordManager.allAccounts()
                writeBuffer = exportDatabase(accounts, masterKey, masterPassword)
                runOnUiThread {
                    saveExportedDatabase.launch("exported.json")
                }
            }
        }

        createMasterPasswordPrompt(layoutInflater, this, ::getAllAccounts)
    }

    private fun importDatabase(fileContents: String) {
        createImportDatabasePrompt(layoutInflater, this, fun (databasePassword: String, currentPassword: String) {
            val masterKey: String
            try {
                masterKey = getMasterKey(applicationContext, currentPassword)
            } catch(_: Exception) {
                passwordIncorrectSnackBar(findViewById(R.id.main_layout), this.resources)
                return
            }

            val database: Database
            try {
                database = Json.decodeFromString(fileContents)
            } catch(_: Exception) {
                invalidDatabaseError(findViewById(R.id.main_layout), this.resources)
                return
            }

            val decrypted: String
            try {
                decrypted = decryptString(database.cipher, databasePassword, database.iv, database.salt)
            } catch(_: Exception) {
                passwordIncorrectSnackBar(findViewById(R.id.main_layout), this.resources)
                return
            }

            if(decrypted == "") {
                return
            }

            val accounts: MutableList<Account>
            try {
                accounts = Json.decodeFromString<List<Account>>(decrypted).toMutableList()
            } catch(_: Exception) {
                invalidDatabaseError(findViewById(R.id.main_layout), this.resources)
                return
            }

            accounts.forEachIndexed { index, account ->
                accounts[index] = createAccount(account.service, account.identity, account.password, masterKey)
            }

            val overwriteDatabasePrompt = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.overwrite_database)
                .setMessage(R.string.overwrite_database_prompt)
                .setPositiveButton(R.string.yes) { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        passwordManager.deleteAll()
                        passwordManager.insertList(accounts.toList())
                    }

                    accountsListAdapter.accountsList.forEach { _ ->
                        accountsListAdapter.notifyItemRemoved(0)
                    }

                    accountsListAdapter.accountsList = mutableListOf()

                    accounts.forEach {
                        accountsListAdapter.insertAccount(it)
                    }

                    updateNoAccountMessage()
                }
                .setNegativeButton(R.string.no) { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        passwordManager.insertList(accounts.toList())
                    }
                    accounts.forEach {
                        accountsListAdapter.insertAccount(it)
                    }

                    updateNoAccountMessage()
                }

            overwriteDatabasePrompt.show()
        })
    }

    private fun setOnClickListeners() {
        findViewById<FloatingActionButton>(R.id.add_account_button).setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            transaction
                .add(android.R.id.content, AddAccountDialog())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun startSearchView() {
        val searchBar = findViewById<SearchBar>(R.id.search_bar)
        val searchView = findViewById<SearchView>(R.id.search_view)

        val searchRecycler = findViewById<RecyclerView>(R.id.search_view_recycler)

        this.searchViewAdapter = SearchViewAdapter(mutableListOf(), ::onAccountClickedListener)

        searchRecycler.adapter = this.searchViewAdapter
        searchRecycler.layoutManager = LinearLayoutManager(this)

        setSupportActionBar(searchBar)
        searchView.setupWithSearchBar(searchBar)

        searchView.editText.addTextChangedListener {
            searchAccountsView(searchView.editText.text.toString())
        }
    }

    private fun onAccountClickedListener(account: Account) {
        createMasterPasswordPrompt(layoutInflater, this, fun(masterPassword: String) {
            var masterKey: String
            try {
                masterKey = getMasterKey(applicationContext, masterPassword)
            } catch(_: Exception) {
                passwordIncorrectSnackBar(findViewById(R.id.main_layout), this.resources)
                return
            }

            val password = decryptString(account.password, masterKey, account.iv, account.salt)

            val decrypted = Account(
                account.id,
                account.service,
                account.identity,
                password,
                account.iv,
                account.salt
            )

            val transaction = supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            transaction
                .add(android.R.id.content, EditAccountDialog(decrypted))
                .addToBackStack(null)
                .commit()
        })
    }

    private fun startAccountsView(accountsList: List<Account>) {
        val accountsView = findViewById<RecyclerView>(R.id.accounts_view)

        this.accountsListAdapter = AccountsViewAdapter(accountsList.toMutableList(), ::onAccountClickedListener)

        accountsView.adapter = this.accountsListAdapter
        accountsView.layoutManager = LinearLayoutManager(this)

        if(this.accountsListAdapter.itemCount == 0) {
            findViewById<TextView>(R.id.no_accounts_message).visibility = View.VISIBLE
        }
    }

    private fun searchAccountsView(query: String) {
        @SuppressLint("NotifyDataSetChanged")
        fun setSearchList(filtered: List<Account>) {
            this.searchViewAdapter.updateSearchList(filtered)

            val searchPromptText = findViewById<TextView>(R.id.search_prompt_text)

            if(filtered.isNotEmpty()) {
                searchPromptText.visibility = View.GONE
            } else {
                if(query.isEmpty()) {
                    searchPromptText.text = resources.getText(R.string.search_your_accounts)
                    searchPromptText.visibility = View.VISIBLE
                } else {
                    searchPromptText.text = resources.getText(R.string.no_account_found)
                    searchPromptText.visibility = View.VISIBLE
                }

            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            var filtered = passwordManager.searchAccounts("%$query%")
            if(query == "") {
                filtered = emptyList()
            }
            runOnUiThread {
                setSearchList(filtered)
            }
        }
    }

    private suspend fun setupDatabase(applicationContext: Context) {
        this.passwordManager = createDatabase(applicationContext)
        val allAccounts = this.passwordManager.allAccounts()
        runOnUiThread {
            this.startAccountsView(allAccounts)
        }
    }

    private fun updateNoAccountMessage() {
        if(accountsListAdapter.itemCount == 0) {
            findViewById<TextView>(R.id.no_accounts_message).visibility = View.VISIBLE
        } else {
            findViewById<TextView>(R.id.no_accounts_message).visibility = View.GONE
        }
    }

    fun updateAccountInserted(account: Account) {
        this.accountsListAdapter.insertAccount(account)

        findViewById<TextView>(R.id.no_accounts_message).visibility = View.GONE
    }

    fun updateAccountRemoved(id: String) {
        this.accountsListAdapter.deleteAccount(id)

        if(this.accountsListAdapter.accountsList.isEmpty()) {
            findViewById<TextView>(R.id.no_accounts_message).visibility = View.VISIBLE
        }
    }
}