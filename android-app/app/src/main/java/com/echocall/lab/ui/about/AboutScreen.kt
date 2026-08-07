package com.echocall.lab.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acerca de") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Atrás"
                        },
                    ) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "EchoCall Lab",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Aplicación de laboratorio local con un escenario " +
                    "simulado de mensajería y llamadas.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "No utiliza WhatsApp real, contactos reales ni un " +
                    "backend. Tampoco realiza llamadas reales.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Escenario inspirado en patrones de procesamiento " +
                    "automático estudiados en vulnerabilidades zero-click.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
