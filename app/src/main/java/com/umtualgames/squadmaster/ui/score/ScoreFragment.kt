package com.umtualgames.squadmaster.ui.score

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.material.tabs.TabLayout
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.application.SessionManager.isAdminUser
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.data.entities.models.MessageEvent
import com.umtualgames.squadmaster.data.entities.models.Result
import com.umtualgames.squadmaster.databinding.FragmentScoreBinding
import com.umtualgames.squadmaster.domain.entities.responses.item.RankItem
import com.umtualgames.squadmaster.ui.base.BaseFragment
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.ui.splash.SplashActivity
import com.umtualgames.squadmaster.ui.start.StartActivity
import com.umtualgames.squadmaster.utils.setGone
import com.umtualgames.squadmaster.utils.setVisibility
import com.umtualgames.squadmaster.utils.setVisible
import com.umtualgames.squadmaster.utils.showAlertDialogTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class ScoreFragment : BaseFragment() {

    private val binding by lazy { FragmentScoreBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<ScoreViewModel>()

    private val pointsAdapter by lazy { ScoreAdapter() }

    private var bestPoints: ArrayList<RankItem> = arrayListOf()
    private var totalPoints: ArrayList<RankItem> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupRecyclerViews()

        with(binding) {
            if (!isAdminUser()) {
                svScore.setVisible()
                llShowScore.setGone()
            }
        }

        viewModel.getUserPoint(getUserID())

        binding.apply {
            ivRefresh.setOnClickListener { viewModel.getUserPoint(getUserID()) }
            btnLoginOrRegister.setOnClickListener { requireContext().startActivity(StartActivity.createIntent(true, requireContext())) }
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab?.position == 0) {
                        pointsAdapter.updateAdapter(bestPoints)
                    } else {
                        pointsAdapter.updateAdapter(totalPoints)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}

            })
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backToMainMenu()
                }
            })
    }

    private fun backToMainMenu() {
        (activity as MainActivity).apply {
            showFragment(homeFragment)
            setItemInNavigation(homeFragment)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.apply {
                launch {
                    rankFlow.collect {
                        when (it) {
                            is Result.Error -> {
                                dismissProgressDialog()
                                showAlertDialogTheme(title = getString(R.string.error), contentMessage = it.message)
                            }
                            is Result.Loading -> {}
                            is Result.Success -> {
                                dismissProgressDialog()
                                loadBannerAd()

                                it.body!!.data.apply {
                                    bestPoints = userBestPoints as ArrayList<RankItem>
                                    totalPoints = userTotalPoints as ArrayList<RankItem>
                                }

                                with(binding) {
                                    setVisibility(View.VISIBLE, cvScore, tabLayout)
                                    if (tabLayout.selectedTabPosition == 0) pointsAdapter.updateAdapter(bestPoints) else pointsAdapter.updateAdapter(totalPoints)
                                }
                            }
                            is Result.Auth -> {
                                dismissProgressDialog()
                                refreshTokenLogin(getRefreshToken())
                            }
                        }
                    }
                }
                launch {
                    getPointFlow.collect {
                        when (it) {
                            is Result.Error -> dismissProgressDialog()
                            is Result.Loading -> showProgressDialog()
                            is Result.Success -> {
                                dismissProgressDialog()
                                with(binding) {
                                    it.body!!.data.apply {
                                        tvTotalScore.text = point.toString()
                                        tvBestScore.text = bestPoint.toString()
                                    }
                                }
                                viewModel.getRankList()
                            }
                            is Result.Auth -> {
                                dismissProgressDialog()
                                refreshTokenLogin(getRefreshToken())
                            }
                        }
                    }
                }
                launch {
                    refreshTokenFlow.collect {
                        when (it) {
                            is Result.Error -> dismissProgressDialog()
                            is Result.Loading -> {}
                            is Result.Success -> {
                                dismissProgressDialog()
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
                                dismissProgressDialog()
                                returnToSplash()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun returnToSplash() = startActivity(SplashActivity.createIntent(requireContext(), false))

    private fun setupRecyclerViews() {
        binding.rvPoints.apply {
            adapter = pointsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun loadBannerAd() {
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
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