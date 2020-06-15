package com.ta.tambahinaja.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import com.ta.tambahinaja.R
import com.ta.tambahinaja.rank.Rank
import com.ta.tambahinaja.utils.showSnackBar
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.quantumhiggs.network.Event
import com.quantumhiggs.network.NetworkConnectivityListener
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity(), NetworkConnectivityListener {
    private lateinit var auth: FirebaseAuth
    private var darkStatusBar = true
    private lateinit var callbackManager : CallbackManager
    private lateinit var database: DatabaseReference

    //Google Login Request Code
    private val RC_SIGN_IN = 7
    //Google Sign In Client
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    //Firebase Auth
    private lateinit var gso : GoogleSignInOptions
    private var loginType = 0 // 1 fb 2 google

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        popup_window_button.typeface = typeface
        popup_window_title.typeface = typeface

        initGoogle()

        if (auth.currentUser != null){
            popup_window_button.visibility = View.GONE
            popup_window_title.text = "Log Out"
        }

        database = FirebaseDatabase.getInstance().reference
        if (Build.VERSION.SDK_INT in 19..20) {
            setWindowFlag(this, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // If you want dark status bar, set darkStatusBar to true
                if (darkStatusBar) {
                    this.window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                this.window.statusBarColor = Color.TRANSPARENT
                setWindowFlag(this, false)
            }
        }

        val alpha = 100 //between 0-255
        val alphaColor = ColorUtils.setAlphaComponent(Color.parseColor("#000000"), alpha)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), Color.TRANSPARENT, alphaColor)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            activity_login.setBackgroundColor(animator.animatedValue as Int)
        }

        popup_window_view_with_border.alpha = 0f
        popup_window_view_with_border.animate().alpha(1f).setDuration(500).setInterpolator(
            DecelerateInterpolator()
        ).start()
        colorAnimation.start()

        popup_window_button.setOnClickListener {
            onBackPressed()
        }

        btnFacebookLogin.onClick {
            loginType = 1

            callbackManager = CallbackManager.Factory.create()
            popup_window_button.text = "Please Wait"

            btnFacebookLogin.setReadPermissions("email", "public_profile")
            btnFacebookLogin.registerCallback(callbackManager, object :
                FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    handleFacebookAccessToken(loginResult.accessToken)

                }

                override fun onCancel() {
                    // ...
                }

                override fun onError(error: FacebookException) {
                    popup_window_button.text = "Please Try Again"
                }
            })// ...

        }
    }

    private fun setWindowFlag(activity: Activity, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        } else {
            winParams.flags = winParams.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
        }
        win.attributes = winParams
    }

    override fun onBackPressed() {
        // Fade animation for the background of Popup Window when you press the back button
        val alpha = 100 // between 0-255
        val alphaColor = ColorUtils.setAlphaComponent(Color.parseColor("#000000"), alpha)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), alphaColor, Color.TRANSPARENT)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            activity_login.setBackgroundColor(
                animator.animatedValue as Int
            )
        }

        // Fade animation for the Popup Window when you press the back button
        popup_window_view_with_border.animate().alpha(0f).setDuration(500).setInterpolator(
            DecelerateInterpolator()
        ).start()

        // After animation finish, close the Activity
        colorAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                finish()
                overridePendingTransition(0, 0)
            }
        })
        colorAnimation.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (loginType == 1)
            callbackManager.onActivityResult(requestCode, resultCode, data)
        else if(loginType == 2){
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        firebaseAuthWithGoogle(account)
                    }
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    toast("Google sign in failed")
                }

            }
        }
    }

