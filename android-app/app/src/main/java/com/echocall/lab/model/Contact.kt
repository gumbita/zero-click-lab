/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

package com.echocall.lab.model

data class Contact(
    val id: String,
    val displayName: String,
    val initials: String,
    val preview: String,
    val timestamp: String,
)
