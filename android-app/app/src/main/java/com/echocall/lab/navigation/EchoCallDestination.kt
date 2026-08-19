/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

package com.echocall.lab.navigation

object EchoCallDestination {
    const val CONVERSATIONS = "conversations"
    const val CHAT = "chat/{contactId}"
    const val CALL_HISTORY = "callHistory"
    const val LAB = "lab"
    const val ABOUT = "about"
    const val OUTGOING_CALL = "outgoingCall"
    const val INCOMING_CALL = "incomingCall"
    const val ACTIVE_CALL = "activeCall"
    const val BLOCKED_CALL = "blockedCall"
    const val INTERRUPTED_PROCESSING = "interruptedProcessing"

    fun chat(contactId: String): String = "chat/$contactId"
}
