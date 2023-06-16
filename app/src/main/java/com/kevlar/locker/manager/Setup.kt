package com.kevlar.locker.manager

import android.content.Context

const val shared_preference_name = "safe"

fun setupMasterPassword(context: Context, masterPassword: String) {
    val key = generateRandom(key_length)
    val (cipher, iv, salt) = encryptString(key, masterPassword)

    context.getSharedPreferences(shared_preference_name, Context.MODE_PRIVATE).edit()
        .putBoolean("setup", true)
        .putString("cipher", cipher)
        .putString("iv", iv)
        .putString("salt", salt)
        .apply()
}

fun getMasterKey(context: Context, masterPassword: String): String {
    val sharedPreferences = context.getSharedPreferences(shared_preference_name, Context.MODE_PRIVATE)

    val cipher = sharedPreferences.getString("cipher", "")
    val iv = sharedPreferences.getString("iv", "")
    val salt = sharedPreferences.getString("salt", "")

    if(cipher == null || iv == null || salt == null) {
        throw Exception("Shared preference is not setup correctly!")
    }

    if (cipher == "" || iv == "" || salt == "") {
        throw Exception("Shared preference is not setup correctly!")
    }

    return decryptString(cipher, masterPassword, iv, salt)
}