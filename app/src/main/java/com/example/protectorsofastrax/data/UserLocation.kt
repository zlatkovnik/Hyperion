package com.example.protectorsofastrax.data

import org.osmdroid.views.overlay.GroundOverlay2
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

data class UserLocation(val uid: String, var latitude: Double, var longitude: Double, var overlay: GroundOverlay2?)
