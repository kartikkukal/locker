package com.kevlar.locker.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kevlar.locker.R

fun createLicensesDialog(inflater: LayoutInflater, context: Context) {
    val licensesDialogView = inflater.inflate(R.layout.license_dialog, null)

    val licensesDialogBuilder = MaterialAlertDialogBuilder(context)
        .setView(licensesDialogView)

    val licensesTextView = licensesDialogView.findViewById<TextView>(R.id.licenses_text_view)
    var licenseTextString = String()
    val fileReaderPoppins = context.resources.openRawResource(R.raw.poppins_license)

    while (true) {
        val byte = fileReaderPoppins.read()

        if (byte == -1) {
            break
        }

        licenseTextString += byte.toChar()
    }

    val fileReaderGPL = context.resources.openRawResource(R.raw.gplv3_license)

    while (true) {
        val byte = fileReaderGPL.read()

        if (byte == -1) {
            break
        }

        licenseTextString += byte.toChar()
    }

    licensesTextView.text = licenseTextString
    licensesDialogBuilder.show()
}