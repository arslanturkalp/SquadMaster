package com.example.squadmaster.ui.game

import BaseActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.squadmaster.R
import com.example.squadmaster.application.SessionManager
import com.example.squadmaster.application.SessionManager.clearScore
import com.example.squadmaster.application.SessionManager.clearUnknownAnswer
import com.example.squadmaster.application.SessionManager.clearWrongCount
import com.example.squadmaster.application.SessionManager.getScore
import com.example.squadmaster.application.SessionManager.getUserID
import com.example.squadmaster.application.SessionManager.getWrongCount
import com.example.squadmaster.application.SessionManager.updateScore
import com.example.squadmaster.application.SessionManager.updateUnknownAnswer
import com.example.squadmaster.application.SessionManager.updateUnknownImage
import com.example.squadmaster.application.SessionManager.updateWrongCount
import com.example.squadmaster.data.enums.PositionTypeIdStatus
import com.example.squadmaster.databinding.ActivitySquadBinding
import com.example.squadmaster.network.requests.UpdatePointRequest
import com.example.squadmaster.network.responses.item.Player
import com.example.squadmaster.network.responses.item.PotentialAnswer
import com.example.squadmaster.ui.answer.AnswerFragment
import com.example.squadmaster.ui.gameover.GameOverFragment
import com.example.squadmaster.ui.main.MainActivity
import com.example.squadmaster.adapter.PotentialAnswersAdapter
import com.example.squadmaster.ui.yellowcard.YellowCardFragment
import com.example.squadmaster.utils.*
import com.example.squadmaster.utils.LangUtils.Companion.checkLanguage
import com.google.android.flexbox.*

class GameActivity : BaseActivity() {

