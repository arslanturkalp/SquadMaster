package com.umtualgames.squadmaster.ui.score

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.databinding.RowLayoutScoreBinding
import com.umtualgames.squadmaster.network.responses.item.RankItem

class ScoreAdapter : RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder>() {

    private val dataList: MutableList<RankItem> = mutableListOf()

    override fun getItemCount(): Int = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreAdapter.ScoreViewHolder = ScoreViewHolder(RowLayoutScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ScoreAdapter.ScoreViewHolder, position: Int) = holder.bind(dataList[position], position)

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(list: List<RankItem>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ScoreViewHolder(private val binding: RowLayoutScoreBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: RankItem, position: Int) {
            with(binding) {
                tvPosition.text = (position + 1).toString()
                tvUserName.text = item.userViewModel.username
                tvScore.text = item.point.toString()
                if (position == 0){
                    ivFirstPosition.visibility = View.VISIBLE
                    tvPosition.visibility = View.GONE
                } else {
                    ivFirstPosition.visibility = View.GONE
                    tvPosition.visibility = View.VISIBLE
                }

                if (item.userID == getUserID()) {
                    itemView.setBackgroundResource(R.drawable.bg_green_with_radius_ten)
                    tvUserName.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                    tvScore.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                } else {
                    itemView.setBackgroundResource(R.drawable.bg_light_green)
                    tvUserName.setTextColor(ContextCompat.getColor(itemView.context, R.color.creme))
                    tvScore.setTextColor(ContextCompat.getColor(itemView.context, R.color.soft_green))
                }
            }
        }
    }

}