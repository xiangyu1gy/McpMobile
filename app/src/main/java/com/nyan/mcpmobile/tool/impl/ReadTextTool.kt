package com.nyan.mcpmobile.tool.impl

import com.nyan.mcpmobile.tool.McpTool
import org.json.JSONObject
import java.io.File

/** 读取文本文件 */
class ReadTextTool : McpTool {
    override val name = "read_text"
    override val description = "读取文本文件内容（默认最多 64KB）"

    override fun inputSchema() = JSONObject(
        """{"type":"object","properties":{"path":{"type":"string"},"max_bytes":{"type":"integer","description":"最大读取字节数，默认 65536"}},"required":["path"]}"""
    )

    override fun invoke(args: JSONObject): String {
        val path = args.getString("path")
        val max = args.optInt("max_bytes", 65536)
        val f = File(path)
        if (!f.exists()) return "文件不存在: $path"
        if (f.isDirectory) return "这是一个目录，请用 list_dir: $path"
        return if (f.length() <= max) {
            f.readText()
        } else {
            "文件较大（${f.length()} B），仅返回前 $max B：\n" + f.readText().take(max)
        }
    }
}