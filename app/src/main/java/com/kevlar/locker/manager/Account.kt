package com.kevlar.locker.manager

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

const val min_password_length = 12
const val max_password_length = 60


@Serializable @Entity(tableName="account")
data class Account(
    @PrimaryKey @ColumnInfo(name="id") val id: String,
    @ColumnInfo(name="service") var service: String,
    @ColumnInfo(name="identity") var identity: String,
    @ColumnInfo(name="password") var password: String,
    @ColumnInfo(name="salt") var salt: String,
    @ColumnInfo(name="iv") var iv: String,
)

fun createAccount(service: String, identity: String, password: String, master_key: String): Account {
    val (encrypted, iv, salt) = encryptString(password, master_key)
    return Account(
        UUID.randomUUID().toString(),
        service,
        identity,
        encrypted,
        salt,
        iv
    )
}