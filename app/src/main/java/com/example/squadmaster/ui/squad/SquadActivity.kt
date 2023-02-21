package com.example.squadmaster.ui.squad

import BaseActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.squadmaster.R
import com.example.squadmaster.adapter.PotentialAnswersAdapter
import com.example.squadmaster.application.SessionManager.clearIsShowedFlag
import com.example.squadmaster.application.SessionManager.clearUnknownAnswer
import com.example.squadmaster.application.SessionManager.getClubLevel
import com.example.squadmaster.application.SessionManager.getIsShowedFlag
import com.example.squadmaster.application.SessionManager.getUserID
import com.example.squadmaster.application.SessionManager.updateClubLevel
import com.example.squadmaster.application.SessionManager.updateIsShowedFlag
import com.example.squadmaster.application.SessionManager.updateRefreshToken
import com.example.squadmaster.application.SessionManager.updateToken
import com.example.squadmaster.application.SessionManager.updateUnknownAnswer
import com.example.squadmaster.application.SessionManager.updateUnknownImage
import com.example.squadmaster.databinding.ActivitySquadBinding
import com.example.squadmaster.network.requests.UpdatePointRequest
import com.example.squadmaster.network.responses.item.Player
import com.example.squadmaster.network.responses.item.PotentialAnswer
import com.example.squadmaster.ui.answer.AnswerFragment
import com.example.squadmaster.utils.*
import com.example.squadmaster.utils.LangUtils.Companion.checkLanguage
import com.google.android.flexbox.*

class SquadActivity : BaseActivity() {

