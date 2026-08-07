package com.echocall.lab

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext

private const val UDP_PACKET_QUEUE_CAPACITY = 16

private data class UdpPacketEvent(
    val packet: ReceivedUdpPacket,
)

class MainActivity : ComponentActivity() {
    private var activityStarted = false
    private val udpPacketEvents =
        Channel<UdpPacketEvent>(UDP_PACKET_QUEUE_CAPACITY)
    private var udpReceiverStatus by mutableStateOf("UDP stopped")
    private var udpRetryAvailable by mutableStateOf(false)

    private val udpReceiver = UdpPacketReceiver(
        onListening = { boundPort ->
            runOnUiThread {
                if (activityStarted) {
                    udpReceiverStatus = "UDP listening on port $boundPort"
                    udpRetryAvailable = false
                }
            }
        },
        onPacket = { packet ->
            runOnUiThread {
                if (activityStarted) {
                    val sendResult = udpPacketEvents.trySend(
                        UdpPacketEvent(
                            packet = packet,
                        ),
                    )
                    if (sendResult.isFailure) {
                        udpReceiverStatus =
                            "UDP receiver error: packet queue full"
                    }
                }
            }
        },
        onError = { error ->
            runOnUiThread {
                if (activityStarted) {
                    udpReceiverStatus =
                        "UDP receiver error: ${error.message ?: "I/O error"}"
                    udpRetryAvailable = true
                }
            }
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nativeStatus = NativeBridge.nativeStatus()
        val compiledParserImplementation =
            NativeBridge.getCompiledParserImplementation()
        setContent {
            EchoCallLabScreen(
                nativeStatus = nativeStatus,
                compiledParserImplementation = compiledParserImplementation,
                udpReceiverStatus = udpReceiverStatus,
                udpRetryAvailable = udpRetryAvailable,
                udpPacketEvents = udpPacketEvents,
                onRetryUdpReceiver = ::retryUdpReceiver,
            )
        }
    }

    override fun onStart() {
        super.onStart()
        activityStarted = true
        udpReceiverStatus = "UDP starting"
        udpRetryAvailable = false
        udpReceiver.start()
    }

    override fun onStop() {
        activityStarted = false
        udpReceiver.stop()
        while (udpPacketEvents.tryReceive().isSuccess) {
            // Discard packets that were queued for an Activity no longer active.
        }
        udpReceiverStatus = "UDP stopped"
        udpRetryAvailable = false
        super.onStop()
    }

    override fun onDestroy() {
        udpReceiver.close()
        udpPacketEvents.close()
        super.onDestroy()
    }

    private fun retryUdpReceiver() {
        if (!activityStarted) {
            return
        }

        udpReceiverStatus = "UDP starting"
        udpRetryAvailable = false
        udpReceiver.retry()
    }
}

@Composable
private fun EchoCallLabScreen(
    nativeStatus: String,
    compiledParserImplementation: String,
    udpReceiverStatus: String,
    udpRetryAvailable: Boolean,
    udpPacketEvents: Channel<UdpPacketEvent>,
    onRetryUdpReceiver: () -> Unit,
) {
    val context = LocalContext.current
    val buildVariant = if (context.packageName.endsWith(".asan")) {
        "ASan"
    } else {
        "Debug"
    }
    var validSampleResult by remember {
        mutableStateOf("Muestra válida no procesada")
    }
    var showIncomingCall by remember { mutableStateOf(false) }
    var incomingCallResult by remember { mutableStateOf("Incoming call not received") }
    var incomingCallEvents by remember { mutableStateOf(emptyList<String>()) }
    var incomingCallSource by remember { mutableStateOf("UDP") }
    var callAction by remember { mutableStateOf("No user action required") }

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
                "NATIVE_PARSE_STARTED",
            )
            incomingCallResult = "Automatic native parsing started"

            try {
                val result = withContext(Dispatchers.Default) {
                    Log.i(
                        UDP_LOG_TAG,
                        "Dispatching datagram length=${event.packet.bytes.size}",
                    )
                    NativeBridge.parsePacket(event.packet.bytes)
                }

                Log.i(
                    UDP_LOG_TAG,
                    "Parser returned result=$result",
                )
                incomingCallResult = result
                incomingCallEvents += if (
                    result.startsWith("status=accepted code=ok")
                ) {
                    "NATIVE_PARSE_OK"
                } else {
                    "PACKET_REJECTED"
                }
            } catch (_: RuntimeException) {
                incomingCallResult = "status=error code=udp_parse_failed"
                incomingCallEvents += "NATIVE_PARSE_ERROR"
            }
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(text = nativeStatus)
                Text(
                    text = "Build: $buildVariant · ${context.packageName}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = "Parser compilado: $compiledParserImplementation · " +
                        "Fijado al compilar",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(text = udpReceiverStatus)
                if (udpRetryAvailable) {
                    Button(onClick = onRetryUdpReceiver) {
                        Text("Retry UDP receiver")
                    }
                }
                if (showIncomingCall) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Incoming call (simulated)",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text("Caller: EchoCall Test")
                            Text("Control packet processed automatically")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        callAction = "Accepted after automatic processing"
                                    },
                                ) {
                                    Text("Accept")
                                }
                                Button(
                                    onClick = {
                                        callAction = "Rejected after automatic processing"
                                    },
                                ) {
                                    Text("Reject")
                                }
                            }
                            Text(callAction)
                        }
                    }
                    Text("SOURCE: $incomingCallSource")
                    Text("AUTOMATIC RESULT")
                    Text(incomingCallResult)
                    Text("EVENTS")
                    incomingCallEvents.forEach { event -> Text(event) }
                }
                Button(
                    onClick = {
                        validSampleResult = context.assets
                            .open("valid_call_control.bin")
                            .use { input ->
                                NativeBridge.parsePacket(input.readBytes())
                            }
                    },
                ) {
                    Text(text = "Procesar muestra válida")
                }
                Text(text = "Resultado de la muestra válida")
                Text(text = validSampleResult)
            }
        }
    }
}
