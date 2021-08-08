package com.supersuman.spd

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar

class CustomClass {

    fun closeKeyboard(context: Activity){
        val view = context.currentFocus
        if (view != null) {
            val imm: InputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}