package com.umtualgames.squadmaster.ui.leagues

import BaseFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.data.models.MessageEvent
import com.umtualgames.squadmaster.databinding.FragmentLeaguesBinding
import com.umtualgames.squadmaster.network.responses.item.League
import com.umtualgames.squadmaster.ui.clubs.ClubsActivity
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.ui.start.StartActivity
import com.umtualgames.squadmaster.utils.showAlertDialogTheme
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class LeaguesFragment : BaseFragment() {

    private val binding by lazy { FragmentLeaguesBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<LeaguesViewModel>()

    private val leagueAdapter by lazy { LeaguesAdapter( { openClubs(it) }, { showRequireDialog(it) }) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupRecyclerViews()

        if (getUserID() != 13) {
            binding.apply {
                rvLeagues.visibility = View.VISIBLE
                llShowLeague.visibility = View.GONE
            }
            viewModel.getLeagues()
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() { backToMainMenu() }
            })

        binding.apply {
            btnLoginOrRegister.setOnClickListener { requireContext().startActivity(StartActivity.createIntent(true, requireContext())) }
        }
    }

    private fun backToMainMenu() {
        (activity as MainActivity).apply {
            showFragment(homeFragment)
            setItemInNavigation(homeFragment)
        }
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
                    binding.tvTitleLeagues.visibility = View.VISIBLE
                    loadBannerAd()
                }
                is LeaguesViewState.ErrorState -> {
                    dismissProgressDialog()
                    showAlertDialogTheme(title = getString(R.string.error), contentMessage = state.message)
                }
                is LeaguesViewState.WarningState -> {
                    dismissProgressDialog()
                    state.message?.let { showAlertDialogTheme(title = getString(R.string.warning), contentMessage = it) }
                }
                is LeaguesViewState.RefreshState -> {
                    dismissProgressDialog()
                    updateToken(state.response.data.token.accessToken)
                    updateRefreshToken(state.response.data.token.refreshToken)
                }
            }
        }
    }

    private fun loadBannerAd() {
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    private fun openClubs(league: League) {
        context?.startActivity((ClubsActivity.createIntent(context, league)))
    }

    private fun showRequireDialog(league: League) {
        showAlertDialogTheme(getString(R.string.warning), String.format(getString(R.string.need_point), league.name, league.point))
    }

    private fun showLeagues(leagues: List<League>) {
        leagueAdapter.updateAdapter(leagues.sortedBy { it.point })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        if (event.message == "League Update") {
            viewModel.getLeagues()
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