package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.CaptionOverlay
import com.example.myapplication.ui.components.MessageBubble
import com.example.myapplication.viewmodel.VoxBridgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoModeScreen(
    viewModel: VoxBridgeViewModel,
    onExitDemo: () -> Unit
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val demoStepIndex by viewModel.demoStepIndex.collectAsState()
    val isNetworkInterrupted by viewModel.isNetworkInterrupted.collectAsState()
    val userLang by viewModel.preferredLanguage.collectAsState()
    val userId by viewModel.participantId.collectAsState()

    // Initialize demo environment on launch
    LaunchedEffect(Unit) {
        viewModel.startDemoMode(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Interactive Demo Mode", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("4 Synthetic Participants (TA, EN, HI, ES)", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onExitDemo) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Demo")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Participant Badge Summary Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    participants.forEach { p ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = p.name.split(" ")[0],
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "(${p.preferredLanguage.uppercase()})",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // Simulated Network Interruption Warning Banner
            AnimatedVisibility(visible = isNetworkInterrupted) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Simulated Network Interruption! Message buffering enabled...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Live Caption Card
            if (messages.isNotEmpty()) {
                val lastMsg = messages.last()
                CaptionOverlay(
                    text = lastMsg.getTranslationFor(userLang),
                    speakerName = lastMsg.senderName
                )
            }

            // Messages Stream
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            currentUserId = userId,
                            userPreferredLanguage = userLang,
                            onSpeakClick = { viewModel.speakMessage(it) },
                            onRetryClick = { viewModel.retryFailedMessage(it) }
                        )
                    }
                }
            }

            // Demo Navigation Bar
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.toggleDemoNetworkInterruption() }
                        ) {
                            Text(if (isNetworkInterrupted) "Restore Network" else "Simulate Disconnect", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.nextDemoStep() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next Step (${demoStepIndex + 1}/4)", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.NavigateNext, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}
