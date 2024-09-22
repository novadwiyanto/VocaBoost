package com.example.vocaboost

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(private val context: Context, private val itemList: List<DataActivity.Item>) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemEnglish: TextView = itemView.findViewById(R.id.itemEnglish)
        val itemIndonesian: TextView = itemView.findViewById(R.id.itemIndonesian)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.itemEnglish.text = item.english
        holder.itemIndonesian.text = item.indonesian
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}