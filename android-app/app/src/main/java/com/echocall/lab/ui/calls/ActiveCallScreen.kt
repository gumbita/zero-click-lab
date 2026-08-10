package com.echocall.lab.ui.calls

import android.os.SystemClock
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.echocall.lab.model.Contact
import com.echocall.lab.model.CurrentCall
import com.echocall.lab.ui.ContactAvatar
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun ActiveCallScreen(
    call: CurrentCall,
    contact: Contact,
    onEnd: () -> Unit,
) {
    var muted by remember(call.id) { mutableStateOf(false) }
    var speakerEnabled by remember(call.id) { mutableStateOf(false) }
    var elapsedSeconds by remember(call.id) { mutableLongStateOf(0L) }

    BackHandler(enabled = true) { }

    LaunchedEffect(call.id, call.activeStartedAtElapsedRealtime) {
        val startedAt = call.activeStartedAtElapsedRealtime ?: return@LaunchedEffect
        while (true) {
            elapsedSeconds = ((SystemClock.elapsedRealtime() - startedAt) / 1_000L)
                .coerceAtLeast(0L)
            delay(1_000L)
        }
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
                text = formatDuration(elapsedSeconds),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(
                onClick = { muted = !muted },
                modifier = Modifier.semantics {
                    contentDescription = if (muted) {
                        "Desactivar silencio"
                    } else {
                        "Silenciar"
                    }
                },
            ) {
                Text(if (muted) "Silenciado" else "Silenciar")
            }
            Button(
                onClick = { speakerEnabled = !speakerEnabled },
                modifier = Modifier.semantics {
                    contentDescription = if (speakerEnabled) {
                        "Desactivar altavoz"
                    } else {
                        "Activar altavoz"
                    }
                },
            ) {
                Text(if (speakerEnabled) "Altavoz activo" else "Altavoz")
            }
        }
        Button(
            onClick = onEnd,
            modifier = Modifier.semantics {
                contentDescription = "Finalizar llamada"
            },
        ) {
            Text("Finalizar")
        }
    }
}

private fun formatDuration(totalSeconds: Long): String = String.format(
    Locale.ROOT,
    "%02d:%02d",
    totalSeconds / 60L,
    totalSeconds % 60L,
)
