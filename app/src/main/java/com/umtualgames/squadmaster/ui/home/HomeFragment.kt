package com.umtualgames.squadmaster.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import com.umtualgames.squadmaster.ui.base.BaseFragment
import com.umtualgames.squadmaster.ui.game.GameActivity
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.ui.online.OnlineActivity
import com.umtualgames.squadmaster.ui.settings.SettingsFragment
import com.umtualgames.squadmaster.ui.splash.SplashActivity
import com.umtualgames.squadmaster.ui.start.StartActivity
import com.umtualgames.squadmaster.utils.setVisibility
import com.umtualgames.squadmaster.utils.showAlertDialogTheme
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class HomeFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<HomeViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSwipeRefresh()
        setupObservers()

        if (getUserID() != 13) {
            viewModel.getUserPoint(getUserID())
        }

        with(binding) {

            cvStart.apply {
                setBackgroundResource(R.drawable.bg_light_green)
                setOnClickListener {
                    startActivity((GameActivity.createIntent(requireContext())))
                    clearScore()
                    clearWrongCount()
                }
            }
            cvStartOnline.apply {
                setBackgroundResource(R.drawable.bg_light_green)
                setOnClickListener {
                    //startActivity(OnlineActivity.createIntent(requireContext()))
                    showAlertDialogTheme(context.getString(R.string.online_mode), context.getString(R.string.coming_soon))
                }
                alpha = 0.8f

            }
            cvLeague.apply {
                setBackgroundResource(R.drawable.bg_light_green)
                setOnClickListener {
                    (activity as MainActivity).apply {
                        showFragment(leaguesFragment)
                        setItemInNavigation(leaguesFragment)
                    }
                }
            }
            cvScore.apply {
                setBackgroundResource(R.drawable.bg_white)
                setOnClickListener {
                    (activity as MainActivity).apply {
                        showFragment(scoreFragment)
                        setItemInNavigation(scoreFragment)
                    }
                }
            }

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

    private fun setupSwipeRefresh() = binding.swipeRefreshLayout.setOnRefreshListener(this)

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
                    dismissProgress()
                    binding.apply {
                        tvBestScore.text = state.response.data.bestPoint.toString()
                        tvTotalScore.text = state.response.data.point.toString()
                    }
                }
                is HomeViewState.WarningState -> {
                    dismissProgress()
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
                is HomeViewState.ReturnSplashState -> {
                    dismissProgress()
                    startActivity(SplashActivity.createIntent(requireContext(), false))
                }
                else -> {}
            }
        }
    }

    private fun showProgress() {
        binding.swipeRefreshLayout.isRefreshing = true
    }

    private fun dismissProgress() {
        binding.swipeRefreshLayout.isRefreshing = false
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

    override fun onRefresh() {
        viewModel.getUserPoint(getUserID())
    }
}