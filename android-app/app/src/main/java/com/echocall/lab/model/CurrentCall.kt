package com.echocall.lab.model

enum class CallPhase {
    OUTGOING,
    INCOMING,
    ACTIVE,
}

data class CurrentCall(
    val id: String,
    val contactId: String,
    val direction: CallDirection,
    val phase: CallPhase,
    val activeStartedAtElapsedRealtime: Long? = null,
)
