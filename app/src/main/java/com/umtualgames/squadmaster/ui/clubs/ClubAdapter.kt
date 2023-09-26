package com.umtualgames.squadmaster.ui.clubs

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.umtualgames.squadmaster.databinding.RowLayoutClubBinding
import com.umtualgames.squadmaster.network.responses.item.Club
import com.umtualgames.squadmaster.utils.setGone
import com.umtualgames.squadmaster.utils.setVisible
import javax.inject.Inject

class ClubAdapter @Inject constructor(private val onClick: (Club) -> Unit) : RecyclerView.Adapter<ClubAdapter.ClubViewHolder>() {

    private val dataList: MutableList<Club> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClubViewHolder = ClubViewHolder(RowLayoutClubBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ClubViewHolder, position: Int) = holder.bind(dataList[position])

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(list: List<Club>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dataList.size

    inner class ClubViewHolder(private val binding: RowLayoutClubBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Club) {

            with(binding) {

                if (item.isLocked) {
                    ivClubLogo.alpha = 0.1f
                    ivLocked.setVisible()
                }

                if (!item.isLocked) {
                    ivClubLogo.alpha = 1f
                    ivLocked.setGone()

                    itemView.setOnClickListener {
                        onClick.invoke(item)
                    }
                }

                if (item.id == 30303) {
                    itemView.setGone()
                }

                ivClubLogo.apply {
                    Glide.with(context)
                        .asBitmap()
                        .load(item.imagePath)
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(this)
                }
                tvClubName.text = item.name
            }
        }
    }
}