package com.example.idlemmorpg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PopupMenuAdapter(
    private val menuItems: List<PopupMenuItem>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<PopupMenuAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.itemTitle)
        val descriptionText: TextView = view.findViewById(R.id.itemDescription)
        val iconText: TextView = view.findViewById(R.id.itemIcon)
        val container: LinearLayout = view.findViewById(R.id.itemContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.popup_menu_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = menuItems[position]
        holder.titleText.text = item.name
        holder.descriptionText.text = item.description

        holder.iconText.text = when {
            item.location.startsWith("trainingGround") -> "⚔️"
            item.location == "weaponShop" -> "🔨"
            item.location == "armorShop" -> "👕"
            item.location == "convenienceStore" -> "🏪"
            else -> "🏰"
        }

        holder.container.setOnClickListener {
            onItemClick(item.location)
        }
    }

    override fun getItemCount() = menuItems.size
}