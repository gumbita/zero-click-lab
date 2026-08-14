package com.echocall.lab.ui.calls

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.echocall.lab.R
import com.echocall.lab.model.Contact
import com.echocall.lab.model.CurrentCall
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

    CallScreenLayout(
        contact = contact,
        headline = formatDuration(elapsedSeconds),
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            CallToggleControl(
                checked = muted,
                onCheckedChange = { muted = it },
                label = "Silenciar",
                activeLabel = "Silenciado",
                iconRes = R.drawable.ic_mic,
            )
            CallToggleControl(
                checked = speakerEnabled,
                onCheckedChange = { speakerEnabled = it },
                label = "Altavoz",
                activeLabel = "Altavoz activo",
                iconRes = R.drawable.ic_volume_up,
            )
        }
        Button(
            onClick = onEnd,
            modifier = Modifier
                .widthIn(min = 200.dp, max = 320.dp)
                .heightIn(min = 52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_call_end),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Finalizar")
        }
    }
}

@Composable
private fun CallToggleControl(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    activeLabel: String,
    iconRes: Int,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconToggleButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = if (checked) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = CircleShape,
                )
                .semantics {
                    contentDescription = label
                    stateDescription = if (checked) "Activado" else "Desactivado"
                    role = Role.Switch
                },
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
            )
        }
        Text(
            text = if (checked) activeLabel else label,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

private fun formatDuration(totalSeconds: Long): String = String.format(
    Locale.ROOT,
    "%02d:%02d",
    totalSeconds / 60L,
    totalSeconds % 60L,
)
