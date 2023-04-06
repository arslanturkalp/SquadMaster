package com.umtualgames.squadmaster.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager.updatePassword
import com.umtualgames.squadmaster.application.SessionManager.updateRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.application.SessionManager.updateUserID
import com.umtualgames.squadmaster.application.SessionManager.updateUserName
import com.umtualgames.squadmaster.databinding.ActivityLoginBinding
import com.umtualgames.squadmaster.network.responses.item.League
import com.umtualgames.squadmaster.ui.base.BaseActivity
import com.umtualgames.squadmaster.ui.clubs.ClubsActivity
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.utils.getDataExtra
import com.umtualgames.squadmaster.utils.setPortraitMode
import com.umtualgames.squadmaster.utils.showAlertDialogTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setPortraitMode()
        setupObservers()


        binding.apply {
            val userName = intent.getDataExtra<String?>(EXTRAS_USER_NAME)
            val password = intent.getDataExtra<String?>(EXTRAS_PASSWORD)
            if (userName != null && password != null) {
                etUserName.setText(userName)
                etPassword.setText(password)
            }
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

    companion object {
        private const val EXTRAS_USER_NAME = "EXTRAS_USER_NAME"
        private const val EXTRAS_PASSWORD = "EXTRAS_PASSWORD"

        fun createIntent(context: Context, userName: String? = null, password: String? = null): Intent = Intent(context, LoginActivity::class.java).apply {
            putExtra("EXTRAS_USER_NAME", userName)
            putExtra("EXTRAS_PASSWORD", password)
        }
    }
}