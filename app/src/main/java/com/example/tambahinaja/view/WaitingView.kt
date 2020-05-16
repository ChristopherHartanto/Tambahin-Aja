package com.example.tambahinaja.view

import com.google.firebase.database.DataSnapshot

interface WaitingView {
    fun loadData(dataSnapshot: DataSnapshot, creator: Boolean)
    fun response(message: String)
    fun loadTips(tips: String)
}