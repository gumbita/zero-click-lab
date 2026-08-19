/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

package com.echocall.lab.ui.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.echocall.lab.R
import com.echocall.lab.model.PendingProcessingMarker

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
    val pendingProcessingMarker: PendingProcessingMarker?,
    val pendingProcessingStatus: String,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LabSection(title = "Aplicación") {
                Text(
                    text = state.nativeStatus,
                    style = MaterialTheme.typography.titleMedium,
                )
                LabValue(label = "Build", value = state.buildVariant)
                LabValue(label = "Package", value = state.packageName)
            }

            LabSection(title = "Implementación del parser") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Text(
                        text = "${state.compiledParserImplementation} · Fijado al compilar",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = "Solo lectura. La interfaz no permite seleccionar el parser.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            LabSection(title = "Receptor UDP") {
                Text(state.udpReceiverStatus)
                LabValue(label = "Puerto", value = "43568")
                if (state.udpRetryAvailable) {
                    Button(onClick = onRetryUdpReceiver) {
                        Text("Retry UDP receiver")
                    }
                }
            }

            LabSection(title = "Procesamiento pendiente o interrumpido") {
                Text(state.pendingProcessingStatus)
                state.pendingProcessingMarker?.let { marker ->
                    LabValue(label = "scenarioId", value = marker.scenarioId)
                    LabValue(label = "variant", value = marker.variant)
                    LabValue(label = "packetLength", value = marker.packetLength.toString())
                    LabValue(label = "timestamp", value = marker.timestamp)
                    LabValue(label = "source", value = marker.source)
                    Text(
                        text = "El marker indica que un procesamiento comenzó y no " +
                            "alcanzó la limpieza posterior al retorno normal. " +
                            "No identifica por sí solo la causa.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            LabSection(title = "Escenario simulado") {
                Text("Llamada simulada: Marta Soler")
                Text(
                    text = "Asociación local del simulador; ECLB no contiene el nombre.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.showIncomingCall) {
                    Text("Señal de llamada recibida")
                    Text("El paquete se procesa automáticamente antes de la UI de llamada")
                    Text(state.callAction)
                    LabValue(label = "SOURCE", value = state.incomingCallSource)
                }
            }

            LabSection(title = "Último resultado automático") {
                Text("RESULTADO JNI COMPLETO")
                Text(state.incomingCallResult)
                Text(
                    text = "EVENTS",
                    style = MaterialTheme.typography.labelLarge,
                )
                if (state.incomingCallEvents.isEmpty()) {
                    Text(
                        text = "Sin eventos registrados",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.incomingCallEvents.forEach { event -> Text(event) }
                }
            }

            LabSection(title = "Muestra válida local") {
                Button(onClick = onProcessValidSample) {
                    Text("Procesar muestra válida")
                }
                LabValue(
                    label = "Resultado de la muestra válida",
                    value = state.validSampleResult,
                )
            }

            LabSection(title = "Limitaciones") {
                Text(
                    text = "Laboratorio local sin audio, backend ni telefonía real. " +
                        "ECLB es sintético y no demuestra RCE, control del flujo " +
                        "ni equivalencia exacta con CVE-2019-3568.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun LabSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun LabValue(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
