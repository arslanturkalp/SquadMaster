package com.umtualgames.squadmaster.ui.squad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.utils.adapter.PotentialAnswersAdapter
import com.umtualgames.squadmaster.application.Constants.AD_UNIT_ID_SQUAD
import com.umtualgames.squadmaster.application.SessionManager.clearIsShowedFlag
import com.umtualgames.squadmaster.application.SessionManager.clearIsShowedNumber
import com.umtualgames.squadmaster.application.SessionManager.clearUnknownAnswer
import com.umtualgames.squadmaster.application.SessionManager.getIsShowedFlag
import com.umtualgames.squadmaster.application.SessionManager.getIsShowedNumber
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.application.SessionManager.updateIsShowedFlag
import com.umtualgames.squadmaster.application.SessionManager.updateIsShowedNumber
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.application.SessionManager.updateUnknownAnswer
import com.umtualgames.squadmaster.application.SessionManager.updateUnknownImage
import com.umtualgames.squadmaster.data.entities.models.MessageEvent
import com.umtualgames.squadmaster.data.entities.models.Result
import com.umtualgames.squadmaster.databinding.ActivitySquadBinding
import com.umtualgames.squadmaster.domain.entities.requests.LevelPassRequest
import com.umtualgames.squadmaster.domain.entities.requests.UpdatePointRequest
import com.umtualgames.squadmaster.domain.entities.responses.item.Club
import com.umtualgames.squadmaster.domain.entities.responses.item.Player
import com.umtualgames.squadmaster.domain.entities.responses.item.PotentialAnswer
import com.umtualgames.squadmaster.ui.answer.AnswerFragment
import com.umtualgames.squadmaster.ui.base.BaseActivity
import com.umtualgames.squadmaster.ui.splash.SplashActivity
import com.umtualgames.squadmaster.utils.LangUtils.Companion.checkLanguage
import com.umtualgames.squadmaster.utils.getDataExtra
import com.umtualgames.squadmaster.utils.ifContains
import com.umtualgames.squadmaster.utils.ifExists10Number
import com.umtualgames.squadmaster.utils.ifTwoBack
import com.umtualgames.squadmaster.utils.ifTwoWinger
import com.umtualgames.squadmaster.utils.setGone
import com.umtualgames.squadmaster.utils.setPortraitMode
import com.umtualgames.squadmaster.utils.setVisibility
import com.umtualgames.squadmaster.utils.setVisible
import com.umtualgames.squadmaster.utils.show
import com.umtualgames.squadmaster.utils.showAlertDialogTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
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

    private var isAllFabButtonsVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val squad = intent.getDataExtra<Club>(EXTRAS_SQUAD)

        checkLanguage(this)
        setStatusBarColor()
        setPortraitMode()
        preventScreenshot()
        clearUnknownAnswer()
        clearIsShowedFlag()
        clearIsShowedNumber()

        setupRecyclerViews()
        setupObservers()
        loadAds()

        viewModel.getSquad(squad.name)

        binding.apply {
            setVisibility(View.GONE, ivWrongFirst, ivWrongSecond, ivWrongThird)

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

            fabJoker.apply {
                shrink()
                setOnClickListener {
                    isAllFabButtonsVisible = if (!isAllFabButtonsVisible) {
                        setVisibility(View.VISIBLE, fabFlag, fabNumber, tvNumber)
                        this.extend()
                        true
                    } else {
                        setVisibility(View.GONE, fabFlag, fabNumber, tvNumber)
                        this.shrink()
                        false
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun setStatusBarColor() {
        val window = this.window
        window.apply {
            addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS)
            statusBarColor = ContextCompat.getColor(this@SquadActivity, R.color.pitch_green)
        }
    }

    private fun preventScreenshot() = window.setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE)

    private fun setupRecyclerViews() {
        with(binding) {
            rvGoalkeeper.apply {
                adapter = goalkeeperAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            rvDefence.apply {
                adapter = defenceAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            rvMiddle.apply {
                adapter = middleAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            rvAttackingMiddle.apply {
                adapter = attackingMiddleAdapter
                layoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP).apply {
                    justifyContent = JustifyContent.SPACE_AROUND
                    alignItems = AlignItems.CENTER
                }
            }
            rvForwards.apply {
                adapter = forwardAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            rvPotentialAnswers.apply {
                adapter = potentialAnswersAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.apply {
                launch {
                    levelPassFlow.collect {
                        when (it) {
                            is Result.Error -> dismissProgressDialog()
                            is Result.Loading -> showProgressDialog()
                            is Result.Success -> {
                                dismissProgressDialog()
                                if (it.code == 300) showAlertDialogTheme(getString(R.string.warning), getString(R.string.club_is_passed))
                            }
                            is Result.Auth -> {
                                dismissProgressDialog()
                                refreshTokenLogin(getRefreshToken())
                            }
                        }
                    }
                }

                launch {
                    squadFlow.collect {
                        when (it) {
                            is Result.Error -> dismissProgressDialog()
                            is Result.Loading -> showProgressDialog()
                            is Result.Success -> {
                                dismissProgressDialog()
                                it.body!!.data.apply {
                                    setList(this.playerList, this.potentialAnswerList)
                                }
                                viewModel.getUserPoint(getUserID())
                            }
                            is Result.Auth -> {
                                dismissProgressDialog()
                                refreshTokenLogin(getRefreshToken())
                            }
                        }
                    }
                }

                launch {
                    updatePointFlow.collect {
                        when (it) {
                            is Result.Error -> dismissProgressDialog()
                            is Result.Loading -> showProgressDialog()
                            is Result.Success -> {
                                dismissProgressDialog()
                                binding.tvScore.text = it.body!!.data.point.toString()
                                EventBus.getDefault().post(MessageEvent("Score Update"))
                            }
                            is Result.Auth -> {
                                dismissProgressDialog()
                                refreshTokenLogin(getRefreshToken())
                            }
                        }
                    }
                }

                launch {
                    getPointFlow.collect {
                        when (it) {
                            is Result.Error -> dismissProgressDialog()
                            is Result.Loading -> showProgressDialog()
                            is Result.Success -> {
                                dismissProgressDialog()
                                setScore(it.body!!.data.point)
                            }
                            is Result.Auth -> {
                                dismissProgressDialog()
                                refreshTokenLogin(getRefreshToken())
                            }
                        }
                    }
                }

                launch {
                    refreshTokenFlow.collect {
                        when (it) {
                            is Result.Error -> {
                                dismissProgressDialog()
                                returnToSplash()
                            }
                            is Result.Loading -> showProgressDialog()
                            is Result.Success -> {
                                dismissProgressDialog()
                                it.body!!.apply {
                                    if (isSuccess) {
                                        updateToken(data.token.accessToken)
                                        updateRefreshToken(data.token.refreshToken)

                                        val squad = intent.getDataExtra<Club>(EXTRAS_SQUAD)
                                        viewModel.getSquad(squad.name)
                                    } else {
                                        returnToSplash()
                                    }
                                }
                            }
                            is Result.Auth -> {
                                dismissProgressDialog()
                                returnToSplash()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun returnToSplash() {
        startActivity(SplashActivity.createIntent(this, false))
    }

    private fun setScore(point: Int?) {
        binding.tvScore.text = point.toString()
    }

    private fun setList(squad: List<Player>, potentialAnswers: List<PotentialAnswer>) {

        val goalkeeper = squad.filter { it.isGoalkeeper() }
        val defences = ifTwoBack(squad.filter { it.isDefender() } as ArrayList<Player>)
        val midfielders = squad.filter { it.isMidfielder() && !it.is10Number() }
        val attackingMidfielders = if (ifExists10Number(squad)) squad.filter { it.isOF() || it.is10Number() } else squad.filter { it.isRightWinger() }
        val forwards = if (ifExists10Number(squad)) ifTwoWinger(squad.filter { it.isForward() && !it.isOF() } as ArrayList<Player>) else ifTwoWinger(squad.filter { it.isForward() && !it.isOF() && !it.isRightWinger() } as ArrayList<Player>)

        val isLevelPassed = intent.getDataExtra<Boolean>(EXTRAS_IS_PASSED)
        setupUI(squad)

        binding.apply {

            if (isLevelPassed) {
                squad.forEach { it.isVisible = true }
                setVisibility(View.GONE, rvPotentialAnswers, fabJoker)
                tvLevelPassed.setVisible()
                tvAnswerTitle.text = getString(R.string.info)
            } else {
                setVisibility(View.VISIBLE, rvPotentialAnswers, fabJoker)
                tvLevelPassed.setGone()
            }
        }

        goalkeeperAdapter.updateAdapter(goalkeeper)
        defenceAdapter.updateAdapter(defences)
        middleAdapter.updateAdapter(midfielders)
        attackingMiddleAdapter.updateAdapter(attackingMidfielders)
        forwardAdapter.updateAdapter(forwards)
        potentialAnswersAdapter.updateAdapter(potentialAnswers)
    }

    private fun setupUI(squad: List<Player>) {

        binding.apply {

            val team = intent.getDataExtra<Club>(EXTRAS_SQUAD)
            val unknownPlayer = squad.first { !it.isVisible }

            tvTeamName.text = team.name

            ivTeam.apply {
                Glide.with(context)
                    .asBitmap()
                    .load(squad.first().squadImagePath)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(this)
            }

            setVisibility(View.VISIBLE, ivFootballPitch, ivFootballGoal, llHalfSquare, tvAnswerTitle)

            fabFlag.setOnClickListener {
                if (!getIsShowedFlag()) {
                    showAlertDialogTheme(
                        title = getString(R.string.show_flag),
                        contentMessage = getString(R.string.show_flag_description),
                        showNegativeButton = true,
                        positiveButtonTitle = getString(R.string.yes),
                        negativeButtonTitle = getString(R.string.no),
                        onPositiveButtonClick = {

                            if (tvScore.text.toString().toInt() >= 20) {
                                fabFlag.apply {
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

            fabNumber.setOnClickListener {
                if (!getIsShowedNumber()) {
                    showAlertDialogTheme(
                        title = getString(R.string.show_number),
                        contentMessage = getString(R.string.show_number_description),
                        showNegativeButton = true,
                        positiveButtonTitle = getString(R.string.yes),
                        negativeButtonTitle = getString(R.string.no),
                        onPositiveButtonClick = {
                            if (tvScore.text.toString().toInt() >= 20) {
                                tvNumber.text = unknownPlayer.number.toString()
                                updateIsShowedNumber(true)
                                viewModel.updatePoint(UpdatePointRequest(getUserID(), -20))
                            } else Toast.makeText(this@SquadActivity, getString(R.string.insufficient_score), Toast.LENGTH_SHORT).show()
                        },
                        onNegativeButtonClick = { dismissProgressDialog() })
                }
            }
        }
    }

    private fun controlAnswer(potentialAnswer: PotentialAnswer) {
        val team = intent.getDataExtra<Club>(EXTRAS_SQUAD)

        val level = team.leagueOrder
        binding.apply {
            if (potentialAnswer.isAnswer) {
                EventBus.getDefault().post(MessageEvent("Score Update"))
                if (level == 5 || level == 10 || level == 15 || level == 18 || level == 20) {
                    if (mInterstitialAd != null) {
                        mInterstitialAd?.show(this@SquadActivity)
                    }
                }
                viewModel.levelPass(
                    LevelPassRequest(
                        userID = getUserID(),
                        point = 25,
                        leagueID = team.leagueID,
                        squadID = team.id
                    )
                )
                updateUnknownAnswer(potentialAnswer.displayName)
                updateUnknownImage(potentialAnswer.imagePath)
                navigateToAnswer(potentialAnswer.imagePath, potentialAnswer.displayName + " + " + if (team.leagueID == 7) getString(R.string.point_15) else getString(R.string.point_25))
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

        InterstitialAd.load(this, AD_UNIT_ID_SQUAD, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
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