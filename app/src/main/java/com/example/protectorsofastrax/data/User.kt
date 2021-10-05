package com.example.protectorsofastrax.data

data class User(
    var id: String = "",
    var email: String = "",
    var username: String = "",
    var name: String = "",
    var surname: String = "",
    var phone: String = "",

    var experience: Double = 0.0,
    var battlesWon: Long = 0,
    var friends: List<String> = arrayOf<String>().toList(),
    var cards: List<String> = arrayOf<String>().toList()
)