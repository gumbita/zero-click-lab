/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

package com.echocall.lab.ui.calls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.echocall.lab.model.CallDirection
import com.echocall.lab.model.CallOutcome
import com.echocall.lab.model.CallRecord
import com.echocall.lab.model.Contact
import com.echocall.lab.ui.ContactAvatar
import com.echocall.lab.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    records: List<CallRecord>,
    contacts: List<Contact>,
    onBack: () -> Unit,
) {
    val contactsById = contacts.associateBy { contact -> contact.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Llamadas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Volver",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            items(
                items = records,
                key = { record -> record.id },
            ) { record ->
                contactsById[record.contactId]?.let { contact ->
                    CallHistoryRow(
                        record = record,
                        contact = contact,
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 80.dp))
                }
            }
        }
    }
}

@Composable
private fun CallHistoryRow(
    record: CallRecord,
    contact: Contact,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ContactAvatar(contact = contact)
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = contact.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = record.displayStatus(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (
                    record.outcome == CallOutcome.MISSED ||
                    record.outcome == CallOutcome.REJECTED
                ) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        Text(
            text = record.timestamp,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun CallRecord.displayStatus(): String {
    val directionText = when (direction) {
        CallDirection.INCOMING -> "Entrante"
        CallDirection.OUTGOING -> "Saliente"
    }
    val outcomeText = when (outcome) {
        CallOutcome.COMPLETED -> "Completada"
        CallOutcome.REJECTED -> "Rechazada"
        CallOutcome.MISSED -> "Perdida"
        CallOutcome.BLOCKED -> "Bloqueada"
        CallOutcome.INTERRUPTED -> "Interrumpida"
        CallOutcome.CANCELLED -> "Cancelada"
    }
    return "$directionText · $outcomeText"
}
