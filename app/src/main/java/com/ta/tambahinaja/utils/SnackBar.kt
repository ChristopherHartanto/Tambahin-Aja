package com.ta.tambahinaja.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun showSnackBar(view: View, text: String, type: String): Unit {
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
    snackbar.setAction("Close") {
        snackbar.dismiss()
    }
    return snackbar.show()
}