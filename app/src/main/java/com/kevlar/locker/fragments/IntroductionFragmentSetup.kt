package com.kevlar.locker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.kevlar.locker.PasswordManager
import com.kevlar.locker.R
import com.kevlar.locker.manager.setupMasterPassword
import com.kevlar.locker.manager.max_password_length
import com.kevlar.locker.manager.min_password_length
import com.kevlar.locker.snackbar.passwordLengthTooLongSnackBar
import com.kevlar.locker.snackbar.passwordLengthTooShortSnackBar
import com.kevlar.locker.snackbar.passwordsDoNotMatch

class IntroductionFragmentSetup: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.introduction_fragment_setup, container, false)

        val masterPasswordInput = layout.findViewById<EditText>(R.id.master_password)
        val confirmMasterPasswordInput = layout.findViewById<EditText>(R.id.confirm_master_password)

        layout.findViewById<Button>(R.id.finish_setup).setOnClickListener {

            val masterPassword = masterPasswordInput.text.toString();
            val confirmMasterPassword = confirmMasterPasswordInput.text.toString();

            if(masterPassword.length < min_password_length) {
                passwordLengthTooShortSnackBar(layout, this.resources)
                return@setOnClickListener;
            }
            if(masterPassword.length > max_password_length) {
                passwordLengthTooLongSnackBar(layout, this.resources)
                return@setOnClickListener;
            }
            if(masterPassword != confirmMasterPassword) {
                passwordsDoNotMatch(layout, this.resources)
                return@setOnClickListener;
            }

            setupMasterPassword(this.requireContext(), masterPassword)

            val passwordManagerIntent = Intent(requireActivity(), PasswordManager::class.java)
            startActivity(passwordManagerIntent)

            requireActivity().finish()
        }
        return layout
    }
}