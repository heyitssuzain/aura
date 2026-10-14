package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraDarkSurfaceVariant
import com.example.ui.theme.AuraViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    onScanLibrary: () -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var animationsEnabled by remember { mutableStateOf(true) }
    var audioQuality by remember { mutableStateOf("High Quality (320 kbps)") }
    var showLicensesDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "PREFERENCES",
            color = AuraCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Settings & Privacy",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        // Playback & Audio Section
        SettingsGroupHeader(title = "Audio Engine")

        SettingsItem(
            icon = Icons.Default.HighQuality,
            title = "Audio Output Quality",
            subtitle = audioQuality,
            onClick = {
                audioQuality = if (audioQuality.contains("320")) "Lossless Studio (FLAC/Direct)" else "High Quality (320 kbps)"
            }
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AuraDarkSurfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Animation,
                    contentDescription = null,
                    tint = AuraCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cosmic Vinyl Animation",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Rotating turntable and audio visualizer wave",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = animationsEnabled,
                    onCheckedChange = { animationsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = AuraCyan
                    )
                )
            }
        }

        // Library Management
        SettingsGroupHeader(title = "Library & Cache")

        SettingsItem(
            icon = Icons.Default.Refresh,
            title = "Rescan Local Audio",
            subtitle = "Re-index device audio files via MediaStore",
            onClick = onScanLibrary
        )

        SettingsItem(
            icon = Icons.Default.Delete,
            title = "Clear Listening History",
            subtitle = "Wipes local play history records",
            onClick = onClearHistory
        )

        // Privacy & Info
        SettingsGroupHeader(title = "Privacy & Open Source")

        SettingsItem(
            icon = Icons.Default.Security,
            title = "Authentication-Free Guarantee",
            subtitle = "No accounts, no cloud database tracking. 100% device-local.",
            onClick = {}
        )

        SettingsItem(
            icon = Icons.Default.Info,
            title = "Open Source Licenses & About",
            subtitle = "AURA v1.0 • AndroidX Media3 • Room • Coil",
            onClick = { showLicensesDialog = true }
        )

        Spacer(modifier = Modifier.height(72.dp))
    }

    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text("AURA Music Player v1.0", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tagline: YOUR MUSIC. YOUR UNIVERSE.", color = AuraCyan, fontSize = 13.sp)
                    Text("Engine: AndroidX Media3 ExoPlayer 1.5.1", color = TextSecondary, fontSize = 12.sp)
                    Text("Database: AndroidX Room SQLite (Local)", color = TextSecondary, fontSize = 12.sp)
                    Text("UI: Jetpack Compose Material 3", color = TextSecondary, fontSize = 12.sp)
                    Text("Licensed under Apache License 2.0.", color = TextSecondary, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text("Close", color = AuraCyan)
                }
            },
            containerColor = AuraDarkSurface
        )
    }
}

@Composable
private fun SettingsGroupHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = AuraViolet,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(top = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AuraDarkSurfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AuraCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
