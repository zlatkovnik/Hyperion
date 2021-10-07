package com.example.protectorsofastrax.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.protectorsofastrax.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class NotificationService : Service() {
    private val channelId = "notification_service"
    private val channelName = "My Notification Service"
    private lateinit var listener: ValueEventListener;
    override fun onCreate() {
        super.onCreate()

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
                PendingIntent.getActivity(this@NotificationService, 0, notificationIntent, 0)
            }

        listener = FirebaseDatabase.getInstance().reference.child("notification").child(Firebase.auth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) {
                        return
                    }
                    val json = snapshot.value as HashMap<String, Any>
                    val title = json["title"] as String
                    val text = json["text"] as String

                    var builder = NotificationCompat.Builder(this@NotificationService, channelId)
                        .setSmallIcon(R.drawable.sword_notif_icon)
                        .setColor(Color.WHITE)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)

                    with(NotificationManagerCompat.from(this@NotificationService)) {
                        notify(999899, builder.build())
                    }

                    FirebaseDatabase.getInstance().reference.child("notification").child(Firebase.auth.uid!!).removeValue()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseDatabase.getInstance().reference.removeEventListener(listener)
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

}