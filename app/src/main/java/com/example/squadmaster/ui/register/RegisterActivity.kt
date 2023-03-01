package com.example.squadmaster.ui.register

import com.example.squadmaster.ui.base.BaseActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.example.squadmaster.R
import com.example.squadmaster.application.Constants.ADMIN_PASSWORD
import com.example.squadmaster.application.Constants.ADMIN_USER
import com.example.squadmaster.application.SessionManager.updateToken
import com.example.squadmaster.databinding.ActivityRegisterBinding
import com.example.squadmaster.network.requests.LoginRequest
import com.example.squadmaster.ui.login.LoginActivity
import com.example.squadmaster.utils.showAlertDialogTheme

class RegisterActivity : BaseActivity() {

    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<RegisterViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupObservers()

        binding.apply {
            btnSignUp.setOnClickListener {
                viewModel.loginAdmin(LoginRequest(ADMIN_USER, ADMIN_PASSWORD))
            }
        }
    }

    private fun setupObservers() {
        viewModel.getViewState.observe(this) { state ->
            when (state) {
                is RegisterViewState.LoadingState -> showProgressDialog()
                is RegisterViewState.SuccessState -> {
                    dismissProgressDialog()
                    if (state.response) {
                        startActivity(LoginActivity.createIntent(this))
                    }
                }
                is RegisterViewState.ErrorState -> {
                    dismissProgressDialog()
                    showAlertDialogTheme(title = getString(R.string.error), contentMessage = state.message)
                }
                is RegisterViewState.WarningState -> {
                    dismissProgressDialog()
                    state.message?.let { showAlertDialogTheme(title = getString(R.string.warning), contentMessage = it) }
                }
                is RegisterViewState.AdminState -> {
                    dismissProgressDialog()
                    updateToken(state.response.data.token.accessToken)

                    binding.apply {
                        viewModel.register(
                            name = etName.text.toString(),
                            surname = etSurname.text.toString(),
                            username = etUserName.text.toString(),
                            email = etMail.text.toString(),
                            password = etPassword.text.toString()
                        )
                    }
                }
                is RegisterViewState.ValidationState -> {
                    dismissProgressDialog()
                    showAlertDialogTheme(title = getString(R.string.warning), contentMessage = state.validationErrorList.joinToString(separator = "\n") { getString(it) })
                }
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, RegisterActivity::class.java)
    }
}