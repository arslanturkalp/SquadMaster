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
import com.example.squadmaster.ui.login.LoginActivity
import com.example.squadmaster.ui.main.MainActivity
import com.example.squadmaster.ui.register.RegisterActivity
import com.example.squadmaster.utils.setVisibility
import com.example.squadmaster.utils.showAlertDialogTheme

class StartActivity : BaseActivity() {

    private val binding by lazy { ActivityStartBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<StartViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupObservers()

        if (getUserName() != "" && getPassword() != "") {
            binding.apply { setVisibility(View.GONE, tvLogo, btnLogin, btnSignUp) }
            viewModel.signIn(LoginRequest(username = getUserName(), password = getPassword()))
        }
        binding.apply {
            btnLogin.setOnClickListener { startActivity(LoginActivity.createIntent(this@StartActivity)) }
            btnSignUp.setOnClickListener { startActivity(RegisterActivity.createIntent(this@StartActivity)) }
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
        fun createIntent(context: Context): Intent = Intent(context, StartActivity::class.java)
    }
}