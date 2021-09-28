package com.example.protectorsofastrax

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.protectorsofastrax.data.BattleLocation
import com.example.protectorsofastrax.data.UserLocation
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class MapActivity : AppCompatActivity() {
    var map: MapView? = null
    var FIREBASE_CHILD = "users"
    var FIREBASE_BATTLES="battles"
    val MY_PERMISSIONS_REQUEST_LOCATION = 99

    var user = Firebase.auth.currentUser as FirebaseUser
    private var locationManager: LocationManager? = null

    private var userLocations: HashMap<String, UserLocation> = HashMap<String, UserLocation>()
    private var battleLocations: ArrayList<BattleLocation> = ArrayList<BattleLocation>();
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
            FirebaseFirestore.getInstance().collection("users").get()
                .addOnSuccessListener { allUsers ->
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
                                userLocations[id] = UserLocation(id, 0.0, 0.0, isFriend, null)
                            }
                            setupMap()
                        }
                }


            FirebaseDatabase.getInstance().reference.child(FIREBASE_BATTLES).get()
                .addOnSuccessListener { it ->
                    it.children.forEach{
                        var battles=it.value as  HashMap<String, BattleLocation>
                        battleLocations.add(BattleLocation(it.key as String,battles["enemyId"] as String,battles["latitude"] as Double, battles["longitude"] as Double, null))
                    }
                    setupMap()
                }
                .addOnFailureListener{
                    val toast =
                        Toast.makeText(applicationContext, "Hello Javatpoint", Toast.LENGTH_SHORT)
                }
//                .addValueEventListener(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot){
//                        if (snapshot.value == null) {
//                            return
//                        }
//                     val battles:ArrayList<String> = snapshot.value as ArrayList<String>
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        TODO("Not yet implemented")
//                    }
//                })}
//                    battles?.forEach{
//                        var enemyID=it["enemyId"] as String
//                        var latitude=it["latitude"] as Double
//                        var longitude= it["longitude"] as Double
//
//                        battleLocations.add(BattleLocation(it.id,enemyID,latitude,longitude,null))
//
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

        GlobalScope.launch {
            userLocations.forEach {
                FirebaseDatabase.getInstance().reference.child(FIREBASE_CHILD).child(it.key)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value == null) {
                                return
                            }
                            val location: HashMap<String, Double> =
                                snapshot.value as HashMap<String, Double>
                            val user = userLocations[it.key]
                            if (user?.marker == null && user!!.isFriend) {
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
                                        user?.marker = marker
                                        marker.setOnMarkerClickListener(Marker.OnMarkerClickListener { _marker, mapView ->

                                            intent= Intent(this@MapActivity, ProfileActivity::class.java)
                                            intent.putExtra("user_id", user?.uid)
                                            startActivity(intent)
                                            return@OnMarkerClickListener true
                                        })
                                    }
                            }
                            if (user?.marker == null && !user!!.isFriend) {
                                val marker = Marker(map)
                                marker.position = GeoPoint(location["latitude"]!!, location["longitude"]!!)
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                map!!.overlays.add(marker)
                                user?.marker = marker
                            }
                            user?.latitude = location["latitude"]!!
                            user?.longitude = location["longitude"]!!
                            user?.marker?.position = GeoPoint(user!!.latitude, user!!.longitude)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
            }
            battleLocations.forEach{
                if(it?.marker==null)
                {
//                        FirebaseStorage.getInstance().reference
//                            .child("battles/" + "borba.png")
//                            .downloadUrl
//                            .addOnSuccessListener { url->
//                                val connection: HttpURLConnection =
//                                    URL(url.toString()).openConnection() as HttpURLConnection
//                                connection.connect()
//                                val input: InputStream = connection.inputStream
//                                val x = BitmapFactory.decodeStream(input)
////
//                                val battleMarker = (com.example.protectorsofastrax.R.drawable.borba)
//                                val drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(x, 100,  100 * x.height / x.width, true))


                                val marker=Marker(map)
                                marker.icon=getDrawable(com.example.protectorsofastrax.R.drawable.borba)
                                marker.position= GeoPoint(it.latitude,it.longitude)
                                marker.setAnchor(Marker.ANCHOR_TOP,Marker.ANCHOR_LEFT)
                                map!!.overlays.add(marker)
                                it.marker=marker
                                marker.setOnMarkerClickListener(Marker.OnMarkerClickListener { _marker, mapView ->

                                  intent= Intent(this@MapActivity, BattleActivity::class.java)
                                  intent.putExtra("battle_id", it.uid)
                                  intent.putExtra("enemyID",it.enemyId)
                                  startActivity(intent)
                                   return@OnMarkerClickListener true
                                 })
//                            }
//                            .addOnFailureListener{
//                                TODO()
//                            }
                    }else {
                        val marker = Marker(map)
                        marker.position = GeoPoint(it.latitude!!, it.longitude!!)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        map!!.overlays.add(marker)
                        it.marker = marker
                    }
                it?.marker!!.position= GeoPoint(it!!.latitude,it!!.longitude)
            }

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