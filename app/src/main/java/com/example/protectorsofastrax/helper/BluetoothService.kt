package com.example.protectorsofastrax.helper

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*

class BluetoothService {
    var NAME ="veza"
    val uuid: UUID = UUID.fromString("8989063a-c9af-463a-b3f1-f21d9b2b827b")
    var mInsecureAcceptThread: AcceptThread? = null
    var mConnectThread: ConnectThread? = null
    var mConnectedThread: ConnectedThread? = null

    public var user : FirebaseUser
    init {
        user = Firebase.auth.currentUser as FirebaseUser
    }





    fun startThread(){
        Log.d(ContentValues.TAG, "start")

        if(mConnectThread != null) {
            mConnectThread?.cancel()
            mConnectThread = null
        }
        if(mInsecureAcceptThread == null){
            mInsecureAcceptThread = AcceptThread()
            mInsecureAcceptThread?.start()
        }
    }

    inner class AcceptThread() : Thread() {

        private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, uuid)
        }
        val currentUserId = user.uid

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            Log.d(ContentValues.TAG, "accept thread run ")
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(ContentValues.TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    Log.d(ContentValues.TAG, "ACCEPTED A SOCKET CONNECTION")
                    // manageMyConnectedSocket(it)
                    mmServerSocket?.close()
                    shouldLoop = false
                    connected(socket, currentUserId)
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(ContentValues.TAG, "Could not close the connect socket", e)
            }
        }
    }


    inner class ConnectThread(device: BluetoothDevice, userId: String) : Thread() {


        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(uuid)
        }


        private val userId = userId
        private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        override fun run() {
            Log.d(ContentValues.TAG, "connect thread run ")
            bluetoothAdapter.cancelDiscovery()

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                try {
                    socket.connect()

                    Log.d(ContentValues.TAG, " connect thread SOCKET CONNECTED ")
                } catch (e: java.lang.Exception) {
                    Log.d(ContentValues.TAG, e.printStackTrace().toString())
                }
                connected(socket, userId)


                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                // manageMyConnectedSocket(socket)
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(ContentValues.TAG, "Could not close the client socket", e)
            }
        }

    }

    inner class ConnectedThread(socket: BluetoothSocket, userId: String) : Thread() {

        private var mmSocket: BluetoothSocket = socket
        private var mmInStream: InputStream?
        private var mmOutStream: OutputStream?
        private val userId = userId


        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            try {
                tmpIn = mmSocket.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {

            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        override fun run() {
            writeUserId(userId.toByteArray())
            Log.d(ContentValues.TAG, "Pokrenut conected thread")
            var buffer: ByteArray = ByteArray(1024)
            var bytes: Int
            val currentUserId = user.uid

            while (true) {
                try {

                    bytes = mmInStream!!.read(buffer)
                    var incomingMessage: String = String(buffer, 0, bytes)
//                    Log.d(TAG, "inputStream: " + incomingMessage)
                    Log.d(ContentValues.TAG, "UserRECIVED: " + incomingMessage + "CURRENT USER: " + currentUserId)


                } catch (e: IOException) {
                }
            }
        }


        fun write(bytes: ByteArray?) {
            val text = String(bytes!!, Charset.defaultCharset())
            Log.d(ContentValues.TAG, "write: Writing to outputstream: $text")
            try {
                mmOutStream!!.write(bytes)
            } catch (e: IOException) {
                Log.e(ContentValues.TAG, "write: Error writing to output stream. " + e.message)
            }
        }



        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {

            }
        }


    }
    fun connected(mmSocket: BluetoothSocket, userId: String) {

        mConnectedThread = ConnectedThread(mmSocket, userId)
        mConnectedThread?.start()

    }
    fun writeUserId(bytes: ByteArray) {
        try {
            mConnectedThread!!.write(bytes)
        } catch (e: IOException) {

        }
    }

}