/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

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
