package com.example.squadmaster.ui.squad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.squadmaster.application.SessionManager.getIsShowedFlag
import com.example.squadmaster.application.SessionManager.getUserID
import com.example.squadmaster.application.SessionManager.updateIsShowedFlag
import com.example.squadmaster.application.SessionManager.updateRefreshToken
import com.example.squadmaster.application.SessionManager.updateToken
import com.example.squadmaster.application.SessionManager.updateUnknownAnswer
import com.example.squadmaster.application.SessionManager.updateUnknownImage
import com.example.squadmaster.application.SquadMasterApp
import com.example.squadmaster.data.enums.PositionIdStatus
import com.example.squadmaster.data.enums.PositionTypeIdStatus
import com.example.squadmaster.data.models.MessageEvent
import com.example.squadmaster.databinding.ActivitySquadBinding
import com.example.squadmaster.network.requests.LevelPassRequest
import com.example.squadmaster.network.requests.UpdatePointRequest
import com.example.squadmaster.network.responses.item.Club
import com.example.squadmaster.network.responses.item.Player
import com.example.squadmaster.network.responses.item.PotentialAnswer
import com.example.squadmaster.ui.answer.AnswerFragment
import com.example.squadmaster.ui.base.BaseActivity
import com.example.squadmaster.utils.*
import com.example.squadmaster.utils.LangUtils.Companion.checkLanguage
import com.google.android.flexbox.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import org.greenrobot.eventbus.EventBus

class SquadActivity : BaseActivity() {

