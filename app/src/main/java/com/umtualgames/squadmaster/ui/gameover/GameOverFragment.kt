package com.umtualgames.squadmaster.ui.gameover

import BaseBottomSheetDialogFragment
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager
import com.umtualgames.squadmaster.application.SessionManager.clearIsUsedExtraLife
import com.umtualgames.squadmaster.application.SessionManager.clearScore
import com.umtualgames.squadmaster.application.SessionManager.clearWrongCount
import com.umtualgames.squadmaster.application.SessionManager.getIsUsedExtraLife
import com.umtualgames.squadmaster.application.SessionManager.getScore
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.application.SessionManager.updateIsUsedExtraLife
import com.umtualgames.squadmaster.application.SessionManager.updateWrongCount
import com.umtualgames.squadmaster.application.SquadMasterApp
import com.umtualgames.squadmaster.databinding.FragmentGameOverBinding
import com.umtualgames.squadmaster.network.requests.UpdatePointRequest
import com.umtualgames.squadmaster.ui.game.GameActivity
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.utils.showAlertDialogTheme

class GameOverFragment : BaseBottomSheetDialogFragment(), OnUserEarnedRewardListener {

    private val binding by lazy { FragmentGameOverBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<GameOverViewModel>()

    private var mRewardedInterstitialAd: RewardedInterstitialAd? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        loadAds()

        binding.apply {
            arguments?.let {
                tvScore.text = String.format(getString(R.string.formatted_score, it.getInt(KEY_SCORE).toString()))
                btnRestart.setOnClickListener {
                    if (getScore() != 0 && getUserID() != 13) {
                        viewModel.updatePoint(UpdatePointRequest(getUserID(), getScore()))
                        clearScore()
                        clearWrongCount()
                        clearIsUsedExtraLife()
                    }
                    context?.startActivity((GameActivity.createIntent(context)))
                }
                if (getIsUsedExtraLife()) btnContinue.visibility = View.GONE
                btnContinue.setOnClickListener {
                    if (mRewardedInterstitialAd != null) {
                        mRewardedInterstitialAd?.show(activity as Activity, this@GameOverFragment)
                    }
                    mRewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            btnContinue.visibility = View.GONE
                        }
                    }
                }
            }
            ivClose.setOnClickListener {
                if (getScore() != 0 && getUserID() != 13) {
                    viewModel.updatePoint(UpdatePointRequest(getUserID(), getScore()))
                }
                clearIsUsedExtraLife()
                startActivity(MainActivity.createIntent(requireContext()))
            }
        }
    }

    private fun setupObservers() {
        viewModel.getViewState.observe(this) { state ->
            when (state) {
                is GameOverViewState.LoadingState -> showProgressDialog()
                is GameOverViewState.SuccessState -> dismissProgressDialog()
                is GameOverViewState.ErrorState -> dismissProgressDialog()
                is GameOverViewState.WarningState -> {
                    dismissProgressDialog()
                    state.message?.let { showAlertDialogTheme(title = getString(R.string.warning), contentMessage = it) }
                }
                is GameOverViewState.RefreshState -> {
                    dismissProgressDialog()
                    with(state.response) {
                        SessionManager.updateToken(accessToken)
                        SessionManager.updateRefreshToken(refreshToken)
                    }
                    if (getScore() != 0 && getUserID() != 13) {
                        viewModel.updatePoint(UpdatePointRequest(getUserID(), getScore()))
                    }
                }
                else -> {}
            }
        }
    }

    private fun loadAds() {
        val adRequest = AdRequest.Builder().build()

        RewardedInterstitialAd.load(requireContext(), "ca-app-pub-4810521807152117/8369556364", adRequest, object : RewardedInterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(SquadMasterApp.TAG, adError.toString())
                mRewardedInterstitialAd = null
            }

            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                Log.d(SquadMasterApp.TAG, "Ad was loaded.")
                mRewardedInterstitialAd = ad
            }
        })
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