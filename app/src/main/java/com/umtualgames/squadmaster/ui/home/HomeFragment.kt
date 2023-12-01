package com.umtualgames.squadmaster.ui.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager.clearScore
import com.umtualgames.squadmaster.application.SessionManager.clearWrongCount
import com.umtualgames.squadmaster.application.SessionManager.getIsMusicOpen
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.application.SessionManager.isAdminUser
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.data.entities.models.MessageEvent
import com.umtualgames.squadmaster.data.entities.models.Result
import com.umtualgames.squadmaster.databinding.FragmentHomeNewBinding
import com.umtualgames.squadmaster.ui.base.BaseFragment
import com.umtualgames.squadmaster.ui.game.GameActivity
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.ui.settings.SettingsFragment
import com.umtualgames.squadmaster.ui.splash.SplashActivity
import com.umtualgames.squadmaster.utils.BackgroundSoundService
import com.umtualgames.squadmaster.utils.setVisible
import com.umtualgames.squadmaster.utils.showAlertDialogTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class HomeFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    private val binding by lazy { FragmentHomeNewBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<HomeViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSwipeRefresh()
        rotateBall()
        setupObservers()

        if (getIsMusicOpen()) {
            requireContext().apply { this.startService(Intent(this, BackgroundSoundService::class.java)) }
        }

        askNotificationPermission()

        if (!isAdminUser()) {
            binding.apply {
                cvScore.setVisible()
            }
            viewModel.getUserPoint(getUserID())
        } else {
            dismissProgress()
        }

        with(binding) {

            cvStart.apply {
                setOnClickListener {
                    startActivity((GameActivity.createIntent(requireContext())))
                    clearScore()
                    clearWrongCount()
                }
            }
            cvLeague.apply {
                setOnClickListener {
                    (activity as MainActivity).apply {
                        showFragment(leaguesFragment)
                        setItemInNavigation(leaguesFragment)
                    }
                }
            }
            cvScore.apply {
                setOnClickListener {
                    (activity as MainActivity).apply {
                        showFragment(scoreFragment)
                        setItemInNavigation(scoreFragment)
                    }
                }
            }
            ivSettings.apply {
                setOnClickListener { SettingsFragment().show(parentFragmentManager, "") }
            }
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!isAdminUser()) {
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

    private fun rotateBall() {
        val rotate = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.apply {
            duration = 2000
            interpolator = LinearInterpolator()
            fillAfter = true
            repeatCount = Animation.INFINITE
        }
        binding.ivLogo.startAnimation(rotate)
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.apply {
                launch {
                    getPointFlow.collect {
                        when (it) {
                            is Result.Error -> {
                                dismissProgress()
                                showAlertDialogTheme(title = getString(R.string.error), contentMessage = it.message)
                            }
                            is Result.Loading -> {
                                binding.apply {
                                    tvBestScore.text = 0.toString()
                                    tvTotalScore.text = 0.toString()
                                }
                                showProgress()
                            }
                            is Result.Success -> {
                                dismissProgress()
                                getLeagues(getUserID())
                                binding.apply {
                                    it.body!!.data.apply {
                                        tvBestScore.text = bestPoint.toString()
                                        tvTotalScore.text = point.toString()
                                    }
                                }
                            }
                            is Result.Auth -> {
                                dismissProgress()
                                refreshTokenLogin(getRefreshToken())
                            }
                        }
                    }
                }
                launch {
                    getLeaguesFlow.collect {
                        when (it) {
                            is Result.Error -> {
                                dismissProgress()
                                showAlertDialogTheme(title = getString(R.string.error), contentMessage = it.message)
                            }
                            is Result.Loading -> {}
                            is Result.Success -> {
                                dismissProgress()
                                (activity as MainActivity).setNotificationBadge(it.body!!.data.count { league -> !league.isLocked })
                            }
                            is Result.Auth -> {
                                dismissProgress()
                                refreshTokenLogin(getRefreshToken())
                            }
                        }
                    }
                }
                launch {
                    refreshTokenFlow.collect {
                        when (it) {
                            is Result.Error -> {
                                dismissProgress()
                                showAlertDialogTheme(title = getString(R.string.error), contentMessage = it.message)
                            }
                            is Result.Loading -> showProgress()
                            is Result.Success -> {
                                dismissProgress()
                                it.body!!.apply {
                                    if (isSuccess) {
                                        updateToken(data.token.accessToken)
                                        updateRefreshToken(data.token.refreshToken)

                                        viewModel.getUserPoint(getUserID())
                                    } else {
                                        returnToSplash()
                                    }
                                }
                            }
                            is Result.Auth -> {
                                dismissProgress()
                                returnToSplash()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun returnToSplash() = startActivity(SplashActivity.createIntent(requireContext(), false))

    private fun showProgress() {
        binding.swipeRefreshLayout.isRefreshing = true
    }

    private fun dismissProgress() {
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pushNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val pushNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        if (event.message == "League Update" || event.message == "Score Update") {
            viewModel.getUserPoint(getUserID())
        }
    }

    override fun onRefresh() {
        viewModel.getUserPoint(getUserID())
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