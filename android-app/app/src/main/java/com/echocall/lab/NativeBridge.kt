package com.echocall.lab

internal object NativeBridge {
    init {
        System.loadLibrary("echocall_native")
    }

    external fun nativeStatus(): String

    external fun parsePacket(packet: ByteArray): String

    external fun parsePacketVulnerable(packet: ByteArray): String
}
