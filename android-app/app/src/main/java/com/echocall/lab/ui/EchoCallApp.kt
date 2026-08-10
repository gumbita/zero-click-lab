package com.echocall.lab.ui

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import com.echocall.lab.NativeBridge
import com.echocall.lab.UDP_LOG_TAG
import com.echocall.lab.UdpPacketEvent
import com.echocall.lab.data.PendingProcessingStore
import com.echocall.lab.model.PendingProcessingMarker
import com.echocall.lab.model.VOIP_CONTROL_PACKET_SCENARIO_ID
import com.echocall.lab.navigation.EchoCallNavHost
import com.echocall.lab.ui.lab.LabModeUiState
import com.echocall.lab.ui.state.rememberEchoCallStateHolder
import com.echocall.lab.ui.theme.EchoCallTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant

@Composable
internal fun EchoCallApp(
    nativeStatus: String,
    compiledParserImplementation: String,
    udpReceiverStatus: String,
    udpRetryAvailable: Boolean,
    udpPacketEvents: Channel<UdpPacketEvent>,
    pendingProcessingStore: PendingProcessingStore,
    pendingMarkerTestCommand: String?,
    onRetryUdpReceiver: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val productStateHolder = rememberEchoCallStateHolder()
    val buildVariant = if (context.packageName.endsWith(".asan")) {
        "ASan"
    } else {
        "Debug"
    }
    var validSampleResult by remember {
        mutableStateOf("Muestra válida no procesada")
    }
    var showIncomingCall by remember { mutableStateOf(false) }
    var incomingCallResult by remember {
        mutableStateOf("Incoming call not received")
    }
    var incomingCallEvents by remember {
        mutableStateOf(emptyList<String>())
    }
    var incomingCallSource by remember { mutableStateOf("UDP") }
    var callAction by remember {
        mutableStateOf("No user action required")
    }
    var pendingProcessingMarker by remember {
        mutableStateOf<PendingProcessingMarker?>(null)
    }
    var interruptedProcessingMarker by remember {
        mutableStateOf<PendingProcessingMarker?>(null)
    }
    var pendingProcessingStatus by remember {
        mutableStateOf("Sin procesamiento pendiente")
    }
    var pendingMarkerLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(pendingProcessingStore) {
        try {
            val marker = pendingProcessingStore.readPending()
            pendingProcessingMarker = marker
            interruptedProcessingMarker = marker
            if (marker != null) {
                pendingProcessingStatus =
                    "Procesamiento pendiente detectado al iniciar; causa no determinada."
                Log.w(
                    UDP_LOG_TAG,
                    "Pending processing marker detected at startup: $marker; " +
                        "cause is not determined",
                )
            }
        } catch (error: Exception) {
            pendingProcessingStatus =
                "No se pudo leer el marker: ${error.message ?: "error de persistencia"}"
            Log.e(UDP_LOG_TAG, "PENDING_MARKER_READ_ERROR", error)
        } finally {
            pendingMarkerLoaded = true
        }
    }

    LaunchedEffect(pendingMarkerTestCommand, pendingMarkerLoaded) {
        if (!pendingMarkerLoaded || pendingMarkerTestCommand == null) {
            return@LaunchedEffect
        }

        when (pendingMarkerTestCommand) {
            "mark" -> {
                val marker = createPendingMarker(
                    variant = context.packageName,
                    packetLength = 17,
                    source = "test",
                )
                pendingProcessingStore.markPending(marker)
                pendingProcessingMarker = marker
                pendingProcessingStatus =
                    "Marker de prueba persistido; no se ejecutó JNI."
                Log.i(UDP_LOG_TAG, "PENDING_MARKER_TEST_MARKED marker=$marker")
            }

            "read" -> {
                val marker = pendingProcessingStore.readPending()
                pendingProcessingMarker = marker
                Log.i(UDP_LOG_TAG, "PENDING_MARKER_TEST_READ marker=$marker")
            }

            "clear" -> {
                pendingProcessingStore.clearPending()
                pendingProcessingMarker = null
                interruptedProcessingMarker = null
                pendingProcessingStatus = "Sin procesamiento pendiente"
                Log.i(UDP_LOG_TAG, "PENDING_MARKER_TEST_CLEARED")
            }

            else -> Log.w(
                UDP_LOG_TAG,
                "Unknown pending marker test command ignored",
            )
        }
    }

    LaunchedEffect(udpPacketEvents) {
        for (event in udpPacketEvents) {
            showIncomingCall = true
            callAction = "No user action required"
            incomingCallSource =
                "UDP ${event.packet.sourceAddress}:${event.packet.sourcePort} " +
                    "(${event.packet.bytes.size} bytes)"
            incomingCallEvents = listOf(
                "CALL_INCOMING",
                "CONTROL_PACKET_RECEIVED",
            )
            incomingCallResult = "Persisting pending marker before native parsing"
            Log.i(UDP_LOG_TAG, "CALL_INCOMING")
            Log.i(UDP_LOG_TAG, "CONTROL_PACKET_RECEIVED")

            try {
                val result = parsePacketWithPendingMarker(
                    packet = event.packet.bytes,
                    variant = context.packageName,
                    source = "udp",
                    store = pendingProcessingStore,
                    onMarkerPersisted = { marker ->
                        pendingProcessingMarker = marker
                        pendingProcessingStatus =
                            "Procesamiento en curso con marker persistido."
                        Log.i(
                            UDP_LOG_TAG,
                            "PENDING_MARKER_PERSISTED marker=$marker",
                        )
                    },
                    onNativeStarted = {
                        incomingCallEvents += "NATIVE_PARSE_STARTED"
                        incomingCallResult = "Automatic native parsing started"
                        Log.i(UDP_LOG_TAG, "NATIVE_PARSE_STARTED")
                    },
                    onMarkerCleared = {
                        pendingProcessingMarker = null
                        pendingProcessingStatus = "Sin procesamiento pendiente"
                        Log.i(UDP_LOG_TAG, "PENDING_MARKER_CLEARED")
                    },
                )

                Log.i(
                    UDP_LOG_TAG,
                    "Parser returned result=$result",
                )
                incomingCallResult = result
                if (result.startsWith("status=accepted code=ok")) {
                    incomingCallEvents += "NATIVE_PARSE_OK"
                    Log.i(UDP_LOG_TAG, "NATIVE_PARSE_OK")
                    if (productStateHolder.startIncomingCallFromAcceptedUdp()) {
                        callAction =
                            "Incoming call UI requested after NATIVE_PARSE_OK"
                        Log.i(
                            UDP_LOG_TAG,
                            "Incoming call state created for simulator contact " +
                                "Marta Soler after NATIVE_PARSE_OK",
                        )
                    } else {
                        callAction =
                            "Accepted packet not presented: call already in progress"
                        Log.i(
                            UDP_LOG_TAG,
                            "Accepted datagram retained in Lab: call already in progress",
                        )
                    }
                } else if (
                    result.startsWith(
                        "status=rejected code=payload_too_large",
                    )
                ) {
                    incomingCallEvents += "PACKET_REJECTED_INVALID_LENGTH"
                    Log.i(UDP_LOG_TAG, "PACKET_REJECTED_INVALID_LENGTH")
                    if (
                        productStateHolder
                            .recordBlockedIncomingCallFromRejectedUdp()
                    ) {
                        callAction =
                            "La variante Patched rechazó la entrada por longitud " +
                                "antes de establecer la llamada simulada."
                        Log.i(
                            UDP_LOG_TAG,
                            "Blocked call state created for simulator contact " +
                                "Marta Soler after rejected payload_too_large",
                        )
                    } else {
                        callAction =
                            "Entrada rechazada conservada en Lab: ya existe " +
                                "una llamada o aviso bloqueado."
                        Log.i(
                            UDP_LOG_TAG,
                            "Rejected datagram retained in Lab: call UI busy",
                        )
                    }
                } else {
                    incomingCallEvents += "PACKET_REJECTED"
                    callAction = "Rejected packet retained in Lab"
                    Log.i(UDP_LOG_TAG, "PACKET_REJECTED")
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                incomingCallResult = "status=error code=udp_parse_failed"
                incomingCallEvents += "NATIVE_PARSE_ERROR"
                callAction =
                    "Native processing or marker persistence failed; see log"
                Log.e(UDP_LOG_TAG, "NATIVE_PARSE_ERROR", error)
            }
        }
    }

    EchoCallTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            if (!pendingMarkerLoaded) {
                return@Surface
            }
            EchoCallNavHost(
            productUiState = productStateHolder.uiState,
            interruptedProcessingMarker = interruptedProcessingMarker,
            labModeUiState = LabModeUiState(
                nativeStatus = nativeStatus,
                buildVariant = buildVariant,
                packageName = context.packageName,
                compiledParserImplementation = compiledParserImplementation,
                udpReceiverStatus = udpReceiverStatus,
                udpRetryAvailable = udpRetryAvailable,
                showIncomingCall = showIncomingCall,
                incomingCallSource = incomingCallSource,
                incomingCallResult = incomingCallResult,
                incomingCallEvents = incomingCallEvents,
                callAction = callAction,
                validSampleResult = validSampleResult,
                pendingProcessingMarker = pendingProcessingMarker,
                pendingProcessingStatus = pendingProcessingStatus,
            ),
            onSendMessage = productStateHolder::sendMessage,
            onResetData = productStateHolder::resetSimulatedData,
            onStartOutgoingCall = productStateHolder::startOutgoingCall,
            onActivateOutgoingCall = productStateHolder::activateOutgoingCall,
            onCancelOutgoingCall = productStateHolder::cancelOutgoingCall,
            onAcceptIncomingCall = productStateHolder::acceptIncomingCall,
            onRejectIncomingCall = productStateHolder::rejectIncomingCall,
            onEndActiveCall = productStateHolder::endActiveCall,
            onCloseBlockedCall = productStateHolder::clearBlockedCallAttempt,
            onClearInterruptedProcessing = {
                try {
                    pendingProcessingStore.clearPending()
                    pendingProcessingMarker = null
                    interruptedProcessingMarker = null
                    pendingProcessingStatus = "Sin procesamiento pendiente"
                    Log.i(UDP_LOG_TAG, "INTERRUPTED_MARKER_CLEARED_BY_USER")
                    true
                } catch (error: Exception) {
                    pendingProcessingStatus =
                        "No se pudo limpiar el marker: " +
                            (error.message ?: "error de persistencia")
                    Log.e(
                        UDP_LOG_TAG,
                        "INTERRUPTED_MARKER_CLEAR_ERROR",
                        error,
                    )
                    false
                }
            },
            onRetryUdpReceiver = onRetryUdpReceiver,
            onProcessValidSample = {
                coroutineScope.launch {
                    val packet = context.assets
                        .open("valid_call_control.bin")
                        .use { input -> input.readBytes() }
                    try {
                        validSampleResult = parsePacketWithPendingMarker(
                            packet = packet,
                            variant = context.packageName,
                            source = "local_sample",
                            store = pendingProcessingStore,
                            onMarkerPersisted = { marker ->
                                pendingProcessingMarker = marker
                                pendingProcessingStatus =
                                    "Procesamiento en curso con marker persistido."
                            },
                            onNativeStarted = {},
                            onMarkerCleared = {
                                pendingProcessingMarker = null
                                pendingProcessingStatus =
                                    "Sin procesamiento pendiente"
                            },
                        )
                    } catch (error: Exception) {
                        validSampleResult =
                            "status=error code=local_parse_failed"
                        Log.e(UDP_LOG_TAG, "LOCAL_PARSE_ERROR", error)
                    }
                }
            },
            )
        }
    }
}

