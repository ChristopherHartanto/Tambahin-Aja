package com.example.training.recyclerview

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.training.R
import kotlinx.android.synthetic.main.activity_recycler_view.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class RecyclerViewActivity : AppCompatActivity() {

    private lateinit var adapter: RecyclerViewAdapter
    private var names: MutableList<String> = mutableListOf("Chris", "Albert")
    private var images: MutableList<Int> = mutableListOf(R.drawable.ic_launcher_background
        ,R.drawable.ic_launcher_background)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)

        adapter = RecyclerViewAdapter(this,names, images)

        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this)

    }

    override fun onStart() {

        btnAddName.onClick {
            names.add(etNewName.text.toString())
            images.add(R.drawable.ic_launcher_background)

            adapter.notifyDataSetChanged()
        }

        super.onStart()
    }

    fun calculate(){
        // tv.setText("")
        //2 for ()
        //3
        //4

    }
}

// bikin 1 row items -- isinya 2, tv Title dan tv Content
// 1 Edit Text dan 1 Button ( et nya reusable) pertama kali isi untuk title, kedua kali untuk content
// habis isi adapter di refresh

// editext dan button sebagai filter
// misalnya isi a, yang muncul title contain a

