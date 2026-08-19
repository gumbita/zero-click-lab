/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

package com.echocall.lab

import android.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException

internal const val UDP_LOG_TAG = "EchoCallUDP"

internal data class ReceivedUdpPacket(
    val bytes: ByteArray,
    val sourceAddress: String,
    val sourcePort: Int,
)

internal class UdpPacketReceiver(
    private val localPort: Int = LAB_PORT,
    private val onListening: (Int) -> Unit,
    private val onPacket: (ReceivedUdpPacket) -> Unit,
    private val onError: (Throwable) -> Unit,
) {
    private val lock = Any()
    private val executor = Executors.newSingleThreadExecutor { task ->
        Thread(task, "EchoCall-UDP-$localPort").apply {
            isDaemon = true
        }
    }

    private var requestedRunning = false
    private var workerScheduled = false
    private var restartPending = false
    private var closed = false
    private var socket: DatagramSocket? = null

    fun start() {
        var scheduleWorker = false

        synchronized(lock) {
            Log.i(
                UDP_LOG_TAG,
                "Receiver start requested ${stateDescriptionLocked()}",
            )
            if (closed) {
                return
            }

            requestedRunning = true
            if (!workerScheduled) {
                workerScheduled = true
                scheduleWorker = true
            } else if (socket == null) {
                restartPending = true
                Log.i(
                    UDP_LOG_TAG,
                    "Worker already scheduled; restart pending " +
                        stateDescriptionLocked(),
                )
            } else {
                Log.i(
                    UDP_LOG_TAG,
                    "Worker already scheduled ${stateDescriptionLocked()}",
                )
            }
        }

        if (scheduleWorker) {
            scheduleWorker()
        }
    }

    fun retry() {
        val activeSocket: DatagramSocket?
        var scheduleWorker = false

        synchronized(lock) {
            Log.i(
                UDP_LOG_TAG,
                "Receiver retry requested ${stateDescriptionLocked()}",
            )
            if (closed) {
                return
            }

            requestedRunning = true
            activeSocket = socket
            socket = null
            if (workerScheduled) {
                restartPending = true
                Log.i(
                    UDP_LOG_TAG,
                    "Worker already scheduled; restart pending " +
                        stateDescriptionLocked(),
                )
            } else {
                workerScheduled = true
                restartPending = false
                scheduleWorker = true
            }
        }

        activeSocket?.close()
        if (scheduleWorker) {
            scheduleWorker()
        }
    }

    fun stop() {
        val activeSocket: DatagramSocket?
        val workerWasScheduled: Boolean

        synchronized(lock) {
            Log.i(
                UDP_LOG_TAG,
                "Receiver stop requested ${stateDescriptionLocked()}",
            )
            requestedRunning = false
            restartPending = false
            activeSocket = socket
            socket = null
            workerWasScheduled = workerScheduled
        }

        activeSocket?.close()
        if (!workerWasScheduled) {
            logState("Receiver stopped")
        }
    }

    fun close() {
        val activeSocket: DatagramSocket?

        synchronized(lock) {
            if (closed) {
                return
            }

            closed = true
            requestedRunning = false
            workerScheduled = false
            restartPending = false
            activeSocket = socket
            socket = null
        }

        activeSocket?.close()
        executor.shutdownNow()
        logState("Receiver closed")
    }

    private fun scheduleWorker() {
        logState("Worker scheduled")
        try {
            executor.execute(::receiveLoop)
        } catch (error: RejectedExecutionException) {
            val reportFailure = synchronized(lock) {
                workerScheduled = false
                requestedRunning = false
                restartPending = false
                !closed
            }
            if (reportFailure) {
                reportError(error)
            }
        }
    }

    private fun receiveLoop() {
        var activeSocket: DatagramSocket? = null
        var terminalFailure = false

        logState("Worker started")
        try {
            val boundSocket = DatagramSocket(null)
            try {
                boundSocket.bind(InetSocketAddress(localPort))
                require(boundSocket.localPort == localPort) {
                    "UDP socket bound to unexpected port: " +
                        "expected=$localPort actual=${boundSocket.localPort}"
                }
            } catch (error: IOException) {
                boundSocket.close()
                throw error
            } catch (error: RuntimeException) {
                boundSocket.close()
                throw error
            }
            activeSocket = boundSocket

            if (!publishSocket(boundSocket)) {
                return
            }

            Log.i(UDP_LOG_TAG, "Receiver started")
            Log.i(
                UDP_LOG_TAG,
                "Socket bound on UDP port ${boundSocket.localPort}",
            )
            onListening(boundSocket.localPort)

            val buffer = ByteArray(MAX_UDP_PAYLOAD_SIZE)
            val datagram = DatagramPacket(buffer, buffer.size)

            while (isCurrentSocket(activeSocket)) {
                datagram.length = buffer.size
                activeSocket.receive(datagram)

                if (!isCurrentSocket(activeSocket)) {
                    break
                }

                try {
                    processDatagram(datagram)
                } catch (error: RuntimeException) {
                    reportError(error)
                }
            }
        } catch (error: IOException) {
            if (shouldReportFailure(activeSocket)) {
                terminalFailure = true
                reportError(error)
            }
        } catch (error: RuntimeException) {
            if (shouldReportFailure(activeSocket)) {
                terminalFailure = true
                reportError(error)
            }
        } finally {
            logState("Worker finishing")
            activeSocket?.close()
            var scheduleRestart = false
            synchronized(lock) {
                if (socket === activeSocket) {
                    socket = null
                }
                workerScheduled = false
                if (terminalFailure && !restartPending) {
                    requestedRunning = false
                }
                if (requestedRunning && !closed) {
                    restartPending = false
                    workerScheduled = true
                    scheduleRestart = true
                } else {
                    restartPending = false
                    requestedRunning = false
                }
            }
            if (scheduleRestart) {
                logState("Restart scheduled")
                scheduleWorker()
            } else {
                logState("Receiver stopped")
            }
        }
    }

    private fun logState(message: String) {
        val state = synchronized(lock) {
            stateDescriptionLocked()
        }
        Log.i(UDP_LOG_TAG, "$message $state")
    }

    private fun stateDescriptionLocked(): String =
        "requestedRunning=$requestedRunning " +
            "workerScheduled=$workerScheduled " +
            "restartPending=$restartPending closed=$closed"

    private fun publishSocket(boundSocket: DatagramSocket): Boolean =
        synchronized(lock) {
            if (closed || !requestedRunning || restartPending) {
                false
            } else {
                socket = boundSocket
                true
            }
        }

    private fun isCurrentSocket(activeSocket: DatagramSocket): Boolean =
        synchronized(lock) {
            !closed &&
                requestedRunning &&
                !restartPending &&
                socket === activeSocket
        }

    private fun shouldReportFailure(activeSocket: DatagramSocket?): Boolean =
        synchronized(lock) {
            !closed &&
                requestedRunning &&
                !restartPending &&
                (activeSocket == null || socket === activeSocket)
        }

    private fun processDatagram(datagram: DatagramPacket) {
        val remoteAddress = datagram.address
            ?: throw IllegalStateException("Received datagram has no source address")
        val remotePort = datagram.port
        if (remotePort !in MIN_UDP_PORT..MAX_UDP_PORT) {
            throw IllegalArgumentException(
                "Received datagram has invalid source port: $remotePort",
            )
        }
        val remoteSocketAddress = datagram.socketAddress

        val payloadOffset = datagram.offset
        val payloadLength = datagram.length
        if (
            payloadOffset < 0 ||
            payloadLength < 0 ||
            payloadOffset > datagram.data.size - payloadLength
        ) {
            throw IllegalArgumentException(
                "Received datagram has invalid payload bounds: " +
                    "offset=$payloadOffset length=$payloadLength " +
                    "capacity=${datagram.data.size}",
            )
        }
        val payload = datagram.data.copyOfRange(
            payloadOffset,
            payloadOffset + payloadLength,
        )
        val sourceAddress = remoteAddress.hostAddress ?: remoteAddress.hostName

        Log.i(
            UDP_LOG_TAG,
            "Datagram received from $remoteSocketAddress length=$payloadLength",
        )
        onPacket(
            ReceivedUdpPacket(
                bytes = payload,
                sourceAddress = sourceAddress,
                sourcePort = remotePort,
            ),
        )
    }

    private fun reportError(error: Throwable) {
        Log.e(UDP_LOG_TAG, "Receiver error", error)
        try {
            onError(error)
        } catch (callbackError: RuntimeException) {
            Log.e(UDP_LOG_TAG, "Error callback failed", callbackError)
        }
    }

    companion object {
        const val LAB_PORT = 43568
        private const val MIN_UDP_PORT = 0
        private const val MAX_UDP_PORT = 65_535
        private const val MAX_UDP_PAYLOAD_SIZE = 65_507
    }
}
