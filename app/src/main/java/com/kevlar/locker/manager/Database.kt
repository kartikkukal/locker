package com.kevlar.locker.manager

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

const val password_manager_database_name = "locker"

@Dao
interface PasswordManager {
    @Insert
    fun insertAccount(account: Account)

    @Insert
    fun insertList(accounts: List<Account>)

    @Query("DELETE FROM account WHERE id = :id")
    fun deleteAccountById(id: String)

    @Query("SELECT * FROM account")
    fun allAccounts(): List<Account>

    @Query("SELECT * FROM account WHERE service LIKE :query")
    fun searchAccounts(query: String): List<Account>

    @Query("UPDATE account SET service = :service, identity = :identity, password = :password, salt = :salt, iv = :iv WHERE id = :id")
    fun updateAccount(id: String, service: String, identity: String, password: String, salt: String, iv: String)

    @Query("DELETE FROM account")
    fun deleteAll()
}

@Database(entities = [Account::class], version = 1)
abstract class PasswordDatabase : RoomDatabase() {
    abstract fun passwordManager(): PasswordManager
}

fun createDatabase(applicationContext: Context): PasswordManager {
    return Room.databaseBuilder(applicationContext,
        PasswordDatabase::class.java,
        password_manager_database_name).build()
        .passwordManager()
}