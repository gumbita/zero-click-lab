package com.echocall.lab.ui

import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.echocall.lab.NativeBridge
import com.echocall.lab.UDP_LOG_TAG
import com.echocall.lab.UdpPacketEvent
import com.echocall.lab.navigation.EchoCallNavHost
import com.echocall.lab.ui.lab.LabModeUiState
import com.echocall.lab.ui.state.rememberEchoCallStateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext

@Composable
internal fun EchoCallApp(
    nativeStatus: String,
    compiledParserImplementation: String,
    udpReceiverStatus: String,
    udpRetryAvailable: Boolean,
    udpPacketEvents: Channel<UdpPacketEvent>,
    onRetryUdpReceiver: () -> Unit,
) {
    val context = LocalContext.current
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
            Log.i(UDP_LOG_TAG, "CALL_INCOMING")
            Log.i(UDP_LOG_TAG, "CONTROL_PACKET_RECEIVED")
            Log.i(UDP_LOG_TAG, "NATIVE_PARSE_STARTED")

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
                } else {
                    incomingCallEvents += "PACKET_REJECTED"
                    callAction = "Rejected packet retained in Lab"
                    Log.i(UDP_LOG_TAG, "PACKET_REJECTED")
                }
            } catch (_: RuntimeException) {
                incomingCallResult = "status=error code=udp_parse_failed"
                incomingCallEvents += "NATIVE_PARSE_ERROR"
                callAction = "Native parsing failed"
                Log.e(UDP_LOG_TAG, "NATIVE_PARSE_ERROR")
            }
        }
    }

    MaterialTheme {
        EchoCallNavHost(
            productUiState = productStateHolder.uiState,
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
            ),
            onSendMessage = productStateHolder::sendMessage,
            onResetData = productStateHolder::resetSimulatedData,
            onStartOutgoingCall = productStateHolder::startOutgoingCall,
            onActivateOutgoingCall = productStateHolder::activateOutgoingCall,
            onCancelOutgoingCall = productStateHolder::cancelOutgoingCall,
            onAcceptIncomingCall = productStateHolder::acceptIncomingCall,
            onRejectIncomingCall = productStateHolder::rejectIncomingCall,
            onEndActiveCall = productStateHolder::endActiveCall,
            onRetryUdpReceiver = onRetryUdpReceiver,
            onProcessValidSample = {
                validSampleResult = context.assets
                    .open("valid_call_control.bin")
                    .use { input ->
                        NativeBridge.parsePacket(input.readBytes())
                    }
            },
        )
    }
}
