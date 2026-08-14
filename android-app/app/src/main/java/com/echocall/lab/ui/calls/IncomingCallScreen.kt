package com.echocall.lab.ui.calls

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.echocall.lab.R
import com.echocall.lab.model.Contact
import com.echocall.lab.UDP_LOG_TAG

@Composable
fun IncomingCallScreen(
    callId: String,
    contact: Contact,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    BackHandler(enabled = true) { }

    LaunchedEffect(callId) {
        Log.i(
            UDP_LOG_TAG,
            "IncomingCallScreen presented after accepted parser result",
        )
    }

    CallScreenLayout(
        contact = contact,
        headline = "Llamada entrante",
    ) {
        Button(
            onClick = onAccept,
            modifier = Modifier
                .widthIn(min = 200.dp, max = 320.dp)
                .heightIn(min = 52.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_call),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Aceptar")
        }
        OutlinedButton(
            onClick = onReject,
            modifier = Modifier
                .widthIn(min = 200.dp, max = 320.dp)
                .heightIn(min = 52.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_call_end),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Rechazar")
        }
    }
}
