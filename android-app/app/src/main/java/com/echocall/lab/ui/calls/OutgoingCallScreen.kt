/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

package com.echocall.lab.ui.calls

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.echocall.lab.R
import com.echocall.lab.model.Contact
import kotlinx.coroutines.delay

private const val OUTGOING_TRANSITION_DELAY_MILLIS = 1_000L

@Composable
fun OutgoingCallScreen(
    callId: String,
    contact: Contact,
    onConnected: () -> Unit,
    onCancel: () -> Unit,
) {
    BackHandler(onBack = onCancel)

    LaunchedEffect(callId) {
        delay(OUTGOING_TRANSITION_DELAY_MILLIS)
        onConnected()
    }

    CallScreenLayout(
        contact = contact,
        headline = "Llamando…",
    ) {
        Button(
            onClick = onCancel,
            modifier = Modifier
                .widthIn(min = 200.dp, max = 320.dp)
                .heightIn(min = 52.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_call_end),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cancelar")
        }
    }
}
