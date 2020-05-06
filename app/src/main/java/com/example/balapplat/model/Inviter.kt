package com.example.balapplat.model

import com.example.balapplat.play.GameType

data class Inviter(
    var facebookId: String? = "",
    var name: String? = "",
    var status: Boolean? = false,
    var type: GameType? = GameType.Normal,
    var timer: Int? = 0
)