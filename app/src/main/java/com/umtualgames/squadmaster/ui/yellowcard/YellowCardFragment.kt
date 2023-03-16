package com.umtualgames.squadmaster.ui.yellowcard

import com.umtualgames.squadmaster.ui.base.BaseBottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.umtualgames.squadmaster.databinding.FragmentYellowCardBinding

class YellowCardFragment : BaseBottomSheetDialogFragment() {

    private val binding by lazy { FragmentYellowCardBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnContinue.setOnClickListener { dismiss() }
    }
}