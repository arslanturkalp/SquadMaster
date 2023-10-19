package com.umtualgames.squadmaster.ui.gameover

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager.clearIsUsedExtraLife
import com.umtualgames.squadmaster.application.SessionManager.clearScore
import com.umtualgames.squadmaster.application.SessionManager.clearWrongCount
import com.umtualgames.squadmaster.application.SessionManager.getIsUsedExtraLife
import com.umtualgames.squadmaster.application.SessionManager.getScore
import com.umtualgames.squadmaster.application.SessionManager.isAdminUser
import com.umtualgames.squadmaster.application.SessionManager.updateIsUsedExtraLife
import com.umtualgames.squadmaster.application.SessionManager.updateWrongCount
import com.umtualgames.squadmaster.databinding.FragmentGameOverBinding
import com.umtualgames.squadmaster.ui.base.BaseBottomSheetDialogFragment
import com.umtualgames.squadmaster.ui.game.GameActivity
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.utils.setGone
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GameOverFragment : BaseBottomSheetDialogFragment(), OnUserEarnedRewardListener {

    private val binding by lazy { FragmentGameOverBinding.inflate(layoutInflater) }

    private var mRewardedInterstitialAd: RewardedInterstitialAd? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            arguments?.let {
                tvScore.text = String.format(getString(R.string.formatted_score, it.getInt(KEY_SCORE).toString()))
                btnRestart.setOnClickListener {
                    if (getScore() != 0 && !isAdminUser()) {
                        clearScore()
                        clearWrongCount()
                        clearIsUsedExtraLife()
                    }
                    context?.startActivity((GameActivity.createIntent(context)))
                }
                if (getIsUsedExtraLife()) btnContinue.setGone()
                btnContinue.setOnClickListener {
                    if (mRewardedInterstitialAd != null) {
                        mRewardedInterstitialAd?.show(activity as Activity, this@GameOverFragment)
                    }
                    mRewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            btnContinue.setGone()
                        }
                    }
                }
            }
            ivClose.setOnClickListener {
                clearIsUsedExtraLife()
                startActivity(MainActivity.createIntent(requireContext()))
            }
        }
    }

    override fun onUserEarnedReward(reward: RewardItem) {
        updateIsUsedExtraLife(true)
        context?.startActivity(GameActivity.createIntent(context))
        updateWrongCount(2)
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