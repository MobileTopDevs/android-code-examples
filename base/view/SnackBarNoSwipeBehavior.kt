package com.sample.app.base.view

import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar

internal class SnackBarNoSwipeBehavior : BaseTransientBottomBar.Behavior() {
    override fun canSwipeDismissView(child: View): Boolean {
        return false
    }
}