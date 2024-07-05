package com.umtualgames.squadmaster.ui.clubs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.data.entities.models.MessageEvent
import com.umtualgames.squadmaster.data.entities.models.Result
import com.umtualgames.squadmaster.databinding.FragmentClubsBinding
import com.umtualgames.squadmaster.domain.entities.responses.item.Club
import com.umtualgames.squadmaster.domain.entities.responses.item.League
import com.umtualgames.squadmaster.ui.answer.AnswerFragment
import com.umtualgames.squadmaster.ui.base.BaseActivity
import com.umtualgames.squadmaster.ui.splash.SplashActivity
import com.umtualgames.squadmaster.ui.squad.SquadActivity
import com.umtualgames.squadmaster.utils.getDataExtra
import com.umtualgames.squadmaster.utils.setPortraitMode
import com.umtualgames.squadmaster.utils.setVisible
import com.umtualgames.squadmaster.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class ClubsActivity : BaseActivity() {

    private val binding by lazy { FragmentClubsBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<ClubsViewModel>()

    private val clubAdapter by lazy { ClubAdapter { openSquad(it) } }

    private var lastLockedClub: Club? = null

    private var reviewInfo: ReviewInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setPortraitMode()

        setupRecyclerViews()

        setupObservers()

        setupInAppReview()

        val league = intent.getDataExtra<League>(EXTRAS_LEAGUE)
        viewModel.getSquadListByLeague(league.id, getUserID())

        binding.apply {
            ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
            tvLeagueName.text = league.name
            ivLeague.apply {
                Glide.with(context)
                    .load(league.imagePath)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(this)
            }
        }
    }

    private fun setupRecyclerViews() {
        binding.rvClubs.apply {
            adapter = clubAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.apply {
                launch {
                    squadListFlow.collect {
                        when (it) {
                            is Result.Error -> dismissProgressDialog()
                            is Result.Loading -> showProgressDialog()
                            is Result.Success -> {
                                dismissProgressDialog()
                                binding.llTeam.setVisible()
                                showClubs(it.body!!.data, it.body.levelPass)
                            }
                            is Result.Auth -> viewModel.refreshTokenLogin(getRefreshToken())
                        }
                    }
                }
                launch {
                    refreshTokenFlow.collect {
                        when (it) {
                            is Result.Error -> dismissProgressDialog()
                            is Result.Loading -> showProgressDialog()
                            is Result.Success -> {
                                dismissProgressDialog()
                                it.body!!.apply {
                                    if (isSuccess) {
                                        updateToken(data.token.accessToken)
                                        updateRefreshToken(data.token.refreshToken)
                                        viewModel.getSquadListByLeague(intent.getDataExtra<League>(EXTRAS_LEAGUE).id, getUserID())
                                    } else {
                                        returnToSplash()
                                    }
                                }
                            }
                            is Result.Auth -> returnToSplash()
                        }
                    }
                }
            }
        }
    }

    private fun returnToSplash() = startActivity(SplashActivity.createIntent(this))

    private fun showClubs(clubs: List<Club>, levelPass: Boolean) {

        lastLockedClub = clubs.last { !it.isLocked }

        if (clubs.all { it.isPassed }) {
            val league = intent.getDataExtra<League>(EXTRAS_LEAGUE)
            AnswerFragment.newInstance(league.name, league.imagePath, isAllClubsFinished = true).show(this)
        }

        if (levelPass && !clubs.all { it.isPassed }) {
            AnswerFragment.newInstance(lastLockedClub!!.name, lastLockedClub!!.imagePath!!, isUnlockedClub = true).show(this)
        }

        if (clubs.last().isPassed) {
            if (reviewInfo != null) {
                val flow = ReviewManagerFactory.create(this).launchReviewFlow(this, reviewInfo!!)
                flow.addOnCompleteListener { }
            }
        }

        clubAdapter.updateAdapter(clubs.sortedBy { it.leagueOrder })
    }

    private fun openSquad(club: Club) {
        startActivity((SquadActivity.createIntent(this, club, club.isPassed)))
    }

    private fun setupInAppReview() {
        val request = ReviewManagerFactory.create(this).requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewInfo = task.result
            }
        }
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