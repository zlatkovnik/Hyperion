package com.example.protectorsofastrax

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import com.example.protectorsofastrax.services.BluetoothService
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_friends.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AddFriendsActivity : AppCompatActivity() {
    val uuid: UUID = UUID.fromString("8989063a-c9af-463a-b3f1-f21d9b2b827b")
    private var btDevicePaired: BluetoothDevice? = null

    private var bluetoothAdapter: BluetoothAdapter? = null

    private val REQUEST_ENABLE_BLUETOOTH = 1
    private val REQUEST_DISCOVERABLE = 2

    private var mArrayAdapter: ArrayAdapter<String>? = null
    private var devices = ArrayList<BluetoothDevice>()
    private var devicesMap = HashMap<String, BluetoothDevice>()



    private var searchDevices = ArrayList<BluetoothDevice>()
    private lateinit var foundListMap: HashMap<String, BluetoothDevice>
    private var searchArrayAdapter: ArrayAdapter<String>? = null


    private var bluetoothService : BluetoothService = BluetoothService()


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friends)


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bluetoothAdapter == null) {
            return
        }
        if(!bluetoothAdapter!!.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

        mArrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1)
        devicesMap = HashMap()
        devices = ArrayList()
        mArrayAdapter!!.clear()


        searchArrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1)
        searchDevices = ArrayList()
        foundListMap = HashMap()
        searchArrayAdapter!!.clear()


        //gets paired devices
        getPairedDevices()


        //registers a receiver
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)

        registerReceiver(receiver, filter)
        select_device_refresh.setOnClickListener {
            bluetoothAdapter!!.startDiscovery()


        }


        btn_visability.setOnClickListener {

            val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }

            startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE)
        }
        paired_device_list.setOnItemClickListener { _, _, position: Int, id: Long ->



            btDevicePaired = devices[position]
            btDevicePaired.let {  }

            showDialog(btDevicePaired!!, uuid)



        }
        select_device_list.setOnItemClickListener { _, _, position: Int, _ ->
            bluetoothAdapter!!.cancelDiscovery()
//            progressBar_discover.visibility = View.GONE
            val device = searchDevices[position]

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                device.createBond()

            }
        }
        find_friends_back_btn.setOnClickListener {
            if(isTaskRoot){
                val intent = Intent(this@AddFriendsActivity, MainActivity::class.java)
                startActivity(intent)
            } else {
                finish()
            }
        }
    }

    private fun getPairedDevices(){

        devicesMap.clear()
        devices.clear()
        mArrayAdapter?.clear()

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->

            devicesMap.put(device.address, device)
            devices.add(device)
            mArrayAdapter!!.add((if (device.name != null) device.name else "Unknown") + "\n" + device.address)
        }
        paired_device_list.adapter = mArrayAdapter
    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action as String
            when(action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {

                    progressBar_discover.visibility = View.VISIBLE
                    foundListMap.clear()
                    searchArrayAdapter!!.clear()
                }

                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice

                    if(!searchDevices.contains(device)){
                        foundListMap.put(device.address, device)
                        searchDevices.add((device))
                        searchArrayAdapter!!.add((if (device.name != null) device.name else "Unknown") + "\n" + device.address)

                        select_device_list.adapter = searchArrayAdapter
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    progressBar_discover.visibility = View.GONE
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    if(device.bondState == BluetoothDevice.BOND_BONDED){
                        getPairedDevices()
                    }
                }
            }

        }
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if(resultCode == Activity.RESULT_OK){
                if(bluetoothAdapter!!.isEnabled){
                    //enabled

                }
                else {}// disabled

            }
        }else if(resultCode == Activity.RESULT_CANCELED){
            //enabling has been canceled
        }
        else if(requestCode == REQUEST_DISCOVERABLE) {
            val acceptThread = bluetoothService.startThread()

        }
    }

    private fun startBtConnection(device: BluetoothDevice,uuid: UUID){

        startClient(device, uuid)
    }


    private fun startClient(device: BluetoothDevice, uuid: UUID){
        var user = Firebase.auth.currentUser as FirebaseUser





        bluetoothService.mConnectThread = bluetoothService.ConnectThread(device, user.uid)
        bluetoothService.mConnectThread?.start()

    }


    private fun showDialog(btDevice: BluetoothDevice, uuid : UUID){
        var builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Add a friend?")
            setTitle("Send request")
            setPositiveButton("Yes") { dialog, which ->
                startBtConnection(btDevice, uuid)
            }
            setNegativeButton("No") { dialog, which ->
                dialog.cancel()
            }

        }
        val dialog = builder.create()
        dialog.show()
    }
}