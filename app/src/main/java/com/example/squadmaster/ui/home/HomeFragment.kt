package com.example.squadmaster.ui.home

import BaseFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.example.squadmaster.R
import com.example.squadmaster.application.SessionManager.clearPassword
import com.example.squadmaster.application.SessionManager.clearScore
import com.example.squadmaster.application.SessionManager.clearUserID
import com.example.squadmaster.application.SessionManager.clearUserName
import com.example.squadmaster.application.SessionManager.clearWrongCount
import com.example.squadmaster.application.SessionManager.getUserID
import com.example.squadmaster.application.SessionManager.updateRefreshToken
import com.example.squadmaster.application.SessionManager.updateToken
import com.example.squadmaster.data.models.MessageEvent
import com.example.squadmaster.databinding.FragmentHomeBinding
import com.example.squadmaster.ui.game.GameActivity
import com.example.squadmaster.ui.main.MainActivity
import com.example.squadmaster.ui.settings.SettingsFragment
import com.example.squadmaster.ui.start.StartActivity
import com.example.squadmaster.utils.setVisibility
import com.example.squadmaster.utils.showAlertDialogTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeFragment : BaseFragment() {

    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<HomeViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()

        loadBannerAd()

        if (getUserID() != 13) {
            viewModel.getUserPoint(getUserID())
        }

        binding.apply {

            if (getUserID() == 13) {
                setVisibility(View.GONE, cvLeague, cvScore)
                ivSignOut.setImageResource(R.drawable.ic_login)
                tvSignOut.text = getString(R.string.login_or_register)
                cvSignOut.setOnClickListener { requireContext().startActivity(StartActivity.createIntent(true, requireContext())) }
            } else {
                cvSignOut.setOnClickListener {
                    showAlertDialogTheme(getString(R.string.logout), getString(R.string.logout_alert), showNegativeButton = true, onPositiveButtonClick = {
                        clearUserName()
                        clearPassword()
                        clearScore()
                        clearUserID()
                        requireContext().startActivity(StartActivity.createIntent(false, requireContext()))
                    })
                }
            }

            ivSettings.setOnClickListener {
                SettingsFragment().show(parentFragmentManager, "")
            }

            cvStart.setOnClickListener {
                requireContext().startActivity((GameActivity.createIntent(requireContext())))
                clearScore()
                clearWrongCount()
            }

            cvScore.setOnClickListener {
                (activity as MainActivity).apply {
                    showFragment(scoreFragment)
                    setItemInNavigation(scoreFragment)
                }
            }

            cvLeague.setOnClickListener {
                (activity as MainActivity).apply {
                    showFragment(leaguesFragment)
                    setItemInNavigation(leaguesFragment)
                }
            }
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (getUserID() != 13) {
                        return
                    } else {
                        showAlertDialogTheme(
                            getString(R.string.quit_game),
                            getString(R.string.game_close_alert),
                            showNegativeButton = true,
                            onPositiveButtonClick = { activity?.finishAndRemoveTask() })
                    }
                }
            })
    }

    private fun loadBannerAd() {
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(listOf("03B094AA787BDF5746C59E26B9356600"))
        MobileAds.setRequestConfiguration(configuration.build())
        MobileAds.initialize(requireContext()) {}

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    private fun setupObservers() {
        viewModel.getViewState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeViewState.LoadingState -> {}
                is HomeViewState.UserPointState -> {
                    binding.apply {
                        tvBestScore.text = state.response.data.bestPoint.toString()
                        tvTotalScore.text = state.response.data.point.toString()
                    }
                }
                is HomeViewState.WarningState -> {
                    dismissProgressDialog()
                    state.message?.let { showAlertDialogTheme(title = getString(R.string.warning), contentMessage = it) }
                }
                is HomeViewState.ErrorState -> {
                    showAlertDialogTheme(title = getString(R.string.error), contentMessage = state.message)
                }
                is HomeViewState.RefreshState -> {
                    updateToken(state.response.accessToken)
                    updateRefreshToken(state.response.refreshToken)
                    viewModel.getUserPoint(getUserID())
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        if (event.message == "League Update") {
            viewModel.getUserPoint(getUserID())
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}