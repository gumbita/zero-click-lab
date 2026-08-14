package com.echocall.lab.ui.calls

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.echocall.lab.UDP_LOG_TAG
import com.echocall.lab.model.Contact

@Composable
fun BlockedCallScreen(
    attemptId: String,
    contact: Contact,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)

    LaunchedEffect(attemptId) {
        Log.i(
            UDP_LOG_TAG,
            "BlockedCallScreen presented after PACKET_REJECTED_INVALID_LENGTH",
        )
    }

    CallScreenLayout(
        contact = contact,
        headline = "Llamada bloqueada",
        supportingContent = {
            Text(
                text = "EchoCall rechazó esta llamada antes de establecerla.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        },
    ) {
        Button(
            onClick = onClose,
            modifier = Modifier
                .widthIn(min = 200.dp, max = 320.dp)
                .heightIn(min = 52.dp),
        ) {
            Text("Cerrar")
        }
    }
}
