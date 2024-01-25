package com.umtualgames.squadmaster.ui.game

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.AudioManager.RINGER_MODE_NORMAL
import android.media.AudioManager.RINGER_MODE_SILENT
import android.media.AudioManager.RINGER_MODE_VIBRATE
import android.media.MediaPlayer
import android.os.*
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
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
import com.umtualgames.squadmaster.application.Constants.AD_UNIT_ID_GAME
import com.umtualgames.squadmaster.application.SessionManager.clearIsShowedFlag
import com.umtualgames.squadmaster.application.SessionManager.clearIsShowedNumber
import com.umtualgames.squadmaster.application.SessionManager.clearUnknownAnswer
import com.umtualgames.squadmaster.application.SessionManager.clearWrongCount
import com.umtualgames.squadmaster.application.SessionManager.getIsShowedFlag
import com.umtualgames.squadmaster.application.SessionManager.getIsShowedNumber
import com.umtualgames.squadmaster.application.SessionManager.getIsSoundOpen
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.getScore
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.application.SessionManager.getWrongCount
import com.umtualgames.squadmaster.application.SessionManager.isAdminUser
import com.umtualgames.squadmaster.application.SessionManager.updateIsShowedFlag
import com.umtualgames.squadmaster.application.SessionManager.updateIsShowedNumber
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateScore
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.application.SessionManager.updateUnknownAnswer
import com.umtualgames.squadmaster.application.SessionManager.updateUnknownImage
import com.umtualgames.squadmaster.application.SessionManager.updateWrongCount
import com.umtualgames.squadmaster.data.entities.models.Result
import com.umtualgames.squadmaster.databinding.ActivitySquadBinding
import com.umtualgames.squadmaster.domain.entities.requests.UpdatePointRequest
import com.umtualgames.squadmaster.domain.entities.responses.item.Player
import com.umtualgames.squadmaster.domain.entities.responses.item.PotentialAnswer
import com.umtualgames.squadmaster.ui.answer.AnswerFragment
import com.umtualgames.squadmaster.ui.base.BaseActivity
import com.umtualgames.squadmaster.ui.gameover.GameOverFragment
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.ui.splash.SplashActivity
import com.umtualgames.squadmaster.ui.yellowcard.YellowCardFragment
import com.umtualgames.squadmaster.utils.*
import com.umtualgames.squadmaster.utils.adapter.PotentialAnswersAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GameActivity : BaseActivity() {

    private val binding by lazy { ActivitySquadBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<GameViewModel>()

    private val goalkeeperAdapter by lazy { GameAdapter() }
    private val defenceAdapter by lazy { GameAdapter() }
    private val middleAdapter by lazy { GameAdapter() }
    private val attackingMiddleAdapter by lazy { GameAdapter() }
    private val forwardAdapter by lazy { GameAdapter() }

    private val potentialAnswersAdapter by lazy { PotentialAnswersAdapter(false) { controlAnswer(it) } }
    private var mInterstitialAd: InterstitialAd? = null

    private var isAllFabButtonsVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setPortraitMode()
        preventScreenshot()
        setStatusBarColor()
        clearUnknownAnswer()
        clearIsShowedFlag()
        clearIsShowedNumber()

        setupRecyclerViews()
        setupObservers()
        loadAds()

        addOnBackPressedListener { backToMainMenu() }

        viewModel.getSquad()

        binding.apply {
            tvScore.text = getScore().toString()
            ivPause.setOnClickListener {
                backToMainMenu()
            }
            svGeneral.postDelayed({ svGeneral.fullScroll(ScrollView.FOCUS_DOWN) }, 350)
            when (getWrongCount()) {
                1 -> setOpacity(0, ivWrongThird)
                2 -> setOpacity(0, ivWrongThird, ivWrongSecond)
                3 -> setOpacity(0, ivWrongThird, ivWrongSecond, ivWrongFirst)
                else -> clearWrongCount()
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
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            statusBarColor = ContextCompat.getColor(this@GameActivity, R.color.pitch_green)
        }
    }

    private fun preventScreenshot() = window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

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
                    squadFlow.collect {
                        when (it) {
                            is Result.Error -> dismissProgressDialog()
                            is Result.Loading -> showProgressDialog()
                            is Result.Success -> {
                                dismissProgressDialog()
                                it.body!!.data.apply {
                                    binding.tvTeamName.text = squad.name
                                    setList(playerList, potentialAnswerList)
                                }
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
                                GameOverFragment.apply { newInstance(score = it.body?.data?.lastPoint!!).showAllowingStateLoss(this@GameActivity) }
                                if (mInterstitialAd != null) {
                                    mInterstitialAd?.show(this@GameActivity)
                                }
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
                            is Result.Error -> dismissProgressDialog()
                            is Result.Loading -> showProgressDialog()
                            is Result.Success -> {
                                dismissProgressDialog()
                                it.body!!.apply {
                                    if (isSuccess) {
                                        updateToken(data.token.accessToken)
                                        updateRefreshToken(data.token.refreshToken)
                                        viewModel.getSquad()
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

    private fun backToMainMenu() {
        showAlertDialogTheme(
            title = getString(R.string.back_to_main_menu),
            contentMessage = getString(R.string.score_not_save_reminder),
            showNegativeButton = true,
            positiveButtonTitle = getString(R.string.yes),
            negativeButtonTitle = getString(R.string.no),
            onPositiveButtonClick = { startActivity(MainActivity.createIntent(this@GameActivity)) }
        )
    }

    private fun showWrongAnswerAnimation() {

        updateWrongCount(getWrongCount() + 1)
        if (getAudioMode() == 2) {
            vibrate()
        }
        when (getWrongCount()) {
            1 -> setBlinkAnimation(binding.ivWrongThird)
            2 -> setBlinkAnimation(binding.ivWrongSecond)
            3 -> setBlinkAnimation(binding.ivWrongFirst)
        }
        if (getWrongCount() == 2) {
            navigateToYellowCard()
        }
        if (getWrongCount() == 3) {
            navigateToGameOver(getScore())
            clearWrongCount()
        }
    }

    private fun setBlinkAnimation(imageView: AppCompatImageView) {
        val anim = AlphaAnimation(1.0f, 0.2f)
        anim.apply {
            duration = 500
            repeatMode = Animation.REVERSE
            repeatCount = 2
        }

        imageView.startAnimation(anim)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                imageView.alpha = 0f
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }

    private fun getAudioMode(): Int {
        val audio = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (audio.ringerMode) {
            RINGER_MODE_NORMAL -> return 0
            RINGER_MODE_SILENT -> return 1
            RINGER_MODE_VIBRATE -> return 2
        }
        return 0
    }

    @Suppress("DEPRECATION")
    private fun vibrate() {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(200, 1))
        } else {
            vib.vibrate(200)
        }
    }

    private fun setList(squad: List<Player>, potentialAnswers: List<PotentialAnswer>) {
        setupUI(squad)
        val goalkeeper = squad.filter { it.isGoalkeeper() }
        val defenders = ifTwoBack(squad.filter { it.isDefender() } as ArrayList<Player>)
        val midfielders = squad.filter { it.isMidfielder() && !it.is10Number() }
        val attackingMidfielders = if (ifExists10Number(squad)) squad.filter { it.isOF() || it.is10Number() } else squad.filter { it.isRightWinger() }
        val forwards = if (ifExists10Number(squad)) ifTwoWinger(squad.filter { it.isForward() && !it.isOF() } as ArrayList<Player>) else ifTwoWinger(squad.filter { it.isForward() && !it.is10Number() && !it.isRightWinger() } as ArrayList<Player>)

        goalkeeperAdapter.updateAdapter(goalkeeper)
        defenceAdapter.updateAdapter(defenders)
        middleAdapter.updateAdapter(midfielders)
        attackingMiddleAdapter.updateAdapter(attackingMidfielders)
        forwardAdapter.updateAdapter(forwards)

        binding.cdAnswer.setVisible()
        potentialAnswersAdapter.updateAdapter(potentialAnswers)
    }

    private fun setupUI(squad: List<Player>) {

        val unknownPlayer = squad.first { !it.isVisible }
        with(binding) {
            ivTeam.apply {
                Glide.with(context)
                    .asBitmap()
                    .load(unknownPlayer.squadImagePath)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
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
                            if (getScore() >= 20) {
                                fabFlag.apply {
                                    setBackgroundColor(ContextCompat.getColor(context, R.color.green))
                                    Glide.with(context)
                                        .asBitmap()
                                        .load("https://flagcdn.com/56x42/${ifContains(unknownPlayer.nationality.lowercase())}.png")
                                        .into(this)
                                }
                                updateIsShowedFlag(true)
                                updateScore(getScore() - 20)
                                tvScore.text = getScore().toString()

                            } else Toast.makeText(this@GameActivity, getString(R.string.insufficient_score), Toast.LENGTH_SHORT).show()
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
                            if (getScore() >= 20) {
                                tvNumber.text = unknownPlayer.number.toString()
                                updateIsShowedNumber(true)
                                fabNumber.setBackgroundColor(ContextCompat.getColor(this@GameActivity, R.color.green))
                                Glide.with(fabNumber.context)
                                    .asBitmap()
                                    .load("")
                                    .into(fabNumber)
                                updateScore(getScore() - 20)
                                tvScore.text = getScore().toString()

                            } else Toast.makeText(this@GameActivity, getString(R.string.insufficient_score), Toast.LENGTH_SHORT).show()
                        },
                        onNegativeButtonClick = { dismissProgressDialog() })
                }
            }
        }
    }

    private fun controlAnswer(potentialAnswer: PotentialAnswer) {
        potentialAnswer.apply {
            if (this.isAnswer) {
                updateScore(getScore() + 10)
                updateUnknownAnswer(this.displayName)
                updateUnknownImage(this.imagePath)
                if (getIsSoundOpen()) MediaPlayer.create(this@GameActivity, R.raw.correct_answer).apply {
                    isLooping = false
                    setVolume(100f, 100f)
                    start()
                }
                navigateToAnswer(this.imagePath, this.displayName)
            } else {
                if (getIsSoundOpen()) MediaPlayer.create(this@GameActivity, R.raw.wrong_answer).apply {
                    isLooping = false
                    setVolume(100f, 100f)
                    start()
                }
                showWrongAnswerAnimation()
            }
        }
    }

    private fun navigateToAnswer(imagePath: String, playerName: String) = AnswerFragment.apply { newInstance(imagePath = imagePath, playerName = playerName, isFromInfiniteMode = true).show(this@GameActivity) }

    private fun navigateToYellowCard() = YellowCardFragment().show(this@GameActivity)

    private fun navigateToGameOver(score: Int) {
        if (isAdminUser()) {
            GameOverFragment.apply { newInstance(score = score).showAllowingStateLoss(this@GameActivity) }
        } else {
            viewModel.updatePoint(UpdatePointRequest(getUserID(), score))
        }

    }

    private fun loadAds() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this, AD_UNIT_ID_GAME, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })
    }

    companion object {
        fun createIntent(context: Context?): Intent {
            return Intent(context, GameActivity::class.java)
        }
    }
}