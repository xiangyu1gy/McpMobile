package com.nyan.mcpmobile.tool

import android.content.Context
import com.nyan.mcpmobile.tool.impl.*

/**
 * 工具注册表。
 * 加新工具只需要在 [init] 里加一行 register(...)
 */
object ToolRegistry {

    private val tools = LinkedHashMap<String, McpTool>()
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true

        // ↓↓↓ 想加工具就在这里加一行 ↓↓↓
        register(EchoTool())
        register(DirListTool())
        register(ReadTextTool())
        register(WriteTextTool())
        register(DeviceInfoTool(context))
    }

    fun register(tool: McpTool) {
        tools[tool.name] = tool
    }

    fun all(): List<McpTool> = tools.values.toList()

    fun get(name: String): McpTool? = tools[name]
}