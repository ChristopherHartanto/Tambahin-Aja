package com.example.tambahinaja.model

data class User (
    var name: String? = "",
    var facebookId: String? = "",
    var email: String? = "",
    var noHandphone: String? = "",
    var online: HashMap<String,Boolean>? = null
)