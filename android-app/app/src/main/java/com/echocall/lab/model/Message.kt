package com.echocall.lab.model

data class Message(
    val id: String,
    val contactId: String,
    val text: String,
    val isOutgoing: Boolean,
    val timestamp: String,
)
