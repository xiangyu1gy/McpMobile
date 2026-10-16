package com.nyan.mcpmobile

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nyan.mcpmobile.databinding.ActivityMainBinding
import com.nyan.mcpmobile.tool.ToolRegistry

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private val prefs by lazy { getSharedPreferences("mcp", MODE_PRIVATE) }

    private val ticker = object : Runnable {
        override fun run() {
            refreshUi()
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ToolRegistry.init(applicationContext)

        // 恢复配置
        binding.etPort.setText(prefs.getInt("port", 8080).toString())
        binding.etToken.setText(prefs.getString("token", ""))
        binding.swOnlyLocal.isChecked = prefs.getBoolean("only_local", false)

        // 按钮
        binding.btnStart.setOnClickListener { startServer() }
        binding.btnStop.setOnClickListener { stopServer() }
        binding.btnCopy.setOnClickListener { copyAddress() }

        // 工具清单
        binding.tvTools.text = ToolRegistry.all().joinToString("\n") {
            "• ${it.name} — ${it.description}"
        }

        requestNotifPermission()
    }

    override fun onResume() {
        super.onResume()
        handler.post(ticker)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(ticker)
    }

    private fun startServer() {
        val port = binding.etPort.text.toString().toIntOrNull() ?: 8080
        prefs.edit()
            .putInt("port", port)
            .putString("token", binding.etToken.text.toString())
            .putBoolean("only_local", binding.swOnlyLocal.isChecked)
            .apply()

        requestAllFilesAccess()
        ContextCompat.startForegroundService(this, Intent(this, McpService::class.java))
        toast("服务启动中…")
    }

    private fun stopServer() {
        stopService(Intent(this, McpService::class.java))
        toast("服务已停止")
    }

    private fun copyAddress() {
        val ip = if (binding.swOnlyLocal.isChecked) "127.0.0.1" else NetUtils.lanIp()
        val port = prefs.getInt("port", 8080)
        val url = "http://$ip:$port/mcp"
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("addr", url))
        toast("已复制: $url")
    }

    private fun refreshUi() {
        val running = McpService.isRunning
        binding.tvStatus.text = if (running) "● 运行中" else "○ 已停止"

        val port = prefs.getInt("port", 8080)
        binding.tvAddrLocal.text = "本地: http://127.0.0.1:$port/mcp"
        binding.tvAddrLan.text = "局域网: http://${NetUtils.lanIp()}:$port/mcp"

        val text = synchronized(McpService.logs) { McpService.logs.joinToString("\n") }
        binding.tvLog.text = text.ifEmpty { "(暂无日志)" }
        binding.svLog.post { binding.svLog.fullScroll(android.view.View.FOCUS_DOWN) }
    }

    private fun requestNotifPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100
            )
        }
    }

    private fun requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
            try {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            } catch (_: Exception) {
                try {
                    startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}