package com.nyan.mcpmobile.tool.impl.dex

import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.iface.ClassDef
import java.io.File

/**
 * DEX 加载工具：从 APK（含多 dex）里读出所有类定义。
 * 底层用 smali 项目的 dexlib2 —— 纯 Java，Android 上跑得飞快。
 */
object DexUtils {

    fun <T> withClasses(apkPath: String, block: (List<ClassDef>) -> T): T {
        val apk = File(apkPath)
        if (!apk.exists()) throw IllegalArgumentException("APK 不存在: $apkPath")

        val container = DexFileFactory.loadDexContainer(apk, Opcodes.getDefault())
        val classes = ArrayList<ClassDef>()
        for (name in container.dexEntryNames) {
            container.getEntry(name)?.let { classes.addAll(it.dexFile.classes) }
        }
        return block(classes)
    }
}