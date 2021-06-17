package com.example.protectorsofastrax.data

data class User(
    var id: String,
    var email: String,
    var username: String,
    var name: String,
    var surname: String,
    var phone: String,

    var experience: Float = 0.0f
) {

}