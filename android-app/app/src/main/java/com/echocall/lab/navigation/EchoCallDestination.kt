package com.echocall.lab.navigation

object EchoCallDestination {
    const val CONVERSATIONS = "conversations"
    const val CHAT = "chat/{contactId}"
    const val CALL_HISTORY = "callHistory"
    const val LAB = "lab"
    const val ABOUT = "about"

    fun chat(contactId: String): String = "chat/$contactId"
}
