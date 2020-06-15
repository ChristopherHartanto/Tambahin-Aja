package com.ta.tambahinaja

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.ta.tambahinaja.utils.showSnackBar
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import com.ta.tambahinaja.main.MainActivity
import kotlinx.android.synthetic.main.activity_play.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast

class PlayActivity : AppCompatActivity(), NetworkConnectivityListener {

//    var dataEmail = intent.extras!!.getString("email")
//    var dataPassword = intent.extras!!.getString("password")
    var email = ""
    var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        email = etEmail.text.toString()
        password = etPassword.text.toString()

        btnLogin.onClick {
            if (email == "" || password == "")
                toast("Insert Email or Password")
            else
                toast("Success")
        }

        btnSignUp.onClick {
            startActivity(intentFor<SignUpActivity>("email" to email, "passwrod" to password))
        }
    }

    fun toSignUp() {

    }



    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(activity_play, "The network is back !", "LONG")
                } else {
                    showSnackBar(activity_play, "There is no more network", "INFINITE")
                }
            }
        }
    }
}
