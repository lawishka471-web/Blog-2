package com.example.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.TokenManager
import com.example.data.repository.AuthRepository
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    tokenManager: TokenManager,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val currentUser by authRepository.currentUser.collectAsState()

    // Settings state
    var selectedLanguage by remember { mutableStateOf("English") }
    var selectedAppearance by remember { mutableStateOf("Dark") }
    var fontSizeSp by remember { mutableFloatStateOf(16f) }
    var lineSpacingRatio by remember { mutableStateOf("1.5x") }
    var notificationsEnabled by remember { mutableStateOf(true) }

    // Dialog state for clicked activity options
    var activeDialogTitle by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // 1. User Profile Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = DarkSurfaceVariant,
                            modifier = Modifier
                                .size(80.dp)
                                .border(2.dp, AmberPrimary, CircleShape)
                        ) {
                            if (currentUser != null && !currentUser?.avatar.isNullOrEmpty()) {
                                AsyncImage(
                                    model = currentUser?.avatar,
                                    contentDescription = currentUser?.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User Avatar",
                                    tint = AmberPrimary,
                                    modifier = Modifier
                                        .padding(20.dp)
                                        .size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = currentUser?.name ?: "Guest Reader",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = currentUser?.email ?: "Browsing in Guest Access Mode",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        if (currentUser != null) {
                            OutlinedButton(
                                onClick = { authRepository.logout() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Logout",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Logout Account", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = onNavigateToLogin,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmberPrimary,
                                        contentColor = Color.Black
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = "Sign In",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sign In", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = onNavigateToRegister,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberPrimary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Register", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section Header: My Activity
                Text(
                    text = "My Activity",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )

                // 2. Profile Options Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        ProfileOptionRow(
                            icon = Icons.Default.Favorite,
                            title = "Liked Chapters",
                            subtitle = "Chapters you've liked",
                            onClick = { activeDialogTitle = "Liked Chapters" }
                        )
                        HorizontalDivider(color = DarkSurfaceVariant, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        ProfileOptionRow(
                            icon = Icons.Default.Person,
                            title = "Following Authors",
                            subtitle = "Authors you stay updated with",
                            onClick = { activeDialogTitle = "Following Authors" }
                        )
                        HorizontalDivider(color = DarkSurfaceVariant, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        ProfileOptionRow(
                            icon = Icons.Default.Notifications,
                            title = "Notifications",
                            subtitle = "New chapter alerts and updates",
                            onClick = { activeDialogTitle = "Notifications" }
                        )
                        HorizontalDivider(color = DarkSurfaceVariant, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        ProfileOptionRow(
                            icon = Icons.Default.History,
                            title = "Reading History",
                            subtitle = "Recently read chapters",
                            onClick = { activeDialogTitle = "Reading History" }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section Header: Settings
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )

                // 3. Settings Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Language Selector
                        SettingSectionHeader(icon = Icons.Default.Language, title = "Language")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PillOption(
                                text = "English",
                                isSelected = selectedLanguage == "English",
                                onClick = { selectedLanguage = "English" },
                                modifier = Modifier.weight(1f)
                            )
                            PillOption(
                                text = "Sinhala",
                                isSelected = selectedLanguage == "Sinhala",
                                onClick = { selectedLanguage = "Sinhala" },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = DarkSurfaceVariant, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Appearance Selector
                        SettingSectionHeader(icon = Icons.Default.Palette, title = "Appearance")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PillOption(
                                text = "Light",
                                isSelected = selectedAppearance == "Light",
                                onClick = { selectedAppearance = "Light" },
                                modifier = Modifier.weight(1f)
                            )
                            PillOption(
                                text = "Dark",
                                isSelected = selectedAppearance == "Dark",
                                onClick = { selectedAppearance = "Dark" },
                                modifier = Modifier.weight(1f)
                            )
                            PillOption(
                                text = "System",
                                isSelected = selectedAppearance == "System",
                                onClick = { selectedAppearance = "System" },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = DarkSurfaceVariant, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Reader Settings
                        SettingSectionHeader(icon = Icons.Default.FormatSize, title = "Reader Settings")
                        Spacer(modifier = Modifier.height(10.dp))

                        // Font size control
                        Text(
                            text = "Font Size: ${fontSizeSp.toInt()} sp",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Slider(
                            value = fontSizeSp,
                            onValueChange = { fontSizeSp = it },
                            valueRange = 12f..24f,
                            steps = 5,
                            colors = SliderDefaults.colors(
                                thumbColor = AmberPrimary,
                                activeTrackColor = AmberPrimary,
                                inactiveTrackColor = DarkSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Line spacing control
                        Text(
                            text = "Line Spacing",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PillOption(
                                text = "1.2x",
                                isSelected = lineSpacingRatio == "1.2x",
                                onClick = { lineSpacingRatio = "1.2x" },
                                modifier = Modifier.weight(1f)
                            )
                            PillOption(
                                text = "1.5x",
                                isSelected = lineSpacingRatio == "1.5x",
                                onClick = { lineSpacingRatio = "1.5x" },
                                modifier = Modifier.weight(1f)
                            )
                            PillOption(
                                text = "1.8x",
                                isSelected = lineSpacingRatio == "1.8x",
                                onClick = { lineSpacingRatio = "1.8x" },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = DarkSurfaceVariant, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Notifications Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = AmberPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Notifications",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = if (notificationsEnabled) "Enabled" else "Disabled",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                    )
                                }
                            }

                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = AmberPrimary,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = DarkSurfaceVariant
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Modal dialog for option items
    activeDialogTitle?.let { title ->
        AlertDialog(
            onDismissRequest = { activeDialogTitle = null },
            confirmButton = {
                TextButton(onClick = { activeDialogTitle = null }) {
                    Text("OK", color = AmberPrimary, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            },
            text = {
                Text(
                    text = when (title) {
                        "Liked Chapters" -> "Your liked chapters list will be displayed here as you explore and read novel chapters."
                        "Following Authors" -> "Authors you follow will appear here with instant updates when they post new content."
                        "Notifications" -> "Notification settings and alerts for newly released novel chapters."
                        "Reading History" -> "Your reading history allows you to resume reading right from where you stopped."
                        else -> "Profile options"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ProfileOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = DarkSurfaceVariant,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = AmberPrimary,
                modifier = Modifier
                    .padding(10.dp)
                    .size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 12.sp
                )
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingSectionHeader(
    icon: ImageVector,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = AmberPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
    }
}

@Composable
private fun PillOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) AmberPrimary else DarkSurfaceVariant,
        contentColor = if (isSelected) Color.Black else TextSecondary,
        modifier = modifier.height(38.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                )
            )
        }
    }
}

private fun String?.isNullOrEmpty(): Boolean = this == null || this.trim().isEmpty()
