package com.nyan.mcpmobile.tool.impl

import com.nyan.mcpmobile.tool.McpTool
import com.nyan.mcpmobile.tool.impl.dex.DexUtils
import org.jf.baksmali.Adaptors.ClassDefinition
import org.jf.baksmali.BaksmaliOptions
import org.jf.util.IndentingWriter
import org.json.JSONObject
import java.io.StringWriter

/** 反编译单个类为 Smali 代码 */
class ApkReadClassTool : McpTool {

    override val name = "read_class_code"
    override val description = "反编译Smali代码。默认返回整个类。如果提供了 method_name，则只返回该方法的代码（节省 Token）。"

    override fun inputSchema() = JSONObject(
        """{"type":"object","properties":{"path":{"type":"string"},"class_name":{"type":"string"},"method_name":{"type":"string"}},"required":["path","class_name"]}"""
    )

    override fun invoke(args: JSONObject): String {
        val path = args.getString("path")
        val raw = args.getString("class_name")
        val className = if (raw.startsWith("L")) raw else "L" + raw.replace('.', '/') + ";"
        val method = args.optString("method_name", "")

        val sw = StringWriter()
        DexUtils.withClasses(path) { classes ->
            val cls = classes.firstOrNull { it.type == className }
            if (cls != null) {
                ClassDefinition(BaksmaliOptions(), cls).write(IndentingWriter(sw))
            }
            cls
        }

        val smali = sw.toString()
        if (smali.isBlank()) return "未找到类: $className"
        if (method.isBlank()) return smali
        return extractMethod(smali, method)
    }

    private fun extractMethod(smali: String, name: String): String {
        val out = StringBuilder()
        var inside = false
        for (line in smali.lines()) {
            val t = line.trim()
            if (t.startsWith(".method")) {
                inside = t.contains(" $name(") || t.contains(" $name<") ||
                        Regex("""\b${Regex.escape(name)}\b""").containsMatchIn(t)
                if (inside) out.appendLine(line)
            } else if (inside) {
                out.appendLine(line)
                if (t == ".end method") inside = false
            }
        }
        return out.toString().ifBlank { "未找到方法: $name" }
    }
}