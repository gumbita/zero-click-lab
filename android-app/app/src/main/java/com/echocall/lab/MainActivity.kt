package com.echocall.lab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.echocall.lab.ui.EchoCallApp
import kotlinx.coroutines.channels.Channel

private const val UDP_PACKET_QUEUE_CAPACITY = 16

internal data class UdpPacketEvent(
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
            EchoCallApp(
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
