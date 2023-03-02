package com.umtualgames.squadmaster.ui.slide

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umtualgames.squadmaster.data.models.SlideModel
import com.umtualgames.squadmaster.databinding.RowLayoutSlideBinding

class SlidePagerAdapter : RecyclerView.Adapter<SlidePagerAdapter.SlideViewHolder>() {

    private val dataList: MutableList<SlideModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlidePagerAdapter.SlideViewHolder = SlideViewHolder(RowLayoutSlideBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: SlidePagerAdapter.SlideViewHolder, position: Int) = holder.bind(dataList[position])

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(list: List<SlideModel>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dataList.size

    inner class SlideViewHolder(private val binding: RowLayoutSlideBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SlideModel) {

            binding.apply {
                ivPhoto.setImageResource(item.imageID)
                tvText.text = item.title
            }
        }
    }
}