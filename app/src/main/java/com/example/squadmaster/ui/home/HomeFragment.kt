package com.example.squadmaster.ui.home

import BaseFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.squadmaster.R
import com.example.squadmaster.application.Constants.KEY_APP_LANG
import com.example.squadmaster.application.SessionManager.clearPassword
import com.example.squadmaster.application.SessionManager.clearScore
import com.example.squadmaster.application.SessionManager.clearUserName
import com.example.squadmaster.application.SessionManager.clearWrongCount
import com.example.squadmaster.application.SessionManager.getUserID
import com.example.squadmaster.databinding.FragmentHomeBinding
import com.example.squadmaster.ui.game.GameActivity
import com.example.squadmaster.ui.leagues.LeaguesFragment
import com.example.squadmaster.ui.main.MainActivity
import com.example.squadmaster.ui.score.ScoreFragment
import com.example.squadmaster.ui.splash.SplashActivity
import com.example.squadmaster.utils.showAlertDialogTheme
import com.orhanobut.hawk.Hawk
import java.util.*

class HomeFragment : BaseFragment() {

    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<HomeViewModel>()

    private lateinit var leagueFragment : LeaguesFragment
    private lateinit var scoreFragment : ScoreFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLanguage()
        setupObservers()

        viewModel.getUserPoint(getUserID())

        leagueFragment = LeaguesFragment()
        scoreFragment = ScoreFragment()

        binding.apply {
            ivLanguage.setOnClickListener {
                if (Hawk.get(KEY_APP_LANG, "en").equals("en")) {
                    changeLanguage("tr")
                } else {
                    changeLanguage("en")
                }
            }

            tvScore.text = getString(R.string.score)

            cvStart.setOnClickListener {
                context?.startActivity((GameActivity.createIntent(context)))
                clearScore()
                clearWrongCount()
            }

            cvScore.setOnClickListener {
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.fragmentContainerView, scoreFragment, "ScoreTag")
                transaction.commit()
                transaction.addToBackStack(null)
                (activity as MainActivity).setItemInNavigation(scoreFragment)
            }

            cvLeague.setOnClickListener {
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.fragmentContainerView, leagueFragment, "LeagueTag")
                transaction.commit()
                transaction.addToBackStack(null)
                (activity as MainActivity).setItemInNavigation(leagueFragment)
            }

            cvClose.setOnClickListener {
                showAlertDialogTheme(getString(R.string.close_game), getString(R.string.game_close_alert), showNegativeButton = true, onPositiveButtonClick = {
                    clearUserName()
                    clearPassword()
                    clearScore()
                    this@HomeFragment.activity?.finishAndRemoveTask()
                })
            }
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    return
                }
            })
    }

    private fun setupLanguage() {
        val lang = Hawk.get(KEY_APP_LANG, "en")

        binding.ivLanguage.apply {
            if (lang == "tr") {
                Glide.with(context)
                    .load("https://flagsapi.com/TR/shiny/64.png")
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(this)
            } else {
                Glide.with(context)
                    .load("https://flagsapi.com/GB/shiny/64.png")
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(this)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun changeLanguage(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
        Hawk.put(KEY_APP_LANG, language)
        navigateToSplash()
    }

    private fun navigateToSplash(isFromChangeLanguage: Boolean = true) {
        startActivity(SplashActivity.createIntent(requireContext(), isFromChangeLanguage))
    }

    private fun setupObservers() {
        viewModel.getViewState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeViewState.LoadingState -> {}
                is HomeViewState.UserPointState -> {
                    binding.tvScore.text = String.format(getString(R.string.total_score), state.response.data.bestPoint.toString(), state.response.data.point.toString())
                }
                is HomeViewState.ErrorState -> {
                    showAlertDialogTheme(title = getString(R.string.error), contentMessage = state.message)
                }
                else -> {}
            }
        }
    }
}