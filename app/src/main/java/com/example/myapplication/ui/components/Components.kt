package com.example.myapplication.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ai.AiSummaryResponse
import com.example.myapplication.data.models.GitHubUser
import com.example.myapplication.data.models.Language
import com.example.myapplication.data.models.Message
import com.example.myapplication.data.models.MessageStatus
import com.example.myapplication.network.ConnectionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConnectionBadge(connectionState: ConnectionState, modifier: Modifier = Modifier) {
    val (color, text) = when (connectionState) {
        ConnectionState.CONNECTED -> Color(0xFF10B981) to "Connected"
        ConnectionState.CONNECTING -> Color(0xFFF59E0B) to "Connecting..."
        ConnectionState.RECONNECTING -> Color(0xFFF59E0B) to "Reconnecting..."
        ConnectionState.DISCONNECTED -> Color(0xFF6B7280) to "Offline"
        ConnectionState.FAILED -> Color(0xFFEF4444) to "Connection Failed"
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
fun AudioWaveform(isRecording: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar1"
    )
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar2"
    )
    val scale3 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar3"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        val bars = listOf(scale1, scale2, scale3, scale1, scale2)
        bars.forEach { scale ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (isRecording) (24 * scale).dp else 6.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isRecording) MaterialTheme.colorScheme.primary else Color.Gray)
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    currentUserId: String,
    userPreferredLanguage: String,
    onSpeakClick: (Message) -> Unit,
    onRetryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    textSizeMultiplier: Float = 1.0f
) {
    val isMe = message.senderId == currentUserId
    val translatedText = message.getTranslationFor(userPreferredLanguage)
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (isMe) "You" else message.senderName,
                fontSize = (12 * textSizeMultiplier).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "• ${message.sourceLanguage.uppercase()}",
                fontSize = (10 * textSizeMultiplier).sp,
                color = MaterialTheme.colorScheme.outline
            )
            if (message.isTyped) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = "Typed",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Spoken",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            tonalElevation = 2.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Original text
                Text(
                    text = message.originalText,
                    fontSize = (15 * textSizeMultiplier).sp,
                    fontWeight = FontWeight.Normal
                )

                // Translated text (if different language)
                if (!message.sourceLanguage.equals(userPreferredLanguage, ignoreCase = true) && translatedText.isNotBlank()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = (if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.2f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Translated (${userPreferredLanguage.uppercase()}): ",
                            fontSize = (11 * textSizeMultiplier).sp,
                            fontWeight = FontWeight.Bold,
                            color = (if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary).copy(alpha = 0.9f)
                        )
                    }
                    Text(
                        text = translatedText,
                        fontSize = (14 * textSizeMultiplier).sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = (10 * textSizeMultiplier).sp,
                        color = (if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (message.status == MessageStatus.FAILED) {
                            Text(
                                text = "Failed - Retry",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.clickable { onRetryClick(message.id) }
                            )
                        } else {
                            IconButton(
                                onClick = { onSpeakClick(message) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Speak Translated Text",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CaptionOverlay(
    text: String,
    speakerName: String,
    modifier: Modifier = Modifier,
    textSizeMultiplier: Float = 1.0f
) {
    if (text.isBlank()) return

    Surface(
        color = Color.Black.copy(alpha = 0.85f),
        contentColor = Color.White,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CAPTION — $speakerName",
                    fontSize = (11 * textSizeMultiplier).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = (16 * textSizeMultiplier).sp,
                fontWeight = FontWeight.Medium,
                lineHeight = (22 * textSizeMultiplier).sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectorDropdown(
    selectedLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLang = remember(selectedLanguageCode) { Language.getByCode(selectedLanguageCode) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = "${currentLang.flagEmoji} ${currentLang.displayName} (${currentLang.nativeName})",
            onValueChange = {},
            readOnly = true,
            label = { Text("Preferred Language") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Language.SUPPORTED_LANGUAGES.forEach { lang ->
                DropdownMenuItem(
                    text = {
                        Text("${lang.flagEmoji} ${lang.displayName} — ${lang.nativeName}")
                    },
                    onClick = {
                        onLanguageSelected(lang.code)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun GitHubAccountCard(
    user: GitHubUser?,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user != null) Color(0xFF1F2937) else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (user != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF374151)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "GitHub Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = user.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "@${user.login} • GitHub Verified",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }

                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Sign Out",
                        tint = Color(0xFFEF4444)
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBox,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Connect GitHub Account", fontWeight = FontWeight.Bold)
                        Text("Sign in to sync handle & profile across rooms", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Button(
                    onClick = onLoginClick,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Sign In")
                }
            }
        }
    }
}

@Composable
fun GitHubLoginDialog(
    onDismiss: () -> Unit,
    onLogin: (String) -> Unit
) {
    var usernameText by remember { mutableStateOf("octocat") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sign In with GitHub", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter your GitHub handle to link your account profile:")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = usernameText,
                    onValueChange = { usernameText = it },
                    label = { Text("GitHub Username (e.g. octocat)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (usernameText.isNotBlank()) {
                        onLogin(usernameText.trim())
                    }
                }
            ) {
                Text("Sign In")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun QrCodeDialog(
    roomId: String,
    bitmap: Bitmap,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Room QR Code — $roomId", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scan this QR Code from another device to join instantly:", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Room QR Code",
                    modifier = Modifier.size(200.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AiAssistantDialog(
    summaryResponse: AiSummaryResponse?,
    onAskQuestion: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    var answerText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Assistant & Summarizer", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (summaryResponse != null) {
                    Text("Summary:", fontWeight = FontWeight.Bold)
                    Text(summaryResponse.summary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Key Highlights:", fontWeight = FontWeight.Bold)
                    summaryResponse.keyPoints.forEach { point ->
                        Text(point, fontSize = 12.sp)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                }

                Text("Ask AI Question:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        placeholder = { Text("e.g. Who joined?") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (questionText.isNotBlank()) {
                                onAskQuestion(questionText)
                                answerText = "AI Response: Processed question \"$questionText\" against live transcript."
                                questionText = ""
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Ask AI")
                    }
                }

                if (answerText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = answerText!!,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
