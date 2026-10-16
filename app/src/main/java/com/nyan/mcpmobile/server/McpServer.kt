package com.nyan.mcpmobile.server

import com.nyan.mcpmobile.tool.ToolRegistry
import fi.iki.elonen.NanoHTTPD
import org.json.JSONArray
import org.json.JSONObject

/**
 * 极简 MCP over HTTP 服务器。
 *
 * 端点：
 *   GET  /health      健康检查
 *   POST /mcp         JSON-RPC（initialize / tools/list / tools/call）
 */
class McpServer(
    port: Int = 8080,
    private val token: String = "",
    private val onlyLocal: Boolean = false,
    private val onLog: (String) -> Unit = {}
) : NanoHTTPD(port) {

    var requestCount = 0
        private set

    override fun serve(session: IHTTPSession): Response {
        // 1) 仅本地访问限制
        if (onlyLocal) {
            val ip = session.remoteIpAddress ?: ""
            val ok = ip == "127.0.0.1" || ip == "::1" || ip == "localhost"
            if (!ok) return finish(Response.Status.FORBIDDEN, jsonError(null, "仅允许本地访问"))
        }

        // 2) Token 鉴权
        if (token.isNotBlank()) {
            val auth = session.headers["authorization"]
            if (auth != "Bearer $token") {
                onLog("401 未授权: ${session.uri}")
                return finish(Response.Status.UNAUTHORIZED, jsonError(null, "unauthorized"))
            }
        }

        // 3) CORS 预检
        if (session.method == Method.OPTIONS) return finish(Response.Status.OK, "")

        val uri = session.uri ?: "/"
        return try {
            when {
                uri == "/" || uri == "/health" -> {
                    onLog("GET $uri")
                    finish(Response.Status.OK, JSONObject().apply {
                        put("status", "ok")
                        put("server", "McpMobile")
                        put("requests", requestCount)
                    }.toString())
                }

                uri == "/mcp" && session.method == Method.POST -> {
                    val body = readBody(session)
                    finish(Response.Status.OK, handleRpc(body))
                }

                else -> finish(Response.Status.NOT_FOUND, jsonError(null, "not found: $uri"))
            }
        } catch (e: Exception) {
            onLog("ERROR: ${e.message}")
            finish(Response.Status.INTERNAL_ERROR, jsonError(null, e.message ?: "error"))
        }
    }

    private fun readBody(session: IHTTPSession): String {
        val files = HashMap<String, String>()
        session.parseBody(files)
        return files["postData"] ?: ""
    }

    /** 处理 JSON-RPC 请求 */
    private fun handleRpc(body: String): String {
        requestCount++
        val req = try {
            JSONObject(body)
        } catch (e: Exception) {
            return jsonError(null, "invalid json")
        }

        val id = req.opt("id")
        val method = req.optString("method", "")
        onLog("RPC: $method")

        val result: JSONObject = when (method) {
            "initialize" -> JSONObject().apply {
                put("protocolVersion", "2024-11-05")
                put("capabilities", JSONObject().put("tools", JSONObject()))
                put("serverInfo", JSONObject().apply {
                    put("name", "McpMobile")
                    put("version", "1.0")
                })
            }

            "tools/list" -> JSONObject().apply {
                val arr = JSONArray()
                ToolRegistry.all().forEach { t ->
                    arr.put(JSONObject().apply {
                        put("name", t.name)
                        put("description", t.description)
                        put("inputSchema", t.inputSchema())
                    })
                }
                put("tools", arr)
            }

            "tools/call" -> {
                val params = req.optJSONObject("params") ?: JSONObject()
                val name = params.optString("name", "")
                val args = params.optJSONObject("arguments") ?: JSONObject()
                val tool = ToolRegistry.get(name)

                val (text, isError) = if (tool == null) {
                    "Unknown tool: $name" to true
                } else {
                    val out = try {
                        tool.invoke(args)
                    } catch (e: Exception) {
                        "执行出错: ${e.message}"
                    }
                    out to false
                }

                JSONObject().apply {
                    put("content", JSONArray().put(JSONObject().apply {
                        put("type", "text")
                        put("text", text)
                    }))
                    put("isError", isError)
                }
            }

            "notifications/initialized" -> JSONObject()

            else -> return jsonError(id, "method not found: $method")
        }

        return JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", id)
            put("result", result)
        }.toString()
    }

    private fun finish(status: Response.IStatus, body: String): Response {
        val r = newFixedLengthResponse(status, "application/json", body)
        r.addHeader("Access-Control-Allow-Origin", "*")
        r.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        r.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
        return r
    }

    private fun jsonError(id: Any?, msg: String): String = JSONObject().apply {
        put("jsonrpc", "2.0")
        put("id", id ?: JSONObject.NULL)
        put("error", JSONObject().apply {
            put("code", -32600)
            put("message", msg)
        })
    }.toString()
}