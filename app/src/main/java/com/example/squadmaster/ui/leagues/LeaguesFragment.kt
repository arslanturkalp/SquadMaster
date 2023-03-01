package com.example.squadmaster.ui.leagues

import BaseFragment
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.squadmaster.R
import com.example.squadmaster.application.SessionManager.getUserID
import com.example.squadmaster.application.SessionManager.updateRefreshToken
import com.example.squadmaster.application.SessionManager.updateToken
import com.example.squadmaster.data.models.MessageEvent
import com.example.squadmaster.databinding.FragmentLeaguesBinding
import com.example.squadmaster.network.responses.item.League
import com.example.squadmaster.ui.clubs.ClubsActivity
import com.example.squadmaster.ui.main.MainActivity
import com.example.squadmaster.ui.start.StartActivity
import com.example.squadmaster.utils.showAlertDialogTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class LeaguesFragment : BaseFragment() {

    private val binding by lazy { FragmentLeaguesBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<LeaguesViewModel>()

    private val leagueAdapter by lazy { LeaguesAdapter { openClubs(it) } }

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
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(listOf("03B094AA787BDF5746C59E26B9356600"))
        MobileAds.setRequestConfiguration(configuration.build())
        MobileAds.initialize(requireContext()) {}

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    private fun openClubs(league: League) {
        context?.startActivity((ClubsActivity.createIntent(context, league)))
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

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}