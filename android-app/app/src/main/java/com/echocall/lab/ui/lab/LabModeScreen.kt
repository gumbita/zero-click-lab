package com.echocall.lab.ui.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

data class LabModeUiState(
    val nativeStatus: String,
    val buildVariant: String,
    val packageName: String,
    val compiledParserImplementation: String,
    val udpReceiverStatus: String,
    val udpRetryAvailable: Boolean,
    val showIncomingCall: Boolean,
    val incomingCallSource: String,
    val incomingCallResult: String,
    val incomingCallEvents: List<String>,
    val callAction: String,
    val validSampleResult: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabModeScreen(
    state: LabModeUiState,
    onBack: () -> Unit,
    onRetryUdpReceiver: () -> Unit,
    onProcessValidSample: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modo Lab") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Atrás"
                        },
                    ) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = state.nativeStatus,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Build: ${state.buildVariant} · ${state.packageName}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Parser compilado: " +
                    "${state.compiledParserImplementation} · Fijado al compilar",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(text = state.udpReceiverStatus)
            if (state.udpRetryAvailable) {
                Button(onClick = onRetryUdpReceiver) {
                    Text("Retry UDP receiver")
                }
            }
            if (state.showIncomingCall) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Señal de llamada recibida",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text("Llamada simulada: Marta Soler")
                        Text("Asociación local del simulador; ECLB no contiene el nombre")
                        Text("El paquete se procesa automáticamente antes de la UI de llamada")
                        Text(state.callAction)
                    }
                }
                Text("SOURCE: ${state.incomingCallSource}")
                Text("RESULTADO JNI COMPLETO")
                Text(state.incomingCallResult)
                Text("EVENTS")
                state.incomingCallEvents.forEach { event -> Text(event) }
            }
            Button(onClick = onProcessValidSample) {
                Text("Procesar muestra válida")
            }
            Text("Resultado de la muestra válida")
            Text(state.validSampleResult)
        }
    }
}
