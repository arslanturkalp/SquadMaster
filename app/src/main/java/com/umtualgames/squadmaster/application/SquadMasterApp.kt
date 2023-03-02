package com.umtualgames.squadmaster.application

import android.app.Application
import android.content.Context
import com.orhanobut.hawk.Hawk

class SquadMasterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        Hawk.init(instance).build()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    companion object {
        val TAG = Application::class.java.simpleName

        @get:Synchronized
        var instance: SquadMasterApp? = null
            private set

    }
}