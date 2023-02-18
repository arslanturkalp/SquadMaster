package com.example.squadmaster.ui.leagues

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.squadmaster.databinding.RowLayoutLeagueBinding
import com.example.squadmaster.network.responses.item.League

class LeaguesAdapter(private val onClick: (League) -> Unit) : RecyclerView.Adapter<LeaguesAdapter.LeagueViewHolder>() {

    private val dataList: MutableList<League> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaguesAdapter.LeagueViewHolder = LeagueViewHolder(RowLayoutLeagueBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: LeaguesAdapter.LeagueViewHolder, position: Int) = holder.bind(dataList[position])

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(list: List<League>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dataList.size

    inner class LeagueViewHolder(private val binding: RowLayoutLeagueBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: League) {

            with(binding) {

                itemView.setOnClickListener {
                    onClick.invoke(item)
                }

                ivLeague.apply {
                    Glide.with(context)
                        .asBitmap()
                        .load(item.imagePath)
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(this)
                }

                tvLeagueName.text = item.name
            }
        }
    }
}