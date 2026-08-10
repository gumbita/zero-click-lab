package com.echocall.lab.ui.calls

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.echocall.lab.model.Contact

@Composable
fun InterruptedProcessingScreen(
    contact: Contact,
    onOpenLab: () -> Unit,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)

    CallScreenLayout(
        contact = contact,
        headline = "Procesamiento interrumpido",
        supportingContent = {
            Text(
                text = "El procesamiento anterior no devolvió el control a EchoCall.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Esto no determina por sí solo la causa de la interrupción. " +
                    "Consulta el Modo Lab y la instrumentación de la ejecución " +
                    "para más detalles.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        },
    ) {
        Button(
            onClick = onOpenLab,
            modifier = Modifier
                .widthIn(min = 200.dp, max = 320.dp)
                .heightIn(min = 52.dp),
        ) {
            Text("Abrir Modo Lab")
        }
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier
                .widthIn(min = 200.dp, max = 320.dp)
                .heightIn(min = 52.dp),
        ) {
            Text("Cerrar y continuar")
        }
    }
}
