package com.nyan.mcpmobile.tool.impl

import com.nyan.mcpmobile.tool.McpTool
import com.nyan.mcpmobile.tool.impl.dex.DexUtils
import org.json.JSONObject

/** 在 APK 中按关键字搜索类名 */
class ApkSearchClassTool : McpTool {

    override val name = "search_class"
    override val description = "在APK中搜索类名，返回完整类名。"

    override fun inputSchema() = JSONObject(
        """{"type":"object","properties":{"path":{"type":"string","description":"APK 路径"},"keyword":{"type":"string","description":"类名关键字"}},"required":["path","keyword"]}"""
    )

    override fun invoke(args: JSONObject): String {
        val path = args.getString("path")
        val keyword = args.getString("keyword")
        val hits = DexUtils.withClasses(path) { classes ->
            classes.map { it.type }
                .filter { it.contains(keyword, ignoreCase = true) }
                .sorted()
        }
        return if (hits.isEmpty()) "未找到匹配类名: $keyword"
        else "找到 ${hits.size} 个类：\n" + hits.take(200).joinToString("\n")
    }
}