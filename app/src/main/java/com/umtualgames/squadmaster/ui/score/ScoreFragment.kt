package com.umtualgames.squadmaster.ui.score

import BaseFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.application.SessionManager.getUserName
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.databinding.FragmentScoreBinding
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.ui.start.StartActivity
import com.umtualgames.squadmaster.utils.setVisibility
import com.umtualgames.squadmaster.utils.showAlertDialogTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

class ScoreFragment: BaseFragment() {

    private val binding by lazy { FragmentScoreBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<ScoreViewModel>()

    private val bestPointsAdapter by lazy { ScoreAdapter() }
    private val totalPointsAdapter by lazy { ScoreAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupRecyclerViews()

        if (getUserID() != 13){
            binding.svScore.visibility = View.VISIBLE
            binding.llShowScore.visibility = View.GONE
            binding.tvUserName.text = getUserName()
            viewModel.getUserPoint(getUserID())
        }

        binding.apply {
            ivRefresh.setOnClickListener { viewModel.getUserPoint(getUserID()) }
            btnLoginOrRegister.setOnClickListener { requireContext().startActivity(StartActivity.createIntent(true, requireContext())) }
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
                    setVisibility(View.VISIBLE, binding.llTitleBestScores, binding.llTitleTotalPoints, binding.tvTitleHighScore, binding.llMyScore)
                    loadBannerAd()
                    bestPointsAdapter.updateAdapter(state.response.data.userBestPoints)
                    totalPointsAdapter.updateAdapter(state.response.data.userTotalPoints)
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
                    binding.tvUserTotalPoint.text = state.response.data.point.toString()
                    binding.tvUserBestScore.text = state.response.data.bestPoint.toString()
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
        binding.rvBestPoints.apply {
            adapter = bestPointsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
        binding.rvTotalPoints.apply {
            adapter = totalPointsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun loadBannerAd() {
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(listOf("03B094AA787BDF5746C59E26B9356600"))
        MobileAds.setRequestConfiguration(configuration.build())
        MobileAds.initialize(requireContext()) {}

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }
}