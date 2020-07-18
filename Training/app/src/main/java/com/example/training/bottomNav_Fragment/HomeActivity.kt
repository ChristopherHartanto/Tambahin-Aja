package com.example.training.bottomNav_Fragment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.training.R
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bottom_navigation.setOnNavigationItemSelectedListener { menu ->

            when (menu.itemId) {
                R.id.home -> {
                    loadHomeFragment(savedInstanceState)
                }
                R.id.tournament -> {
                    loadTournamentFragment(savedInstanceState)
                }
                R.id.profile -> {
                    loadProfileFragment(savedInstanceState)
                }
            }

            true
        }
        bottom_navigation.selectedItemId = R.id.tournament
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putString("email","christopher@gmail.com")

        super.onSaveInstanceState(outState)
    }

    private fun loadHomeFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.main_container,
                    FragmentA(), FragmentA::class.java.simpleName
                ).commit()
        }
    }

    private fun loadTournamentFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.main_container,
                    FragmentB(), FragmentB::class.java.simpleName)
                .commit()
        }
    }

    private fun loadProfileFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.main_container,
                    FragmentC(), FragmentC::class.java.simpleName)
                .commit()
        }
    }

}
