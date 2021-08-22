package com.example.protectorsofastrax

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.protectorsofastrax.data.UserLocation
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_map.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.GroundOverlay2
import org.osmdroid.views.overlay.Marker
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class MapActivity : AppCompatActivity() {
    var map: MapView? = null
    var FIREBASE_CHILD = "users"
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    var user = Firebase.auth.currentUser as FirebaseUser
    private var locationManager: LocationManager? = null

    private var friendLocations: HashMap<String, UserLocation> = HashMap<String, UserLocation>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val ctx: Context = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        setContentView(R.layout.activity_map)


        map = findViewById<View>(R.id.map) as MapView
        map!!.setTileSource(TileSourceFactory.MAPNIK)
        map!!.setMultiTouchControls(true)
        map!!.controller.setZoom(15.0)
        val startPoint = GeoPoint(43.3209, 21.8958)
        map!!.controller.setCenter(startPoint)

        map_back_btn.setOnClickListener {
            finish()
        }


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        } else {
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                .addOnSuccessListener { ds ->
                    var friends: List<String> = ds.data!!["friends"] as List<String>
                    friends.forEach { id ->
                        friendLocations[id] = UserLocation(id, 0.0, 0.0, null)
                    }
                    setupMap()
                }

        }


    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            FirebaseDatabase.getInstance().reference.child(FIREBASE_CHILD).child(user.uid)
                .setValue(GeoPoint(location.latitude, location.longitude))
            setupMap()
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        setupMap()
                    }

                } else {
                    Toast.makeText(this, "Enable location to use battlefield", Toast.LENGTH_LONG)
                        .show()
                }
                return
            }
        }
    }

    private fun requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupMap() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        locationManager?.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0L,
            0f,
            locationListener
        )

        friendLocations.forEach {
            FirebaseDatabase.getInstance().reference.child(FIREBASE_CHILD).child(it.key)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value == null) {
                            return
                        }
                        val location: HashMap<String, Double> =
                            snapshot.value as HashMap<String, Double>
                        val friend = friendLocations[it.key]
                        if (friend?.marker == null) {
                            FirebaseStorage.getInstance().reference
                                .child("avatars/" + it.key)
                                .downloadUrl
                                .addOnSuccessListener { url ->
                                    val connection: HttpURLConnection =
                                        URL(url.toString()).openConnection() as HttpURLConnection
                                    connection.connect()
                                    val input: InputStream = connection.inputStream

                                    val x = BitmapFactory.decodeStream(input)

                                    val drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(x, 100,  100 * x.height / x.width, true))

                                    val marker = Marker(map)
                                    marker.position = GeoPoint(location["latitude"]!!, location["longitude"]!!)
                                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                    marker.icon = drawable
                                    map!!.overlays.add(marker)
                                    friend?.marker = marker
                                    marker.setOnMarkerClickListener(Marker.OnMarkerClickListener { _marker, mapView ->

                                        intent= Intent(this@MapActivity, ProfileActivity::class.java)
                                        intent.putExtra("user_id", friend?.uid)
                                        startActivity(intent)
                                        return@OnMarkerClickListener true
                                    })
                                }
                        }
                        friend?.latitude = location["latitude"]!!
                        friend?.longitude = location["longitude"]!!
                        friend?.marker?.position = GeoPoint(friend!!.latitude, friend!!.longitude)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

    }

    fun findFriends() {

    }

    override fun onResume() {
        super.onResume()
        map!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        map!!.onPause()
    }
}