package com.ta.tambahinaja.profile

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ta.tambahinaja.R
import com.ta.tambahinaja.admin.AdminActivity
import com.ta.tambahinaja.main.LoginActivity
import com.facebook.Profile
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_setting.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class SettingActivity : AppCompatActivity() {

    private lateinit var adapter: SettingRecyclerViewAdapter
    private val settingItems : MutableList<Setting> = mutableListOf()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()
        val typeface = ResourcesCompat.getFont(this, R.font.fredokaone_regular)
        tvSettingTitle.typeface = typeface

        settingItems.add(Setting("Review Us!", ""))
        settingItems.add(Setting("Privacy Policy", ""))
        settingItems.add(Setting("Terms and Condition", ""))
        settingItems.add(Setting("About Us", ""))
        settingItems.add(Setting("Version", ""))
        settingItems.add(Setting("Contact Us", ""))

        val version = packageManager.getPackageInfo(packageName,0).versionName

        adapter = SettingRecyclerViewAdapter(this, settingItems) {
            when (it) {
                1 -> openWebsite(getString(R.string.privacy_url))
                2 -> openWebsite(getString(R.string.terms_url))
                4 -> toast("Current Version $version")
                5 -> sendEmail()
            }
        }
        rvSetting.layoutManager = LinearLayoutManager(this)
        rvSetting.adapter = adapter

        btnLogOut.onClick {
            auth.signOut()
            LoginManager.getInstance().logOut()
            finish()
        }

        btnAdmin.onClick {
            if (auth.currentUser!!.uid == getString(R.string.admin1) || auth.currentUser!!.uid == getString(R.string.admin2))
                startActivity<AdminActivity>()
        }
    }

    private fun openWebsite(url: String){
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(i)
    }

    private fun sendEmail() {
        /*ACTION_SEND action to launch an email client installed on your Android device.*/
        val mIntent = Intent(Intent.ACTION_SEND)
        /*To send an email you need to specify mailto: as URI using setData() method
        and data type will be to text/plain using setType() method*/
        mIntent.data = Uri.parse("mailto:")
        mIntent.type = "text/plain"
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.tambahin_aja_email)))
        //put the Subject in the intent
//        mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
//        //put the message in the intent
//        mIntent.putExtra(Intent.EXTRA_TEXT, message)


        try {
            //start email intent
            startActivity(Intent.createChooser(mIntent, "Choose Email Application"))
        }
        catch (e: Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }

    }
}

data class Setting(
        var title: String? = "",
        var desc: String? = ""
)
