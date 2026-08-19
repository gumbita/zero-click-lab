/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

package com.echocall.lab.ui.state

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.echocall.lab.data.FakeEchoCallData
import com.echocall.lab.model.CallDirection
import com.echocall.lab.model.CallOutcome
import com.echocall.lab.model.CallPhase
import com.echocall.lab.model.CallRecord
import com.echocall.lab.model.BlockedCallAttempt
import com.echocall.lab.model.Contact
import com.echocall.lab.model.CurrentCall
import com.echocall.lab.model.Message
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class EchoCallUiState(
    val contacts: List<Contact>,
    val messages: List<Message>,
    val callHistory: List<CallRecord>,
    val currentCall: CurrentCall?,
    val blockedCallAttempt: BlockedCallAttempt?,
) {
    fun contact(contactId: String): Contact? =
        contacts.firstOrNull { contact -> contact.id == contactId }

    fun messagesFor(contactId: String): List<Message> =
        messages.filter { message -> message.contactId == contactId }
}

@Stable
class EchoCallStateHolder {
    var uiState by mutableStateOf(createInitialUiState())
        private set

    private var nextLocalMessageId = 1L
    private var nextLocalCallId = 1L

    fun sendMessage(
        contactId: String,
        rawText: String,
    ) {
        val text = rawText.trim()
        if (text.isEmpty()) {
            return
        }

        val contact = uiState.contact(contactId) ?: return
        val timestamp = LocalTime.now().format(MESSAGE_TIME_FORMATTER)
        val message = Message(
            id = "local_${nextLocalMessageId++}",
            contactId = contactId,
            text = text,
            isOutgoing = true,
            timestamp = timestamp,
        )
        val updatedContact = contact.copy(
            preview = text,
            timestamp = timestamp,
        )

        uiState = uiState.copy(
            contacts = buildList {
                add(updatedContact)
                addAll(
                    uiState.contacts.filterNot { existing ->
                        existing.id == contactId
                    },
                )
            },
            messages = uiState.messages + message,
        )
    }

    fun resetSimulatedData() {
        if (uiState.currentCall != null || uiState.blockedCallAttempt != null) {
            return
        }

        uiState = createInitialUiState()
        nextLocalMessageId = 1L
        nextLocalCallId = 1L
    }

    fun startOutgoingCall(contactId: String): Boolean {
        if (uiState.currentCall != null || uiState.contact(contactId) == null) {
            return false
        }

        uiState = uiState.copy(
            currentCall = CurrentCall(
                id = nextCallId(),
                contactId = contactId,
                direction = CallDirection.OUTGOING,
                phase = CallPhase.OUTGOING,
            ),
        )
        return true
    }

    fun activateOutgoingCall() {
        val call = uiState.currentCall ?: return
        if (
            call.direction != CallDirection.OUTGOING ||
            call.phase != CallPhase.OUTGOING
        ) {
            return
        }

        activateCall(call)
    }

    fun startIncomingCallFromAcceptedUdp(): Boolean {
        if (uiState.currentCall != null || uiState.blockedCallAttempt != null) {
            return false
        }

        // ECLB carries no contact name; Marta is a deterministic simulator mapping.
        val contactId = FakeEchoCallData.UDP_SCENARIO_CONTACT_ID
        if (uiState.contact(contactId) == null) {
            return false
        }

        uiState = uiState.copy(
            currentCall = CurrentCall(
                id = nextCallId(),
                contactId = contactId,
                direction = CallDirection.INCOMING,
                phase = CallPhase.INCOMING,
            ),
        )
        return true
    }

    fun recordBlockedIncomingCallFromRejectedUdp(): Boolean {
        if (uiState.currentCall != null || uiState.blockedCallAttempt != null) {
            return false
        }

        // ECLB carries no contact name; Marta is a deterministic simulator mapping.
        val contactId = FakeEchoCallData.UDP_SCENARIO_CONTACT_ID
        if (uiState.contact(contactId) == null) {
            return false
        }

        val attempt = BlockedCallAttempt(
            id = nextCallId(),
            contactId = contactId,
        )
        val record = CallRecord(
            id = "${attempt.id}_${CallOutcome.BLOCKED}",
            contactId = contactId,
            direction = CallDirection.INCOMING,
            outcome = CallOutcome.BLOCKED,
            timestamp = "Hoy, ${currentTime()}",
        )
        uiState = uiState.copy(
            callHistory = listOf(record) + uiState.callHistory,
            blockedCallAttempt = attempt,
        )
        return true
    }

    fun clearBlockedCallAttempt() {
        if (uiState.blockedCallAttempt == null) {
            return
        }

        uiState = uiState.copy(blockedCallAttempt = null)
    }

    fun acceptIncomingCall() {
        val call = uiState.currentCall ?: return
        if (
            call.direction != CallDirection.INCOMING ||
            call.phase != CallPhase.INCOMING
        ) {
            return
        }

        activateCall(call)
    }

    fun rejectIncomingCall() {
        val call = uiState.currentCall ?: return
        if (
            call.direction != CallDirection.INCOMING ||
            call.phase != CallPhase.INCOMING
        ) {
            return
        }

        finishCall(call, CallOutcome.REJECTED)
    }

    fun cancelOutgoingCall() {
        val call = uiState.currentCall ?: return
        if (
            call.direction != CallDirection.OUTGOING ||
            call.phase != CallPhase.OUTGOING
        ) {
            return
        }

        finishCall(call, CallOutcome.CANCELLED)
    }

    fun endActiveCall() {
        val call = uiState.currentCall ?: return
        if (call.phase != CallPhase.ACTIVE) {
            return
        }

        finishCall(call, CallOutcome.COMPLETED)
    }

    private fun activateCall(call: CurrentCall) {
        uiState = uiState.copy(
            currentCall = call.copy(
                phase = CallPhase.ACTIVE,
                activeStartedAtElapsedRealtime = SystemClock.elapsedRealtime(),
            ),
        )
    }

    private fun finishCall(
        call: CurrentCall,
        outcome: CallOutcome,
    ) {
        val record = CallRecord(
            id = "${call.id}_$outcome",
            contactId = call.contactId,
            direction = call.direction,
            outcome = outcome,
            timestamp = "Hoy, ${currentTime()}",
        )
        uiState = uiState.copy(
            callHistory = listOf(record) + uiState.callHistory,
            currentCall = null,
        )
    }

    private fun nextCallId(): String = "session_call_${nextLocalCallId++}"

    private fun currentTime(): String =
        LocalTime.now().format(MESSAGE_TIME_FORMATTER)
}

@Composable
fun rememberEchoCallStateHolder(): EchoCallStateHolder =
    remember { EchoCallStateHolder() }

private fun createInitialUiState(): EchoCallUiState = EchoCallUiState(
    contacts = FakeEchoCallData.contacts.map { contact -> contact.copy() },
    messages = FakeEchoCallData.messages.map { message -> message.copy() },
    callHistory = FakeEchoCallData.callRecords.map { record -> record.copy() },
    currentCall = null,
    blockedCallAttempt = null,
)

private val MESSAGE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
