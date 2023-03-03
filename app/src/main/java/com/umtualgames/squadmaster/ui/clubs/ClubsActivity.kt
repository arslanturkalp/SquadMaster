package com.umtualgames.squadmaster.ui.clubs

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
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.data.models.MessageEvent
import com.umtualgames.squadmaster.databinding.FragmentClubsBinding
import com.umtualgames.squadmaster.network.responses.item.Club
import com.umtualgames.squadmaster.network.responses.item.League
import com.umtualgames.squadmaster.ui.answer.AnswerFragment
import com.umtualgames.squadmaster.ui.base.BaseActivity
import com.umtualgames.squadmaster.ui.squad.SquadActivity
import com.umtualgames.squadmaster.utils.getDataExtra
import com.umtualgames.squadmaster.utils.showAlertDialogTheme
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class ClubsActivity : BaseActivity() {

    private val binding by lazy { FragmentClubsBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<ClubsViewModel>()

    private val clubAdapter by lazy { ClubAdapter { openSquad(it) } }

    private var lastLockedClub: Club? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setStatusBarColor()

        setupRecyclerViews()

        setupObservers()

        val league = intent.getDataExtra<League>(EXTRAS_LEAGUE)
        viewModel.getSquadListByLeague(league.id, getUserID())

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
                    showClubs(state.response, state.levelPass)
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
                    updateToken(state.response.accessToken)
                    updateRefreshToken(state.response.refreshToken)

                    viewModel.getSquadListByLeague(intent.getDataExtra<League>(EXTRAS_LEAGUE).id, getUserID())
                }
            }
        }
    }

    private fun showClubs(clubs: List<Club>, levelPass: Boolean) {

        clubs.forEach { club ->
            club.isPassed = false
        }
        if (clubs.count { !it.isLocked } > 1) {
            clubs.forEach { club ->
                if (!club.isLocked) {
                    club.isPassed = true
                }
            }
        }
        if (clubs.count { it.isLocked } > 0) {
            clubs.last { !it.isLocked }.isPassed = false
        }

        lastLockedClub = clubs.last { !it.isLocked }

        if (levelPass) {
            AnswerFragment.newInstance(lastLockedClub!!.name, lastLockedClub!!.imagePath!!, isUnlockedClub = true).show(supportFragmentManager, "")
        }

        clubAdapter.updateAdapter(clubs.sortedBy { it.level })
    }

    private fun openSquad(club: Club) {
        startActivity((SquadActivity.createIntent(this, club, club.isPassed!!)))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        val league = intent.getDataExtra<League>(EXTRAS_LEAGUE)

        if (event.message == "League Update") {
            viewModel.getSquadListByLeague(league.id, getUserID(), true)
        }

        if (event.message == "Wrong Answer") {
            openSquad(lastLockedClub!!)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
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