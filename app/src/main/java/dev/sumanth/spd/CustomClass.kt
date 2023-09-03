package com.supersuman.spd

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager

class CustomClass {

    fun closeKeyboard(context: Activity){
        val view = context.currentFocus
        if (view != null) {
            val imm: InputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}