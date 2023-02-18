package com.example.squadmaster.ui.clubs

import BaseActivity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.squadmaster.R
import com.example.squadmaster.application.SessionManager.updateRefreshToken
import com.example.squadmaster.application.SessionManager.updateToken
import com.example.squadmaster.databinding.FragmentClubsBinding
import com.example.squadmaster.network.responses.item.Club
import com.example.squadmaster.network.responses.item.League
import com.example.squadmaster.ui.squad.SquadActivity
import com.example.squadmaster.utils.LangUtils.Companion.checkLanguage
import com.example.squadmaster.utils.getDataExtra
import com.example.squadmaster.utils.showAlertDialogTheme


class ClubsActivity : BaseActivity() {

    private val binding by lazy { FragmentClubsBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<ClubsViewModel>()

    private val clubAdapter by lazy { ClubAdapter { openSquad(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkLanguage(this)

        setStatusBarColor()

        setupRecyclerViews()

        setupObservers()

        val league = (intent.getDataExtra(EXTRAS_LEAGUE) as League)
        viewModel.getSquadListByLeague(league.id)
        binding.apply {
            tvLeagueName.text = league.name
            ivLeague.apply {
                Glide.with(context)
                    .load(league.imagePath)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(this)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun setStatusBarColor() {
        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.WHITE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun setupRecyclerViews() {
        binding.rvClubs.apply {
            adapter = clubAdapter
            layoutManager = GridLayoutManager(context, 4)
        }
    }

    private fun setupObservers() {
        viewModel.getViewState.observe(this) { state ->
            when (state) {
                is GetSquadListViewState.LoadingState -> showProgressDialog()
                is GetSquadListViewState.SuccessState -> {
                    dismissProgressDialog()
                    binding.llTeam.visibility = View.VISIBLE
                    showClubs(state.response)
                }
                is GetSquadListViewState.ErrorState -> {
                    dismissProgressDialog()
                }
                is GetSquadListViewState.WarningState -> {
                    dismissProgressDialog()
                    state.message?.let { showAlertDialogTheme(title = getString(R.string.warning), contentMessage = it) }
                }
                is GetSquadListViewState.RefreshState -> {
                    dismissProgressDialog()
                    updateToken(state.response.data.token.accessToken)
                    updateRefreshToken(state.response.data.token.refreshToken)

                    viewModel.getSquadListByLeague(1)
                }
            }
        }
    }

    private fun showClubs(clubs: List<Club>) {
        clubAdapter.updateAdapter(clubs.sortedBy { it.name })
    }

    private fun openSquad(club: Club) {
        startActivity((SquadActivity.createIntent(this, club.name)))
    }

    companion object {

        private const val EXTRAS_LEAGUE = "EXTRAS_LEAGUE"

        fun createIntent(context: Context?, league: League): Intent {
            return Intent(context, ClubsActivity::class.java).apply {
                putExtra(EXTRAS_LEAGUE, league)
            }
        }
    }
}