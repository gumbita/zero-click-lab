package com.echocall.lab.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.echocall.lab.ui.about.AboutScreen
import com.echocall.lab.ui.calls.CallHistoryScreen
import com.echocall.lab.ui.chat.ChatScreen
import com.echocall.lab.ui.conversations.ConversationsScreen
import com.echocall.lab.ui.lab.LabModeScreen
import com.echocall.lab.ui.lab.LabModeUiState
import com.echocall.lab.ui.state.EchoCallUiState

@Composable
fun EchoCallNavHost(
    productUiState: EchoCallUiState,
    labModeUiState: LabModeUiState,
    onSendMessage: (String, String) -> Unit,
    onResetData: () -> Unit,
    onRetryUdpReceiver: () -> Unit,
    onAcceptIncoming: () -> Unit,
    onRejectIncoming: () -> Unit,
    onProcessValidSample: () -> Unit,
) {
    val navController = rememberNavController()

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
                onAcceptIncoming = onAcceptIncoming,
                onRejectIncoming = onRejectIncoming,
                onProcessValidSample = onProcessValidSample,
            )
        }
        composable(EchoCallDestination.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
