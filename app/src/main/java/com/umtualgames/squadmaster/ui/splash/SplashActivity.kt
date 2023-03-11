package com.umtualgames.squadmaster.ui.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.Constants.ADMIN_PASSWORD
import com.umtualgames.squadmaster.application.Constants.ADMIN_USER
import com.umtualgames.squadmaster.databinding.ActivitySplashBinding
import com.umtualgames.squadmaster.network.requests.LoginRequest
import com.umtualgames.squadmaster.ui.base.BaseActivity
import com.umtualgames.squadmaster.ui.start.StartActivity
import com.umtualgames.squadmaster.utils.LangUtils.Companion.checkLanguage
import com.umtualgames.squadmaster.utils.showAlertDialogTheme

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<SplashViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkLanguage(this)
        setupObservers()
        setNavigationBarColor()
        rotateBall()

        viewModel.loginAdmin(LoginRequest(ADMIN_USER, ADMIN_PASSWORD))
    }

    private fun setNavigationBarColor() {
        window.navigationBarColor = ContextCompat.getColor(this, R.color.green)
    }

    private fun rotateBall() {
        val rotate = RotateAnimation(0f, 180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 3000
        rotate.interpolator = LinearInterpolator()
        binding.imageView.startAnimation(rotate)
    }

    private fun setupObservers() {
        viewModel.getViewState.observe(this) { state ->
            when (state) {
                is SplashViewState.LoadingState -> {}
                is SplashViewState.SuccessState -> {
                    if (state.response.data.first().settingName == "IsOnline" && state.response.data.first().settingValue == "true") {
                        goToStart()
                    } else {
                        showAlertDialogTheme(title = getString(R.string.error), contentMessage = getString(R.string.is_not_online))
                    }
                }
                is SplashViewState.ErrorState -> {
                    showAlertDialogTheme(title = getString(R.string.error), contentMessage = state.message)
                }
                is SplashViewState.AdminState -> {
                    viewModel.getProjectSettings()
                }
            }
        }
    }

    private fun goToStart() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(StartActivity.createIntent(false, this))
        }, 2500)
    }

    companion object {

        private const val EXTRAS_IS_FROM_CHANGE_LANGUAGE = "EXTRAS_IS_FROM_CHANGE_LANGUAGE"

        fun createIntent(context: Context, isFromChangeLanguage: Boolean = false): Intent {
            return Intent(context, SplashActivity::class.java).apply {
                putExtra(EXTRAS_IS_FROM_CHANGE_LANGUAGE, isFromChangeLanguage)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}