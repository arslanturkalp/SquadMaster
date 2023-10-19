package com.umtualgames.squadmaster.ui.online.compare

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.application.SquadMasterApp
import com.umtualgames.squadmaster.databinding.FragmentCompareBinding
import com.umtualgames.squadmaster.domain.entities.responses.item.Player
import com.umtualgames.squadmaster.ui.base.BaseBottomSheetDialogFragment
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.ui.splash.SplashActivity
import com.umtualgames.squadmaster.utils.getParcelableDataExtra
import com.umtualgames.squadmaster.utils.setVisibility
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompareFragment : BaseBottomSheetDialogFragment(), OnUserEarnedRewardListener {

    private val binding by lazy { FragmentCompareBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<CompareViewModel>()

    private var mRewardedInterstitialAd: RewardedInterstitialAd? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        setupObservers()
        loadAds()

        return binding.root.also {
            arguments?.let { bundle ->
                with(binding) {

                    if (bundle.getString(KEY_MY_CHOOSE_NAME) == null && bundle.getString(KEY_RIVAL_CHOOSE_NAME) == null) {
                        tvTitle.text = getString(R.string.rival_disconnect)
                        setVisibility(View.GONE, tvMyChoose, ivMyChoose, tvRivalChoose, ivRivalChoose)
                    }

                    tvMyChoose.text = bundle.getString("KEY_MY_CHOOSE_NAME")
                    ivMyChoose.apply {
                        Glide.with(context)
                            .asBitmap()
                            .load(bundle.getString("KEY_MY_CHOOSE_IMAGE"))
                            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(this)
                    }

                    tvRivalChoose.text = bundle.getString("KEY_RIVAL_CHOOSE_NAME")
                    ivRivalChoose.apply {
                        Glide.with(context)
                            .asBitmap()
                            .load(bundle.getString("KEY_RIVAL_CHOOSE_IMAGE"))
                            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(this)
                    }
                    btnGoToMain.setOnClickListener {
                        if (mRewardedInterstitialAd != null) {
                            mRewardedInterstitialAd?.show(activity as Activity, this@CompareFragment)
                        }
                        mRewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                context?.startActivity(MainActivity.createIntent(requireContext()))
                            }
                        }

                    }

                    if (bundle.getString("KEY_RIVAL_CHOOSE_NAME") == bundle.getString("KEY_MY_CHOOSE_NAME")) {
                        tvMatchStatus.text = getString(R.string.draw)
                        tvMatchStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.yellow))
                    } else {
                        if (bundle.getParcelableDataExtra<Player>(KEY_CORRECT_ANSWER)?.displayName == bundle.getString("KEY_RIVAL_CHOOSE_NAME")) {
                            tvMatchStatus.text = getString(R.string.defeat)
                            tvMatchStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                        }

                        if (bundle.getParcelableDataExtra<Player>(KEY_CORRECT_ANSWER)?.displayName == bundle.getString("KEY_MY_CHOOSE_NAME")) {
                            tvMatchStatus.text = getString(R.string.won_and_50_point)
                            tvMatchStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_two))
                            viewModel.updatePoint(com.umtualgames.squadmaster.domain.entities.requests.UpdatePointRequest(getUserID(), 50))
                        }
                    }

                    if (bundle.getParcelableDataExtra<Player>(KEY_CORRECT_ANSWER) != null) {

                        setVisibility(View.VISIBLE, ivRivalStatus, ivMyStatus)

                        if (bundle.getParcelableDataExtra<Player>(KEY_CORRECT_ANSWER)?.displayName == tvRivalChoose.text.toString()) {
                            ivRivalStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pitch_green))
                        } else {
                            ivRivalStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
                        }

                        if (bundle.getParcelableDataExtra<Player>(KEY_CORRECT_ANSWER)?.displayName == tvMyChoose.text.toString()) {
                            ivMyStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pitch_green))
                        } else {
                            ivMyStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
                        }
                    }
                }
            }
        }

    }

    private fun setupObservers() {
        viewModel.getViewState.observe(this) { state ->
            when (state) {
                is CompareViewState.LoadingState -> {
                    showProgressDialog()
                }

                is CompareViewState.WarningState -> {}
                is CompareViewState.ErrorState -> {}
                is CompareViewState.RefreshState -> {
                    dismissProgressDialog()
                    SessionManager.updateToken(state.response.accessToken)
                    SessionManager.updateRefreshToken(state.response.refreshToken)

                    viewModel.updatePoint(com.umtualgames.squadmaster.domain.entities.requests.UpdatePointRequest(getUserID(), 50))
                }

                is CompareViewState.ReturnSplashState -> {
                    dismissProgressDialog()
                    startActivity(SplashActivity.createIntent(requireContext(), false))
                }

                is CompareViewState.UpdateState -> {
                    dismissProgressDialog()
                }

                is CompareViewState.UserPointLoadingState -> {}
            }
        }
    }

    private fun loadAds() {
        val adRequest = AdRequest.Builder().build()

        RewardedInterstitialAd.load(requireContext(), "ca-app-pub-5776386569149871/5057308377", adRequest, object : RewardedInterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(SquadMasterApp.TAG, adError.toString())
                mRewardedInterstitialAd = null
                context?.startActivity(MainActivity.createIntent(requireContext()))
            }

            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                Log.d(SquadMasterApp.TAG, "Ad was loaded.")
                mRewardedInterstitialAd = ad
            }
        })
    }

    companion object {
        private const val KEY_MY_CHOOSE_NAME = "KEY_MY_CHOOSE_NAME"
        private const val KEY_MY_CHOOSE_IMAGE = "KEY_MY_CHOOSE_IMAGE"

        private const val KEY_RIVAL_CHOOSE_NAME = "KEY_RIVAL_CHOOSE_NAME"
        private const val KEY_RIVAL_CHOOSE_IMAGE = "KEY_RIVAL_CHOOSE_IMAGE"

        private const val KEY_CORRECT_ANSWER = "KEY_CORRECT_ANSWER"

        fun newInstance(myChooseImage: String? = null, myChooseName: String? = null, rivalChooseImage: String? = null, rivalChooseName: String? = null, correctAnswer: Player? = null): CompareFragment = CompareFragment().apply {

            arguments = Bundle().apply {
                putString(KEY_MY_CHOOSE_IMAGE, myChooseImage)
                putString(KEY_MY_CHOOSE_NAME, myChooseName)
                putString(KEY_RIVAL_CHOOSE_IMAGE, rivalChooseImage)
                putString(KEY_RIVAL_CHOOSE_NAME, rivalChooseName)
                putString(KEY_RIVAL_CHOOSE_NAME, rivalChooseName)

                putParcelable(KEY_CORRECT_ANSWER, correctAnswer)
            }
        }
    }

    override fun onUserEarnedReward(reward: RewardItem) {
        context?.startActivity(MainActivity.createIntent(requireContext()))
    }
}