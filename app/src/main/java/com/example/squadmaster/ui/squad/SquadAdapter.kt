package com.example.squadmaster.ui.squad

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.squadmaster.R
import com.example.squadmaster.application.SessionManager.getUnknownAnswer
import com.example.squadmaster.application.SessionManager.getUnknownImage
import com.example.squadmaster.databinding.RowLayoutPlayerBinding
import com.example.squadmaster.network.responses.item.Player
import com.example.squadmaster.utils.ifContains

class SquadAdapter : RecyclerView.Adapter<SquadAdapter.SquadViewHolder>() {

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
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            with(binding) {
                ivFlag.apply {
                    Glide.with(context)
                        .asBitmap()
                        .load("https://flagcdn.com/56x42/${ifContains(item.nationality.lowercase())}.png")
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .placeholder(circularProgressDrawable)
                        .into(this)
                }

                if (!item.isVisible) {
                    ivFlag.visibility = View.GONE

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
                        textSize = if (item.displayName.length > 23) 10f else if(item.displayName.length > 17) 11f else 12f
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