package com.example.cloudvibe.favorit.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudvibe.R
import com.example.cloudvibe.model.database.FavoriteCity

class FavoriteCityAdapter(
    private val onDeleteClick: (FavoriteCity) -> Unit,
    private val onItemClick: (FavoriteCity) -> Unit,
) : RecyclerView.Adapter<FavoriteCityAdapter.FavoriteCityViewHolder>() {

    private var cityList: List<FavoriteCity> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteCityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite_city, parent, false)
        return FavoriteCityViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteCityViewHolder, position: Int) {
        val city = cityList[position]
        holder.bind(city)
        holder.itemView.findViewById<ImageButton>(R.id.btn_delete_city).setOnClickListener {
            onDeleteClick(city)
        }
        holder.itemView.setOnClickListener {
            onItemClick(city)
        }
    }

    override fun getItemCount(): Int = cityList.size

    fun updateList(newList: List<FavoriteCity>) {
        val diffCallback = FavoriteCityDiffUtil(cityList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        cityList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    class FavoriteCityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cityNameTextView: TextView = itemView.findViewById(R.id.tv_city_name)

        fun bind(city: FavoriteCity) {
            cityNameTextView.text = city.cityName
        }
    }
}

class FavoriteCityDiffUtil(
    private val oldList: List<FavoriteCity>,
    private val newList: List<FavoriteCity>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
