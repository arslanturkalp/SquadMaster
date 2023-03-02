package com.umtualgames.squadmaster.ui.base

import androidx.appcompat.app.AppCompatActivity
import com.umtualgames.squadmaster.ui.generic.GenericProgressDialog

open class BaseActivity : AppCompatActivity() {

    private var progressDialog: GenericProgressDialog? = null

    fun showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = GenericProgressDialog()
            progressDialog?.show(supportFragmentManager, "ProgressDialog")
        }
    }

    fun dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog?.dismissAllowingStateLoss()
            progressDialog = null
        }
    }
}