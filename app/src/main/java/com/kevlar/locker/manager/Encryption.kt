package com.kevlar.locker.manager

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

const val iv_length = 16
const val salt_length = 32
const val key_length = 64

fun encryptString(data: String, key: String): Triple<String, String, String> {
    val iv = generateRandom(iv_length)
    val salt = generateRandom(salt_length)

    val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val spec =  PBEKeySpec(key.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
    val tmp = factory.generateSecret(spec)
    val secretKey =  SecretKeySpec(tmp.encoded, "AES")

    val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)


    return Triple(
        Base64.encodeToString(cipher.doFinal(data.toByteArray(Charsets.UTF_8)), Base64.DEFAULT),
        iv,
        salt)

}

fun decryptString(cipherText: String, key: String, iv: String, salt: String): String {
    val ivParameterSpec =  IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val spec =  PBEKeySpec(key.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
    val tmp = factory.generateSecret(spec)
    val secretKey =  SecretKeySpec(tmp.encoded, "AES")

    val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
    return  String(cipher.doFinal(Base64.decode(cipherText, Base64.DEFAULT)))
}

fun generateRandom(size: Int): String {
    val random = ByteArray(size)
    Random.nextBytes(random)
    return Base64.encodeToString(random, Base64.DEFAULT)
}