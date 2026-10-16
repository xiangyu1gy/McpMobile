package com.nyan.mcpmobile.tool.impl

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.nyan.mcpmobile.tool.McpTool
import org.json.JSONObject

/** 设备信息工具 */
class DeviceInfoTool(private val context: Context) : McpTool {
    override val name = "device_info"
    override val description = "获取设备型号、系统版本、内存等信息"

    override fun inputSchema() = JSONObject(
        """{"type":"object","properties":{}}"""
    )

    override fun invoke(args: JSONObject): String {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return buildString {
            appendLine("厂商: ${Build.MANUFACTURER}")
            appendLine("型号: ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("CPU: ${Build.SUPPORTED_ABIS.joinToString()}")
            appendLine("总内存: ${mi.totalMem / 1024 / 1024} MB")
            appendLine("可用内存: ${mi.availMem / 1024 / 1024} MB")
        }
    }
}