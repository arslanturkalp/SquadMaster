package com.umtualgames.squadmaster.ui.base

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umtualgames.squadmaster.ui.generic.GenericProgressDialog

open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var progressDialog: GenericProgressDialog? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        bottomSheetDialog.setOnShowListener {
            val dialog = it as BottomSheetDialog

            val behavior = BottomSheetBehavior.from(dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)!!)
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        return bottomSheetDialog
    }

    fun showProgressDialog() {
        progressDialog = GenericProgressDialog()
        progressDialog?.show(childFragmentManager, "ProgressDialog")
    }

    fun dismissProgressDialog() {
        progressDialog?.dismissAllowingStateLoss()
    }
}