package com.example.protectorsofastrax.data

import org.osmdroid.views.overlay.Marker

data class UserLocation(val uid: String, var latitude: Double, var longitude: Double, var isFriend: Boolean?, var marker: Marker?)