    private val binding by lazy { ActivitySquadBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<GameViewModel>()

    private val goalkeeperAdapter by lazy { GameAdapter() }
    private val defenceAdapter by lazy { GameAdapter() }
    private val middleAdapter by lazy { GameAdapter() }
    private val attackingMiddleAdapter by lazy { GameAdapter() }
    private val forwardAdapter by lazy { GameAdapter() }

    private val potentialAnswersAdapter by lazy { PotentialAnswersAdapter { controlAnswer(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkLanguage(this)

        clearUnknownAnswer()

        setupRecyclerViews()
        setupObservers()

        addOnBackPressedListener { backToMainMenu() }

        viewModel.getSquad()

        binding.apply {
            tvScore.text = getScore().toString()
            ivPause.setOnClickListener {
                backToMainMenu()
            }
            svGeneral.postDelayed({ svGeneral.fullScroll(ScrollView.FOCUS_DOWN) }, 350)
            when (getWrongCount()) {
                1 -> binding.ivWrongThird.alpha = 0.2f
                2 -> {
                    binding.ivWrongThird.alpha = 0.2f
                    binding.ivWrongSecond.alpha = 0.2f
                }
                3 -> {
                    binding.ivWrongThird.alpha = 0.2f
                    binding.ivWrongSecond.alpha = 0.2f
                    binding.ivWrongFirst.alpha = 0.2f
                }
                else -> clearWrongCount()
            }
        }
    }

    private fun setupRecyclerViews() {
        binding.rvGoalkeeper.apply {
            adapter = goalkeeperAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.rvDefence.apply {
            adapter = defenceAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.rvMiddle.apply {
            adapter = middleAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.rvAttackingMiddle.apply {
            adapter = attackingMiddleAdapter
            layoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP).apply {
                justifyContent = JustifyContent.SPACE_AROUND
                alignItems = AlignItems.CENTER
            }
        }

        binding.rvForwards.apply {
            adapter = forwardAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.rvPotentialAnswers.apply {
            adapter = potentialAnswersAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupObservers() {
        viewModel.getViewState.observe(this) { state ->
            when (state) {
                is GameViewState.LoadingState -> showProgressDialog()
                is GameViewState.SuccessState -> {
                    dismissProgressDialog()
                    setList(state.response.data.playerList, state.response.data.potentialAnswerList)
                    binding.tvTeamName.text = state.response.data.squad.name
                }
                is GameViewState.ErrorState -> {
                    dismissProgressDialog()
                }
                is GameViewState.WarningState -> {
                    dismissProgressDialog()
                    state.message?.let { showAlertDialogTheme(title = getString(R.string.warning), contentMessage = it) }
                }
                is GameViewState.RefreshState -> {
                    dismissProgressDialog()
                    SessionManager.updateToken(state.response.data.token.accessToken)
                    SessionManager.updateRefreshToken(state.response.data.token.refreshToken)

                    viewModel.getSquad()
                }
                else -> {}
            }
        }
    }

    private fun backToMainMenu() {
        showAlertDialogTheme(
            title = getString(R.string.back_to_main_menu),
            contentMessage = getString(R.string.score_not_save_reminder),
            showNegativeButton = true,
            positiveButtonTitle = getString(R.string.yes),
            negativeButtonTitle = getString(R.string.no),
            onPositiveButtonClick = { startActivity(MainActivity.createIntent(this@GameActivity)) })
    }

    private fun showWrongAnswerAnimation() {
        updateWrongCount(getWrongCount() + 1)
        binding.etUnknownPlayerName.startAnimation(AnimationUtils.loadAnimation(this@GameActivity, R.anim.shake))
        when (getWrongCount()) {
            1 -> binding.ivWrongThird.alpha = 0.2f
            2 -> binding.ivWrongSecond.alpha = 0.2f
            3 -> binding.ivWrongFirst.alpha = 0.2f
        }

        if (getWrongCount() == 2) {
            navigateToYellowCard()
        }
        if (getWrongCount() == 3) {
            navigateToGameOver(getScore())
            if (getScore() != 0) {
                viewModel.updatePoint(UpdatePointRequest(getUserID(), getScore()))
            }
            clearWrongCount()
            clearScore()
        }
    }

    private fun setList(squad: List<Player>, potentialAnswers: List<PotentialAnswer>) {
        setupUI(squad)
        goalkeeperAdapter.updateAdapter(squad.filter { it.positionTypeID == PositionTypeIdStatus.GOALKEEPER.value })
        defenceAdapter.updateAdapter(ifTwoBack(squad.filter { it.positionTypeID == PositionTypeIdStatus.DEFENCE.value } as ArrayList<Player>))
        middleAdapter.updateAdapter(squad.filter { it.positionTypeID == PositionTypeIdStatus.MIDFIELDER.value && it.positionID != 9 })
        attackingMiddleAdapter.updateAdapter(squad.filter { it.positionID == 10 || it.positionID == 9 })
        forwardAdapter.updateAdapter(ifTwoWinger(squad.filter { it.positionTypeID == PositionTypeIdStatus.FORWARD.value && it.positionID != 10 } as ArrayList<Player>))

        potentialAnswersAdapter.updateAdapter(potentialAnswers)
    }

    private fun setupUI(squad: List<Player>) {

        val unknownPlayer = squad.first { !it.isVisible }
        with(binding) {
            ivTeam.apply {
                Glide.with(context)
                    .asBitmap()
                    .load(unknownPlayer.squadImagePath)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(this)
            }

            setVisibility(View.VISIBLE, ivFootballPitch, ivFootballGoal, llHalfSquare, tvAnswerTitle)

            ivUnknownPlayer.apply {
                setImageResource(R.drawable.ic_question_mark)
                setColorFilter(ContextCompat.getColor(ivUnknownPlayer.context, R.color.green))
                setBackgroundColor(ContextCompat.getColor(this.context, R.color.white))
            }

            ivFlag.setOnClickListener {
                showAlertDialogTheme(
                    title = getString(R.string.show_flag),
                    contentMessage = getString(R.string.show_flag_description),
                    showNegativeButton = true,
                    positiveButtonTitle = getString(R.string.yes),
                    negativeButtonTitle = getString(R.string.no),
                    onPositiveButtonClick = {

                        if (getScore() >= 50) {
                            ivFlag.apply {
                                setBackgroundColor(ContextCompat.getColor(context, R.color.green))
                                Glide.with(context)
                                    .asBitmap()
                                    .load("https://countryflagsapi.com/png/${ifContains(unknownPlayer.nationality)}")
                                    .into(this)
                            }
                            updateScore(getScore() - 50)
                            tvScore.text = getScore().toString()

                        }
                        else Toast.makeText(this@GameActivity, getString(R.string.insufficient_score), Toast.LENGTH_SHORT).show()
                    },
                    onNegativeButtonClick = { dismissProgressDialog() })
            }
        }
    }

    private fun controlAnswer(potentialAnswer: PotentialAnswer) {
        binding.apply {
            if (potentialAnswer.isAnswer) {
                updateScore(getScore() + 10)
                updateUnknownAnswer(potentialAnswer.displayName)
                updateUnknownImage(potentialAnswer.imagePath)
                navigateToAnswer(potentialAnswer.imagePath, potentialAnswer.displayName)
            } else {
                showWrongAnswerAnimation()
            }
        }
    }

    private fun navigateToAnswer(imagePath: String, playerName: String) = AnswerFragment.apply { newInstance(imagePath = imagePath, playerName = playerName, isFromInfiniteMode = true).show(this@GameActivity) }

    private fun navigateToYellowCard() = YellowCardFragment().show(this@GameActivity)

    private fun navigateToGameOver(score: Int) = GameOverFragment.apply { newInstance(score = score).show(this@GameActivity) }

    companion object {
        fun createIntent(context: Context?): Intent = Intent(context, GameActivity::class.java)
    }
}