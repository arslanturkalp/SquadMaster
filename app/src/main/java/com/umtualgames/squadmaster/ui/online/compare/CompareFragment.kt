package com.umtualgames.squadmaster.ui.online.compare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.databinding.FragmentCompareBinding
import com.umtualgames.squadmaster.network.responses.item.Player
import com.umtualgames.squadmaster.ui.base.BaseBottomSheetDialogFragment
import com.umtualgames.squadmaster.ui.main.MainActivity
import com.umtualgames.squadmaster.utils.getParcelableDataExtra
import com.umtualgames.squadmaster.utils.setVisibility
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompareFragment : BaseBottomSheetDialogFragment() {

    private val binding by lazy { FragmentCompareBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root.also {
            arguments?.let { bundle ->
                with(binding) {

                    if (bundle.getString(KEY_MY_CHOOSE_NAME) == null && bundle.getString(KEY_RIVAL_CHOOSE_NAME) == null) {
                        tvTitle.text = getString(R.string.rival_disconnect)
                        setVisibility(View.GONE, tvMyChoose, ivMyChoose, tvRivalChoose, ivRivalChoose)
                    }

                    tvMyChoose.text = bundle.getString("KEY_MY_CHOOSE_NAME")
                    ivMyChoose.apply {
                        Glide.with(context)
                            .asBitmap()
                            .load(bundle.getString("KEY_MY_CHOOSE_IMAGE"))
                            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(this)
                    }

                    tvRivalChoose.text = bundle.getString("KEY_RIVAL_CHOOSE_NAME")
                    ivRivalChoose.apply {
                        Glide.with(context)
                            .asBitmap()
                            .load(bundle.getString("KEY_RIVAL_CHOOSE_IMAGE"))
                            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(this)
                    }
                    btnGoToMain.setOnClickListener {
                        startActivity(MainActivity.createIntent(requireContext()))
                    }

                    if (bundle.getParcelableDataExtra<Player>(KEY_CORRECT_ANSWER) != null) {

                        setVisibility(View.VISIBLE, ivRivalStatus, ivMyStatus)

                        if (bundle.getParcelableDataExtra<Player>(KEY_CORRECT_ANSWER)?.displayName == tvRivalChoose.text.toString()) {
                            ivRivalStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pitch_green))
                        } else {
                            ivRivalStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
                        }

                        if (bundle.getParcelableDataExtra<Player>(KEY_CORRECT_ANSWER)?.displayName == tvMyChoose.text.toString()) {
                            ivMyStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pitch_green))
                        } else {
                            ivMyStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
                        }

                    }
                }
            }
        }
    }

    companion object {
        private const val KEY_MY_CHOOSE_NAME = "KEY_MY_CHOOSE_NAME"
        private const val KEY_MY_CHOOSE_IMAGE = "KEY_MY_CHOOSE_IMAGE"

        private const val KEY_RIVAL_CHOOSE_NAME = "KEY_RIVAL_CHOOSE_NAME"
        private const val KEY_RIVAL_CHOOSE_IMAGE = "KEY_RIVAL_CHOOSE_IMAGE"

        private const val KEY_CORRECT_ANSWER = "KEY_CORRECT_ANSWER"

        fun newInstance(myChooseImage: String? = null, myChooseName: String? = null, rivalChooseImage: String? = null, rivalChooseName: String? = null, correctAnswer: Player? = null): CompareFragment = CompareFragment().apply {

            arguments = Bundle().apply {
                putString(KEY_MY_CHOOSE_IMAGE, myChooseImage)
                putString(KEY_MY_CHOOSE_NAME, myChooseName)
                putString(KEY_RIVAL_CHOOSE_IMAGE, rivalChooseImage)
                putString(KEY_RIVAL_CHOOSE_NAME, rivalChooseName)
                putString(KEY_RIVAL_CHOOSE_NAME, rivalChooseName)

                putParcelable(KEY_CORRECT_ANSWER, correctAnswer)
            }
        }
    }

}