package com.umtualgames.squadmaster.ui.answer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.data.entities.models.MessageEvent
import com.umtualgames.squadmaster.databinding.FragmentAnswerBinding
import com.umtualgames.squadmaster.ui.base.BaseBottomSheetDialogFragment
import com.umtualgames.squadmaster.ui.game.GameActivity
import com.umtualgames.squadmaster.utils.LangUtils.Companion.checkLanguage
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
class AnswerFragment : BaseBottomSheetDialogFragment() {

    private val binding by lazy { FragmentAnswerBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        return binding.root.also {
            arguments?.let { bundle ->
                val playerName = bundle.getString("KEY_PLAYER_NAME")
                val imagePath = bundle.getString("KEY_IMAGE_PATH")
                val isUnlockedClub = bundle.getBoolean("KEY_IS_UNLOCKED_CLUB")
                val isAllClubsFinished = bundle.getBoolean("KEY_IS_ALL_CLUBS_FINISHED")
                val isFromInfiniteMode = bundle.getBoolean("KEY_IS_FROM_INFINITE_MODE")

                with(binding) {
                    tvPlayerName.text = playerName
                    ivPlayerPhoto.apply {
                        Glide.with(context)
                            .load(imagePath)
                            .fitCenter()
                            .into(this)
                    }

                    if (isUnlockedClub) {
                        this@AnswerFragment.isCancelable = true
                        ivPlayerPhoto.scaleType = ImageView.ScaleType.FIT_XY
                        tvTitle.text = getString(R.string.club_unlocked)
                    }

                    if (isAllClubsFinished) {
                        this@AnswerFragment.isCancelable = true
                        ivPlayerPhoto.setColorFilter(ContextCompat.getColor(requireContext(), R.color.green_two))
                        tvTitle.text = getString(R.string.all_clubs_finished)
                    }

                    btnNext.apply {
                        text = if (isFromInfiniteMode) {
                            getString(R.string.next_level)
                        } else {
                            getString(R.string.txt_continue)
                        }
                        setOnClickListener {
                            if (isFromInfiniteMode) {
                                context?.startActivity((GameActivity.createIntent(context)))
                            } else if (isUnlockedClub) {
                                dismiss()
                            } else {
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

        checkLanguage(requireContext())
    }

    companion object {

        private const val KEY_PLAYER_NAME = "KEY_PLAYER_NAME"
        private const val KEY_IMAGE_PATH = "KEY_IMAGE_PATH"

        private const val KEY_IS_FROM_INFINITE_MODE = "KEY_IS_FROM_INFINITE_MODE"
        private const val KEY_IS_UNLOCKED_CLUB = "KEY_IS_UNLOCKED_CLUB"
        private const val KEY_IS_ALL_CLUBS_FINISHED = "KEY_IS_ALL_CLUBS_FINISHED"

        fun newInstance(playerName: String, imagePath: String, isFromInfiniteMode: Boolean = false, isUnlockedClub: Boolean = false, isAllClubsFinished: Boolean = false): AnswerFragment = AnswerFragment().apply {
            this.isCancelable = false
            arguments = Bundle().apply {
                putString(KEY_PLAYER_NAME, playerName)
                putString(KEY_IMAGE_PATH, imagePath)
                putBoolean(KEY_IS_FROM_INFINITE_MODE, isFromInfiniteMode)
                putBoolean(KEY_IS_UNLOCKED_CLUB, isUnlockedClub)
                putBoolean(KEY_IS_ALL_CLUBS_FINISHED, isAllClubsFinished)
            }
        }
    }
}
