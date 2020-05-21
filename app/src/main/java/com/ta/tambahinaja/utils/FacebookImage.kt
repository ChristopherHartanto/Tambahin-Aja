package com.ta.tambahinaja.utils

fun getFacebookProfilePicture(userID: String): String {
    return "https://graph.facebook.com/$userID/picture?type=large"
}