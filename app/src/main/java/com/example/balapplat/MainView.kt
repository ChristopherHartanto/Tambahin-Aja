package com.example.balapplat

import com.google.firebase.database.DataSnapshot

interface MainView {
    fun loadData(dataSnapshot: DataSnapshot)
    fun response(message: String)
}