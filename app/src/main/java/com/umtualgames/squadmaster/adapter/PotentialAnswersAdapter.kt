package com.umtualgames.squadmaster.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.umtualgames.squadmaster.network.responses.item.PotentialAnswer
import com.umtualgames.squadmaster.databinding.RowLayoutPlayerBinding

class PotentialAnswersAdapter(private val isLeagueMode: Boolean, private val onClick: (PotentialAnswer) -> Unit) : RecyclerView.Adapter<PotentialAnswersAdapter.PotentialAnswersViewHolder>() {

    private val dataList: MutableList<PotentialAnswer> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PotentialAnswersViewHolder = PotentialAnswersViewHolder(RowLayoutPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: PotentialAnswersViewHolder, position: Int) = holder.bind(dataList[position])

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(list: List<PotentialAnswer>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dataList.size

    inner class PotentialAnswersViewHolder(private val binding: RowLayoutPlayerBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PotentialAnswer) {

            val scale = itemView.resources.displayMetrics.density

            with(binding) {
                ivPlayer.apply {
                    Glide.with(context)
                        .asBitmap()
                        .load(item.imagePath)
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(this)
                }

                ivFlag.visibility = View.GONE

                tvPlayerName.apply {
                    setPadding((1 * scale + 0.5f).toInt(), (7 * scale + 0.5f).toInt(), (1 * scale + 0.5f).toInt(), (7 * scale + 0.5f).toInt())
                    text = item.displayName
                    textSize = if (item.displayName.length > 23) 10f else if(item.displayName.length > 17) 11f else 12f
                }

                ivPlayer.setOnClickListener {
                    if (!item.isAnswer && !isLeagueMode){
                        itemView.visibility = View.GONE
                    }
                    onClick.invoke(item)
                }
                llPlayer.setOnClickListener {
                    if (!item.isAnswer && !isLeagueMode){
                        itemView.visibility = View.GONE
                    }
                    onClick.invoke(item)
                }
            }
        }
    }
}