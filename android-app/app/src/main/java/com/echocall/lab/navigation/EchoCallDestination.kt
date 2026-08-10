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

    fun chat(contactId: String): String = "chat/$contactId"
}
