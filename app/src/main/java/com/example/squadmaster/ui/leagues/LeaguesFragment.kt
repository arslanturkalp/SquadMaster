package com.example.squadmaster.ui.leagues

import BaseFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.squadmaster.R
import com.example.squadmaster.application.SessionManager
import com.example.squadmaster.databinding.FragmentLeaguesBinding
import com.example.squadmaster.network.responses.item.League
import com.example.squadmaster.ui.clubs.ClubsActivity
import com.example.squadmaster.ui.home.HomeFragment
import com.example.squadmaster.ui.main.MainActivity
import com.example.squadmaster.utils.showAlertDialogTheme

class LeaguesFragment: BaseFragment() {

    private val binding by lazy { FragmentLeaguesBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<LeaguesViewModel>()

    private val leagueAdapter by lazy { LeaguesAdapter { openClubs(it) } }

    private val homeFragment = HomeFragment()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()

        setupObservers()

        viewModel.getLeagues()

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backToMainMenu()
                }
            })
    }

    private fun backToMainMenu() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, homeFragment, "HomeTag")
        transaction.commit()
        transaction.addToBackStack(null)
        (activity as MainActivity).setItemInNavigation(homeFragment)
    }

    private fun setupRecyclerViews() {
        binding.rvLeagues.apply {
            adapter = leagueAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }


    private fun setupObservers() {
        viewModel.getViewState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LeaguesViewState.LoadingState -> showProgressDialog()
                is LeaguesViewState.SuccessState -> {
                    dismissProgressDialog()
                    showLeagues(state.response)
                }
                is LeaguesViewState.ErrorState -> {
                    dismissProgressDialog()
                }
                is LeaguesViewState.WarningState -> {
                    dismissProgressDialog()
                    state.message?.let { showAlertDialogTheme(title = getString(R.string.warning), contentMessage = it) }
                }
                is LeaguesViewState.RefreshState -> {
                    dismissProgressDialog()
                    SessionManager.updateToken(state.response.data.token.accessToken)
                    SessionManager.updateRefreshToken(state.response.data.token.refreshToken)
                }
            }
        }
    }

    private fun openClubs(league: League) {
        context?.startActivity((ClubsActivity.createIntent(context, league)))
    }

    private fun showLeagues(leagues: List<League>) {
        leagueAdapter.updateAdapter(leagues.sortedBy { it.id })
    }
}