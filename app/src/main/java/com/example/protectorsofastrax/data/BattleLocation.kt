package com.example.protectorsofastrax.data

import org.osmdroid.views.overlay.Marker
data class BattleLocation(val uid: String,val enemyId: String, var latitude: Double, var longitude: Double,var marker: Marker?)
