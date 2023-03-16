package com.umtualgames.squadmaster.application

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.orhanobut.hawk.Hawk

class SquadMasterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        Hawk.init(instance).build()
        MobileAds.initialize(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    companion object {
        val TAG: String = Application::class.java.simpleName

        @get:Synchronized
        var instance: SquadMasterApp? = null
            private set

    }
}