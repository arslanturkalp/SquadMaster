package com.umtualgames.squadmaster.ui.slide

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.umtualgames.squadmaster.data.entities.models.SlideModel
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
                ivPhoto.apply {
                    Glide.with(context)
                        .asBitmap()
                        .load(item.imageID)
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                        .into(this)
                }
                tvText.text = item.title
            }
        }
    }
}