package com.example.djimini3activetrack

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dji.v5.common.error.IDJIError
import dji.v5.common.register.DJISDKInitEvent
import dji.v5.manager.SDKManager
import dji.v5.manager.interfaces.SDKManagerCallback

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.READ_PHONE_STATE
        )
    } else {
        arrayOf(
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }
    private val REQUEST_CODE = 101

    private lateinit var statusTextView: TextView
    private lateinit var connectButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTextView = findViewById(R.id.statusTextView)
        connectButton = findViewById(R.id.connectButton)

        checkAndRequestPermissions()

        connectButton.setOnClickListener {
            registerApp()
        }

        // Xử lý khi app được mở thông qua USB Accessory Attached intent
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null && UsbManager.ACTION_USB_ACCESSORY_ATTACHED == intent.action) {
            Log.d(TAG, "USB Accessory Attached Intent received")
            // Tự động thử đăng ký khi nhận được intent USB
            registerApp()
        }
    }

    private fun checkAndRequestPermissions() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), REQUEST_CODE)
        }
    }

    private fun registerApp() {
        try {
            statusTextView.text = "Initializing SDK..."
            SDKManager.getInstance().init(this, object : SDKManagerCallback {
                override fun onInitProcess(event: DJISDKInitEvent, totalProcess: Int) {
                    Log.d(TAG, "onInitProcess: $event")
                    if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
                        SDKManager.getInstance().registerApp()
                        runOnUiThread {
                            statusTextView.text = "SDK Initialized, registering..."
                        }
                    }
                }

                override fun onRegisterSuccess() {
                    Log.d(TAG, "onRegisterSuccess")
                    runOnUiThread {
                        statusTextView.text = "SDK Registered Successfully"
                        Toast.makeText(this@MainActivity, "SDK Registered", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onRegisterFailure(error: IDJIError) {
                    Log.d(TAG, "onRegisterFailure: ${error.description()}")
                    runOnUiThread {
                        statusTextView.text = "Registration Failed: ${error.description()}"
                        Toast.makeText(this@MainActivity, "Registration Failed: ${error.description()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onProductDisconnect(productId: Int) {
                    Log.d(TAG, "onProductDisconnect: $productId")
                    runOnUiThread {
                        statusTextView.text = "Product Disconnected"
                    }
                }

                override fun onProductConnect(productId: Int) {
                    Log.d(TAG, "onProductConnect: $productId")
                    runOnUiThread {
                        statusTextView.text = "Product Connected: $productId"
                    }
                }

                override fun onProductChanged(productId: Int) {
                    Log.d(TAG, "onProductChanged: $productId")
                }

                override fun onDatabaseDownloadProgress(current: Long, total: Long) {
                    Log.d(TAG, "onDatabaseDownloadProgress: $current/$total")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error during SDK init: ${e.message}")
            statusTextView.text = "Error: ${e.message}"
            Toast.makeText(this, "SDK Init Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
