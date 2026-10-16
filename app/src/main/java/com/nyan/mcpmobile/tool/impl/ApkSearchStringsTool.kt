package com.nyan.mcpmobile.tool.impl

import com.nyan.mcpmobile.tool.McpTool
import com.nyan.mcpmobile.tool.impl.dex.DexUtils
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.StringReference
import org.json.JSONObject

/** 全局搜索字符串常量 */
class ApkSearchStringsTool : McpTool {

    override val name = "search_strings"
    override val description = "全局搜索字符串常量。"

    override fun inputSchema() = JSONObject(
        """{"type":"object","properties":{"path":{"type":"string"},"keyword":{"type":"string"}},"required":["path","keyword"]}"""
    )

    override fun invoke(args: JSONObject): String {
        val path = args.getString("path")
        val keyword = args.getString("keyword")
        val hits = LinkedHashSet<String>()

        DexUtils.withClasses(path) { classes ->
            outer@ for (cls in classes) {
                for (m in cls.methods) {
                    val impl = m.implementation ?: continue
                    for (inst in impl.instructions) {
                        if (inst is ReferenceInstruction) {
                            val ref = inst.reference
                            if (ref is StringReference) {
                                val s = ref.string
                                if (s.isNotEmpty() && s.contains(keyword, ignoreCase = true)) {
                                    hits.add(s)
                                }
                            }
                        }
                        if (hits.size >= 300) break@outer
                    }
                }
            }
        }

        return if (hits.isEmpty()) "未找到含 \"$keyword\" 的字符串"
        else "找到 ${hits.size} 条（最多 300）：\n" + hits.joinToString("\n")
    }
}