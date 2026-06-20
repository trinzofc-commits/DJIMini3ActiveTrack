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
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dji.v5.common.error.IDJIError
import dji.v5.common.register.DJISDKInitEvent
import dji.v5.manager.SDKManager
import dji.v5.manager.interfaces.SDKManagerCallback
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    private lateinit var logTextView: TextView
    private lateinit var logScrollView: ScrollView
    private lateinit var connectButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTextView = findViewById(R.id.statusTextView)
        logTextView = findViewById(R.id.logTextView)
        logScrollView = findViewById(R.id.logScrollView)
        connectButton = findViewById(R.id.connectButton)

        addLog("App Started")
        addLog("Device: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}")

        checkAndRequestPermissions()

        connectButton.setOnClickListener {
            addLog("Connect Button Clicked")
            registerApp()
        }

        handleIntent(intent)
    }

    private fun addLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logLine = "[$timestamp] $message\n"
        Log.d(TAG, message)
        runOnUiThread {
            logTextView.append(logLine)
            logScrollView.post {
                logScrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        addLog("onNewIntent received")
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) {
            addLog("Intent is null")
            return
        }
        addLog("Action: ${intent.action}")
        if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED == intent.action) {
            addLog("USB Accessory Attached detected!")
            val accessory = intent.getParcelableExtra<android.hardware.usb.UsbAccessory>(UsbManager.EXTRA_ACCESSORY)
            addLog("Accessory: ${accessory?.manufacturer} ${accessory?.model}")
            registerApp()
        }
    }

    private fun checkAndRequestPermissions() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            val isGranted = ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) addLog("Missing Permission: $it")
            !isGranted
        }

        if (missingPermissions.isNotEmpty()) {
            addLog("Requesting ${missingPermissions.size} permissions...")
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), REQUEST_CODE)
        } else {
            addLog("All permissions already granted")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            addLog("Permission request result received")
            grantResults.forEachIndexed { index, result ->
                if (result == PackageManager.PERMISSION_GRANTED) {
                    addLog("Granted: ${permissions[index]}")
                } else {
                    addLog("DENIED: ${permissions[index]}")
                }
            }
        }
    }

    private fun registerApp() {
        try {
            addLog("Starting SDK Initialization...")
            SDKManager.getInstance().init(this, object : SDKManagerCallback {
                override fun onInitProcess(event: DJISDKInitEvent, totalProcess: Int) {
                    addLog("SDK Init Process: $event ($totalProcess)")
                    if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
                        addLog("Initialization Complete, now registering App...")
                        SDKManager.getInstance().registerApp()
                        runOnUiThread {
                            statusTextView.text = "SDK Initialized, registering..."
                        }
                    }
                }

                override fun onRegisterSuccess() {
                    addLog("SDK REGISTER SUCCESS!")
                    runOnUiThread {
                        statusTextView.text = "SDK Registered Successfully"
                        Toast.makeText(this@MainActivity, "SDK Registered", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onRegisterFailure(error: IDJIError) {
                    addLog("SDK REGISTER FAILURE: ${error.description()} (Code: ${error.errorCode()})")
                    runOnUiThread {
                        statusTextView.text = "Registration Failed: ${error.description()}"
                    }
                }

                override fun onProductDisconnect(productId: Int) {
                    addLog("Product Disconnected: $productId")
                    runOnUiThread {
                        statusTextView.text = "Product Disconnected"
                    }
                }

                override fun onProductConnect(productId: Int) {
                    addLog("Product Connected! ID: $productId")
                    runOnUiThread {
                        statusTextView.text = "Product Connected: $productId"
                    }
                }

                override fun onProductChanged(productId: Int) {
                    addLog("Product Changed to: $productId")
                }

                override fun onDatabaseDownloadProgress(current: Long, total: Long) {
                    // Tránh làm đầy log bằng tiến trình download
                    if (current % 10 == 0L) {
                        Log.d(TAG, "Database Download: $current/$total")
                    }
                }
            })
        } catch (e: Throwable) {
            // Sử dụng Throwable để bắt cả Error (như NoClassDefFoundError)
            val errorMsg = "CRITICAL ERROR during SDK init: ${e.javaClass.simpleName} - ${e.message}"
            addLog(errorMsg)
            e.printStackTrace()
            runOnUiThread {
                statusTextView.text = "Crash: ${e.javaClass.simpleName}"
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }
}
