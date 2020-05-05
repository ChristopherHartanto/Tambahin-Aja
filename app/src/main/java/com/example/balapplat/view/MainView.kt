package com.example.balapplat.view

import com.google.firebase.database.DataSnapshot

interface MainView {
    fun loadData(dataSnapshot: DataSnapshot,response: String)
    fun response(message: String)
}