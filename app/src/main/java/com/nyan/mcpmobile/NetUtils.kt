package com.nyan.mcpmobile

import java.net.Inet4Address
import java.net.NetworkInterface

object NetUtils {
    /** 获取局域网 IPv4 地址 */
    fun lanIp(): String {
        try {
            val nifs = NetworkInterface.getNetworkInterfaces() ?: return "0.0.0.0"
            for (nif in nifs) {
                if (!nif.isUp || nif.isLoopback) continue
                for (addr in nif.inetAddresses) {
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        val ip = addr.hostAddress ?: continue
                        if (ip.startsWith("169.254")) continue
                        return ip
                    }
                }
            }
        } catch (_: Exception) {
        }
        return "0.0.0.0"
    }
}