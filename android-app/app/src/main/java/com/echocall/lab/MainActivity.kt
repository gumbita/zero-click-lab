package com.echocall.lab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

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

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "EchoCall Lab",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = nativeStatus)
                Spacer(modifier = Modifier.height(24.dp))
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "SAFE")
                Text(text = safeResult)
                Spacer(modifier = Modifier.height(24.dp))
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
