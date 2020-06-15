package com.ta.tambahinaja

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ta.tambahinaja.main.MainActivity
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class SignUpActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    var a = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        sharedPreferences =  this.getSharedPreferences("rent", Context.MODE_PRIVATE)

        val isRent = sharedPreferences.getBoolean(tvTitle.text.toString(),false)

        if (isRent)
            btnSewa.visibility = View.INVISIBLE // button masih ada, tapi ga nampak
        //View.GONE // buttonn ga ada dan ga nampak

        btnSewa.onClick {
            var isRent = sharedPreferences.getBoolean(tvTitle.text.toString(),false)

            if (isRent)
                toast("You have Been Rent")
            else if(!isRent){
                alert ("Do you want to Rent?"){
                    title = "Confirmation"

                    yesButton {
                        editor = sharedPreferences.edit()
                        editor.putBoolean(tvTitle.text.toString(),true)
                        editor.apply()
                    }
                    noButton {
                        toast("So Sad")
                    }
                }.show()
            }
        }


    }

}