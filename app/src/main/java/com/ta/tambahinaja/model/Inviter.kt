package com.ta.tambahinaja.model

import com.ta.tambahinaja.play.GameType

data class Inviter(
    var facebookId: String? = "",
    var name: String? = "",
    var status: Boolean? = false,
    var type: GameType? = GameType.Normal,
    var timer: Int? = 0
)