    private val binding by lazy { ActivitySquadBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<SquadViewModel>()

    private val goalkeeperAdapter by lazy { SquadAdapter() }
    private val defenceAdapter by lazy { SquadAdapter() }
    private val middleAdapter by lazy { SquadAdapter() }
    private val attackingMiddleAdapter by lazy { SquadAdapter() }
    private val forwardAdapter by lazy { SquadAdapter() }

    private val potentialAnswersAdapter by lazy { PotentialAnswersAdapter(false) { controlAnswer(it) } }
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkLanguage(this)
        clearUnknownAnswer()
        clearIsShowedFlag()

        setupRecyclerViews()
        setupObservers()
        loadAds()

        viewModel.getSquad(intent.getDataExtra<Club>(EXTRAS_SQUAD).name)

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
                    viewModel.getUserPoint(getUserID())
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

                    viewModel.getSquad(intent.getDataExtra<Club>(EXTRAS_SQUAD).name)
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
                is GetSquadViewState.LevelPassState -> {
                    if (state.response.statusCode == 200) { } else {
                        showAlertDialogTheme(getString(R.string.warning), getString(R.string.club_is_passed))
                    }
                }
            }
        }
    }

    private fun sendEvent() {
        EventBus.getDefault().post(MessageEvent("League Update"))
    }

    private fun setScore(point: Int?) {
        binding.tvScore.text = point.toString()
    }

    private fun setList(squad: List<Player>, potentialAnswers: List<PotentialAnswer>) {
        setupUI(squad)
        binding.apply {
            if (intent.getDataExtra(EXTRAS_IS_PASSED)) {
                squad.forEach { it.isVisible = true }
                cdAnswer.visibility = View.GONE
                ivFlag.visibility = View.GONE
            } else {
                cdAnswer.visibility = View.VISIBLE
                ivFlag.visibility = View.VISIBLE
            }
        }
        goalkeeperAdapter.updateAdapter(squad.filter { it.positionTypeID == PositionTypeIdStatus.GOALKEEPER.value })
        defenceAdapter.updateAdapter(ifTwoBack(squad.filter { it.positionTypeID == PositionTypeIdStatus.DEFENCE.value } as ArrayList<Player>))
        middleAdapter.updateAdapter(squad.filter { it.positionTypeID == PositionTypeIdStatus.MIDFIELDER.value && it.positionID != PositionIdStatus.ON.value })
        attackingMiddleAdapter.updateAdapter(if (ifExists10Number(squad)) squad.filter { it.positionID == PositionIdStatus.FA.value || it.positionID == PositionIdStatus.ON.value } else squad.filter { it.positionID == 11 })
        forwardAdapter.updateAdapter(if(ifExists10Number(squad)) ifTwoWinger(squad.filter { it.positionTypeID == PositionTypeIdStatus.FORWARD.value && it.positionID != PositionIdStatus.FA.value } as ArrayList<Player>) else ifTwoWinger(squad.filter { it.positionTypeID == 4 && it.positionID != 10 && it.positionID != 11} as ArrayList<Player>))

        potentialAnswersAdapter.updateAdapter(potentialAnswers)
    }

    private fun setupUI(squad: List<Player>) {

        binding.apply {

            val unknownPlayer = squad.first { !it.isVisible }
            tvTeamName.text = intent.getDataExtra<Club>(EXTRAS_SQUAD).name
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

                            if (binding.tvScore.text.toString().toInt() >= 20) {
                                ivFlag.apply {
                                    setBackgroundColor(ContextCompat.getColor(context, R.color.green))
                                    Glide.with(context)
                                        .asBitmap()
                                        .load("https://flagcdn.com/56x42/${ifContains(unknownPlayer.nationality.lowercase())}.png")
                                        .into(this)
                                }
                                updateIsShowedFlag(true)
                                viewModel.updatePoint(UpdatePointRequest(getUserID(), -20))
                            } else Toast.makeText(this@SquadActivity, getString(R.string.insufficient_score), Toast.LENGTH_SHORT).show()
                        },
                        onNegativeButtonClick = { dismissProgressDialog() })
                }
            }
        }
    }

    private fun controlAnswer(potentialAnswer: PotentialAnswer) {
        val level = intent.getDataExtra<Club>(EXTRAS_SQUAD).level
        binding.apply {
            if (potentialAnswer.isAnswer) {
                if (level == 5 || level == 10 || level == 15 || level == 18) {
                    if (mInterstitialAd != null) {
                        mInterstitialAd?.show(this@SquadActivity)
                    }
                }
                viewModel.levelPass(LevelPassRequest(
                    userID = getUserID(),
                    point = 25,
                    leagueID = intent.getDataExtra<Club>(EXTRAS_SQUAD).leagueID,
                    squadID = intent.getDataExtra<Club>(EXTRAS_SQUAD).id
                ))
                updateUnknownAnswer(potentialAnswer.displayName)
                updateUnknownImage(potentialAnswer.imagePath)
                navigateToAnswer(potentialAnswer.imagePath, potentialAnswer.displayName + " + " + getString(R.string.point_25))
            } else {
                showAlertDialogTheme(
                    title = getString(R.string.wrong_answer),
                    contentMessage = String.format(getString(R.string.formatted_wrong_answer), potentialAnswer.displayName),
                    positiveButtonTitle = getString(R.string.try_again),
                    onPositiveButtonClick = {
                        onBackPressedDispatcher.onBackPressed()
                        EventBus.getDefault().post(MessageEvent("Wrong Answer"))
                    }
                )
            }
        }
    }

    private fun navigateToAnswer(imagePath: String, playerName: String) = AnswerFragment.apply { newInstance(imagePath = imagePath, playerName = playerName).show(this@SquadActivity) }

    private fun loadAds() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, object: InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(SquadMasterApp.TAG, adError.toString())
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(SquadMasterApp.TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })
    }

    companion object {

        private const val EXTRAS_SQUAD = "EXTRAS_SQUAD"
        private const val EXTRAS_IS_PASSED = "EXTRAS_IS_PASSED"

        fun createIntent(context: Context?, squad: Club, isPassed: Boolean): Intent {
            return Intent(context, SquadActivity::class.java).apply {
                putExtra(EXTRAS_SQUAD, squad)
                putExtra(EXTRAS_IS_PASSED, isPassed)
            }
        }
    }
}