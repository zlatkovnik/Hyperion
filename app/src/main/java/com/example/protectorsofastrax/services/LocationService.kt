package com.example.protectorsofastrax.services

import android.Manifest
import android.app.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.protectorsofastrax.BattleActivity
import com.example.protectorsofastrax.ProfileActivity
import com.example.protectorsofastrax.R
import com.example.protectorsofastrax.data.UserLocation
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.SphericalUtil
import org.osmdroid.util.GeoPoint

class LocationService : Service() {
    private val channelId = "location_service"
    private val channelName = "My Location Service"
    private var cachedLocation: Location? = null
    private val userMap = HashMap<String, Boolean>()
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        onTaskRemoved(intent)

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(channelId, channelName)
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val pendingIntent: PendingIntent =
            Intent(this, LocationService::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("You are live!")
            .setContentText("Other heroes can now see your location")
            .setSmallIcon(R.drawable.sword_notif_icon)
            .setColor(Color.WHITE)
            .setContentIntent(pendingIntent)
            .setTicker("nzm")
            .build()

        requestLocationUpdates()
        startForeground(8080, notification)

        return Service.START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location: Location = locationResult.lastLocation
            if (location != null) {
                FirebaseDatabase.getInstance().reference.child("users").child(Firebase.auth.uid!!)
                    .setValue(GeoPoint(location.latitude, location.longitude))
                cachedLocation = location
            }
        }
    }

    private fun requestLocationUpdates() {
        val request = LocationRequest()
        request.interval = 10000
        request.fastestInterval = 5000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val client: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permission == PackageManager.PERMISSION_GRANTED) { // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, locationCallback, null)
        }

        FirebaseDatabase.getInstance().reference
            .child("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val usersLocations = snapshot.value as HashMap<String, HashMap<String, Double>>
                    for((key, value) in usersLocations) {
                        if(key == Firebase.auth.uid){
                            continue
                        }
                        if(userMap[key] != null){
                            continue
                        }
                        userMap[key] = true
                        if(cachedLocation == null){
                            continue
                        }
                        val latitude = value["latitude"]
                        val longitude = value["longitude"]
                        val userLocation = LatLng(latitude!!, longitude!!)
                        val myLocation = LatLng(cachedLocation!!.latitude, cachedLocation!!.longitude)
                        val distance = SphericalUtil.computeDistanceBetween(userLocation, myLocation)
                        if(distance < 500.0){
                            FirebaseFirestore.getInstance().collection("users").document(key).get()
                                .addOnSuccessListener{
                                    val username = it.data!!["username"] as String

                                    val intent = Intent(this@LocationService, ProfileActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                    intent.putExtra("user_id", key)

                                    val pendingIntent: PendingIntent = PendingIntent.getActivity(this@LocationService, 0, intent, 0)

                                    var builder = NotificationCompat.Builder(this@LocationService, channelId)
                                        .setSmallIcon(R.drawable.sword_notif_icon)
                                        .setColor(Color.WHITE)
                                        .setContentTitle("$username is nearby!")
                                        .setContentText("Why not say hi?")
                                        .setContentIntent(pendingIntent)
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)

                                    with(NotificationManagerCompat.from(this@LocationService)) {
                                        notify((Math.random() * 10000).toInt(), builder.build())
                                    }
                                }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(ContentValues.TAG, error.message);
                }
            })

        FirebaseDatabase.getInstance().reference
            .child("battles")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value==null)
                    {
                        return
                    }
                    val battleLocations = snapshot.value as HashMap<String, HashMap<String, Any>>
                    for((key, value) in battleLocations) {
                        if(cachedLocation == null){
                            continue
                        }
                        val latitude = value["latitude"] as Double
                        val longitude = value["longitude"] as Double
                        val enemyId=value["enemyId"] as String
                        val userLocation = LatLng(latitude, longitude)
                        val myLocation = LatLng(cachedLocation!!.latitude, cachedLocation!!.longitude)
                        val distance = SphericalUtil.computeDistanceBetween(userLocation, myLocation)
                        if(distance < 500.0){
                            val intent = Intent(this@LocationService, BattleActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            intent.putExtra("battle_id", key)
                            intent.putExtra("enemyID", enemyId)
                            val pendingIntent: PendingIntent = PendingIntent.getActivity(this@LocationService, 0, intent, FLAG_UPDATE_CURRENT)

                            var builder = NotificationCompat.Builder(this@LocationService, channelId)
                                .setSmallIcon(R.drawable.sword_notif_icon)
                                .setColor(Color.WHITE)
                                .setContentTitle("New battle nearby!")
                                .setContentText("Join in on the action")
                                .setContentIntent(pendingIntent)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)

                            with(NotificationManagerCompat.from(this@LocationService)) {
                                notify((Math.random() * 10000).toInt(), builder.build())
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(ContentValues.TAG, error.message);
                }
            })
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onDestroy() {
        super.onDestroy()
        val client: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        client.removeLocationUpdates(locationCallback)
        stopForeground(true)
    }
}