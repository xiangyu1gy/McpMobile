package com.nyan.mcpmobile.tool.impl

import com.nyan.mcpmobile.tool.McpTool
import org.json.JSONObject

/** 连通性测试工具 */
class EchoTool : McpTool {
    override val name = "echo"
    override val description = "回显输入文本，用于连通性测试"

    override fun inputSchema() = JSONObject(
        """{"type":"object","properties":{"text":{"type":"string","description":"要回显的文本"}},"required":["text"]}"""
    )

    override fun invoke(args: JSONObject): String = "echo: " + args.optString("text", "")
}