package com.nyan.mcpmobile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import fi.iki.elonen.NanoHTTPD
import com.nyan.mcpmobile.server.McpServer
import com.nyan.mcpmobile.tool.ToolRegistry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 前台服务：持有 MCP 服务器，保证后台常驻。
 */
class McpService : Service() {

    private var server: McpServer? = null
    private val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    companion object {
        const val CHANNEL_ID = "mcp_server"
        const val NOTIF_ID = 1001

        @Volatile
        var isRunning = false
            private set

        val logs = ArrayDeque<String>()
        private const val MAX_LOGS = 200

        fun addLog(line: String) {
            synchronized(logs) {
                logs.addLast(line)
                while (logs.size > MAX_LOGS) logs.removeFirst()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ToolRegistry.init(applicationContext)

        val prefs = getSharedPreferences("mcp", MODE_PRIVATE)
        val port = prefs.getInt("port", 8080)
        val token = prefs.getString("token", "") ?: ""
        val onlyLocal = prefs.getBoolean("only_local", false)

        startForeground(NOTIF_ID, buildNotification("MCP 服务器运行中 · 端口 $port"))

        try {
            server?.stop()
            val s = McpServer(port, token, onlyLocal) { msg ->
                addLog("${timeFmt.format(Date())}  $msg")
            }
            s.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            server = s
            isRunning = true
            addLog("${timeFmt.format(Date())}  System: server started on port $port")
        } catch (e: Exception) {
            addLog("${timeFmt.format(Date())}  启动失败: ${e.message}")
            isRunning = false
        }

        return START_STICKY
    }

    override fun onDestroy() {
        try {
            server?.stop()
        } catch (_: Exception) {
        }
        server = null
        isRunning = false
        addLog("${timeFmt.format(Date())}  System: server stopped")
        super.onDestroy()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID, "MCP 服务器", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(text: String): Notification {
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("McpMobile")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
    }
}