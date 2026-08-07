package com.echocall.lab.model

enum class CallDirection {
    INCOMING,
    OUTGOING,
}

enum class CallOutcome {
    COMPLETED,
    REJECTED,
    MISSED,
    BLOCKED,
    INTERRUPTED,
    CANCELLED,
}

data class CallRecord(
    val id: String,
    val contactId: String,
    val direction: CallDirection,
    val outcome: CallOutcome,
    val timestamp: String,
)
