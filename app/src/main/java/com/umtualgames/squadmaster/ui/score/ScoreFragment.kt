package com.umtualgames.squadmaster.ui.score

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.material.tabs.TabLayout
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.application.SessionManager.isAdminUser
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.data.models.MessageEvent
import com.umtualgames.squadmaster.databinding.FragmentScoreBinding
import com.umtualgames.squadmaster.network.responses.item.RankItem
import com.umtualgames.squadmaster.ui.base.BaseFragment
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.ui.start.StartActivity
import com.umtualgames.squadmaster.utils.setGone
import com.umtualgames.squadmaster.utils.setVisibility
import com.umtualgames.squadmaster.utils.setVisible
import com.umtualgames.squadmaster.utils.showAlertDialogTheme
import dagger.hilt.android.AndroidEntryPoint
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
        viewModel.getViewState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ScoreViewState.LoadingState -> showProgressDialog()
                is ScoreViewState.SuccessState -> {
                    dismissProgressDialog()
                    loadBannerAd()
                    bestPoints = state.response.data.userBestPoints as ArrayList<RankItem>
                    totalPoints = state.response.data.userTotalPoints as ArrayList<RankItem>

                    with(binding) {
                        setVisibility(View.VISIBLE, cvScore, tabLayout)
                        if (tabLayout.selectedTabPosition == 0) pointsAdapter.updateAdapter(bestPoints) else pointsAdapter.updateAdapter(totalPoints)
                    }
                }
                is ScoreViewState.ErrorState -> {
                    dismissProgressDialog()
                    showAlertDialogTheme(title = getString(R.string.error), contentMessage = state.message)
                }
                is ScoreViewState.WarningState -> {
                    dismissProgressDialog()
                    state.message?.let { showAlertDialogTheme(title = getString(R.string.warning), contentMessage = it) }
                }
                is ScoreViewState.UserPointState -> {
                    dismissProgressDialog()
                    with(binding) {
                        state.response.data.apply {
                            tvTotalScore.text = point.toString()
                            tvBestScore.text = bestPoint.toString()
                        }
                    }
                }
                is ScoreViewState.RefreshState -> {
                    updateToken(state.response.accessToken)
                    updateRefreshToken(state.response.refreshToken)
                    viewModel.getUserPoint(getUserID())
                }
            }
        }
    }

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