package com.echocall.lab.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.echocall.lab.data.FakeEchoCallData
import com.echocall.lab.model.CallRecord
import com.echocall.lab.model.Contact
import com.echocall.lab.model.Message
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class EchoCallUiState(
    val contacts: List<Contact>,
    val messages: List<Message>,
    val callHistory: List<CallRecord>,
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
        uiState = createInitialUiState()
        nextLocalMessageId = 1L
    }
}

@Composable
fun rememberEchoCallStateHolder(): EchoCallStateHolder =
    remember { EchoCallStateHolder() }

private fun createInitialUiState(): EchoCallUiState = EchoCallUiState(
    contacts = FakeEchoCallData.contacts.map { contact -> contact.copy() },
    messages = FakeEchoCallData.messages.map { message -> message.copy() },
    callHistory = FakeEchoCallData.callRecords.map { record -> record.copy() },
)

private val MESSAGE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
