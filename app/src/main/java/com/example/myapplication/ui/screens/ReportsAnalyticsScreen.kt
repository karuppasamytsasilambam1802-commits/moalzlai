package com.example.myapplication.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.reports.AnalyticsData
import com.example.myapplication.viewmodel.VoxBridgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsAnalyticsScreen(
    viewModel: VoxBridgeViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val analytics = remember { viewModel.getAnalyticsReport() }
    val userRole by viewModel.userRole.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val auditLogs = remember { viewModel.getAuditLogs() }
    var selectedReportPeriod by remember { mutableIntStateOf(0) } // 0 = Daily, 1 = Weekly, 2 = Monthly

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Analytics", fontWeight = FontWeight.Bold) },
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
            // Security, Location & Role Status Banner
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Role: ${userRole.name} • ${userLocation.city}, ${userLocation.country}", fontWeight = FontWeight.Bold)
                        Text("Secure encrypted logs • Multi-device synced", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            // Statistics Summary Cards
            Text("Session Overview", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Records",
                    value = "${analytics.totalRecords}",
                    icon = Icons.Default.Analytics,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Messages",
                    value = "${analytics.totalMessages}",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Words Translated",
                    value = "${analytics.translatedWordsCount}",
                    icon = Icons.Default.Translate,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Participants",
                    value = "${analytics.totalParticipants}",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
            }

            // Report Period Charts
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Translation Activity Chart", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    PrimaryTabRow(selectedTabIndex = selectedReportPeriod) {
                        Tab(selected = selectedReportPeriod == 0, onClick = { selectedReportPeriod = 0 }) {
                            Text("Daily", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                        }
                        Tab(selected = selectedReportPeriod == 1, onClick = { selectedReportPeriod = 1 }) {
                            Text("Weekly", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                        }
                        Tab(selected = selectedReportPeriod == 2, onClick = { selectedReportPeriod = 2 }) {
                            Text("Monthly", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val activeStats = when (selectedReportPeriod) {
                        0 -> analytics.dailyStats
                        1 -> analytics.weeklyStats
                        else -> analytics.monthlyStats
                    }

                    BarChart(stats = activeStats)
                }
            }

            // Audit Logs Activity Card
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Audit Logs & Activity History", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (auditLogs.isEmpty()) {
                        Text("No audit logs recorded yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    } else {
                        auditLogs.take(5).forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${log.action} by ${log.userName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(log.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }

            // Export Data Section (CSV & PDF)
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Export Data & Logs", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val file = viewModel.exportCsvReport(context)
                                if (file != null) {
                                    Toast.makeText(context, "CSV Exported: ${file.name}", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Failed to export CSV", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export CSV", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val file = viewModel.exportPdfReport(context)
                                if (file != null) {
                                    Toast.makeText(context, "PDF Exported: ${file.name}", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Failed to export PDF", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF", fontSize = 13.sp)
                        }
                    }
                }
            }

            // Notifications & Reminders Actions
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Notifications & Announcements Test", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.triggerAnnouncementNotification(
                                "New Feature Update",
                                "VoxBridge AI v1.0 adds CSV/PDF Export, QR Codes, and AI Summarizer!"
                            )
                            Toast.makeText(context, "Announcement notification sent", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Campaign, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Trigger Platform Announcement")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.triggerReminderNotification("ROOM-7890")
                            Toast.makeText(context, "Reminder notification sent", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Alarm, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Trigger Room Reminder")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun BarChart(stats: List<Pair<String, Int>>) {
    val maxVal = (stats.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        stats.forEach { (label, value) ->
            val fraction = (value.toFloat() / maxVal.toFloat()).coerceIn(0.1f, 1.0f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(text = "$value", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .fillMaxHeight(fraction)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
