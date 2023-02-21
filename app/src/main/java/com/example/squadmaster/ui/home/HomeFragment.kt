package com.example.squadmaster.ui.home

import BaseFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.example.squadmaster.R
import com.example.squadmaster.application.SessionManager.clearClubLevel
import com.example.squadmaster.application.SessionManager.clearPassword
import com.example.squadmaster.application.SessionManager.clearScore
import com.example.squadmaster.application.SessionManager.clearUserName
import com.example.squadmaster.application.SessionManager.clearWrongCount
import com.example.squadmaster.application.SessionManager.getUserID
import com.example.squadmaster.databinding.FragmentHomeBinding
import com.example.squadmaster.ui.game.GameActivity
import com.example.squadmaster.ui.main.MainActivity
import com.example.squadmaster.ui.settings.SettingsFragment
import com.example.squadmaster.utils.showAlertDialogTheme

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

        viewModel.getUserPoint(getUserID())

        binding.apply {

            tvScore.text = getString(R.string.score)

            ivSettings.setOnClickListener { SettingsFragment().show(parentFragmentManager, "") }

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

            cvClose.setOnClickListener {
                showAlertDialogTheme(getString(R.string.close_game), getString(R.string.game_close_alert), showNegativeButton = true, onPositiveButtonClick = {
                    clearUserName()
                    clearPassword()
                    clearScore()
                    clearClubLevel()
                    this@HomeFragment.activity?.finishAndRemoveTask()
                })
            }
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    return
                }
            })
    }

    private fun setupObservers() {
        viewModel.getViewState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeViewState.LoadingState -> {}
                is HomeViewState.UserPointState -> {
                    if (state.response.statusCode == 200) {
                        binding.tvScore.text = String.format(getString(R.string.total_score), state.response.data.bestPoint?.toString(), state.response.data.point?.toString())
                    }
                }
                is HomeViewState.ErrorState -> {
                    showAlertDialogTheme(title = getString(R.string.error), contentMessage = state.message)
                }
                else -> {}
            }
        }
    }
}