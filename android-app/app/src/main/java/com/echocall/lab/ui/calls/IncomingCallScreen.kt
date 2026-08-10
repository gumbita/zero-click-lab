package com.echocall.lab.ui.calls

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.echocall.lab.UDP_LOG_TAG
import com.echocall.lab.ui.ContactAvatar

@Composable
fun IncomingCallScreen(
    callId: String,
    contact: Contact,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    BackHandler(enabled = true) { }

    LaunchedEffect(callId) {
        Log.i(
            UDP_LOG_TAG,
            "IncomingCallScreen presented after accepted parser result",
        )
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
                text = "Llamada entrante",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(
                onClick = onReject,
                modifier = Modifier.semantics {
                    contentDescription = "Rechazar llamada"
                },
            ) {
                Text("Rechazar")
            }
            Button(
                onClick = onAccept,
                modifier = Modifier.semantics {
                    contentDescription = "Aceptar llamada"
                },
            ) {
                Text("Aceptar")
            }
        }
    }
}
