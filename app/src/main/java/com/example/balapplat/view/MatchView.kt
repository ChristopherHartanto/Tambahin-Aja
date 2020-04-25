package com.example.balapplat.view

import com.google.firebase.database.DataSnapshot

interface MatchView{
    fun fetchOpponentData(dataSnapshot: DataSnapshot, inviter: Boolean)
}