package com.example.squadmaster.ui.score

import BaseFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.squadmaster.R
import com.example.squadmaster.application.SessionManager.getUserID
import com.example.squadmaster.databinding.FragmentScoreBinding
import com.example.squadmaster.ui.main.MainActivity
import com.example.squadmaster.utils.setVisibility
import com.example.squadmaster.utils.showAlertDialogTheme

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

        viewModel.getUserPoint(getUserID())

        binding.ivRefresh.setOnClickListener { viewModel.getUserPoint(getUserID()) }

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
                    setVisibility(View.VISIBLE, binding.tvTitleBestScores, binding.tvTitleTotalPoints)
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
}