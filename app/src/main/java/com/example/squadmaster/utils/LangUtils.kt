package com.example.squadmaster.utils

import android.content.Context
import android.os.Build
import com.example.squadmaster.application.Constants.KEY_APP_LANG
import com.example.squadmaster.application.SquadMasterApp
import com.orhanobut.hawk.Hawk
import java.util.*

class LangUtils {
    companion object {
        private fun getCurrentAppLanguage(context: Context): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) context.resources.configuration.locales.get(0).language else context.resources.configuration.locale.language

        fun checkLanguage(context: Context) {
            val appLanguage = getCurrentAppLanguage(context)
            val savedAppLanguage = Hawk.get(KEY_APP_LANG, "tr")

            if (savedAppLanguage != null && savedAppLanguage != appLanguage) {
                changeLanguage(context, savedAppLanguage)
            }
        }

        @Suppress("DEPRECATION")
        fun changeLanguage(context: Context, language: String) {
            val locale = Locale(language)
            Locale.setDefault(locale)

            SquadMasterApp.instance?.let {
                val configuration = it.resources.configuration
                configuration.locale = locale
                it.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            }

            val config = context.resources.configuration
            config.locale = locale
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }
}