package com.umtualgames.squadmaster.ui.squad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.adapter.PotentialAnswersAdapter
import com.umtualgames.squadmaster.application.SessionManager.clearIsShowedFlag
import com.umtualgames.squadmaster.application.SessionManager.clearUnknownAnswer
import com.umtualgames.squadmaster.application.SessionManager.getIsShowedFlag
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.application.SessionManager.updateIsShowedFlag
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.application.SessionManager.updateUnknownAnswer
import com.umtualgames.squadmaster.application.SessionManager.updateUnknownImage
import com.umtualgames.squadmaster.application.SquadMasterApp
import com.umtualgames.squadmaster.data.enums.PositionIdStatus
import com.umtualgames.squadmaster.data.enums.PositionTypeIdStatus
import com.umtualgames.squadmaster.data.models.MessageEvent
import com.umtualgames.squadmaster.databinding.ActivitySquadBinding
import com.umtualgames.squadmaster.network.requests.LevelPassRequest
import com.umtualgames.squadmaster.network.requests.UpdatePointRequest
import com.umtualgames.squadmaster.network.responses.item.Club
import com.umtualgames.squadmaster.network.responses.item.Player
import com.umtualgames.squadmaster.network.responses.item.PotentialAnswer
import com.umtualgames.squadmaster.ui.answer.AnswerFragment
import com.umtualgames.squadmaster.ui.base.BaseActivity
import com.umtualgames.squadmaster.utils.*
import com.umtualgames.squadmaster.utils.LangUtils.Companion.checkLanguage
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
        setPortraitMode()
        preventScreenshot()
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

    private fun preventScreenshot() = window.setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE)

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
                    if (state.response.statusCode == 300) {
                        showAlertDialogTheme(getString(R.string.warning), getString(R.string.club_is_passed))
                    }
                }
            }
        }
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
        val level = intent.getDataExtra<Club>(EXTRAS_SQUAD).leagueOrder
        binding.apply {
            if (potentialAnswer.isAnswer) {
                if (level == 5 || level == 10 || level == 15 || level == 18 || level == 20) {
                    if (mInterstitialAd != null) {
                        mInterstitialAd?.show(this@SquadActivity)
                    }
                }
                viewModel.levelPass(LevelPassRequest(
                    userID = getUserID(),
                    point = if (intent.getDataExtra<Club>(EXTRAS_SQUAD).leagueID == 7) 15 else 25,
                    leagueID = intent.getDataExtra<Club>(EXTRAS_SQUAD).leagueID,
                    squadID = intent.getDataExtra<Club>(EXTRAS_SQUAD).id
                ))
                updateUnknownAnswer(potentialAnswer.displayName)
                updateUnknownImage(potentialAnswer.imagePath)
                navigateToAnswer(potentialAnswer.imagePath, potentialAnswer.displayName + " + " + if (intent.getDataExtra<Club>(EXTRAS_SQUAD).leagueID == 7) getString(R.string.point_15) else getString(R.string.point_25))
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

        InterstitialAd.load(this,"ca-app-pub-4810521807152117/8646950040", adRequest, object: InterstitialAdLoadCallback() {
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