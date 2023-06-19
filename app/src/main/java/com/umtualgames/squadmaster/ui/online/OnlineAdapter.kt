package com.umtualgames.squadmaster.ui.online

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umtualgames.squadmaster.databinding.RowLayoutMessageBinding
import javax.inject.Inject

class OnlineAdapter @Inject constructor() : RecyclerView.Adapter<OnlineAdapter.OnlineViewHolder>() {

    private val dataList: MutableList<String> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnlineViewHolder = OnlineViewHolder(RowLayoutMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: OnlineViewHolder, position: Int) = holder.bind(dataList[position])

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(list: List<String>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dataList.size

    inner class OnlineViewHolder(private val binding: RowLayoutMessageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String) {
            binding.apply {
                message.text = item
            }
        }
    }
}