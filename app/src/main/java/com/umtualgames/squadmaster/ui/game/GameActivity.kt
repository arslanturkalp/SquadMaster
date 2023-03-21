package com.umtualgames.squadmaster.ui.game

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.*
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
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
import com.umtualgames.squadmaster.application.SessionManager.clearWrongCount
import com.umtualgames.squadmaster.application.SessionManager.getIsShowedFlag
import com.umtualgames.squadmaster.application.SessionManager.getScore
import com.umtualgames.squadmaster.application.SessionManager.getWrongCount
import com.umtualgames.squadmaster.application.SessionManager.updateIsShowedFlag
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateScore
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.application.SessionManager.updateUnknownAnswer
import com.umtualgames.squadmaster.application.SessionManager.updateUnknownImage
import com.umtualgames.squadmaster.application.SessionManager.updateWrongCount
import com.umtualgames.squadmaster.application.SquadMasterApp.Companion.TAG
import com.umtualgames.squadmaster.data.enums.PositionIdStatus
import com.umtualgames.squadmaster.data.enums.PositionTypeIdStatus
import com.umtualgames.squadmaster.databinding.ActivitySquadBinding
import com.umtualgames.squadmaster.network.responses.item.Player
import com.umtualgames.squadmaster.network.responses.item.PotentialAnswer
import com.umtualgames.squadmaster.ui.answer.AnswerFragment
import com.umtualgames.squadmaster.ui.base.BaseActivity
import com.umtualgames.squadmaster.ui.gameover.GameOverFragment
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.ui.splash.SplashActivity
import com.umtualgames.squadmaster.ui.yellowcard.YellowCardFragment
import com.umtualgames.squadmaster.utils.*

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

    private var backgroundStartTime: Long = 0
    private var backgroundEndTime: Long = 0

    override fun onPause() {
        super.onPause()
        backgroundStartTime = SystemClock.elapsedRealtime()
    }

    override fun onResume() {
        super.onResume()
        if (intent.getDataExtra(EXTRAS_FROM_BACKGROUND)) {
            backgroundEndTime = SystemClock.elapsedRealtime()
            val elapsedSeconds: Double = ((backgroundEndTime - backgroundStartTime) / 1000.0)
            if (elapsedSeconds >= 1799) {
                startActivity(SplashActivity.createIntent(this, isFromChangeLanguage = false))
            }
        }
        intent.putExtra(EXTRAS_FROM_BACKGROUND, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        System.gc()

        setPortraitMode()
        preventScreenshot()
        clearUnknownAnswer()
        clearIsShowedFlag()

        setupRecyclerViews()
        setupObservers()
        loadAds()

        addOnBackPressedListener { backToMainMenu() }

        viewModel.getSquad()

        binding.apply {
            ivFlag.visibility = View.VISIBLE
            tvTeamName.apply { textSize = if (text.length > 23) 11f else if (text.length > 17) 12f else 14f }

            tvScore.text = getScore().toString()
            ivPause.setOnClickListener {
                backToMainMenu()
            }
            svGeneral.postDelayed({ svGeneral.fullScroll(ScrollView.FOCUS_DOWN) }, 350)
            when (getWrongCount()) {
                1 -> ivWrongThird.alpha = 0.2f
                2 -> {
                    ivWrongThird.alpha = 0.2f
                    ivWrongSecond.alpha = 0.2f
                }
                3 -> {
                    ivWrongThird.alpha = 0.2f
                    ivWrongSecond.alpha = 0.2f
                    ivWrongFirst.alpha = 0.2f
                }
                else -> clearWrongCount()
            }
        }
    }

    private fun preventScreenshot() = window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

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
                    with(state.response.data) {
                        setList(playerList, potentialAnswerList)
                        binding.tvTeamName.text = squad.name
                    }
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
                    with(state.response) {
                        updateToken(accessToken)
                        updateRefreshToken(refreshToken)
                    }
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
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this)
            }
            clearWrongCount()
        }
    }

    private fun setBlinkAnimation(imageView: AppCompatImageView) {
        val anim = AlphaAnimation(1.0f, 0.2f)
        anim.duration = 500
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = 2

        imageView.startAnimation(anim)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                imageView.alpha = 0.2f
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }

    private fun getAudioMode(): Int {
        val audio: AudioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (audio.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> return 0
            AudioManager.RINGER_MODE_SILENT -> return 1
            AudioManager.RINGER_MODE_VIBRATE -> return 2
        }
        return 0
    }

    @Suppress("DEPRECATION")
    private fun vibrate() {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
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
        goalkeeperAdapter.updateAdapter(squad.filter { it.positionTypeID == PositionTypeIdStatus.GOALKEEPER.value })
        defenceAdapter.updateAdapter(ifTwoBack(squad.filter { it.positionTypeID == PositionTypeIdStatus.DEFENCE.value } as ArrayList<Player>))
        middleAdapter.updateAdapter(squad.filter { it.positionTypeID == PositionTypeIdStatus.MIDFIELDER.value && it.positionID != PositionIdStatus.ON.value })
        attackingMiddleAdapter.updateAdapter(if (ifExists10Number(squad)) squad.filter { it.positionID == PositionIdStatus.FA.value || it.positionID == PositionIdStatus.ON.value } else squad.filter { it.positionID == 11 })
        forwardAdapter.updateAdapter(if (ifExists10Number(squad)) ifTwoWinger(squad.filter { it.positionTypeID == PositionTypeIdStatus.FORWARD.value && it.positionID != PositionIdStatus.FA.value } as ArrayList<Player>) else ifTwoWinger(squad.filter { it.positionTypeID == 4 && it.positionID != 10 && it.positionID != 11 } as ArrayList<Player>))

        binding.cdAnswer.visibility = View.VISIBLE
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

            ivFlag.setOnClickListener {
                if (!getIsShowedFlag()) {
                    showAlertDialogTheme(
                        title = getString(R.string.show_flag),
                        contentMessage = getString(R.string.show_flag_description),
                        showNegativeButton = true,
                        positiveButtonTitle = getString(R.string.yes),
                        negativeButtonTitle = getString(R.string.no),
                        onPositiveButtonClick = {
                            if (getScore() >= 20) {
                                ivFlag.apply {
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

    private fun loadAds() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this, "ca-app-pub-5776386569149871/5560350183", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.toString())
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })
    }

    companion object {
        private const val EXTRAS_FROM_BACKGROUND = "EXTRAS_FROM_BACKGROUND"

        fun createIntent(context: Context?): Intent {
            return Intent(context, GameActivity::class.java).apply {
                putExtra(EXTRAS_FROM_BACKGROUND, false)
            }
        }
    }
}