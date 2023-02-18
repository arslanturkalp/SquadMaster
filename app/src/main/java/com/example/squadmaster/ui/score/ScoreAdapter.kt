package com.example.squadmaster.ui.score

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.squadmaster.R
import com.example.squadmaster.application.SessionManager.getUserID
import com.example.squadmaster.databinding.RowLayoutScoreBinding
import com.example.squadmaster.network.responses.item.RankItem

class ScoreAdapter : RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder>() {

    private val dataList: MutableList<RankItem> = mutableListOf()

    override fun getItemCount(): Int = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreAdapter.ScoreViewHolder = ScoreViewHolder(RowLayoutScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ScoreAdapter.ScoreViewHolder, position: Int) = holder.bind(dataList[position])

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(list: List<RankItem>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ScoreViewHolder(private val binding: RowLayoutScoreBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RankItem) {
            with(binding) {
                tvUserName.text = item.userViewModel.name
                tvScore.text = item.point.toString()

                if (item.userID == getUserID()) {
                    itemView.setBackgroundResource(R.drawable.bg_green_with_radius_ten)
                    tvUserName.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                    tvScore.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                }
            }
        }
    }

}