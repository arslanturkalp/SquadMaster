package com.umtualgames.squadmaster.utils.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.umtualgames.squadmaster.databinding.RowLayoutPlayerBinding
import com.umtualgames.squadmaster.domain.entities.responses.item.PotentialAnswer
import com.umtualgames.squadmaster.utils.setGone
import com.umtualgames.squadmaster.utils.setVisibility

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

            with(binding) {
                ivPlayer.apply {
                    Glide.with(context)
                        .asBitmap()
                        .load(item.imagePath)
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(this)
                }

                setVisibility(View.GONE, flNumber, ivFlag)

                tvPlayerName.apply {
                    text = item.displayName
                    textSize = if (item.displayName.length > 23) 10f else if (item.displayName.length > 17) 11f else 12f
                }

                ivPlayer.setOnClickListener {
                    if (!item.isAnswer && !isLeagueMode) {
                        itemView.setGone()
                    }
                    onClick.invoke(item)
                }
                llPlayer.setOnClickListener {
                    if (!item.isAnswer && !isLeagueMode) {
                        itemView.setGone()
                    }
                    onClick.invoke(item)
                }
            }
        }
    }
}