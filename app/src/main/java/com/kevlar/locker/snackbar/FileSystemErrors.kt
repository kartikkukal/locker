package com.kevlar.locker.snackbar

import android.content.res.Resources
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.kevlar.locker.R

fun errorWhileSaving(layout: View, resources: Resources) {
    Snackbar.make(layout, resources.getText(R.string.save_file_error), Snackbar.LENGTH_LONG).show()
}
fun invalidDatabaseError(layout: View, resources: Resources) {
    Snackbar.make(layout, resources.getText(R.string.invalid_database), Snackbar.LENGTH_LONG).show()
}