/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

package com.echocall.lab

internal object NativeBridge {
    init {
        System.loadLibrary("echocall_native")
    }

    external fun nativeStatus(): String

    external fun parsePacket(packet: ByteArray): String

    external fun getCompiledParserImplementation(): String
}
