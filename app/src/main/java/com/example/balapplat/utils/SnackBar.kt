package com.example.balapplat.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun showSnackBar(view: View, text: String, type: String): Snackbar {
    val snackbar = when (type) {
        "INFINITE" -> {
            Snackbar.make(
                view,
                text,
                Snackbar.LENGTH_INDEFINITE
            )
        }
        "LONG" -> {
            Snackbar.make(
                view,
                text,
                Snackbar.LENGTH_LONG
            )
        }
        else -> {
            Snackbar.make(
                view,
                text,
                Snackbar.LENGTH_SHORT
            )
        }
    }
//    snackbar.setBackgroundTint(0xe74c3c)
    snackbar.setAction("Close") {
        snackbar.dismiss()
    }
    return snackbar
}