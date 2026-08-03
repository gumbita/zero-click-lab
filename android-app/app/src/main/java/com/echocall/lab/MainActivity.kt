package com.echocall.lab

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class ParserMode {
    SAFE,
    VULNERABLE
}

private const val UDP_PACKET_QUEUE_CAPACITY = 16

private data class UdpPacketEvent(
    val packet: ReceivedUdpPacket,
    val parserMode: ParserMode,
)

class MainActivity : ComponentActivity() {
    private var activityStarted = false
    private val udpPacketEvents =
        Channel<UdpPacketEvent>(UDP_PACKET_QUEUE_CAPACITY)
    private var udpReceiverStatus by mutableStateOf("UDP stopped")
    private var udpRetryAvailable by mutableStateOf(false)
    private val parserMode = MutableStateFlow(ParserMode.SAFE)

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
            val parserModeSnapshot = parserMode.value
            runOnUiThread {
                if (activityStarted) {
                    val sendResult = udpPacketEvents.trySend(
                        UdpPacketEvent(
                            packet = packet,
                            parserMode = parserModeSnapshot,
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
        setContent {
            EchoCallLabScreen(
                nativeStatus = nativeStatus,
                udpReceiverStatus = udpReceiverStatus,
                udpRetryAvailable = udpRetryAvailable,
                udpPacketEvents = udpPacketEvents,
                parserMode = parserMode,
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
    udpReceiverStatus: String,
    udpRetryAvailable: Boolean,
    udpPacketEvents: Channel<UdpPacketEvent>,
    parserMode: MutableStateFlow<ParserMode>,
    onRetryUdpReceiver: () -> Unit,
) {
    val context = LocalContext.current
    val buildVariant = if (context.packageName.endsWith(".asan")) {
        "ASan"
    } else {
        "Debug"
    }
    var safeResult by remember { mutableStateOf("Valid sample not parsed") }
    var vulnerableResult by remember { mutableStateOf("Valid sample not parsed") }
    val selectedMode by parserMode.collectAsState()
    var showIncomingCall by remember { mutableStateOf(false) }
    var incomingCallResult by remember { mutableStateOf("Incoming call not simulated") }
    var incomingCallEvents by remember { mutableStateOf(emptyList<String>()) }
    var incomingCallSource by remember { mutableStateOf("LOCAL ASSET") }
    var incomingCallMode by remember { mutableStateOf(ParserMode.SAFE) }
    var callAction by remember { mutableStateOf("No user action required") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(udpPacketEvents) {
        for (event in udpPacketEvents) {
            val parserModeSnapshot = event.parserMode

            showIncomingCall = true
            callAction = "No user action required"
            incomingCallMode = parserModeSnapshot
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
                        "Dispatching datagram mode=${parserModeSnapshot.name} " +
                            "length=${event.packet.bytes.size}",
                    )
                    when (parserModeSnapshot) {
                        ParserMode.SAFE ->
                            NativeBridge.parsePacket(event.packet.bytes)
                        ParserMode.VULNERABLE ->
                            NativeBridge.parsePacketVulnerable(event.packet.bytes)
                    }
                }

                Log.i(
                    UDP_LOG_TAG,
                    "Parser returned mode=${parserModeSnapshot.name} " +
                        "result=$result",
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
                Text(text = udpReceiverStatus)
                if (udpRetryAvailable) {
                    Button(onClick = onRetryUdpReceiver) {
                        Text("Retry UDP receiver")
                    }
                }
                Text("Incoming parser mode")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selectedMode == ParserMode.SAFE,
                        onClick = { parserMode.value = ParserMode.SAFE },
                    )
                    Text("SAFE")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = selectedMode == ParserMode.VULNERABLE,
                        onClick = { parserMode.value = ParserMode.VULNERABLE },
                    )
                    Text("VULNERABLE")
                }
                Button(
                    onClick = {
                        showIncomingCall = true
                        callAction = "No user action required"
                        incomingCallMode = selectedMode
                        incomingCallSource =
                            "LOCAL ASSET: oversized_complete_payload.bin"
                        when (selectedMode) {
                            ParserMode.SAFE -> {
                                incomingCallEvents = listOf("CALL_INCOMING")
                                val packet = context.assets
                                    .open("oversized_complete_payload.bin")
                                    .use { input -> input.readBytes() }
                                incomingCallEvents += "CONTROL_PACKET_RECEIVED"
                                incomingCallEvents += "NATIVE_PARSE_STARTED"
                                incomingCallResult = NativeBridge.parsePacket(packet)
                                incomingCallEvents +=
                                    "PACKET_REJECTED_INVALID_LENGTH"
                            }
                            ParserMode.VULNERABLE -> {
                                val packet = context.assets
                                    .open("oversized_complete_payload.bin")
                                    .use { input -> input.readBytes() }
                                incomingCallResult = "Automatic native parsing started"
                                coroutineScope.launch {
                                    incomingCallEvents = listOf("CALL_INCOMING")
                                    delay(250L)
                                    incomingCallEvents = listOf(
                                        "CALL_INCOMING",
                                        "CONTROL_PACKET_RECEIVED",
                                    )
                                    delay(250L)
                                    incomingCallEvents += "NATIVE_PARSE_STARTED"
                                    delay(250L)
                                    incomingCallResult =
                                        NativeBridge.parsePacketVulnerable(packet)
                                }
                            }
                        }
                    },
                ) {
                    Text("Simulate incoming call")
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
                    Text("AUTOMATIC RESULT (${incomingCallMode.name})")
                    Text(incomingCallResult)
                    Text("EVENTS")
                    incomingCallEvents.forEach { event -> Text(event) }
                }
                Button(
                    onClick = {
                        safeResult = context.assets
                            .open("valid_call_control.bin")
                            .use { input ->
                                NativeBridge.parsePacket(input.readBytes())
                            }
                    },
                ) {
                    Text(text = "Parse valid sample")
                }
                Text(text = "SAFE")
                Text(text = safeResult)
                Button(
                    onClick = {
                        vulnerableResult = context.assets
                            .open("valid_call_control.bin")
                            .use { input ->
                                NativeBridge.parsePacketVulnerable(input.readBytes())
                            }
                    },
                ) {
                    Text(text = "Parse valid sample (vulnerable)")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "VULNERABLE")
                Text(text = vulnerableResult)
            }
        }
    }
}
