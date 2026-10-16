package com.nyan.mcpmobile.tool.impl

import com.nyan.mcpmobile.tool.McpTool
import org.json.JSONObject
import java.io.File

/** 列目录工具 */
class DirListTool : McpTool {
    override val name = "list_dir"
    override val description = "列出指定目录下的文件和文件夹"

    override fun inputSchema() = JSONObject(
        """{"type":"object","properties":{"path":{"type":"string","description":"目录路径，默认 /sdcard"}}}"""
    )

    override fun invoke(args: JSONObject): String {
        val path = args.optString("path", "/sdcard")
        val dir = File(path)
        if (!dir.exists()) return "目录不存在: $path"
        if (!dir.isDirectory) return "不是目录: $path"

        val items = dir.listFiles()?.sortedWith(
            compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() }
        ) ?: emptyList()

        return buildString {
            appendLine("目录: ${dir.absolutePath}")
            appendLine("共 ${items.size} 项")
            appendLine("------")
            items.take(500).forEach {
                appendLine((if (it.isDirectory) "[D] " else "[F] ") + it.name +
                        (if (it.isFile) "  (${it.length()} B)" else ""))
            }
            if (items.size > 500) appendLine("... 仅显示前 500 项")
        }
    }
}