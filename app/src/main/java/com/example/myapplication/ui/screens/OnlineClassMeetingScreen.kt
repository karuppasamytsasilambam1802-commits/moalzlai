package com.example.myapplication.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.components.CaptionOverlay
import com.example.myapplication.ui.components.ConnectionBadge
import com.example.myapplication.ui.components.MessageBubble
import com.example.myapplication.viewmodel.VoxBridgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineClassMeetingScreen(
    viewModel: VoxBridgeViewModel,
    onLeaveClass: () -> Unit
) {
    val context = LocalContext.current
    val currentClass by viewModel.currentClass.collectAsState()
    val raisedHands by viewModel.raisedHandsQueue.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val userId by viewModel.participantId.collectAsState()
    val userLang by viewModel.preferredLanguage.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val autoSpeakAudio by viewModel.autoSpeakAudio.collectAsState()
    val textSizeMultiplier by viewModel.textSizeMultiplier.collectAsState()

    var typedText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to latest lecture message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Mic Permission Launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.startSpeechRecognition(context)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentClass?.subjectName ?: "Online Class Lecture",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Instructor: ${currentClass?.instructorName ?: "Prof. Alan"} • ${participants.size} Students",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onLeaveClass) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ConnectionBadge(connectionState = connectionState)
                    IconButton(onClick = onLeaveClass) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Leave Class",
                            tint = MaterialTheme.colorScheme.error
                        )
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
            // Raised Hands Banner for Instructor / Students
            AnimatedVisibility(visible = raisedHands.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✋", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Hand Raised by ${raisedHands.lastOrNull()?.studentName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "${raisedHands.size} Student(s) in Q&A Queue",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                raisedHands.lastOrNull()?.let {
                                    viewModel.acknowledgeHandRaise(it.studentId)
                                    Toast.makeText(context, "Acknowledged student ${it.studentName}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Permit to Speak", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Live Lecture Caption Overlay
            if (messages.isNotEmpty()) {
                val lastMsg = messages.last()
                CaptionOverlay(
                    text = lastMsg.getTranslationFor(userLang),
                    speakerName = lastMsg.senderName,
                    textSizeMultiplier = textSizeMultiplier
                )
            }

            // Lecture Messages & Q&A Stream
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Live lecture started.\nInstructor speech and student questions will appear here translated in real time.",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            MessageBubble(
                                message = msg,
                                currentUserId = userId,
                                userPreferredLanguage = userLang,
                                onSpeakClick = { viewModel.speakMessage(it) },
                                onRetryClick = { viewModel.retryFailedMessage(it) },
                                textSizeMultiplier = textSizeMultiplier
                            )
                        }
                    }
                }
            }

            // Lecture Toolbar & Q&A Bar
            Surface(
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Raise Hand Button
                        Button(
                            onClick = {
                                viewModel.raiseHandInClass()
                                Toast.makeText(context, "Hand Raised! Instructor notified.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = CircleShape
                        ) {
                            Text("✋ Raise Hand")
                        }

                        // Speech Mic Button
                        Button(
                            onClick = {
                                if (isListening) {
                                    viewModel.stopSpeechRecognition()
                                } else {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        viewModel.startSpeechRecognition(context)
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Lecture Mic"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isListening) "Stop Mic" else "Lecture Mic")
                        }

                        // Auto-Speak Audio Switch
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (autoSpeakAudio) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = autoSpeakAudio,
                                onCheckedChange = { viewModel.setAutoSpeakAudio(it) },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }

                    // Student Question / Lecture Text Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = typedText,
                            onValueChange = { typedText = it },
                            placeholder = { Text("Ask a question or type in your language...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (typedText.isNotBlank()) {
                                    viewModel.sendTypedMessage("❓ Question: $typedText")
                                    typedText = ""
                                }
                            },
                            enabled = typedText.isNotBlank(),
                            modifier = Modifier
                                .background(
                                    color = if (typedText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                )
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Question",
                                tint = if (typedText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}
