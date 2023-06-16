package com.kevlar.locker.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kevlar.locker.R

fun createImportDatabasePrompt(inflater: LayoutInflater, context: Context, callback: (databasePassword: String, currentPassword: String) -> Unit) {
    val importDatabasePromptView = inflater.inflate(R.layout.import_database_prompt, null)

    val importDatabasePromptBuilder = MaterialAlertDialogBuilder(context)
        .setView(importDatabasePromptView)

    val importDatabasePrompt = importDatabasePromptBuilder.show()

    importDatabasePromptView.findViewById<Button>(R.id.import_database_prompt_cancel).setOnClickListener {
        importDatabasePrompt.cancel()
    }

    importDatabasePromptView.findViewById<Button>(R.id.import_database_prompt_continue).setOnClickListener {

        val databasePassword = importDatabasePromptView.findViewById<EditText>(R.id.database_password_input_field).text.toString()
        if(databasePassword == "") {
            return@setOnClickListener
        }

        val currentPassword = importDatabasePromptView.findViewById<EditText>(R.id.current_password_input_field).text.toString()
        if(currentPassword == "") {
            return@setOnClickListener
        }

        callback(databasePassword, currentPassword)
        importDatabasePrompt.cancel()
    }
}