package com.umtualgames.squadmaster.ui.squad

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager.getUnknownAnswer
import com.umtualgames.squadmaster.application.SessionManager.getUnknownImage
import com.umtualgames.squadmaster.databinding.RowLayoutPlayerBinding
import com.umtualgames.squadmaster.domain.entities.responses.item.Player
import com.umtualgames.squadmaster.utils.ifContains
import com.umtualgames.squadmaster.utils.setGone
import javax.inject.Inject

class SquadAdapter @Inject constructor() : RecyclerView.Adapter<SquadAdapter.SquadViewHolder>() {

    private val dataList: MutableList<Player> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SquadViewHolder = SquadViewHolder(RowLayoutPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: SquadViewHolder, position: Int) = holder.bind(dataList[position])

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(list: List<Player>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dataList.size

    inner class SquadViewHolder(private val binding: RowLayoutPlayerBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Player) {

            val circularProgressDrawable = CircularProgressDrawable(itemView.context)
            circularProgressDrawable.apply {
                strokeWidth = 5f
                centerRadius = 30f
                start()
            }

            with(binding) {
                ivFlag.apply {
                    Glide.with(context)
                        .asBitmap()
                        .load("https://flagcdn.com/w160/${ifContains(item.nationality.lowercase())}.png")
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .placeholder(circularProgressDrawable)
                        .into(this)
                }

                tvNumber.text = item.number.toString()

                if (!item.isVisible) {
                    flNumber.setGone()
                    ivFlag.setGone()

                    ivPlayer.apply {
                        setImageResource(R.drawable.ic_question_mark)
                        setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.green))
                    }

                    if (getUnknownAnswer() != "") {
                        tvPlayerName.text = getUnknownAnswer()
                        ivPlayer.apply {
                            setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))

                            Glide.with(context)
                                .asBitmap()
                                .load(getUnknownImage())
                                .skipMemoryCache(true)
                                .placeholder(circularProgressDrawable)
                                .into(this)
                        }
                    } else {
                        tvPlayerName.text = ""
                    }
                } else {
                    tvPlayerName.apply {
                        text = item.displayName
                        textSize = if (item.displayName.length > 23) 12f else if (item.displayName.length > 17) 13f else 14f
                    }

                    ivPlayer.apply {
                        Glide.with(context)
                            .asBitmap()
                            .load(item.imagePath)
                            .skipMemoryCache(true)
                            .placeholder(circularProgressDrawable)
                            .into(this)
                    }
                }
            }
        }
    }
}