/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

package com.echocall.lab.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.echocall.lab.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acerca de") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Volver",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "EchoCall Lab",
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineMedium,
            )
            AboutSection(title = "Laboratorio local") {
                Text(
                    text = "Aplicación local con un escenario simulado de " +
                        "mensajería y llamadas.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "No utiliza contactos reales, backend, telefonía " +
                        "real ni audio.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            AboutSection(title = "Alcance técnico") {
                Text(
                    text = "ECLB es un formato sintético. El escenario está " +
                        "inspirado en patrones de procesamiento automático " +
                        "estudiados en vulnerabilidades zero-click.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "EchoCall no reproduce WhatsApp ni acredita una " +
                        "explotación o ejecución remota de código.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun AboutSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleMedium,
            )
            content()
        }
    }
}
