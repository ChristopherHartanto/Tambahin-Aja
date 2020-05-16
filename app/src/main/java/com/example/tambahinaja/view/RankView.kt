package com.example.tambahinaja.view

import com.google.firebase.database.DataSnapshot

interface RankView{
    fun loadData(dataSnapshot: DataSnapshot, response: String)
    fun response(message: String,response: String)
}