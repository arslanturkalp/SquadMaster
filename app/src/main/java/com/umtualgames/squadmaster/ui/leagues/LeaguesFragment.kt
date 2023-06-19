package com.umtualgames.squadmaster.ui.leagues

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.application.SquadMasterApp
import com.umtualgames.squadmaster.data.models.MessageEvent
import com.umtualgames.squadmaster.databinding.FragmentLeaguesBinding
import com.umtualgames.squadmaster.network.requests.UpdatePointRequest
import com.umtualgames.squadmaster.network.responses.item.League
import com.umtualgames.squadmaster.ui.base.BaseFragment
import com.umtualgames.squadmaster.ui.clubs.ClubsActivity
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.ui.start.StartActivity
import com.umtualgames.squadmaster.utils.setVisibility
import com.umtualgames.squadmaster.utils.showAlertDialogTheme
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class LeaguesFragment : BaseFragment(), OnUserEarnedRewardListener {

    private val binding by lazy { FragmentLeaguesBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<LeaguesViewModel>()

    private val leagueAdapter by lazy { LeaguesAdapter({ openClubs(it) }, { showRequireDialog(it) }) }

    private var mRewardedInterstitialAd: RewardedInterstitialAd? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupRecyclerViews()
        loadAds()

        if (getUserID() != 13) {
            binding.apply {
                setVisibility(View.VISIBLE, rvLeagues, llLeagueTitle)
                llShowLeague.visibility = View.GONE
            }
            viewModel.getLeagues(getUserID())
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backToMainMenu()
                }
            })

        binding.apply {
            btnLoginOrRegister.setOnClickListener { requireContext().startActivity(StartActivity.createIntent(true, requireContext())) }
            ivRefresh.setOnClickListener { viewModel.getLeagues(getUserID()) }
        }
    }

    private fun backToMainMenu() {
        (activity as MainActivity).apply {
            showFragment(homeFragment)
            setItemInNavigation(homeFragment)
        }
    }

    private fun setupRecyclerViews() {
        binding.rvLeagues.apply {
            adapter = leagueAdapter
            setAlpha(true)
            set3DItem(true)
        }
    }

    private fun setupObservers() {
        viewModel.getViewState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LeaguesViewState.LoadingState -> showProgressDialog()
                is LeaguesViewState.SuccessState -> {
                    dismissProgressDialog()
                    showLeagues(state.response)
                }

                is LeaguesViewState.ErrorState -> {
                    dismissProgressDialog()
                    showAlertDialogTheme(title = getString(R.string.error), contentMessage = state.message)
                }

                is LeaguesViewState.WarningState -> {
                    dismissProgressDialog()
                    state.message?.let { showAlertDialogTheme(title = getString(R.string.warning), contentMessage = it) }
                }

                is LeaguesViewState.RefreshState -> {
                    dismissProgressDialog()
                    updateToken(state.response.data.token.accessToken)
                    updateRefreshToken(state.response.data.token.refreshToken)
                }

                is LeaguesViewState.UserPointLoadingState -> {}
                is LeaguesViewState.UpdateState -> {
                    EventBus.getDefault().post("Score Update")
                }
            }
        }
    }

    private fun openClubs(league: League) {
        context?.startActivity((ClubsActivity.createIntent(context, league)))
    }

    private fun showRequireDialog(league: League) {
        showAlertDialogTheme(
            title = getString(R.string.warning),
            contentMessage = String.format(getString(R.string.need_point), league.name, league.point) + " " + getString(R.string.watch_and_earn),
            showNegativeButton = true,
            negativeButtonTitle = getString(R.string.watch_ad),
            onNegativeButtonClick = {
                if (mRewardedInterstitialAd != null) {
                    mRewardedInterstitialAd?.show(activity as Activity, this@LeaguesFragment)
                }
            })
    }

    private fun showLeagues(leagues: List<League>) {
        leagueAdapter.updateAdapter(leagues.sortedBy { it.point })
    }

    private fun loadAds() {
        val adRequest = AdRequest.Builder().build()

        RewardedInterstitialAd.load(requireContext(), "ca-app-pub-5776386569149871/5057308377", adRequest, object : RewardedInterstitialAdLoadCallback() {
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
        viewModel.updatePoint(UpdatePointRequest(getUserID(), 50))
        showAlertDialogTheme(getString(R.string.info), getString(R.string.point_50))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        if (event.message == "League Update") {
            viewModel.getLeagues(getUserID())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}