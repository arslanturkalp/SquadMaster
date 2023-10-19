package com.umtualgames.squadmaster.ui.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.orhanobut.hawk.Hawk
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.Constants
import com.umtualgames.squadmaster.application.Constants.ADMIN_PASSWORD
import com.umtualgames.squadmaster.application.Constants.ADMIN_USER
import com.umtualgames.squadmaster.application.SessionManager.updateToken
import com.umtualgames.squadmaster.databinding.ActivityRegisterBinding
import com.umtualgames.squadmaster.ui.base.BaseActivity
import com.umtualgames.squadmaster.ui.login.LoginActivity
import com.umtualgames.squadmaster.utils.setPortraitMode
import com.umtualgames.squadmaster.utils.showAlertDialogTheme
import com.umtualgames.squadmaster.utils.spaceControl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : BaseActivity() {

    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<RegisterViewModel>()

    private val lang = Hawk.get(Constants.KEY_APP_LANG, "en")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setPortraitMode()
        setupObservers()

        viewModel.loginAdmin(com.umtualgames.squadmaster.domain.entities.requests.LoginRequest(ADMIN_USER, ADMIN_PASSWORD))

        binding.apply {

            etUserName.spaceControl()
            etPassword.spaceControl()
            etMail.spaceControl()

            btnSignUp.setOnClickListener {
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
        }
    }

    private fun setupObservers() {
        viewModel.getViewState.observe(this) { state ->
            when (state) {
                is RegisterViewState.LoadingState -> showProgressDialog()
                is RegisterViewState.SuccessState -> {
                    dismissProgressDialog()
                    if (state.response.statusCode == 200) {
                        startActivity(LoginActivity.createIntent(this, state.response.data.username, binding.etPassword.text.toString()))
                    } else if (state.response.statusCode == 400) {
                        showAlertDialogTheme(getString(R.string.error), if (lang == "en") getString(R.string.used_username) else state.response.message)
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
                    updateToken(state.response.data.token.accessToken)
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