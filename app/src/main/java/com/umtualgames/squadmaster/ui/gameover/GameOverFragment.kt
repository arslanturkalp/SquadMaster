package com.umtualgames.squadmaster.ui.gameover

import BaseBottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.databinding.FragmentGameOverBinding
import com.umtualgames.squadmaster.ui.game.GameActivity
import com.umtualgames.squadmaster.ui.main.MainActivity

class GameOverFragment : BaseBottomSheetDialogFragment() {

    private val binding by lazy { FragmentGameOverBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            arguments?.let {
                tvScore.text = String.format(getString(R.string.formatted_score, it.getInt(KEY_SCORE).toString()))
                btnRestart.setOnClickListener { context?.startActivity((GameActivity.createIntent(context))) }
            }
            ivClose.setOnClickListener { startActivity(MainActivity.createIntent(requireContext())) }
        }
    }

    companion object {

        private const val KEY_SCORE = "KEY_SCORE"

        fun newInstance(score: Int): GameOverFragment = GameOverFragment().apply {
            this.isCancelable = false
            arguments = Bundle().apply {
                putInt(KEY_SCORE, score)
            }
        }
    }
}