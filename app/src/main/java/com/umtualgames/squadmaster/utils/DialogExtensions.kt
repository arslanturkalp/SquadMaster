package com.umtualgames.squadmaster.utils

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

fun DialogFragment.show(activity: FragmentActivity) {
    show(activity.supportFragmentManager, this::class.java.canonicalName)
}

fun BottomSheetDialogFragment.show(activity: FragmentActivity) {
    show(activity.supportFragmentManager, this::class.java.canonicalName)
}

fun BottomSheetDialogFragment.showAllowingStateLoss(activity: FragmentActivity) {
    activity.supportFragmentManager.beginTransaction().let {
        it.add(this, this::class.java.canonicalName)
        it.commitAllowingStateLoss()
    }
}

fun BottomSheetDialogFragment.showWithCondition(activity: FragmentActivity, predicate: () -> Boolean) {
    if (predicate.invoke()) {
        show(activity.supportFragmentManager, this::class.java.canonicalName)
    }
}