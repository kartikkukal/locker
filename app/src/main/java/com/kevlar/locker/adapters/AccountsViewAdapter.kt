package com.kevlar.locker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kevlar.locker.R
import com.kevlar.locker.manager.Account


class AccountsViewAdapter(var accountsList: MutableList<Account>, val onClickListener: (account: Account) -> Unit
) : RecyclerView.Adapter<AccountsViewAdapter.ViewHolder>() {

    class ViewHolder(view: View, onClickListener: (account: Account) -> Unit) : RecyclerView.ViewHolder(view) {

        private lateinit var account: Account

        private val service = view.findViewById<TextView>(R.id.service)
        private val identity = view.findViewById<TextView>(R.id.identity)
        private val serviceLetter = view.findViewById<TextView>(R.id.service_letter)

        fun setData(account: Account) {
            this.service.text = account.service
            this.identity.text = account.identity

            this.serviceLetter.text = account.service.first().toString().uppercase()

            this.account = account
        }

        init {
            view.setOnClickListener {
                onClickListener(this.account)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.account_view, parent, false)
        return ViewHolder(view, onClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(accountsList[position])
    }

    override fun getItemCount(): Int {
        return accountsList.size
    }

    fun insertAccount(account: Account) {
        this.accountsList.add(account)
        this.notifyItemInserted(this.accountsList.size - 1)
    }

    fun updateAccount(id: String, newAccount: Account) {
        var found = -1

        this.accountsList.forEachIndexed { index, account ->
            if(account.id == id) {
                found = index
            }
        }

        if(found == -1) {
            return
        }

        this.accountsList[found] = newAccount
        this.notifyItemChanged(found)
    }

    fun deleteAccount(id: String) {
        var found = -1

        this.accountsList.forEachIndexed { index, account ->
            if(account.id == id) {
                found = index
            }
        }

        if(found == -1) {
            return
        }

        this.accountsList.removeAt(found)
        this.notifyItemRemoved(found)
    }
}