package com.example.protectorsofastrax

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.protectorsofastrax.data.BattleLocation
import com.example.protectorsofastrax.data.UserLocation
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.maps.android.SphericalUtil
import kotlinx.android.synthetic.main.activity_map.*
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class MapActivity : AppCompatActivity() {
    var map: MapView? = null
    private val MY_PERMISSIONS_REQUEST_LOCATION = 99

    var user = Firebase.auth.currentUser as FirebaseUser
    private var locationManager: LocationManager? = null

    private var userLocations: HashMap<String, UserLocation> = HashMap()
    private var battleLocations: HashMap<String, BattleLocation> = HashMap();

    private var myCircle: Polygon? = null

    private var showBattles = true
    private var showFriendsOnly = false
    private var cachedLocation: GeoPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val ctx: Context = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        setContentView(com.example.protectorsofastrax.R.layout.activity_map)


        map = findViewById<View>(com.example.protectorsofastrax.R.id.map) as MapView
        map!!.setTileSource(TileSourceFactory.MAPNIK)
        map!!.setMultiTouchControls(true)
        map!!.controller.setZoom(15.0)
        val startPoint = GeoPoint(43.3209, 21.8958)
        map!!.controller.setCenter(startPoint)

        val mReceive: MapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                return false
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                var intent = Intent(this@MapActivity, CreateBattleActivity::class.java)
                intent.putExtra("latitude", p.latitude)
                intent.putExtra("longitude", p.longitude)
                startActivity(intent)
                return false
            }
        }

        val OverlayEvents = MapEventsOverlay(baseContext, mReceive)
        map!!.overlays.add(OverlayEvents)

        map_back_btn.setOnClickListener {
            finish()
        }

        map_battle_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            showBattles = isChecked
            battleLocations.forEach { (key, value) ->
                value.marker?.setVisible(isChecked)
            }
        }


        map_friends_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            showFriendsOnly = isChecked
            if (isChecked) {
                userLocations.forEach { (key, value) ->
                    value!!.marker?.setVisible(value.isFriend!!)
                }
            } else {
                userLocations.forEach { (key, value) ->
                    value!!.marker?.setVisible(true)
                }
            }
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
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0L,
                0f,
                locationListener
            )
            setupUsersOnMap()
            setupBattlesOnMap()
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            cachedLocation = GeoPoint(location.latitude, location.longitude)
            if (myCircle == null) {
                myCircle = Polygon(map)
            } else {
                map!!.overlays.remove(myCircle)
            }
            val radius = 500.0
            val circlePoints = ArrayList<GeoPoint>()
            myCircle!!.strokeWidth = 3.0f
            myCircle!!.strokeColor = Color.rgb(65, 105, 225)
            myCircle!!.fillColor = Color.argb(50, 65, 105, 225)
            for (i in 0..360 step 10) {
                circlePoints.add(
                    GeoPoint(location.latitude, location.longitude).destinationPoint(
                        radius,
                        i.toDouble()
                    )
                )
            }
            myCircle!!.points = circlePoints
            myCircle!!.setOnClickListener { polygon, mapView, eventPos -> false }
            map!!.overlays.add(myCircle)
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
                        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
                        locationManager?.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            0L,
                            0f,
                            locationListener
                        )
                        setupUsersOnMap()
                        setupBattlesOnMap()
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

    private fun setupUsersOnMap() {
        FirebaseFirestore.getInstance().collection("users").get()
            .addOnSuccessListener { snapshot ->
                val allUsers = snapshot.documents
                var users: ArrayList<String> = ArrayList();
                allUsers.forEach {
                    users.add(it.id)
                }
                FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                    .addOnSuccessListener { allFriends ->
                        var friends: List<String> = allFriends.data!!["friends"] as List<String>
                        users.forEach { id ->
                            var isFriend = false
                            if (friends.contains(id)) {
                                isFriend = true
                            }
                            if (userLocations[id] != null) {
                                map!!.overlays.remove(userLocations[id]!!.marker)
                                userLocations[id]!!.isFriend = isFriend
                                userLocations[id]!!.marker = null
                            } else {
                                userLocations[id] = UserLocation(id, 0.0, 0.0, isFriend, null)
                            }
                        }
                    }
            }

        FirebaseDatabase.getInstance().reference.child("users")
            .addValueEventListener(usersLocationListener)
    }

    private val usersLocationListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.value == null) {
                return
            }
            val locations = snapshot.value as HashMap<String, HashMap<String, Double>>
            locations.forEach { (key, value) ->
                if(userLocations[key] == null || userLocations[key] != null && userLocations[key]!!.isFriend == null){
                    userLocations[key] = UserLocation(
                        key,
                        value["latitude"]!!,
                        value["longitude"]!!,
                        false,
                        null
                    )
                }
                userLocations[key]!!.latitude = value["latitude"]!!
                userLocations[key]!!.longitude = value["longitude"]!!
                var user = userLocations[key]
                if (user?.marker == null && user!!.isFriend!!) {
                    FirebaseStorage.getInstance().reference
                        .child("avatars/$key")
                        .downloadUrl
                        .addOnSuccessListener { url ->
                            val marker = Marker(map)
                            marker.position =
                                GeoPoint(value["latitude"]!!, value["longitude"]!!)
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            val options: RequestOptions = RequestOptions()
                                .centerCrop()
                                .placeholder(android.R.mipmap.sym_def_app_icon)
                                .error(android.R.mipmap.sym_def_app_icon)

                            Glide.with(this@MapActivity)
                                .asBitmap()
                                .load(url)
                                .apply(options)
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(
                                        bmp: Bitmap,
                                        transition: Transition<in Bitmap>?
                                    ) {
                                        val d: Drawable = BitmapDrawable(
                                            resources, Bitmap.createScaledBitmap(
                                                bmp,
                                                100,
                                                100 * bmp.height / bmp.width,
                                                true
                                            )
                                        )
                                        marker.icon = d
                                        map!!.overlays.add(marker)
                                        user?.marker = marker
                                        marker.setOnMarkerClickListener(Marker.OnMarkerClickListener { _marker, mapView ->
                                            intent =
                                                Intent(this@MapActivity, ProfileActivity::class.java)
                                            intent.putExtra("user_id", user?.uid)
                                            startActivity(intent)
                                            return@OnMarkerClickListener true
                                        })
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {}
                                });
                        }
                } else if (user!!.isFriend!!) {
                    map!!.overlays.remove(user.marker!!)
                    FirebaseStorage.getInstance().reference
                        .child("avatars/$key")
                        .downloadUrl
                        .addOnSuccessListener { url ->
                            val marker = Marker(map)
                            marker.position =
                                GeoPoint(value["latitude"]!!, value["longitude"]!!)
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            val options: RequestOptions = RequestOptions()
                                .centerCrop()
                                .placeholder(android.R.mipmap.sym_def_app_icon)
                                .error(android.R.mipmap.sym_def_app_icon)

                            Glide.with(this@MapActivity)
                                .asBitmap()
                                .load(url)
                                .apply(options)
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(
                                        bmp: Bitmap,
                                        transition: Transition<in Bitmap>?
                                    ) {
                                        val d: Drawable = BitmapDrawable(
                                            resources, Bitmap.createScaledBitmap(
                                                bmp,
                                                100,
                                                100 * bmp.height / bmp.width,
                                                true
                                            )
                                        )
                                        marker.icon = d
                                        map!!.overlays.add(marker)
                                        user?.marker = marker
                                        marker.setOnMarkerClickListener(Marker.OnMarkerClickListener { _marker, mapView ->
                                            intent =
                                                Intent(this@MapActivity, ProfileActivity::class.java)
                                            intent.putExtra("user_id", user?.uid)
                                            startActivity(intent)
                                            return@OnMarkerClickListener true
                                        })
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {}
                                });
                        }
                }
                if (map != null && user?.marker == null && !user!!.isFriend!!) {
                    if (user?.uid != Firebase.auth.uid!!) {
                        val marker = Marker(map)
                        marker.position =
                            GeoPoint(value["latitude"]!!, value["longitude"]!!)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        map!!.overlays.add(marker)
                        user?.marker = marker
                        if (showFriendsOnly) {
                            user!!.marker!!.setVisible(false)
                        } else {
                            user!!.marker!!.setVisible(true)
                        }
                    }
                    user?.latitude = value["latitude"]!!
                    user?.longitude = value["longitude"]!!
                    user?.marker?.position = GeoPoint(user!!.latitude, user!!.longitude)
                } else if(!user!!.isFriend!!) {
                    map!!.overlays.remove(user.marker)
                    if (user?.uid != Firebase.auth.uid!!) {
                        val marker = Marker(map)
                        marker.position =
                            GeoPoint(value["latitude"]!!, value["longitude"]!!)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        map!!.overlays.add(marker)
                        user?.marker = marker
                        if (showFriendsOnly) {
                            user!!.marker!!.setVisible(false)
                        } else {
                            user!!.marker!!.setVisible(true)
                        }
                    }
                    user?.latitude = value["latitude"]!!
                    user?.longitude = value["longitude"]!!
                    user?.marker?.position = GeoPoint(user!!.latitude, user!!.longitude)
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    private fun setupBattlesOnMap() {
        FirebaseDatabase.getInstance().reference.child("battles")
            .addValueEventListener(battleLocationListener)
    }

    private val battleLocationListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.value == null) {
                return
            }
            val locations = snapshot.value as HashMap<String, HashMap<String, Any>>
            locations.forEach { (key, value) ->
                if (battleLocations[key] == null) {
                    battleLocations[key] = BattleLocation(
                        key,
                        value["enemyId"] as String,
                        value["latitude"] as Double,
                        value["longitude"] as Double,
                        null,
                        null
                    )
                }
                battleLocations[key]!!.latitude = value["latitude"] as Double
                battleLocations[key]!!.longitude = value["longitude"] as Double
                val location = battleLocations[key]
                if (location?.marker != null) {
                    map!!.overlays.remove(location.marker)
                }
                val marker = Marker(map)
                marker.icon = getDrawable(R.drawable.sword_notif_icon)
                marker.position = GeoPoint(location!!.latitude, location!!.longitude)
                marker.setAnchor(Marker.ANCHOR_TOP, Marker.ANCHOR_LEFT)
                map!!.overlays.add(marker)
                location!!.marker = marker
                marker.setOnMarkerClickListener(Marker.OnMarkerClickListener { _marker, mapView ->
                    if (cachedLocation == null) {
                        return@OnMarkerClickListener true
                    }
                    val myLocation =
                        LatLng(cachedLocation!!.latitude, cachedLocation!!.longitude)
                    val battleLocation = LatLng(location.latitude, location.longitude)
                    val distance =
                        SphericalUtil.computeDistanceBetween(myLocation, battleLocation)
                    if (distance > 500.0) {
                        return@OnMarkerClickListener true
                    }
                    intent = Intent(this@MapActivity, BattleActivity::class.java)
                    intent.putExtra("battle_id", location.uid)
                    intent.putExtra("enemyID", location.enemyId)
                    startActivity(intent)
                    return@OnMarkerClickListener true
                })
                location?.marker!!.position =
                    GeoPoint(location!!.latitude, location!!.longitude)
                if (!showBattles) {
                    location?.marker!!.setVisible(false)
                } else {
                    location?.marker!!.setVisible(true)
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {}
    }

    override fun onResume() {
        super.onResume()
        map!!.onResume()
        setupBattlesOnMap()
        setupUsersOnMap()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        }
        locationManager?.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0L,
            0f,
            locationListener
        )
    }

    override fun onPause() {
        super.onPause()
        map!!.onPause()
        FirebaseDatabase.getInstance().reference.child("battles")
            .removeEventListener(battleLocationListener!!)
        FirebaseDatabase.getInstance().reference.child("users")
            .removeEventListener(usersLocationListener!!)
    }

    override fun onStop() {
        super.onStop()
        FirebaseDatabase.getInstance().reference.child("battles")
            .removeEventListener(battleLocationListener!!)
        FirebaseDatabase.getInstance().reference.child("users")
            .removeEventListener(usersLocationListener!!)
        if (locationManager != null)
            locationManager!!.removeUpdates(locationListener)
    }
}