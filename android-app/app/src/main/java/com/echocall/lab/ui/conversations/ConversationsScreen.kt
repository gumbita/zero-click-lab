package com.echocall.lab.ui.conversations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.echocall.lab.model.Contact
import com.echocall.lab.ui.ContactAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    contacts: List<Contact>,
    onContactClick: (String) -> Unit,
    onOpenCallHistory: () -> Unit,
    onOpenLab: () -> Unit,
    onOpenAbout: () -> Unit,
    onResetData: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "EchoCall",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                actions = {
                    TextButton(onClick = onOpenCallHistory) {
                        Text("Llamadas")
                    }
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.semantics {
                            contentDescription = "Más opciones"
                        },
                    ) {
                        Text(
                            text = "⋮",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Modo Lab") },
                            onClick = {
                                menuExpanded = false
                                onOpenLab()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Acerca de") },
                            onClick = {
                                menuExpanded = false
                                onOpenAbout()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Restablecer datos") },
                            onClick = {
                                menuExpanded = false
                                showResetDialog = true
                            },
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            items(
                items = contacts,
                key = { contact -> contact.id },
            ) { contact ->
                ConversationRow(
                    contact = contact,
                    onClick = { onContactClick(contact.id) },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 80.dp))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Restablecer datos") },
            text = {
                Text(
                    "Se restaurarán las conversaciones y el historial " +
                        "simulados de EchoCall.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetData()
                        showResetDialog = false
                    },
                ) {
                    Text("Restablecer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun ConversationRow(
    contact: Contact,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ContactAvatar(contact = contact)
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = contact.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = contact.preview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = contact.timestamp,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
