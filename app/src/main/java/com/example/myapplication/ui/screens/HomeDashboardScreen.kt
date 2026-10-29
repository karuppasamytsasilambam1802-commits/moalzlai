package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.models.Language
import com.example.myapplication.ui.components.ConnectionBadge
import com.example.myapplication.ui.components.GitHubAccountCard
import com.example.myapplication.ui.components.GitHubLoginDialog
import com.example.myapplication.viewmodel.VoxBridgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    viewModel: VoxBridgeViewModel,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    onLanguageSettings: () -> Unit,
    onDemoMode: () -> Unit,
    onReportsClick: (() -> Unit)? = null,
    onOnlineClassClick: (() -> Unit)? = null
) {
    val name by viewModel.participantName.collectAsState()
    val langCode by viewModel.preferredLanguage.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val githubUser by viewModel.githubUser.collectAsState()
    val currentLang = Language.getByCode(langCode)

    var showGithubDialog by remember { mutableStateOf(false) }

    if (showGithubDialog) {
        GitHubLoginDialog(
            onDismiss = { showGithubDialog = false },
            onLogin = { username ->
                viewModel.loginWithGitHub(username)
                showGithubDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VoxBridge Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    ConnectionBadge(connectionState = connectionState, modifier = Modifier.padding(end = 12.dp))
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Welcome Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentLang.flagEmoji,
                            fontSize = 26.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Hello, $name!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Language: ${currentLang.displayName} (${currentLang.code.uppercase()})",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // GitHub Account Card
            GitHubAccountCard(
                user = githubUser,
                onLoginClick = { showGithubDialog = true },
                onLogoutClick = { viewModel.logoutGitHub() }
            )

            // Online Class Meetings Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                onClick = { onOnlineClassClick?.invoke() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Online Class Meetings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            Text("Live lecture translation & Raise Hand Q&A", fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f))
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }

            // Quick Actions: Create or Join Room
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    onClick = onCreateRoom
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Create Room", fontWeight = FontWeight.Bold)
                        Text("Start a new room", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    onClick = onJoinRoom
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.GroupAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Join Room", fontWeight = FontWeight.Bold)
                        Text("Enter room code", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            // Reports & Analytics Shortcut Card
            Card(
                shape = RoundedCornerShape(16.dp),
                onClick = { onReportsClick?.invoke() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Reports, Charts & CSV/PDF Export", fontWeight = FontWeight.Bold)
                            Text("View translation statistics and export reports", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                }
            }

            // Quick Settings Card
            Card(
                shape = RoundedCornerShape(16.dp),
                onClick = onLanguageSettings
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Language & Accessibility", fontWeight = FontWeight.Bold)
                            Text("Change language, TTS, captions, or server IP", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                }
            }

            // Demo Mode Launcher
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                onClick = onDemoMode
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Interactive Demo Mode", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("Simulate live 4-participant cross-language conversation", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                    }
                }
            }

            // Privacy & Server Info Card
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Privacy & Network Status", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• No audio retained on disk by default.", fontSize = 12.sp)
                    Text("• Temporary room IDs automatically expire.", fontSize = 12.sp)
                    Text("• Server Endpoint: $serverUrl", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
