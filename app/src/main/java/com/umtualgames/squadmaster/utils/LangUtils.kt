package com.umtualgames.squadmaster.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.orhanobut.hawk.Hawk
import com.umtualgames.squadmaster.application.Constants.KEY_APP_LANG
import com.umtualgames.squadmaster.application.SquadMasterApp
import java.util.*

@Suppress("DEPRECATION")
class LangUtils {
    companion object {
        private fun getCurrentAppLanguage(context: Context): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) context.resources.configuration.locales.get(0).language else context.resources.configuration.locale.language

        fun checkLanguage(context: Context, activity: Activity) {
            val appLanguage = getCurrentAppLanguage(context)
            val settingsLanguage = AppCompatDelegate.getApplicationLocales()[0]?.language ?: "en"
            val savedAppLanguage = Hawk.get(KEY_APP_LANG, "en")

            if (settingsLanguage != savedAppLanguage) {
                changeLanguage(context, settingsLanguage)
                Hawk.put(KEY_APP_LANG, settingsLanguage)
            }

            if (settingsLanguage != appLanguage) {
                changeLanguage(context, settingsLanguage)
                activity.recreate()
            }
        }

        @Suppress("DEPRECATION")
        private fun changeLanguage(context: Context, language: String) {
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