private fun createPendingMarker(
    variant: String,
    packetLength: Int,
    source: String,
): PendingProcessingMarker = PendingProcessingMarker(
    scenarioId = VOIP_CONTROL_PACKET_SCENARIO_ID,
    variant = variant,
    packetLength = packetLength,
    timestamp = Instant.now().toString(),
    source = source,
)

private suspend fun parsePacketWithPendingMarker(
    packet: ByteArray,
    variant: String,
    source: String,
    store: PendingProcessingStore,
    onMarkerPersisted: (PendingProcessingMarker) -> Unit,
    onNativeStarted: () -> Unit,
    onMarkerCleared: () -> Unit,
): String {
    val marker = createPendingMarker(
        variant = variant,
        packetLength = packet.size,
        source = source,
    )
    store.markPending(marker)

    val result = try {
        onMarkerPersisted(marker)
        onNativeStarted()
        withContext(Dispatchers.Default) {
            Log.i(
                UDP_LOG_TAG,
                "Dispatching packet length=${packet.size} source=$source",
            )
            NativeBridge.parsePacket(packet)
        }
    } catch (error: Exception) {
        try {
            store.clearPending()
            onMarkerCleared()
        } catch (clearError: Exception) {
            error.addSuppressed(clearError)
        }
        throw error
    }

    store.clearPending()
    onMarkerCleared()
    return result
}
