package dev.sumanth.spd.utils

import android.app.Activity
import android.app.Activity.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager


fun closeKeyboard(context: Activity) {
    val view = context.currentFocus
    if (view != null) {
        val imm: InputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}