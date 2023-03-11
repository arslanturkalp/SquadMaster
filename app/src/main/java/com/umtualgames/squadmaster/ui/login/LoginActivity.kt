package com.umtualgames.squadmaster.ui.login

import com.umtualgames.squadmaster.ui.base.BaseActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager.updatePassword
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.application.SessionManager.updateUserID
import com.umtualgames.squadmaster.application.SessionManager.updateUserName
import com.umtualgames.squadmaster.databinding.ActivityLoginBinding
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.utils.setPortraitMode
import com.umtualgames.squadmaster.utils.showAlertDialogTheme

class LoginActivity : BaseActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setPortraitMode()
        setNavigationBarColor()
        setupObservers()

        binding.apply {
            btnLogin.setOnClickListener {
                viewModel.login(username = etUserName.text.toString(), password = etPassword.text.toString())
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

    private fun setNavigationBarColor() {
        window.navigationBarColor = ContextCompat.getColor(this, R.color.green)
    }

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, LoginActivity::class.java)
    }
}