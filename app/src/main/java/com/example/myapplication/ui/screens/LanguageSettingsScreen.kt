package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.models.CommunicationMode
import com.example.myapplication.ui.components.GitHubAccountCard
import com.example.myapplication.ui.components.GitHubLoginDialog
import com.example.myapplication.ui.components.LanguageSelectorDropdown
import com.example.myapplication.viewmodel.VoxBridgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(
    viewModel: VoxBridgeViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val preferredLang by viewModel.preferredLanguage.collectAsState()
    val currentMode by viewModel.communicationMode.collectAsState()
    val autoSpeak by viewModel.autoSpeakAudio.collectAsState()
    val captionsEnabled by viewModel.captionsEnabled.collectAsState()
    val textSizeMultiplier by viewModel.textSizeMultiplier.collectAsState()
    val highContrast by viewModel.highContrast.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val name by viewModel.participantName.collectAsState()
    val githubUser by viewModel.githubUser.collectAsState()

    var tempName by remember { mutableStateOf(name) }
    var tempUrl by remember { mutableStateOf(serverUrl) }
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
                title = { Text("Language & Preferences", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
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
            // Profile Name
            OutlinedTextField(
                value = tempName,
                onValueChange = {
                    tempName = it
                    viewModel.setParticipantName(it)
                },
                label = { Text("Your Display Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // GitHub Account Integration
            GitHubAccountCard(
                user = githubUser,
                onLoginClick = { showGithubDialog = true },
                onLogoutClick = { viewModel.logoutGitHub() }
            )

            // Language Selector
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. Select Your Preferred Language", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LanguageSelectorDropdown(
                        selectedLanguageCode = preferredLang,
                        onLanguageSelected = { viewModel.setPreferredLanguage(it) }
                    )
                }
            }

            // Input / Communication Mode
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("2. Communication Mode", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    CommunicationMode.entries.forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (currentMode == mode),
                                onClick = { viewModel.setCommunicationMode(mode) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when(mode) {
                                    CommunicationMode.VOICE_AND_TEXT -> "Voice & Text (Recommended)"
                                    CommunicationMode.VOICE_ONLY -> "Voice Only"
                                    CommunicationMode.TEXT_ONLY -> "Text Only (No Mic Required)"
                                    CommunicationMode.CAPTIONS_ONLY -> "Captions Only"
                                },
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Output & Accessibility Toggles
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("3. Output & Accessibility Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Speak Translated Audio", fontWeight = FontWeight.SemiBold)
                            Text("Read incoming translations aloud in your language.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Switch(checked = autoSpeak, onCheckedChange = { viewModel.setAutoSpeakAudio(it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Live Captions", fontWeight = FontWeight.SemiBold)
                            Text("Show prominent on-screen caption overlay.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Switch(checked = captionsEnabled, onCheckedChange = { viewModel.setCaptionsEnabled(it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("High Contrast Mode", fontWeight = FontWeight.SemiBold)
                            Text("Increase UI text visibility.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Switch(checked = highContrast, onCheckedChange = { viewModel.setHighContrast(it) })
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Text Size Multiplier: ${(textSizeMultiplier * 100).toInt()}%", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = textSizeMultiplier,
                        onValueChange = { viewModel.setTextSizeMultiplier(it) },
                        valueRange = 0.8f..1.5f,
                        steps = 7
                    )
                }
            }

            // Laptop Server URL Config
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("4. FastAPI Backend Server URL", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Emulator default: http://10.0.2.2:8000\nPhysical phone: http://<laptop-ip>:8000", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = {
                            tempUrl = it
                            viewModel.setServerUrl(it)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Continue Button
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save & Continue to Dashboard", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
