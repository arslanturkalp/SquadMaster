package com.example.squadmaster.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.squadmaster.databinding.RowLayoutPlayerBinding
import com.example.squadmaster.network.responses.item.PotentialAnswer

class PotentialAnswersAdapter(private val onClick: (PotentialAnswer) -> Unit) : RecyclerView.Adapter<PotentialAnswersAdapter.PotentialAnswersViewHolder>() {

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

                ivFlag.visibility = View.GONE

                tvPlayerName.text = item.displayName

                ivPlayer.setOnClickListener {
                    if (!item.isAnswer){
                        itemView.visibility = View.GONE
                    }
                    onClick.invoke(item)
                }
                llPlayer.setOnClickListener {
                    if (!item.isAnswer){
                        itemView.visibility = View.GONE
                    }
                    onClick.invoke(item)
                }
            }
        }
    }
}