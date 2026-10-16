package com.nyan.mcpmobile.tool.impl

import com.nyan.mcpmobile.tool.McpTool
import org.json.JSONObject
import java.io.File

/** 写入文本文件 */
class WriteTextTool : McpTool {
    override val name = "write_text"
    override val description = "写入文本到指定文件（可追加）"

    override fun inputSchema() = JSONObject(
        """{"type":"object","properties":{"path":{"type":"string"},"content":{"type":"string"},"append":{"type":"boolean","description":"true 表示追加，默认覆盖"}},"required":["path","content"]}"""
    )

    override fun invoke(args: JSONObject): String {
        val path = args.getString("path")
        val content = args.getString("content")
        val append = args.optBoolean("append", false)
        val f = File(path)
        f.parentFile?.mkdirs()
        if (append) f.appendText(content) else f.writeText(content)
        return "已写入 ${f.absolutePath}（${content.length} 字符，append=$append）"
    }
}