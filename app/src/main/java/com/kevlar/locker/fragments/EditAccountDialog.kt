package com.kevlar.locker.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.kevlar.locker.PasswordManager
import com.kevlar.locker.R
import com.kevlar.locker.dialogs.createMasterPasswordPrompt
import com.kevlar.locker.manager.Account
import com.kevlar.locker.manager.getMasterKey
import com.kevlar.locker.manager.encryptString
import com.kevlar.locker.snackbar.passwordIncorrectSnackBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditAccountDialog(private var account: Account) : DialogFragment() {

    private lateinit var dialogLayout: View

    private lateinit var serviceNameInput: EditText
    private lateinit var identityInput: EditText
    private lateinit var passwordInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialogLayout = inflater.inflate(R.layout.account_edit_fragment, container, false)
        setOnEditListeners()
        setOnClickListeners()
        return dialogLayout
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }

    private fun setOnEditListeners() {
        serviceNameInput = this.dialogLayout.findViewById(R.id.edit_account_service_name_input)
        identityInput = this.dialogLayout.findViewById(R.id.edit_account_identity_input)
        passwordInput = this.dialogLayout.findViewById(R.id.edit_account_password_input)

        serviceNameInput.setText(this.account.service)
        identityInput.setText(this.account.identity)
        passwordInput.setText(this.account.password)

        var (serviceChanged, identityChanged, passwordChanged) = Triple(false, false, false)

        serviceNameInput.addTextChangedListener {
            serviceChanged = serviceNameInput.text.toString() != this.account.service
            this.updateButtonTextUpdated(serviceChanged, identityChanged, passwordChanged)
        }
        identityInput.addTextChangedListener {
            identityChanged = identityInput.text.toString() != this.account.identity
            this.updateButtonTextUpdated(serviceChanged, identityChanged, passwordChanged)
        }
        passwordInput.addTextChangedListener {
            passwordChanged = passwordInput.text.toString() != this.account.password
            this.updateButtonTextUpdated(serviceChanged, identityChanged, passwordChanged)
        }
    }

    private fun setOnClickListeners() {
        this.dialogLayout.findViewById<Button>(R.id.close_edit_account_dialog_button).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        this.dialogLayout.findViewById<Button>(R.id.update_account_dialog_button).setOnClickListener {
            createMasterPasswordPrompt(layoutInflater, requireContext(), ::updateAccount)
        }
        this.dialogLayout.findViewById<Button>(R.id.delete_account_dialog_button).setOnClickListener {
            createMasterPasswordPrompt(layoutInflater, requireContext(), ::deleteAccount)
        }
        this.dialogLayout.findViewById<Button>(R.id.reset_account_dialog_button).setOnClickListener {
            serviceNameInput.setText(account.service)
            identityInput.setText(account.identity)
            passwordInput.setText(account.password)
        }
    }

    private fun updateButtonTextUpdated(serviceChanged: Boolean, identityChanged: Boolean, passwordChanged: Boolean) {
        val isEnabled = serviceChanged || identityChanged || passwordChanged

        this.dialogLayout.findViewById<Button>(R.id.update_account_dialog_button).isEnabled = isEnabled
        this.dialogLayout.findViewById<Button>(R.id.reset_account_dialog_button).isEnabled = isEnabled
    }

    private fun updateAccount(masterPassword: String) {
        val serviceName = this.dialogLayout.findViewById<EditText>(R.id.edit_account_service_name_input).text.toString().trim()
        val identity = this.dialogLayout.findViewById<EditText>(R.id.edit_account_identity_input).text.toString().trim()
        val password = this.dialogLayout.findViewById<EditText>(R.id.edit_account_password_input).text.toString()

        var masterKey: String
        try {
            masterKey = getMasterKey(requireContext(), masterPassword)
        } catch(_: Exception) {
            passwordIncorrectSnackBar(this.dialogLayout, this.resources)

            return
        }

        val (cipher, iv, salt) = encryptString(password, masterKey)

        val activity = (requireActivity() as PasswordManager)
        val account = Account(this.account.id, serviceName, identity, password, salt, iv,)

        lifecycleScope.launch(Dispatchers.IO) {
            activity.passwordManager.updateAccount(account.id, serviceName, identity, cipher, salt, iv)
        }

        this.account = account
        account.password = cipher

        activity.accountsListAdapter.updateAccount(this.account.id, account)
        this.dialogLayout.findViewById<Button>(R.id.update_account_dialog_button).isEnabled = false
        this.dialogLayout.findViewById<Button>(R.id.reset_account_dialog_button).isEnabled = false
    }

    private fun deleteAccount(masterPassword: String) {
        try {
            getMasterKey(requireContext(), masterPassword)
        } catch(_: Exception) {
            passwordIncorrectSnackBar(this.dialogLayout, this.resources)

            return
        }

        val activity = (requireActivity() as PasswordManager)

        lifecycleScope.launch(Dispatchers.IO) {
            activity.passwordManager.deleteAccountById(account.id)
        }

        activity.supportFragmentManager.popBackStack()
        activity.updateAccountRemoved(this.account.id)
    }
}