package com.umtualgames.squadmaster.ui.start

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.orhanobut.hawk.Hawk
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.Constants
import com.umtualgames.squadmaster.application.Constants.ADMIN_PASSWORD
import com.umtualgames.squadmaster.application.Constants.ADMIN_USER
import com.umtualgames.squadmaster.application.SessionManager.clearPassword
import com.umtualgames.squadmaster.application.SessionManager.clearUserName
import com.umtualgames.squadmaster.application.SessionManager.getIsShowedTutorial
import com.umtualgames.squadmaster.application.SessionManager.getPassword
import com.umtualgames.squadmaster.application.SessionManager.getUserName
import com.umtualgames.squadmaster.application.SessionManager.updatePassword
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.application.SessionManager.updateUserID
import com.umtualgames.squadmaster.application.SessionManager.updateUserName
import com.umtualgames.squadmaster.databinding.ActivityStartBinding
import com.umtualgames.squadmaster.network.requests.LoginRequest
import com.umtualgames.squadmaster.ui.base.BaseActivity
import com.umtualgames.squadmaster.ui.login.LoginActivity
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.ui.register.RegisterActivity
import com.umtualgames.squadmaster.ui.settings.SettingsFragment
import com.umtualgames.squadmaster.ui.splash.SplashActivity
import com.umtualgames.squadmaster.utils.addOnBackPressedListener
import com.umtualgames.squadmaster.utils.getDataExtra
import com.umtualgames.squadmaster.utils.setVisibility
import com.umtualgames.squadmaster.utils.showAlertDialogTheme

class StartActivity : BaseActivity() {

    private val binding by lazy { ActivityStartBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<StartViewModel>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val lang = Hawk.get(Constants.KEY_APP_LANG, "en")

        if (!getIsShowedTutorial() && lang == "en") {
            SettingsFragment().show(supportFragmentManager, "")
        }

        setNavigationBarColor()
        setupObservers()

        if (intent.getDataExtra(EXTRAS_FROM_GUEST)) {
            binding.btnLoginAsGuest.visibility = View.GONE
        }

        addOnBackPressedListener {
            showAlertDialogTheme(getString(R.string.quit_game), getString(R.string.game_close_alert), showNegativeButton = true, onPositiveButtonClick = { finishAndRemoveTask() })
        }

        if (getUserName() != "" && getPassword() != "") {
            if (!intent.getDataExtra<Boolean>(EXTRAS_FROM_GUEST)) {
                binding.apply { setVisibility(View.GONE, tvLogo, btnLogin, btnSignUp, btnLoginAsGuest) }
                viewModel.signIn(LoginRequest(username = getUserName(), password = getPassword()))
            }
        }

        binding.apply {
            btnLogin.setOnClickListener { startActivity(LoginActivity.createIntent(this@StartActivity)) }
            btnSignUp.setOnClickListener { startActivity(RegisterActivity.createIntent(this@StartActivity)) }
            btnLoginAsGuest.setOnClickListener {
                showAlertDialogTheme(
                    getString(R.string.login_as_guest),
                    getString(R.string.login_as_guest_description),
                    showNegativeButton = true,
                    negativeButtonTitle = getString(R.string.yes),
                    positiveButtonTitle = getString(R.string.no),
                    onNegativeButtonClick = {
                        viewModel.signIn(LoginRequest(username = ADMIN_USER, password = ADMIN_PASSWORD))
                        updateUserName(ADMIN_USER)
                        updatePassword(ADMIN_PASSWORD)
                    })
            }
        }
    }

    private fun setupObservers() {
        viewModel.getViewState.observe(this) { state ->
            when (state) {
                is StartViewState.LoadingState -> showProgressDialog()
                is StartViewState.SuccessState -> {
                    dismissProgressDialog()
                    state.response.data.apply {
                        updateToken(token.accessToken)
                        updateRefreshToken(token.refreshToken)
                        updateUserID(id)
                    }
                    goToMain()

                }
                is StartViewState.WarningState -> {
                    dismissProgressDialog()
                    state.message?.let { showAlertDialogTheme(title = getString(R.string.warning), contentMessage = it) }
                    clearUserName()
                    clearPassword()
                    startActivity(SplashActivity.createIntent(this, false))
                }
                is StartViewState.ErrorState -> {
                    dismissProgressDialog()
                    showAlertDialogTheme(title = getString(R.string.error), contentMessage = state.message)
                }
            }
        }
    }

    private fun goToMain() {
        startActivity(MainActivity.createIntent(this))
    }

    private fun setNavigationBarColor() {
        window.navigationBarColor = ContextCompat.getColor(this, R.color.green)
    }

    companion object {
        private const val EXTRAS_FROM_GUEST = "EXTRAS_FROM_GUEST"

        fun createIntent(isFromGuest: Boolean, context: Context): Intent = Intent(context, StartActivity::class.java).apply {
            putExtra(EXTRAS_FROM_GUEST, isFromGuest)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}