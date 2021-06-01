package com.supersuman.spd

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar
import kotlin.concurrent.thread

class CustomClass {

    fun snackBarMessage(context: Activity, string: String){
        Snackbar.make(context.window.decorView.rootView, string, Snackbar.LENGTH_SHORT).show()
    }
    fun snackBarMessage(view: View, string: String){
        Snackbar.make(view, string, Snackbar.LENGTH_SHORT).show()
    }
    fun closeKeyboard(context: Activity){
        val view = context.currentFocus
        if (view != null) {
            val imm: InputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
    fun isInternetConnection(): Boolean {
        var returnVal = false
        thread {
            returnVal = try {
                khttp.get("https://www.google.com/")
                true
            }catch (e:Exception){
                false
            }
        }.join()
        return returnVal
    }
}