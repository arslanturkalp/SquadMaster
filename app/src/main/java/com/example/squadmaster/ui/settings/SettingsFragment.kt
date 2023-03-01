package com.example.squadmaster.ui.settings

import BaseBottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.squadmaster.R
import com.example.squadmaster.application.Constants
import com.example.squadmaster.application.SessionManager.getIsShowedTutorial
import com.example.squadmaster.databinding.FragmentSettingsBinding
import com.example.squadmaster.ui.splash.SplashActivity
import com.example.squadmaster.utils.LangUtils.Companion.checkLanguage
import com.example.squadmaster.utils.showAlertDialogTheme
import com.orhanobut.hawk.Hawk
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

        if (!getIsShowedTutorial()) {
            binding.btnAbout.visibility = View.GONE
        }
        binding.btnAbout.setOnClickListener {
            dismiss()
            showAlertDialogTheme(getString(R.string.app_name), "Umtual Games 2023©")
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
            if (lang == "en") ivTurkish.alpha = 0.5f else ivEnglish.alpha = 0.5f
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