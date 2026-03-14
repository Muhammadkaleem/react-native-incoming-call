package com.margelo.nitro.incomingcall

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule

class IncomingCallModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val MODULE_NAME = "IncomingCallModule"
        const val ACTION_ANSWER = "com.margelo.nitro.incomingcall.ANSWER"
        const val ACTION_REJECT = "com.margelo.nitro.incomingcall.REJECT"
        const val ACTION_TIMEOUT = "com.margelo.nitro.incomingcall.TIMEOUT"

        // Shared call state accessible from Activity/Service
        var currentCallUuid: String? = null
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val uuid = intent?.getStringExtra("uuid") ?: currentCallUuid ?: ""
            when (intent?.action) {
                ACTION_ANSWER -> sendEvent("onAnswer", uuid)
                ACTION_REJECT -> sendEvent("onReject", uuid)
                ACTION_TIMEOUT -> sendEvent("onTimeout", uuid)
            }
        }
    }

    override fun getName() = MODULE_NAME

    override fun initialize() {
        super.initialize()
        val filter = IntentFilter().apply {
            addAction(ACTION_ANSWER)
            addAction(ACTION_REJECT)
            addAction(ACTION_TIMEOUT)
        }
        ContextCompat.registerReceiver(
            reactContext,
            broadcastReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy()
        try {
            reactContext.unregisterReceiver(broadcastReceiver)
        } catch (_: Exception) {}
    }

    @ReactMethod
    fun displayIncomingCall(options: ReadableMap, promise: Promise) {
        try {
            val uuid = options.getString("uuid") ?: ""
            val callerName = options.getString("callerName") ?: "Unknown"
            val avatar = if (options.hasKey("avatar")) options.getString("avatar") else null
            val backgroundColor = if (options.hasKey("backgroundColor")) options.getString("backgroundColor") else null
            val callType = if (options.hasKey("callType")) options.getString("callType") else "audio"
            val timeout = if (options.hasKey("timeout")) options.getDouble("timeout").toLong() else 30000L

            currentCallUuid = uuid

            val serviceIntent = Intent(reactContext, IncomingCallService::class.java).apply {
                putExtra("uuid", uuid)
                putExtra("callerName", callerName)
                putExtra("avatar", avatar)
                putExtra("backgroundColor", backgroundColor)
                putExtra("callType", callType)
                putExtra("timeout", timeout)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                reactContext.startForegroundService(serviceIntent)
            } else {
                reactContext.startService(serviceIntent)
            }

            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("DISPLAY_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun answerCall(uuid: String, promise: Promise) {
        try {
            stopService()
            sendEvent("onAnswer", uuid)
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("ANSWER_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun rejectCall(uuid: String, promise: Promise) {
        try {
            stopService()
            sendEvent("onReject", uuid)
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("REJECT_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun endCall(uuid: String, promise: Promise) {
        try {
            stopService()
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("END_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun canShowFullScreen(promise: Promise) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = reactContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            promise.resolve(nm.canUseFullScreenIntent())
        } else {
            promise.resolve(true)
        }
    }

    @ReactMethod
    fun requestFullScreenPermission(promise: Promise) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = Uri.parse("package:${reactContext.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            reactContext.startActivity(intent)
        }
        promise.resolve(null)
    }

    // Required by NativeEventEmitter on JS side
    @ReactMethod
    fun addListener(eventName: String) {}

    @ReactMethod
    fun removeListeners(count: Int) {}

    private fun stopService() {
        currentCallUuid = null
        val serviceIntent = Intent(reactContext, IncomingCallService::class.java)
        reactContext.stopService(serviceIntent)
    }

    private fun sendEvent(eventName: String, uuid: String) {
        val params = Arguments.createMap().apply {
            putString("uuid", uuid)
            putDouble("timestamp", System.currentTimeMillis().toDouble())
        }
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }
}
