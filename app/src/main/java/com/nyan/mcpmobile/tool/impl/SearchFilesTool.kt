package com.nyan.mcpmobile.tool.impl

import com.nyan.mcpmobile.tool.McpTool
import org.json.JSONObject
import java.io.File

/**
 * 按文件名关键字递归搜索文件。
 * 这就是"加一个文件"的完整样子：说明书 + 实现，全在这一个类里。
 */
class SearchFilesTool : McpTool {

    override val name = "search_files"

    override val description = "在指定目录下按文件名关键字递归搜索文件"

    override fun inputSchema() = JSONObject(
        """{"type":"object","properties":{"path":{"type":"string","description":"搜索根目录，默认 /sdcard"},"keyword":{"type":"string","description":"文件名关键字"},"max_results":{"type":"integer","description":"最多返回数量，默认 50"}},"required":["keyword"]}"""
    )

    override fun invoke(args: JSONObject): String {
        val root = File(args.optString("path", "/sdcard"))
        val keyword = args.getString("keyword").lowercase()
        val max = args.optInt("max_results", 50)

        if (!root.exists()) return "目录不存在: ${root.absolutePath}"

        val hits = ArrayList<String>()
        val stack = ArrayDeque<File>()
        stack.addLast(root)
        var scanned = 0

        while (stack.isNotEmpty() && hits.size < max) {
            val dir = stack.removeLast()
            val children = dir.listFiles() ?: continue
            for (f in children) {
                scanned++
                if (f.isDirectory) {
                    stack.addLast(f)
                } else if (f.name.lowercase().contains(keyword)) {
                    hits.add(f.absolutePath)
                    if (hits.size >= max) break
                }
            }
        }

        return buildString {
            appendLine("关键字: $keyword")
            appendLine("根目录: ${root.absolutePath}")
            appendLine("命中 ${hits.size} 个（已扫描 $scanned 个条目）")
            appendLine("------")
            hits.forEach { appendLine(it) }
        }
    }
}