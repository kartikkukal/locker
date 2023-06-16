package com.kevlar.locker.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.kevlar.locker.PasswordManager
import com.kevlar.locker.R
import com.kevlar.locker.dialogs.createMasterPasswordPrompt
import com.kevlar.locker.manager.getMasterKey
import com.kevlar.locker.manager.createAccount
import com.kevlar.locker.snackbar.passwordIncorrectSnackBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddAccountDialog : DialogFragment() {

    private lateinit var dialogLayout: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.dialogLayout = inflater.inflate(R.layout.account_add_fragment, container, false)
        setOnClickListeners()
        return dialogLayout
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }

    private fun setOnClickListeners() {
        this.dialogLayout.findViewById<Button>(R.id.close_add_account_dialog_button).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        this.dialogLayout.findViewById<Button>(R.id.add_account_add_account_button).setOnClickListener {
            val service = this.dialogLayout.findViewById<EditText>(R.id.add_account_service_name_input).text.toString().trim()
            val identity = this.dialogLayout.findViewById<EditText>(R.id.add_account_identity_input).text.toString().trim()
            val password = this.dialogLayout.findViewById<EditText>(R.id.add_account_password_input).text.toString()

            if(service == "" || identity == "" || password == "") {
                return@setOnClickListener
            }

            createMasterPasswordPrompt(layoutInflater, this.requireContext(), fun (masterPassword: String) {
                this.onMasterPasswordInputHandler(service, identity, password, masterPassword)
            })
        }
    }

    private fun onMasterPasswordInputHandler(service: String, identity: String, password: String, masterPassword: String) {
        val context = this.requireContext()
        val activity = (requireActivity() as PasswordManager)

        var masterKey = ""

        try {
            masterKey = getMasterKey(context, masterPassword)

        } catch(_: Exception) {
            passwordIncorrectSnackBar(this.dialogLayout, this.resources)

            return
        }

        val account = createAccount(
            service,
            identity,
            password,
            masterKey
        )

        lifecycleScope.launch(Dispatchers.IO) {
            activity.passwordManager.insertAccount(account)
        }

        activity.updateAccountInserted(account)
        activity.supportFragmentManager.popBackStack()
    }
}