package com.android.opensocket.ir

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.lang.Exception
import java.util.Timer
import kotlin.concurrent.schedule

class OpenSocket {

    constructor(context: Context) {
        this@OpenSocket.context = context

        settings = context.getSharedPreferences("osocket", Context.MODE_PRIVATE)
        editSetting = settings.edit()
    }


    // change at [2022-02-15]
    private val version = 1;

    private val TAG = "opensocket";
    private val server_url = "https://socket.opensocket.ir/";

    private lateinit var project_id: String;
    private lateinit var client_token: String;
    private lateinit var developer_id: String;
    private var context: Context;

    private var settings: SharedPreferences;
    private var editSetting: SharedPreferences.Editor;
    private lateinit var socket: Socket;

    private var PRIORITY_DEFAULT = NotificationCompat.PRIORITY_DEFAULT;


    fun setProjectConfig(id: String, token: String) {
        project_id = id
        client_token = token
    }

    fun setDeveloperConfig(id: String) {
        developer_id = id
    }

    fun setPriority(value: Int) {
        PRIORITY_DEFAULT = value;
    }

    var onReceiveMessage: (json_object: JsonObject) -> Unit = {}
    var onReceiveToken: (token: String) -> Unit = {}
    var onConnect: () -> Unit = {}
    var onDisconnect: () -> Unit = {}
    var onConectionError: (msg: String) -> Unit = {}

    fun userToken(): String? {
        return settings.getString("token", "");
    }

    private fun disconnect(){
        socket.close()
        socket.off()
    }

    private fun reConnect() {
        println("$TAG try reconnect..")

        Timer().schedule(30000) {
            connect()
        }
    }

    fun connected(): Boolean {
        if (socket != null)
            return socket.connected();
        else return false
    }

    @SuppressLint("HardwareIds")
    fun connect() {
        try {
            var time: String? = getTime()
            var register: Boolean = settings.getBoolean("register", false)
            var token = userToken()
            var id: String? = settings.getString("user_id", "")
            val systemid: String =
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

            var countryCodeValue = "us"
            try {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                countryCodeValue = tm.networkCountryIso
            } catch (e: Exception) {
            }


            var extra =
                "&time=$time&register=$register&id=$id&system_id=$systemid&cc=$countryCodeValue&token=$token"

            var options = IO.Options.builder()
                .setQuery("project_id=$project_id&client_token=$client_token&developer_id=$developer_id$extra&ver=$version")
                .setReconnection(true)
                .setReconnectionDelay(20000)
                .setReconnectionDelayMax(20000)
                .build()

            socket = IO.socket(server_url , options)
            socket.connect()

            socket.on(Socket.EVENT_CONNECT, Emitter.Listener {
                println("$TAG CONNECT");
                this.onConnect();
            })

            socket.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
                println("$TAG DISCONNECT");
                disconnect()
                reConnect()
                this.onDisconnect();
            })

            socket.on(Socket.EVENT_CONNECT_ERROR, Emitter.Listener {
                var msg = "NaN";

                if (it.isNotEmpty())
                    msg = it[0].toString();

                println("$TAG CONNECT_ERROR : $msg");
                this.onConectionError(msg);
            })

            socket.on("receive", Emitter.Listener {
                println("$TAG receive " + it[0]);
                val ob: JsonObject = JsonParser.parseString(it[0].toString()).asJsonObject
                editSetting.putString("lastReceive", ob.get("time").asString)
                editSetting.apply()

                if (!ob.has("show_notify") || ob.get("show_notify").asBoolean) {
                    notification(ob)
                } else {
                    this.onReceiveMessage(ob);
                }

                if (ob.has("callback") && ob.get("callback").asBoolean) {
                    socket.emit("receive-answer", ob.get("message_id").asString, userToken());
                }
            })

            socket.on("register", Emitter.Listener {

                val ob: JsonObject = JsonParser.parseString(it[0].toString()).asJsonObject
                editSetting.putString("token", ob.get("token").asString)
                editSetting.putBoolean("register", true)
                editSetting.putString("verify_code", ob.get("verify_code").asString)
                editSetting.apply()

                this.onReceiveToken(ob.get("token").asString)

                disconnect()
            })
        } catch (error: Exception) {
            error.printStackTrace()
        }


    }

    private fun getTime(): String? {
        return settings.getString("lastReceive", System.currentTimeMillis().toString());
    }

    private fun notification(ob: JsonObject) {

        createNotificationChannel()

        val title = ob.get("title").asString
        val text = ob.get("text").asString

        var color: Int = 0;
        if (ob.has("set_small_icon_color")) {
            val get_color_name = ob.get("set_small_icon_color").asString

            if (get_color_name == "blue") {
                color = Color.BLUE
            } else if (get_color_name == "green") {
                color = Color.GREEN
            } else if (get_color_name == "yellow") {
                color = Color.YELLOW
            } else if (get_color_name == "red") {
                color = Color.RED
            } else if (get_color_name == "white") {
                color = Color.WHITE
            }
        }

        var big_text_support = false;
        if (ob.has("big_text") && ob.get("big_text").asBoolean) {
            big_text_support = true
        }

        var sound = true
        if (ob.has("set_sound") && !ob.get("set_sound").asBoolean) {
            sound = false
        }

        var icon = 0;
        if (ob.has("set_icon")) {
            val get_icon_name = ob.get("set_icon").asString;

            if (get_icon_name == "account") {
                icon = R.drawable.ic_baseline_account_circle_24
            } else if (get_icon_name == "done") {
                icon = R.drawable.ic_baseline_done_outline_24
            } else if (get_icon_name == "info") {
                icon = R.drawable.ic_baseline_info_24
            } else if (get_icon_name == "lens") {
                icon = R.drawable.ic_baseline_lens_24
            } else if (get_icon_name == "live") {
                icon = R.drawable.ic_baseline_live_tv_24
            } else if (get_icon_name == "mail") {
                icon = R.drawable.ic_baseline_mail_24
            } else if (get_icon_name == "comment") {
                icon = R.drawable.ic_baseline_mode_comment_24
            } else if (get_icon_name == "star") {
                icon = R.drawable.ic_baseline_star_24
            } else if (get_icon_name == "verified") {
                icon = R.drawable.ic_baseline_verified_24
            } else if (get_icon_name == "verified_user") {
                icon = R.drawable.ic_baseline_verified_user_24
            } else if (get_icon_name == "warning") {
                icon = R.drawable.ic_baseline_warning_24
            } else if (get_icon_name == "notifications") {
                icon = R.drawable.ic_baseline_notifications_active_24
            } else {
                icon = R.drawable.ic_baseline_notifications_active_24
            }
        } else {
            icon = R.drawable.ic_baseline_notifications_active_24
        }


        var id = settings.getInt("notify_id", 1);
        id++;

        if (id > 10000000)
            id = 1;

        editSetting.putInt("notify_id", id)
        editSetting.apply()


        var intt = context.packageManager.getLaunchIntentForPackage(context.packageName)


        intt?.putExtra("notify_id", id)
        intt?.putExtra("message_id", ob.get("message_id").asString)
        intt?.putExtra("time", ob.get("time").asLong)
        if (ob.has("data")) {
            val dataString = ob.get("data").toString()
            intt?.putExtra("data_string",dataString);
        }


        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intt, 0)

        var builder = NotificationCompat.Builder(context, context.packageName)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        if (color != 0) {
            builder.setColor(color)
        }

        if (big_text_support) {
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
            )
        }

        if (sound) {
            builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
        }


        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(id, builder.build())
        }

    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(context.packageName, context.packageName, importance).apply {
                    description = "description"
                }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}