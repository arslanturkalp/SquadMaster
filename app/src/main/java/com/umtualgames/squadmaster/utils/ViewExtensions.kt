package com.umtualgames.squadmaster.utils

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.text.InputFilter
import android.view.View
import android.widget.EditText
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

fun EditText.spaceControl() {
    this.filters = this.filters.let {
        it + InputFilter { source, _, _, _, _, _ ->
            source.filterNot { char -> char.isWhitespace() }
        }
    }
}

@SuppressLint("SourceLockedOrientationActivity")
fun AppCompatActivity.setPortraitMode() {
    if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}