package com.umtualgames.squadmaster.ui.home

import BaseFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.google.android.gms.ads.AdRequest
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager.clearPassword
import com.umtualgames.squadmaster.application.SessionManager.clearScore
import com.umtualgames.squadmaster.application.SessionManager.clearUserID
import com.umtualgames.squadmaster.application.SessionManager.clearUserName
import com.umtualgames.squadmaster.application.SessionManager.clearWrongCount
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.data.models.MessageEvent
import com.umtualgames.squadmaster.databinding.FragmentHomeBinding
import com.umtualgames.squadmaster.ui.game.GameActivity
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.ui.settings.SettingsFragment
import com.umtualgames.squadmaster.ui.start.StartActivity
import com.umtualgames.squadmaster.utils.setVisibility
import com.umtualgames.squadmaster.utils.showAlertDialogTheme
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

            cvStart.setBackgroundResource(R.drawable.bg_light_green)
            cvLeague.setBackgroundResource(R.drawable.bg_light_green)
            cvScore.setBackgroundResource(R.drawable.bg_light_green)
            cvSignOut.setBackgroundResource(R.drawable.bg_light_green)

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
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    private fun setupObservers() {
        viewModel.getViewState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeViewState.LoadingState -> {
                    binding.apply {
                        tvBestScore.text = 0.toString()
                        tvTotalScore.text = 0.toString()
                    }
                }
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
                is HomeViewState.LeagueSuccessState -> {
                    (activity as MainActivity).setNotificationBadge(state.response.count { !it.isLocked })
                }
                else -> {}
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        if (event.message == "League Update" || event.message == "Score Update") {
            viewModel.getUserPoint(getUserID())
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