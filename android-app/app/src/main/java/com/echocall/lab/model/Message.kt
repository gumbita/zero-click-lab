/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

package com.echocall.lab.model

data class Message(
    val id: String,
    val contactId: String,
    val text: String,
    val isOutgoing: Boolean,
    val timestamp: String,
)