    private val binding by lazy { ActivitySquadBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<SquadViewModel>()

    private val goalkeeperAdapter by lazy { SquadAdapter() }
    private val defenceAdapter by lazy { SquadAdapter() }
    private val middleAdapter by lazy { SquadAdapter() }
    private val attackingMiddleAdapter by lazy { SquadAdapter() }
    private val forwardAdapter by lazy { SquadAdapter() }

    private val potentialAnswersAdapter by lazy { PotentialAnswersAdapter(false) { controlAnswer(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkLanguage(this)

        clearUnknownAnswer()
        clearIsShowedFlag()

        setupRecyclerViews()
        setupObservers()

        viewModel.getSquad(intent.getDataExtra(EXTRAS_SQUAD_NAME))
        viewModel.getUserPoint(getUserID())

        binding.apply {
            ivWrongFirst.visibility = View.GONE
            ivWrongSecond.visibility = View.GONE
            ivWrongThird.visibility = View.GONE

            svGeneral.postDelayed({ svGeneral.fullScroll(ScrollView.FOCUS_DOWN) }, 350)
            ivPause.setOnClickListener {
                showAlertDialogTheme(
                    title = getString(R.string.back_to_main_menu),
                    contentMessage = getString(R.string.back_to_menu_description),
                    showNegativeButton = true,
                    positiveButtonTitle = getString(R.string.yes),
                    negativeButtonTitle = getString(R.string.no),
                    onPositiveButtonClick = { onBackPressedDispatcher.onBackPressed() })
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
                is GetSquadViewState.LoadingState -> showProgressDialog()
                is GetSquadViewState.SuccessState -> {
                    dismissProgressDialog()
                    setList(state.response.data.playerList, state.response.data.potentialAnswerList)
                }
                is GetSquadViewState.ErrorState -> {
                    dismissProgressDialog()
                }
                is GetSquadViewState.WarningState -> {
                    dismissProgressDialog()
                    state.message?.let { showAlertDialogTheme(title = getString(R.string.warning), contentMessage = it) }
                }
                is GetSquadViewState.RefreshState -> {
                    dismissProgressDialog()
                    updateToken(state.response.accessToken)
                    updateRefreshToken(state.response.refreshToken)

                    viewModel.getSquad(intent.getDataExtra(EXTRAS_SQUAD_NAME))
                }
                is GetSquadViewState.UserPointState -> {
                    dismissProgressDialog()
                    setScore(state.response.data.point)
                }
                is GetSquadViewState.UserPointLoadingState -> {}
                is GetSquadViewState.UpdateState -> {
                    dismissProgressDialog()
                    viewModel.getUserPoint(getUserID())
                }
            }
        }
    }

    private fun setScore(point: Int?) {
        binding.tvScore.text = point.toString()
    }

    private fun setList(squad: List<Player>, potentialAnswers: List<PotentialAnswer>) {
        setupUI(squad)
        goalkeeperAdapter.updateAdapter(squad.filter { it.positionTypeID == 1 })
        defenceAdapter.updateAdapter(ifTwoBack(squad.filter { it.positionTypeID == 2 } as ArrayList<Player>))
        middleAdapter.updateAdapter(squad.filter { it.positionTypeID == 3 && it.positionID != 9 })
        attackingMiddleAdapter.updateAdapter(squad.filter { it.positionID == 10 || it.positionID == 9 })
        forwardAdapter.updateAdapter(ifTwoWinger(squad.filter { it.positionTypeID == 4 && it.positionID != 10 } as ArrayList<Player>))

        binding.cdAnswer.visibility = View.VISIBLE
        potentialAnswersAdapter.updateAdapter(potentialAnswers)
    }

    private fun setupUI(squad: List<Player>) {

        binding.apply {

            val unknownPlayer = squad.first { !it.isVisible }
            tvTeamName.text = intent.getDataExtra(EXTRAS_SQUAD_NAME)
            ivTeam.apply {
                Glide.with(context)
                    .asBitmap()
                    .load(squad.first().squadImagePath)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(this)
            }

            setVisibility(View.VISIBLE, ivFootballPitch, ivFootballGoal, llHalfSquare, tvAnswerTitle)

            ivFlag.setOnClickListener {
                if (!getIsShowedFlag()) {
                    showAlertDialogTheme(
                        title = getString(R.string.show_flag),
                        contentMessage = getString(R.string.show_flag_description),
                        showNegativeButton = true,
                        positiveButtonTitle = getString(R.string.yes),
                        negativeButtonTitle = getString(R.string.no),
                        onPositiveButtonClick = {

                            if (binding.tvScore.text.toString().toInt() >= 30) {
                                ivFlag.apply {
                                    setBackgroundColor(ContextCompat.getColor(context, R.color.green))
                                    Glide.with(context)
                                        .asBitmap()
                                        .load("https://flagcdn.com/56x42/${ifContains(unknownPlayer.nationality.lowercase())}.png")
                                        .into(this)
                                }
                                updateIsShowedFlag(true)
                                viewModel.updatePoint(UpdatePointRequest(getUserID(), -30))
                            } else Toast.makeText(this@SquadActivity, getString(R.string.insufficient_score), Toast.LENGTH_SHORT).show()
                        },
                        onNegativeButtonClick = { dismissProgressDialog() })
                }
            }
        }
    }

    private fun controlAnswer(potentialAnswer: PotentialAnswer) {
        binding.apply {
            if (potentialAnswer.isAnswer) {
                updateClubLevel(getClubLevel() + 1)
                viewModel.updatePoint(UpdatePointRequest(getUserID(), 10))
                updateUnknownAnswer(potentialAnswer.displayName)
                updateUnknownImage(potentialAnswer.imagePath)
                navigateToAnswer(potentialAnswer.imagePath, potentialAnswer.displayName)
            } else {
                showAlertDialogTheme(
                    title = getString(R.string.wrong_answer),
                    contentMessage = String.format(getString(R.string.formatted_wrong_answer), potentialAnswer.displayName),
                    showNegativeButton = true,
                    positiveButtonTitle = getString(R.string.try_again),
                    negativeButtonTitle = getString(R.string.back),
                    onPositiveButtonClick = {
                        dismissProgressDialog()
                    },
                    onNegativeButtonClick = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }

    private fun navigateToAnswer(imagePath: String, playerName: String) = AnswerFragment.apply { newInstance(imagePath = imagePath, playerName = playerName).show(this@SquadActivity) }

    companion object {

        private const val EXTRAS_SQUAD_NAME = "EXTRAS_SQUAD_NAME"

        fun createIntent(context: Context?, squadName: String = ""): Intent {
            return Intent(context, SquadActivity::class.java).apply {
                putExtra(EXTRAS_SQUAD_NAME, squadName)
            }
        }
    }
}