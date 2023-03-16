package com.umtualgames.squadmaster.ui.start

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
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
import com.umtualgames.squadmaster.network.requests.RegisterRequest
import com.umtualgames.squadmaster.ui.base.BaseActivity
import com.umtualgames.squadmaster.ui.login.LoginActivity
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.ui.register.RegisterActivity
import com.umtualgames.squadmaster.ui.settings.SettingsFragment
import com.umtualgames.squadmaster.ui.splash.SplashActivity
import com.umtualgames.squadmaster.utils.*

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

        setPortraitMode()
        setupObservers()

        if (intent.getDataExtra(EXTRAS_FROM_GUEST)) {
            binding.apply { setVisibility(View.GONE, btnLoginAsGuest, tvOr) }
        }

        (binding.btnGoogleSignIn.getChildAt(0) as TextView).text = getString(R.string.login_with_google)

        addOnBackPressedListener {
            showAlertDialogTheme(getString(R.string.quit_game), getString(R.string.game_close_alert), showNegativeButton = true, onPositiveButtonClick = { finishAndRemoveTask() })
        }

        if (getUserName() != "" && getPassword() != "") {
            if (!intent.getDataExtra<Boolean>(EXTRAS_FROM_GUEST)) {
                binding.apply { setVisibility(View.GONE, tvLogo, btnLogin, btnLoginAsGuest, btnGoogleSignIn, btnSignUp, tvOr, tvDontHaveAnAccount) }
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
            btnGoogleSignIn.setOnClickListener {
                viewModel.loginAdmin(LoginRequest(ADMIN_USER, ADMIN_PASSWORD))
            }
        }
    }

    private fun signInOptimisation() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(this, gso)

        val intent = client.signInIntent
        signInResult.launch(intent)

    }

    private val signInResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleSignInResult(task)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val userName = account.displayName.orEmpty().split(" ")[0].lowercase() + account.displayName.orEmpty().split(" ")[1].lowercase().replaceChars()

            viewModel.register(
                RegisterRequest(
                    name = account.displayName.orEmpty().split(" ")[0],
                    surname = account.displayName.orEmpty().split(" ")[1],
                    username = userName,
                    password = "GmailUser",
                    email = account.email.orEmpty()
                )
            )
            updateUserName(userName)
            updatePassword("GmailUser")

        } catch (e: ApiException) {
            Log.d("TAG", e.statusCode.toString())
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
                    if (state.message == "HTTP 403 Forbidden") {
                        viewModel.signIn(LoginRequest(getUserName(), getPassword()))
                    } else {
                        showAlertDialogTheme(title = getString(R.string.error), contentMessage = state.message)
                    }
                }
                is StartViewState.RegisterState -> {
                    dismissProgressDialog()
                    viewModel.signIn(LoginRequest(getUserName(), getPassword()))
                }
                is StartViewState.AdminState -> {
                    updateToken(state.response.data.token.accessToken)
                    signInOptimisation()
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