//    public override fun onStart() { // untuk cek apakah udah login
//        super.onStart()
//        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//        updateUI(currentUser)
//    }

    private fun handleFacebookAccessToken(token: AccessToken) {

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    popup_window_button.text = "Let's Go"
                    checkExist()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }

                // ...
            }
    }

    private fun checkExist(){
        GlobalScope.launch {
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    if(!dataSnapshot.exists())
                        Init(0)
                    else
                        Init(loginType)
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            }

            database.child("users").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(postListener)

        }
    }

    private fun initGoogle(){
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso)

        btnGoogleLogin.onClick {
            loginType = 2
            loginGoogle()
        }
    }

    private fun loginGoogle(){
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("Login", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        toast("success")
                        checkExist()
                        popup_window_button.text = "Let's Go"
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Login", "signInWithCredential:failure", task.exception)
                        Toast.makeText(this,"Auth Failed",Toast.LENGTH_LONG).show()
                    }
                }
    }

    fun Init(loginType : Int){
        if (loginType == 0){
            val sdf = SimpleDateFormat("dd MMM yyyy")
            val currentDate = sdf.format(Date())
            var values: HashMap<String, Any> = hashMapOf()

            if (Profile.getCurrentProfile() != null)
                values  = hashMapOf(
                        "name" to auth.currentUser!!.displayName.toString(),
                        "facebookId" to Profile.getCurrentProfile().id,
                        "registerDate" to currentDate,
                        "dailyPuzzle" to "FreeFirstTime",
                        "currentRank" to Rank.Toddler
                )
            else if (GoogleSignIn.getLastSignedInAccount(this) != null){
                val acct = GoogleSignIn.getLastSignedInAccount(this)
//            if (acct != null) {
//                String personName = acct.getDisplayName();
//                String personGivenName = acct.getGivenName();
//                String personFamilyName = acct.getFamilyName();
//                String personEmail = acct.getEmail();
//                String personId = acct.getId();
//                Uri personPhoto = acct.getPhotoUrl();
//            }
                values  = hashMapOf(
                        "name" to auth.currentUser!!.displayName.toString(),
                        "googleUrl" to acct!!.photoUrl.toString(),
                        "registerDate" to currentDate,
                        "dailyPuzzle" to "FreeFirstTime",
                        "currentRank" to Rank.Toddler
                )
            }

            database.child("users").child(auth.currentUser!!.uid).setValue(values).addOnFailureListener {
                toast(""+ it.message)
            }

        values  = hashMapOf(
                "description" to "You Got 200 Credit",
                "type" to "Credit",
                "quantity" to 200
        )

        database.child("users").child(auth.currentUser!!.uid).child("reward").setValue(values).addOnFailureListener {
            toast(""+ it.message)
        }

            values = hashMapOf(
                    "credit" to 200,
                    "energy" to 100,
                    "energyLimit" to 100,
                    "point" to 200
            )

            database.child("users").child(auth.currentUser!!.uid).child("balance").setValue(values).addOnFailureListener {
                toast(""+ it.message)
            }

            values = hashMapOf(
                    "normal" to true,
                    "rush" to false,
                    "oddEven" to false,
                    "alphaNum" to false,
                    "mix" to false,
                    "doubleAttack" to false
            )

            database.child("users").child(auth.currentUser!!.uid).child("availableGame").setValue(values).addOnFailureListener {
                toast(""+ it.message)
            }

            values = hashMapOf(
                    "win" to 0,
                    "lose" to 0,
                    "tournamentWin" to 0,
                    "tournamentJoined" to 0
            )

            database.child("users").child(auth.currentUser!!.uid).child("stats").setValue(values).addOnFailureListener {
                toast(""+ it.message)
            }
        }else if (loginType == 1){
            database.child("users").child(auth.currentUser!!.uid).child("facebookId").setValue(Profile.getCurrentProfile().id)
        }else if(loginType == 2){
            val acct = GoogleSignIn.getLastSignedInAccount(this)
            database.child("users").child(auth.currentUser!!.uid).child("googleUrl").setValue(acct?.photoUrl.toString())
        }
        finish()
    }


    override fun networkConnectivityChanged(event: Event) {
        when (event) {
            is Event.ConnectivityEvent -> {
                if (event.state.isConnected) {
                    showSnackBar(activity_login, "The network is back !", "LONG")
                } else {
                    showSnackBar(activity_login, "There is no more network", "INFINITE")
                }
            }
        }
    }

}
