package com.example.myapplication.ui.screens

import android.Manifest
import android.content.pm.PackageManager
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
import com.example.myapplication.ai.AiSummaryResponse
import com.example.myapplication.ui.components.AiAssistantDialog
import com.example.myapplication.ui.components.AudioWaveform
import com.example.myapplication.ui.components.CaptionOverlay
import com.example.myapplication.ui.components.ConnectionBadge
import com.example.myapplication.ui.components.MessageBubble
import com.example.myapplication.ui.components.QrCodeDialog
import com.example.myapplication.viewmodel.VoxBridgeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveConversationScreen(
    viewModel: VoxBridgeViewModel,
    onExitConversation: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val roomId by viewModel.currentRoomId.collectAsState()
    val userId by viewModel.participantId.collectAsState()
    val userLang by viewModel.preferredLanguage.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val audioCallState by viewModel.audioCallState.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val interimTranscript by viewModel.interimTranscript.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val autoSpeakAudio by viewModel.autoSpeakAudio.collectAsState()
    val captionsEnabled by viewModel.captionsEnabled.collectAsState()
    val textSizeMultiplier by viewModel.textSizeMultiplier.collectAsState()

    var typedText by remember { mutableStateOf("") }
    var showQrDialog by remember { mutableStateOf(false) }
    var showAiDialog by remember { mutableStateOf(false) }
    var aiSummary by remember { mutableStateOf<AiSummaryResponse?>(null) }

    val listState = rememberLazyListState()

    // File Attachment Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { viewModel.uploadAttachment(it, "Attachment_${System.currentTimeMillis()}.dat") }
        }
    )

    // Microphone Permission Launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.startSpeechRecognition(context)
            }
        }
    )

    if (showQrDialog) {
        val qrBitmap = remember(roomId) { viewModel.generateQrCodeBitmap(roomId ?: "ROOM") }
        QrCodeDialog(
            roomId = roomId ?: "ROOM",
            bitmap = qrBitmap,
            onDismiss = { showQrDialog = false }
        )
    }

    if (showAiDialog) {
        AiAssistantDialog(
            summaryResponse = aiSummary,
            onAskQuestion = { q ->
                scope.launch { viewModel.askAi(q) }
            },
            onDismiss = { showAiDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = roomId ?: "Live Conversation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "${participants.size} Participant(s) • Lang: ${userLang.uppercase()}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showQrDialog = true }) {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = "Room QR")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            aiSummary = viewModel.getAiSummary()
                            showAiDialog = true
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = "AI Assistant")
                    }
                    ConnectionBadge(connectionState = connectionState)
                    IconButton(onClick = onExitConversation) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Leave Room",
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
            // Live Captions Overlay Banner
            if (captionsEnabled && messages.isNotEmpty()) {
                val lastMsg = messages.last()
                CaptionOverlay(
                    text = lastMsg.getTranslationFor(userLang),
                    speakerName = lastMsg.senderName,
                    textSizeMultiplier = textSizeMultiplier
                )
            }

            // Message Stream Area
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
                            text = "No messages yet.\nTap the mic or type below to start!",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 14.sp
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

            // Speech Recognition Interim Transcript Banner
            AnimatedVisibility(visible = interimTranscript.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AudioWaveform(isRecording = true)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Listening: $interimTranscript",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Controls & Text Input Area
            Surface(
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {

                    // Voice Audio Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mic Speech Button
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
                                contentDescription = "Speech Mic"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isListening) "Stop Mic" else "Speak Now")
                        }

                        // Auto-Speak Audio Quick Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (autoSpeakAudio) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = "Auto-Speak Audio",
                                tint = if (autoSpeakAudio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Auto-Speak",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (autoSpeakAudio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Switch(
                                checked = autoSpeakAudio,
                                onCheckedChange = { viewModel.setAutoSpeakAudio(it) },
                                modifier = Modifier.height(24.dp)
                            )
                        }

                        // Call Mute Toggle
                        IconButton(
                            onClick = { viewModel.toggleMute() },
                            modifier = Modifier.background(
                                color = if (audioCallState.isMuted) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                        ) {
                            Icon(
                                imageVector = if (audioCallState.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Mute Audio Call"
                            )
                        }

                        // Stop TTS Audio
                        if (isSpeaking) {
                            IconButton(
                                onClick = { viewModel.stopAudioPlayback() },
                                modifier = Modifier.background(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = CircleShape
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop Speech Playback"
                                )
                            }
                        }
                    }

                    // Text Input Bar with File Attachment Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                            Icon(imageVector = Icons.Default.AttachFile, contentDescription = "Attach File")
                        }

                        OutlinedTextField(
                            value = typedText,
                            onValueChange = { typedText = it },
                            placeholder = { Text("Type message in your language...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (typedText.isNotBlank()) {
                                    viewModel.sendTypedMessage(typedText)
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
                                contentDescription = "Send",
                                tint = if (typedText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}
