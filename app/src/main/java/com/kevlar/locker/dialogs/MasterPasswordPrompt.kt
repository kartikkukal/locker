package com.kevlar.locker.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kevlar.locker.R

fun createMasterPasswordPrompt(inflater: LayoutInflater, context: Context, callback: (String) -> Unit) {
    val masterPasswordPromptView = inflater.inflate(R.layout.master_password_prompt, null)

    val masterPasswordPromptBuilder = MaterialAlertDialogBuilder(context)
        .setView(masterPasswordPromptView)

    val masterPasswordPrompt = masterPasswordPromptBuilder.show()

    masterPasswordPromptView.findViewById<Button>(R.id.master_password_prompt_cancel).setOnClickListener {
        masterPasswordPrompt.cancel()
    }

    masterPasswordPromptView.findViewById<Button>(R.id.master_password_prompt_continue).setOnClickListener {

        val masterPassword = masterPasswordPromptView.findViewById<EditText>(R.id.master_password_prompt_password_input).text.toString()
        if(masterPassword == "") {
            return@setOnClickListener
        }
        callback(masterPassword)
        masterPasswordPrompt.cancel()
    }
}