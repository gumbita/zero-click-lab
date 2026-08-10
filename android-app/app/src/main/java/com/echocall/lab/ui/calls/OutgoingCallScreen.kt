package com.echocall.lab.ui.calls

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.echocall.lab.model.Contact
import com.echocall.lab.ui.ContactAvatar
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ContactAvatar(contact = contact, size = 112.dp)
            Text(
                text = contact.displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Llamando…",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Button(
            onClick = onCancel,
            modifier = Modifier.semantics {
                contentDescription = "Cancelar llamada"
            },
        ) {
            Text("Cancelar")
        }
    }
}
