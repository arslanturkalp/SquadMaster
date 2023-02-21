package com.example.squadmaster.ui.start

import BaseActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.example.squadmaster.R
import com.example.squadmaster.application.SessionManager.getPassword
import com.example.squadmaster.application.SessionManager.getUserName
import com.example.squadmaster.application.SessionManager.updateRefreshToken
import com.example.squadmaster.application.SessionManager.updateToken
import com.example.squadmaster.application.SessionManager.updateUserID
import com.example.squadmaster.databinding.ActivityStartBinding
import com.example.squadmaster.network.requests.LoginRequest
import com.example.squadmaster.ui.clubs.ClubsActivity
import com.example.squadmaster.ui.login.LoginActivity
import com.example.squadmaster.ui.main.MainActivity
import com.example.squadmaster.ui.register.RegisterActivity
import com.example.squadmaster.utils.addOnBackPressedListener
import com.example.squadmaster.utils.getDataExtra
import com.example.squadmaster.utils.setVisibility
import com.example.squadmaster.utils.showAlertDialogTheme

class StartActivity() : BaseActivity() {

    private val binding by lazy { ActivityStartBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<StartViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupObservers()

        if (intent.getDataExtra(EXTRAS_FROM_GUEST)) {
            binding.btnLoginAsGuest.visibility = View.GONE
        }

        addOnBackPressedListener {
            showAlertDialogTheme(getString(R.string.quit_game), getString(R.string.game_close_alert), showNegativeButton = true, onPositiveButtonClick = { finishAndRemoveTask() })
        }

        if (getUserName() != "" && getPassword() != "") {
            binding.apply { setVisibility(View.GONE, tvLogo, btnLogin, btnSignUp) }
            viewModel.signIn(LoginRequest(username = getUserName(), password = getPassword()))
        }
        binding.apply {
            btnLogin.setOnClickListener { startActivity(LoginActivity.createIntent(this@StartActivity)) }
            btnSignUp.setOnClickListener { startActivity(RegisterActivity.createIntent(this@StartActivity)) }
            btnLoginAsGuest.setOnClickListener {
                showAlertDialogTheme(getString(R.string.login_as_guest), getString(R.string.login_as_guest_description), showNegativeButton = true, negativeButtonTitle = getString(R.string.yes), positiveButtonTitle = getString(R.string.no), onNegativeButtonClick = {
                    viewModel.signIn(LoginRequest(username = "admin", password = "admin1234"))
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
                    updateToken(state.response.data.token.accessToken)
                    updateRefreshToken(state.response.data.token.refreshToken)
                    updateUserID(state.response.data.id)
                    goToMain()
                }
                is StartViewState.ErrorState -> {
                    dismissProgressDialog()
                    showAlertDialogTheme(title = getString(R.string.error), contentMessage = state.message)
                }
                is StartViewState.WarningState -> {
                    dismissProgressDialog()
                    state.message?.let { showAlertDialogTheme(title = getString(R.string.warning), contentMessage = it) }
                }
            }
        }
    }

    private fun goToMain() {
        startActivity(MainActivity.createIntent(this))
    }

    companion object {
        private const val EXTRAS_FROM_GUEST = "EXTRAS_FROM_GUEST"

        fun createIntent(isFromGuest: Boolean, context: Context): Intent = Intent(context, StartActivity::class.java).apply {
            putExtra(EXTRAS_FROM_GUEST, isFromGuest)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}