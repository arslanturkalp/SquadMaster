package com.umtualgames.squadmaster.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.orhanobut.hawk.Hawk
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.Constants
import com.umtualgames.squadmaster.application.SessionManager.clearPassword
import com.umtualgames.squadmaster.application.SessionManager.clearScore
import com.umtualgames.squadmaster.application.SessionManager.clearUserID
import com.umtualgames.squadmaster.application.SessionManager.clearUserName
import com.umtualgames.squadmaster.application.SessionManager.getIsShowedTutorial
import com.umtualgames.squadmaster.application.SessionManager.getUserName
import com.umtualgames.squadmaster.application.SessionManager.isAdminUser
import com.umtualgames.squadmaster.databinding.FragmentSettingsBinding
import com.umtualgames.squadmaster.ui.base.BaseBottomSheetDialogFragment
import com.umtualgames.squadmaster.ui.splash.SplashActivity
import com.umtualgames.squadmaster.ui.start.StartActivity
import com.umtualgames.squadmaster.utils.LangUtils.Companion.checkLanguage
import com.umtualgames.squadmaster.utils.setGone
import com.umtualgames.squadmaster.utils.setVisible
import com.umtualgames.squadmaster.utils.showAlertDialogTheme
import java.util.*

class SettingsFragment : BaseBottomSheetDialogFragment() {

    private val binding by lazy { FragmentSettingsBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkLanguage(requireContext())
        setFlags()

        with(binding) {
            tvUserName.text = getUserName()

            if (!getIsShowedTutorial()) {
                btnAbout.setGone()
            }
            btnAbout.setOnClickListener {
                dismiss()
                showAlertDialogTheme(getString(R.string.app_name), "Umtual Games 2023©")
            }
            btnLogOut.apply {
                setOnClickListener {
                    clearUserName()
                    clearPassword()
                    clearScore()
                    clearUserID()
                    startActivity(StartActivity.createIntent(false, requireContext()))
                }

                if (isAdminUser()) setGone() else setVisible()
            }
        }
    }

    private fun setFlags() {
        val lang = Hawk.get(Constants.KEY_APP_LANG, "en")

        with(binding) {
            ivTurkish.apply {
                Glide.with(context)
                    .load(R.drawable.ic_turkish)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(this)

                setOnClickListener { if (lang == "en") changeLanguage("tr") }
            }
            ivEnglish.apply {
                Glide.with(context)
                    .load(R.drawable.ic_english)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(this)

                setOnClickListener { if (lang == "tr") changeLanguage("en") }
            }
            if (lang == "en") ivTurkish.alpha = 0.2f else ivEnglish.alpha = 0.2f
        }
    }

    @Suppress("DEPRECATION")
    private fun changeLanguage(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
        Hawk.put(Constants.KEY_APP_LANG, language)
        navigateToSplash()
    }

    private fun navigateToSplash(isFromChangeLanguage: Boolean = true) {
        startActivity(SplashActivity.createIntent(requireContext(), isFromChangeLanguage))
    }
}