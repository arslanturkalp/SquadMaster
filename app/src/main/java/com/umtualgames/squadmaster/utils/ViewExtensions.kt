package com.umtualgames.squadmaster.utils

import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

fun setVisibility(visibility: Int, vararg views: View?) {
    for (view in views) {
        view?.visibility = visibility
    }
}

fun AppCompatActivity.addOnBackPressedListener(onBackPressed: () -> Unit) = onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        onBackPressed.invoke()
    }
})