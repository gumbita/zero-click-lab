package com.echocall.lab.data

import com.echocall.lab.model.CallDirection
import com.echocall.lab.model.CallOutcome
import com.echocall.lab.model.CallRecord
import com.echocall.lab.model.Contact
import com.echocall.lab.model.Message

object FakeEchoCallData {
    const val UDP_SCENARIO_CONTACT_ID = "marta_soler"

    val contacts = listOf(
        Contact(
            id = "marta_soler",
            displayName = "Marta Soler",
            initials = "MS",
            preview = "¿Puedes hablar ahora?",
            timestamp = "09:42",
        ),
        Contact(
            id = "pau_ferrer",
            displayName = "Pau Ferrer",
            initials = "PF",
            preview = "Vale, luego te llamo",
            timestamp = "Ayer",
        ),
        Contact(
            id = "lucia_navarro",
            displayName = "Lucía Navarro",
            initials = "LN",
            preview = "Perfecto 👍",
            timestamp = "Ayer",
        ),
        Contact(
            id = "dani_campos",
            displayName = "Dani Campos",
            initials = "DC",
            preview = "Llamada perdida",
            timestamp = "Lun",
        ),
        Contact(
            id = "irene_vidal",
            displayName = "Irene Vidal",
            initials = "IV",
            preview = "Nos vemos luego",
            timestamp = "Dom",
        ),
    )

    val messages = listOf(
        Message(
            id = "marta_1",
            contactId = "marta_soler",
            text = "Hola, ¿tienes un momento?",
            isOutgoing = false,
            timestamp = "09:38",
        ),
        Message(
            id = "marta_2",
            contactId = "marta_soler",
            text = "Sí, dime",
            isOutgoing = true,
            timestamp = "09:40",
        ),
        Message(
            id = "marta_3",
            contactId = "marta_soler",
            text = "¿Puedes hablar ahora?",
            isOutgoing = false,
            timestamp = "09:42",
        ),
        Message(
            id = "pau_1",
            contactId = "pau_ferrer",
            text = "¿Te va bien esta tarde?",
            isOutgoing = true,
            timestamp = "18:01",
        ),
        Message(
            id = "pau_2",
            contactId = "pau_ferrer",
            text = "Vale, luego te llamo",
            isOutgoing = false,
            timestamp = "18:04",
        ),
        Message(
            id = "lucia_1",
            contactId = "lucia_navarro",
            text = "Te envío el resumen ahora.",
            isOutgoing = true,
            timestamp = "17:22",
        ),
        Message(
            id = "lucia_2",
            contactId = "lucia_navarro",
            text = "Perfecto 👍",
            isOutgoing = false,
            timestamp = "17:24",
        ),
        Message(
            id = "dani_1",
            contactId = "dani_campos",
            text = "Te llamo en cuanto pueda.",
            isOutgoing = false,
            timestamp = "16:10",
        ),
        Message(
            id = "irene_1",
            contactId = "irene_vidal",
            text = "Nos vemos luego",
            isOutgoing = false,
            timestamp = "12:15",
        ),
    )

    val callRecords = listOf(
        CallRecord(
            id = "call_marta",
            contactId = "marta_soler",
            direction = CallDirection.INCOMING,
            outcome = CallOutcome.COMPLETED,
            timestamp = "Hoy, 09:18",
        ),
        CallRecord(
            id = "call_dani",
            contactId = "dani_campos",
            direction = CallDirection.INCOMING,
            outcome = CallOutcome.MISSED,
            timestamp = "Ayer, 18:42",
        ),
        CallRecord(
            id = "call_pau",
            contactId = "pau_ferrer",
            direction = CallDirection.OUTGOING,
            outcome = CallOutcome.COMPLETED,
            timestamp = "Ayer, 17:05",
        ),
        CallRecord(
            id = "call_irene",
            contactId = "irene_vidal",
            direction = CallDirection.INCOMING,
            outcome = CallOutcome.REJECTED,
            timestamp = "Lun, 12:31",
        ),
    )

    fun contact(contactId: String): Contact? =
        contacts.firstOrNull { it.id == contactId }

    fun messagesFor(contactId: String): List<Message> =
        messages.filter { it.contactId == contactId }
}
