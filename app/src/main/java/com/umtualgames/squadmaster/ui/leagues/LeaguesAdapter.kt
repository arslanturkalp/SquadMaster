package com.umtualgames.squadmaster.ui.leagues

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.umtualgames.squadmaster.databinding.RowLayoutLeagueBinding
import com.umtualgames.squadmaster.network.responses.item.League
import com.umtualgames.squadmaster.utils.setVisibility

class LeaguesAdapter(private val onClick: (League) -> Unit, private val onLockedClick: (League) -> Unit) : RecyclerView.Adapter<LeaguesAdapter.LeagueViewHolder>() {

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

                if (item.isLocked) {
                    ivLeague.alpha = 0.1f
                    setVisibility(View.VISIBLE, ivLocked, llPoint)
                    tvLeagueName.visibility = View.GONE
                    itemView.setOnClickListener {
                        onLockedClick.invoke(item)
                    }
                } else {
                    ivLeague.alpha = 1f
                    setVisibility(View.GONE, ivLocked, llPoint)
                    tvLeagueName.visibility = View.VISIBLE
                    itemView.setOnClickListener {
                        onClick.invoke(item)
                    }
                }

                ivLeague.apply {
                    Glide.with(context)
                        .asBitmap()
                        .load(item.imagePath)
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(this)
                }
                tvLeagueName.text = item.name
                tvPoint.text = item.point.toString()
            }
        }
    }
}