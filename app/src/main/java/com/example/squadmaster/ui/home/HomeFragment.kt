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
import com.example.squadmaster.ui.start.StartActivity
import com.example.squadmaster.utils.setVisibility
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

        if (getUserID() != 13) {
            viewModel.getUserPoint(getUserID())
        }

        binding.apply {

            if (getUserID() == 13) {
                setVisibility(View.GONE, cvLeague, cvScore)
                tvSignOut.text = getString(R.string.login_or_register)
                cvSignOut.setOnClickListener { requireContext().startActivity(StartActivity.createIntent(true, requireContext())) }
            } else {
                cvSignOut.setOnClickListener {
                    showAlertDialogTheme(getString(R.string.logout), getString(R.string.logout_alert), showNegativeButton = true, onPositiveButtonClick = {
                        clearUserName()
                        clearPassword()
                        clearScore()
                        clearClubLevel()
                        requireContext().startActivity(StartActivity.createIntent(false, requireContext()))
                    })
                }
            }

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
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (getUserID() != 13) { return } else {
                        showAlertDialogTheme(getString(R.string.quit_game), getString(R.string.game_close_alert), showNegativeButton = true, onPositiveButtonClick = { activity?.finishAndRemoveTask() })
                    }
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