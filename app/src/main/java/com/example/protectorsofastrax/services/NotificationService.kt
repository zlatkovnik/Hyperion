package com.example.protectorsofastrax.services
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.Constants
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationService: FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d(Constants.TAG, "Stigla poruka " + remoteMessage.data["text"])
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if(Firebase.auth.uid != null){
            data class UserToken(val uid: String, val token: String)
            FirebaseFirestore.getInstance().collection("fcm").document(Firebase.auth.uid!!).set(UserToken(Firebase.auth.uid!!, token!!))
        }
    }
}