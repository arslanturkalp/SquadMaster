package com.umtualgames.squadmaster.ui.main

import com.umtualgames.squadmaster.ui.base.BaseActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.application.SessionManager.getIsShowedTutorial
import com.umtualgames.squadmaster.application.SessionManager.updateIsShowedTutorial
import com.umtualgames.squadmaster.databinding.ActivityMainBinding
import com.umtualgames.squadmaster.ui.home.HomeFragment
import com.umtualgames.squadmaster.ui.leagues.LeaguesFragment
import com.umtualgames.squadmaster.ui.score.ScoreFragment
import com.umtualgames.squadmaster.ui.slide.SlideFragment

class MainActivity : BaseActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val fragmentList: MutableList<Fragment> = mutableListOf()

    val homeFragment = HomeFragment()
    val leaguesFragment = LeaguesFragment()
    val scoreFragment = ScoreFragment()
    private val slideFragment = SlideFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initFragments()
        if (!getIsShowedTutorial()){
            showFragment(fragmentList.last())
            binding.bottomNavigationView.visibility = View.GONE
            updateIsShowedTutorial(true)
        } else{
            showFragment(fragmentList.first())
            binding.bottomNavigationView.visibility = View.VISIBLE
        }
        setupBottomNavigationView()
    }

    private fun initFragments() {
        fragmentList.apply {
            add(homeFragment)
            add(leaguesFragment)
            add(scoreFragment)
            add(slideFragment)
        }
    }

    fun showFragment(selectedFragment: Fragment) {

        val fragmentTransaction = supportFragmentManager.beginTransaction()

        fragmentList.forEach { fragment ->

            if (selectedFragment == fragment) {

                if (!selectedFragment.isAdded) {
                    fragmentTransaction.add(binding.fragmentContainerView.id, selectedFragment, getFragmentTag(selectedFragment))
                }
                fragmentTransaction.show(selectedFragment)

            } else {
                if (fragment.isAdded) {
                    fragmentTransaction.hide(fragment)
                }
            }
        }

        fragmentTransaction.commit()
    }

    private fun setupBottomNavigationView() {
        binding.bottomNavigationView.apply {
            itemIconTintList = null
            setOnItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.navigation_home -> {
                        showFragment(homeFragment)
                        true
                    }
                    R.id.navigation_leagues -> {
                        showFragment(leaguesFragment)
                        true
                    }
                    R.id.navigation_score -> {
                        showFragment(scoreFragment)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    fun setItemInNavigation(fragment: Fragment) {
        binding.bottomNavigationView.apply {
            if (fragment == homeFragment) {
                this.menu.getItem(0).isChecked = true
            }
            if (fragment == leaguesFragment) {
                this.menu.getItem(1).isChecked = true
            }
            if (fragment == scoreFragment) {
                this.menu.getItem(2).isChecked = true
            }
        }

    }

    private fun getFragmentTag(fragment: Fragment): String = fragment.javaClass.simpleName

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}