package com.example.squadmaster.ui.splash

import com.example.squadmaster.ui.base.BaseActivity
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.core.content.ContextCompat
import com.example.squadmaster.R
import com.example.squadmaster.databinding.ActivitySplashBinding
import com.example.squadmaster.ui.start.StartActivity
import com.example.squadmaster.utils.LangUtils.Companion.checkLanguage

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkLanguage(this)
        setNavigationBarColor()
        rotateBall()
        goToStart()
    }

    private fun setNavigationBarColor() {
        window.navigationBarColor = ContextCompat.getColor(this, R.color.green)
    }

    private fun rotateBall() {
        val rotate = RotateAnimation(0f, 180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 3000
        rotate.interpolator = LinearInterpolator()
        binding.imageView.startAnimation(rotate)
    }

    private fun goToStart() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(StartActivity.createIntent(false,this))
        }, 2500)
    }

    companion object {

        private const val EXTRAS_IS_FROM_CHANGE_LANGUAGE = "EXTRAS_IS_FROM_CHANGE_LANGUAGE"

        fun createIntent(context: Context, isFromChangeLanguage: Boolean = false): Intent {
            return Intent(context, SplashActivity::class.java).apply {
                putExtra(EXTRAS_IS_FROM_CHANGE_LANGUAGE, isFromChangeLanguage)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}