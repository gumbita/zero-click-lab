package com.echocall.lab.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.echocall.lab.ui.about.AboutScreen
import com.echocall.lab.ui.calls.ActiveCallScreen
import com.echocall.lab.ui.calls.BlockedCallScreen
import com.echocall.lab.ui.calls.CallHistoryScreen
import com.echocall.lab.ui.calls.IncomingCallScreen
import com.echocall.lab.ui.calls.OutgoingCallScreen
import com.echocall.lab.ui.chat.ChatScreen
import com.echocall.lab.ui.conversations.ConversationsScreen
import com.echocall.lab.ui.lab.LabModeScreen
import com.echocall.lab.ui.lab.LabModeUiState
import com.echocall.lab.model.CallPhase
import com.echocall.lab.ui.state.EchoCallUiState

@Composable
fun EchoCallNavHost(
    productUiState: EchoCallUiState,
    labModeUiState: LabModeUiState,
    onSendMessage: (String, String) -> Unit,
    onResetData: () -> Unit,
    onStartOutgoingCall: (String) -> Boolean,
    onActivateOutgoingCall: () -> Unit,
    onCancelOutgoingCall: () -> Unit,
    onAcceptIncomingCall: () -> Unit,
    onRejectIncomingCall: () -> Unit,
    onEndActiveCall: () -> Unit,
    onCloseBlockedCall: () -> Unit,
    onRetryUdpReceiver: () -> Unit,
    onProcessValidSample: () -> Unit,
) {
    val navController = rememberNavController()
    val currentCall = productUiState.currentCall
    val blockedCallAttempt = productUiState.blockedCallAttempt

    LaunchedEffect(blockedCallAttempt?.id) {
        if (blockedCallAttempt != null) {
            navController.navigate(EchoCallDestination.BLOCKED_CALL) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(currentCall?.id, currentCall?.phase) {
        when (currentCall?.phase) {
            CallPhase.OUTGOING -> navController.navigate(
                EchoCallDestination.OUTGOING_CALL,
            ) {
                launchSingleTop = true
            }

            CallPhase.INCOMING -> navController.navigate(
                EchoCallDestination.INCOMING_CALL,
            ) {
                launchSingleTop = true
            }

            CallPhase.ACTIVE -> navController.navigate(
                EchoCallDestination.ACTIVE_CALL,
            ) {
                launchSingleTop = true
            }

            null -> Unit
        }
    }

    NavHost(
        navController = navController,
        startDestination = EchoCallDestination.CONVERSATIONS,
    ) {
        composable(EchoCallDestination.CONVERSATIONS) {
            ConversationsScreen(
                contacts = productUiState.contacts,
                onContactClick = { contactId ->
                    navController.navigate(EchoCallDestination.chat(contactId))
                },
                onOpenCallHistory = {
                    navController.navigate(EchoCallDestination.CALL_HISTORY)
                },
                onOpenLab = {
                    navController.navigate(EchoCallDestination.LAB)
                },
                onOpenAbout = {
                    navController.navigate(EchoCallDestination.ABOUT)
                },
                onResetData = onResetData,
                resetEnabled = currentCall == null && blockedCallAttempt == null,
            )
        }
        composable(
            route = EchoCallDestination.CHAT,
            arguments = listOf(
                navArgument("contactId") {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId")
            val contact = contactId?.let(productUiState::contact)
            if (contact != null) {
                ChatScreen(
                    contact = contact,
                    messages = productUiState.messagesFor(contact.id),
                    onSendMessage = { text ->
                        onSendMessage(contact.id, text)
                    },
                    onStartCall = {
                        onStartOutgoingCall(contact.id)
                    },
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(EchoCallDestination.CALL_HISTORY) {
            CallHistoryScreen(
                records = productUiState.callHistory,
                contacts = productUiState.contacts,
                onBack = { navController.popBackStack() },
            )
        }
        composable(EchoCallDestination.LAB) {
            LabModeScreen(
                state = labModeUiState,
                onBack = { navController.popBackStack() },
                onRetryUdpReceiver = onRetryUdpReceiver,
                onProcessValidSample = onProcessValidSample,
            )
        }
        composable(EchoCallDestination.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable(EchoCallDestination.OUTGOING_CALL) {
            val call = currentCall?.takeIf {
                it.phase == CallPhase.OUTGOING
            }
            val contact = call?.let { productUiState.contact(it.contactId) }
            if (call != null && contact != null) {
                OutgoingCallScreen(
                    callId = call.id,
                    contact = contact,
                    onConnected = onActivateOutgoingCall,
                    onCancel = {
                        onCancelOutgoingCall()
                        navController.popBackStack()
                    },
                )
            }
        }
        composable(EchoCallDestination.INCOMING_CALL) {
            val call = currentCall?.takeIf {
                it.phase == CallPhase.INCOMING
            }
            val contact = call?.let { productUiState.contact(it.contactId) }
            if (call != null && contact != null) {
                IncomingCallScreen(
                    callId = call.id,
                    contact = contact,
                    onAccept = onAcceptIncomingCall,
                    onReject = {
                        onRejectIncomingCall()
                        navController.returnToConversations()
                    },
                )
            }
        }
        composable(EchoCallDestination.ACTIVE_CALL) {
            val call = currentCall?.takeIf {
                it.phase == CallPhase.ACTIVE
            }
            val contact = call?.let { productUiState.contact(it.contactId) }
            if (call != null && contact != null) {
                ActiveCallScreen(
                    call = call,
                    contact = contact,
                    onEnd = {
                        onEndActiveCall()
                        navController.returnToConversations()
                    },
                )
            }
        }
        composable(EchoCallDestination.BLOCKED_CALL) {
            val attempt = blockedCallAttempt
            val contact = attempt?.let {
                productUiState.contact(it.contactId)
            }
            if (attempt != null && contact != null) {
                BlockedCallScreen(
                    attemptId = attempt.id,
                    contact = contact,
                    onClose = {
                        onCloseBlockedCall()
                        navController.returnToConversations()
                    },
                )
            }
        }
    }
}

private fun androidx.navigation.NavHostController.returnToConversations() {
    navigate(EchoCallDestination.CONVERSATIONS) {
        popUpTo(EchoCallDestination.CONVERSATIONS) {
            inclusive = false
        }
        launchSingleTop = true
    }
}
