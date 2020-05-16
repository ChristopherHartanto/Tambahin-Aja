package com.example.tambahinaja.model

import com.example.tambahinaja.play.GameType

data class Inviter(
    var facebookId: String? = "",
    var name: String? = "",
    var status: Boolean? = false,
    var type: GameType? = GameType.Normal,
    var timer: Int? = 0
)