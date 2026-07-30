package com.echocall.lab

import android.os.Bundle
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class ParserMode {
    SAFE,
    VULNERABLE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nativeStatus = NativeBridge.nativeStatus()
        setContent {
            EchoCallLabScreen(nativeStatus)
        }
    }
}

@Composable
private fun EchoCallLabScreen(nativeStatus: String) {
    val context = LocalContext.current
    var safeResult by remember { mutableStateOf("Valid sample not parsed") }
    var vulnerableResult by remember { mutableStateOf("Valid sample not parsed") }
    var selectedMode by remember { mutableStateOf(ParserMode.SAFE) }
    var showIncomingCall by remember { mutableStateOf(false) }
    var incomingCallResult by remember { mutableStateOf("Incoming call not simulated") }
    var incomingCallEvents by remember { mutableStateOf(emptyList<String>()) }
    var callAction by remember { mutableStateOf("No user action required") }
    val coroutineScope = rememberCoroutineScope()

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
                    text = "EchoCall Lab",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(text = nativeStatus)
                Text("Incoming parser mode")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selectedMode == ParserMode.SAFE,
                        onClick = { selectedMode = ParserMode.SAFE },
                    )
                    Text("SAFE")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = selectedMode == ParserMode.VULNERABLE,
                        onClick = { selectedMode = ParserMode.VULNERABLE },
                    )
                    Text("VULNERABLE")
                }
                Button(
                    onClick = {
                        showIncomingCall = true
                        callAction = "No user action required"
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
                    Text("AUTOMATIC RESULT (${selectedMode.name})")
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
