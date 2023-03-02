package com.example.squadmaster.ui.answer

import BaseBottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.squadmaster.R
import com.example.squadmaster.application.SessionManager.updateRefreshToken
import com.example.squadmaster.application.SessionManager.updateToken
import com.example.squadmaster.data.models.MessageEvent
import com.example.squadmaster.databinding.FragmentAnswerBinding
import com.example.squadmaster.ui.game.GameActivity
import com.example.squadmaster.utils.showAlertDialogTheme
import org.greenrobot.eventbus.EventBus

class AnswerFragment : BaseBottomSheetDialogFragment() {

    private val binding by lazy { FragmentAnswerBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<AnswerViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root.also {
            arguments?.let { bundle ->
                with(binding) {
                    tvPlayerName.text = bundle.getString("KEY_PLAYER_NAME")
                    ivPlayerPhoto.apply {
                        Glide.with(context)
                            .asBitmap()
                            .load(bundle.getString("KEY_IMAGE_PATH"))
                            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(this)
                    }

                    if (bundle.getBoolean("KEY_IS_UNLOCKED_CLUB")) {
                        this@AnswerFragment.isCancelable = true
                        tvTitle.text = getString(R.string.club_unlocked)
                    }

                    btnNext.apply {
                        text = if (bundle.getBoolean("KEY_IS_FROM_INFINITE_MODE")) { getString(R.string.next_level) } else { getString(R.string.txt_continue) }
                        setOnClickListener {
                            if (bundle.getBoolean("KEY_IS_FROM_INFINITE_MODE")) {
                                context?.startActivity((GameActivity.createIntent(context)))
                            }
                            else if (bundle.getBoolean("KEY_IS_UNLOCKED_CLUB")) {
                                dismiss()
                            }
                            else {
                                EventBus.getDefault().post(MessageEvent("League Update"))
                                activity?.onBackPressedDispatcher?.onBackPressed()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.getViewState.observe(this) { state ->
            when (state) {
                is AnswerViewState.LoadingState -> showProgressDialog()
                is AnswerViewState.ErrorState -> {
                    dismissProgressDialog()
                }
                is AnswerViewState.WarningState -> {
                    dismissProgressDialog()
                    state.message?.let { showAlertDialogTheme(title = getString(R.string.warning), contentMessage = it) }
                }
                is AnswerViewState.RefreshState -> {
                    dismissProgressDialog()
                    updateToken(state.response.accessToken)
                    updateRefreshToken(state.response.refreshToken)
                }
            }
        }
    }

    companion object {

        private const val KEY_PLAYER_NAME = "KEY_PLAYER_NAME"
        private const val KEY_IMAGE_PATH = "KEY_IMAGE_PATH"

        private const val KEY_IS_FROM_INFINITE_MODE = "KEY_IS_FROM_INFINITE_MODE"
        private const val KEY_IS_UNLOCKED_CLUB = "KEY_IS_UNLOCKED_CLUB"

        fun newInstance(playerName: String, imagePath: String, isFromInfiniteMode: Boolean = false, isUnlockedClub: Boolean = false): AnswerFragment = AnswerFragment().apply {

            this.isCancelable = false
            arguments = Bundle().apply {
                putString(KEY_PLAYER_NAME, playerName)
                putString(KEY_IMAGE_PATH, imagePath)
                putBoolean(KEY_IS_FROM_INFINITE_MODE, isFromInfiniteMode)
                putBoolean(KEY_IS_UNLOCKED_CLUB, isUnlockedClub)
            }
        }
    }
}
