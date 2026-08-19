/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

package com.echocall.lab.model

const val VOIP_CONTROL_PACKET_SCENARIO_ID = "voip_control_packet"

data class PendingProcessingMarker(
    val scenarioId: String,
    val variant: String,
    val packetLength: Int,
    val timestamp: String,
    val source: String,
)
