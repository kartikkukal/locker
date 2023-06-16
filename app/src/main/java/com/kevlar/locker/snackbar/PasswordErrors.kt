package com.kevlar.locker.snackbar

import android.content.res.Resources
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.kevlar.locker.R

fun passwordLengthTooShortSnackBar(layout: View, resources: Resources) {
    Snackbar.make(layout, resources.getText(R.string.password_too_short), Snackbar.LENGTH_LONG).show()
}
fun passwordLengthTooLongSnackBar(layout: View, resources: Resources) {
    Snackbar.make(layout, resources.getText(R.string.password_too_long), Snackbar.LENGTH_LONG).show()
}
fun passwordIncorrectSnackBar(layout: View, resources: Resources) {
    Snackbar.make(layout, resources.getText(R.string.password_incorrect), Snackbar.LENGTH_LONG).show()
}
fun passwordsDoNotMatch(layout: View, resources: Resources) {
    Snackbar.make(layout, resources.getText(R.string.passwords_do_not_match), Snackbar.LENGTH_LONG).show()
}