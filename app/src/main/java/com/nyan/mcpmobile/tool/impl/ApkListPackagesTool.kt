package com.nyan.mcpmobile.tool.impl

import com.nyan.mcpmobile.tool.McpTool
import com.nyan.mcpmobile.tool.impl.dex.DexUtils
import org.json.JSONObject
import java.util.TreeMap

/** 分析 APK 的包结构树 */
class ApkListPackagesTool : McpTool {

    override val name = "list_packages"
    override val description = "分析 APK 的包结构树。列出所有包名及包内类的数量。"

    override fun inputSchema() = JSONObject(
        """{"type":"object","properties":{"path":{"type":"string"}},"required":["path"]}"""
    )

    override fun invoke(args: JSONObject): String {
        val path = args.getString("path")
        val map = TreeMap<String, Int>()

        DexUtils.withClasses(path) { classes ->
            for (c in classes) {
                val name = c.type.removePrefix("L").removeSuffix(";").replace('/', '.')
                val pkg = name.substringBeforeLast('.', "(default)")
                map[pkg] = (map[pkg] ?: 0) + 1
            }
        }

        val sb = StringBuilder()
        sb.appendLine("共 ${map.values.sum()} 个类，${map.size} 个包")
        sb.appendLine("---------------------------------")
        for ((pkg, n) in map) sb.appendLine("$pkg  [$n]")
        return sb.toString()
    }
}