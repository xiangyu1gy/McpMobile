package com.nyan.mcpmobile.tool

import org.json.JSONObject

/**
 * 所有工具的祖宗接口。
 * 想加工具？实现这个接口，然后在 ToolRegistry 里注册一行即可。
 *
 * 一个工具 = 说明书(name/description/inputSchema) + 实现(invoke)
 */
interface McpTool {

    /** 工具名，调用时按它点名，全局唯一 */
    val name: String

    /** 给模型看的一句话用途说明 */
    val description: String

    /** 参数格式说明书（JSON Schema） */
    fun inputSchema(): JSONObject

    /** 真正干活的代码 */
    fun invoke(args: JSONObject): String
}