package com.kevlar.locker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kevlar.locker.R
import com.kevlar.locker.manager.Account


class SearchViewAdapter(var searchResult: MutableList<Account>, private val onClickListener: (account: Account) -> Unit) : RecyclerView.Adapter<SearchViewAdapter.ViewHolder>() {

    class ViewHolder(view: View, onClickListener: (account: Account) -> Unit) : RecyclerView.ViewHolder(view) {

        private lateinit var account: Account

        private val service = view.findViewById<TextView>(R.id.service)
        private val identity = view.findViewById<TextView>(R.id.identity)

        fun setData(account: Account) {
            this.service.text = account.service
            this.identity.text = account.identity

            this.account = account
        }

        init {
            view.setOnClickListener {
                onClickListener(this.account)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_account_view, parent, false)
        return ViewHolder(view, onClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(searchResult[position])
    }

    override fun getItemCount(): Int {
        return searchResult.size
    }

    fun updateSearchList(list: List<Account>) {

        val additionList = list.toMutableList()
        val deletionList = mutableListOf<Account>()

        searchResult.forEach {
            val found = list.indexOf(it)
            if(found == -1) {
                deletionList.add(it)
            } else {
                additionList.remove(it)
            }
        }

        deletionList.forEach {
            val index = searchResult.indexOf(it)

            searchResult.removeAt(index)
            notifyItemRemoved(index)
        }

        additionList.forEach {
            searchResult.add(it)
            notifyItemInserted(searchResult.size - 1)
        }
    }
}