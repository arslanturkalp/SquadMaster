package com.example.squadmaster.ui.login

import BaseActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.example.squadmaster.R
import com.example.squadmaster.application.SessionManager.updatePassword
import com.example.squadmaster.application.SessionManager.updateRefreshToken
import com.example.squadmaster.application.SessionManager.updateToken
import com.example.squadmaster.application.SessionManager.updateUserID
import com.example.squadmaster.application.SessionManager.updateUserName
import com.example.squadmaster.databinding.ActivityLoginBinding
import com.example.squadmaster.ui.main.MainActivity
import com.example.squadmaster.utils.showAlertDialogTheme

class LoginActivity : BaseActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupObservers()

        binding.apply {
            btnLogin.setOnClickListener {
                if (cbRememberMe.isChecked) {
                    viewModel.login(username = etUserName.text.toString(), password = etPassword.text.toString())
                } else {
                    showAlertDialogTheme(
                        title = getString(R.string.warning),
                        contentMessage = getString(R.string.remember_me_reminder),
                        showNegativeButton = true,
                        negativeButtonTitle = getString(R.string.login),
                        onNegativeButtonClick = { viewModel.login(username = etUserName.text.toString(), password = etPassword.text.toString()) },
                        positiveButtonTitle = getString(R.string.back),
                        onPositiveButtonClick = {
                            cbRememberMe.isChecked = true
                            dismissProgressDialog()
                        }
                    )
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.getViewState.observe(this) { state ->
            when (state) {
                is LoginViewState.LoadingState -> showProgressDialog()
                is LoginViewState.SuccessState -> {
                    dismissProgressDialog()
                    updateToken(state.response.data.token.accessToken)
                    updateRefreshToken(state.response.data.token.refreshToken)
                    updateUserID(state.response.data.id)

                    updateUserName(binding.etUserName.text.toString())
                    updatePassword(binding.etPassword.text.toString())

                    goToMain()
                }
                is LoginViewState.ErrorState -> {
                    dismissProgressDialog()
                    showAlertDialogTheme(title = getString(R.string.error), contentMessage = state.message)
                }
                is LoginViewState.WarningState -> {
                    dismissProgressDialog()
                    state.message?.let { showAlertDialogTheme(title = getString(R.string.warning), contentMessage = it) }
                }
                is LoginViewState.ValidationState -> {
                    dismissProgressDialog()
                    showAlertDialogTheme(title = getString(R.string.warning), contentMessage = state.validationErrorList.joinToString(separator = "\n") { getString(it) })
                }
            }
        }
    }

    private fun goToMain() {
        startActivity(MainActivity.createIntent(this))
    }

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, LoginActivity::class.java)
    }
}