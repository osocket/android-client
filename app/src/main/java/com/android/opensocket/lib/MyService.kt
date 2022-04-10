package com.android.opensocket.lib

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.opensocket.ir.OpenSocket

class MyService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val osocket = OpenSocket(this);

        osocket.setProjectConfig("project_id","client_token")
        osocket.setDeveloperConfig("develper_id")

        osocket.connect()
        osocket.setPriority(NotificationCompat.PRIORITY_HIGH)

        osocket.onReceiveToken = {
            Log.i("opensocket","service : onReceiveToken : $it")
        }

        osocket.onConnect = {
            Log.i("opensocket","service : onConnect")
        }

        osocket.onReceiveMessage = {
            Log.i("opensocket", "message:$it")
        }
    }
}