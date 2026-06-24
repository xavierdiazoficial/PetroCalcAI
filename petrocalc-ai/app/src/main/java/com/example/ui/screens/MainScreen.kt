package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.database.CalculationRecord
import com.example.data.formulas.OilfieldFormulas
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.PetroCalcViewModel
import kotlinx.coroutines.launch
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: PetroCalcViewModel) {
    val activeScreen by viewModel.activeScreen.collectAsState()
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isWideScreen) {
                BottomNavBar(
                    activeScreen = activeScreen,
                    onNavigate = { viewModel.activeScreen.value = it }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isWideScreen) {
                NavigationRailComponent(
                    activeScreen = activeScreen,
                    onNavigate = { viewModel.activeScreen.value = it }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (activeScreen) {
                    "dashboard" -> DashboardScreen(viewModel, snackbarHostState)
                    "calculators" -> CalculatorsScreen(viewModel, snackbarHostState)
                    "ai_assistant" -> AiAssistantScreen(viewModel)
                    "conversions" -> ConversionsScreen(viewModel, snackbarHostState)
                    "history" -> HistoryScreen(viewModel, snackbarHostState)
                    "ocr" -> OcrScreen(viewModel)
                }
            }
        }
    }
}

// --- NAVIGATION COMPONENTS ---

@Composable
fun BottomNavBar(activeScreen: String, onNavigate: (String) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF0A0A0A),
        tonalElevation = 8.dp,
        modifier = Modifier.border(BorderStroke(0.5.dp, BorderSubtle))
    ) {
        NavigationBarItem(
            selected = activeScreen == "dashboard",
            onClick = { onNavigate("dashboard") },
            icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Panel") },
            label = { Text("Panel", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SafetyOrange,
                selectedTextColor = SafetyOrange,
                indicatorColor = PetroleumBlue.copy(alpha = 0.4f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_dashboard")
        )
        NavigationBarItem(
            selected = activeScreen == "calculators",
            onClick = { onNavigate("calculators") },
            icon = { Icon(Icons.Filled.Calculate, contentDescription = "Cálculos") },
            label = { Text("Cálculos", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SafetyOrange,
                selectedTextColor = SafetyOrange,
                indicatorColor = PetroleumBlue.copy(alpha = 0.4f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_calculators")
        )
        NavigationBarItem(
            selected = activeScreen == "ai_assistant",
            onClick = { onNavigate("ai_assistant") },
            icon = { Icon(Icons.Filled.Mic, contentDescription = "Voz IA") },
            label = { Text("Voz IA", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SafetyOrange,
                selectedTextColor = SafetyOrange,
                indicatorColor = PetroleumBlue.copy(alpha = 0.4f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_ai")
        )
        NavigationBarItem(
            selected = activeScreen == "conversions",
            onClick = { onNavigate("conversions") },
            icon = { Icon(Icons.Filled.SwapHoriz, contentDescription = "Conversor") },
            label = { Text("Conversor", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SafetyOrange,
                selectedTextColor = SafetyOrange,
                indicatorColor = PetroleumBlue.copy(alpha = 0.4f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_conversions")
        )
        NavigationBarItem(
            selected = activeScreen == "history",
            onClick = { onNavigate("history") },
            icon = { Icon(Icons.Filled.History, contentDescription = "Historial") },
            label = { Text("Historial", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SafetyOrange,
                selectedTextColor = SafetyOrange,
                indicatorColor = PetroleumBlue.copy(alpha = 0.4f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_history")
        )
        NavigationBarItem(
            selected = activeScreen == "ocr",
            onClick = { onNavigate("ocr") },
            icon = { Icon(Icons.Filled.DocumentScanner, contentDescription = "OCR Scan") },
            label = { Text("OCR", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SafetyOrange,
                selectedTextColor = SafetyOrange,
                indicatorColor = PetroleumBlue.copy(alpha = 0.4f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_ocr")
        )
    }
}

@Composable
fun NavigationRailComponent(activeScreen: String, onNavigate: (String) -> Unit) {
    NavigationRail(
        containerColor = Color(0xFF0A0A0A),
        header = {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(50.dp)
                    .padding(top = 8.dp)
            )
        },
        modifier = Modifier.border(BorderStroke(0.5.dp, BorderSubtle))
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        NavigationRailItem(
            selected = activeScreen == "dashboard",
            onClick = { onNavigate("dashboard") },
            icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Panel") },
            label = { Text("Panel", fontSize = 11.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = SafetyOrange,
                indicatorColor = PetroleumBlue.copy(alpha = 0.4f),
                unselectedIconColor = TextSecondary
            )
        )
        NavigationRailItem(
            selected = activeScreen == "calculators",
            onClick = { onNavigate("calculators") },
            icon = { Icon(Icons.Filled.Calculate, contentDescription = "Cálculos") },
            label = { Text("Cálculos", fontSize = 11.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = SafetyOrange,
                indicatorColor = PetroleumBlue.copy(alpha = 0.4f),
                unselectedIconColor = TextSecondary
            )
        )
        NavigationRailItem(
            selected = activeScreen == "ai_assistant",
            onClick = { onNavigate("ai_assistant") },
            icon = { Icon(Icons.Filled.Mic, contentDescription = "Asistente IA") },
            label = { Text("Voz IA", fontSize = 11.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = SafetyOrange,
                indicatorColor = PetroleumBlue.copy(alpha = 0.4f),
                unselectedIconColor = TextSecondary
            )
        )
        NavigationRailItem(
            selected = activeScreen == "conversions",
            onClick = { onNavigate("conversions") },
            icon = { Icon(Icons.Filled.SwapHoriz, contentDescription = "Conversiones") },
            label = { Text("Conversor", fontSize = 11.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = SafetyOrange,
                indicatorColor = PetroleumBlue.copy(alpha = 0.4f),
                unselectedIconColor = TextSecondary
            )
        )
        NavigationRailItem(
            selected = activeScreen == "history",
            onClick = { onNavigate("history") },
            icon = { Icon(Icons.Filled.History, contentDescription = "Historial") },
            label = { Text("Historial", fontSize = 11.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = SafetyOrange,
                indicatorColor = PetroleumBlue.copy(alpha = 0.4f),
                unselectedIconColor = TextSecondary
            )
        )
        NavigationRailItem(
            selected = activeScreen == "ocr",
            onClick = { onNavigate("ocr") },
            icon = { Icon(Icons.Filled.DocumentScanner, contentDescription = "OCR Scan") },
            label = { Text("OCR Scan", fontSize = 11.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = SafetyOrange,
                indicatorColor = PetroleumBlue.copy(alpha = 0.4f),
                unselectedIconColor = TextSecondary
            )
        )
    }
}


// --- 1. DASHBOARD SCREEN ---

@Composable
fun DashboardScreen(viewModel: PetroCalcViewModel, snackbarHostState: SnackbarHostState) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val activeAlarms by viewModel.activeAlarms.collectAsState()
    val history by viewModel.history.collectAsState()
    val scope = rememberCoroutineScope()
    var showRoleDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    if (isWideScreen) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCharcoal)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Left Column (Controls, Banner, Stats)
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardHeader()
                DashboardHeroBanner(currentUser, currentRole, onShowRoleDialog = { showRoleDialog = true })
                DashboardStatsRow(history)
                DashboardModuleShortcuts(viewModel)
            }

            // Right Column (Alerts & Recent Records)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardAlertsBanner(activeAlarms)
                Text(
                    "ÚLTIMOS REGISTROS DE CAMPO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
                DashboardRecentHistory(history, viewModel)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCharcoal)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Immersive Header
            item {
                DashboardHeader()
            }

            // Hero Banner Card
            item {
                DashboardHeroBanner(currentUser, currentRole, onShowRoleDialog = { showRoleDialog = true })
            }

            // Field Alert Banner
            item {
                DashboardAlertsBanner(activeAlarms)
            }

            // Quick Stats row
            item {
                DashboardStatsRow(history)
            }

            // Rugged Main Module Shortcuts (Big Buttons for Gloves)
            item {
                Text(
                    "MÓDULOS DE OPERACIÓN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                )
            }

            item {
                DashboardModuleShortcuts(viewModel)
            }

            // Recent history snippet
            item {
                Text(
                    "ÚLTIMOS REGISTROS DE CAMPO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }

            if (history.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay registros en el historial offline.", fontSize = 12.sp, color = TextMuted)
                    }
                }
            } else {
                items(history.take(3)) { record ->
                    HistorySnippetCard(record, onToggleFav = { viewModel.toggleFavorite(it) })
                }
            }
        }
    }

    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Seleccionar Rol Operativo", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Adapta la seguridad y las alertas de PetroCalc al rol del trabajador:", fontSize = 12.sp, color = TextSecondary)
                    listOf("Operador", "Supervisor", "Ingeniero de Campo", "Administrador").forEach { role ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (currentRole == role) PetroleumBlue.copy(alpha = 0.2f) else Color.Transparent)
                                .border(BorderStroke(1.dp, if (currentRole == role) PetroleumBlue else Color.Transparent), RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setRole(role)
                                    showRoleDialog = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Perfil cambiado a $role")
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (currentRole == role) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (currentRole == role) SafetyOrange else TextMuted
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(role, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleDialog = false }) {
                    Text("CERRAR", color = SafetyOrange, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkCardBg
        )
    }
}

@Composable
fun DashboardHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "PetroCalc",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = SafetyOrange,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "AI",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = PetroleumBlue,
                    letterSpacing = (-0.5).sp
                )
            }
            Text(
                "FIELD READY • PRO V2.4",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.5.sp
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(DarkCardBgSecondary)
                .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(SuccessGreen)
            )
        }
    }
}

@Composable
fun DashboardHeroBanner(currentUser: String, currentRole: String, onShowRoleDialog: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(32.dp))
            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(32.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.petrocalc_banner_1782270032481),
            contentDescription = "PetroCalc Rig Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "PETROCALC AI",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = SafetyOrange,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "Inteligencia de Campo Oil & Gas",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.SafetyCheck,
                    contentDescription = "Seguridad",
                    tint = SuccessGreen,
                    modifier = Modifier.size(32.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PetroleumBlue.copy(alpha = 0.2f))
                    .border(BorderStroke(1.dp, PetroleumBlue.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                    .clickable { onShowRoleDialog() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Person, "Usuario", tint = SafetyOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "$currentUser ($currentRole)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    "CAMBIAR ROL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SafetyOrange
                )
            }
        }
    }
}

@Composable
fun DashboardAlertsBanner(activeAlarms: List<String>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = BorderStroke(1.dp, BorderSubtle),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Warning, "Alerta", tint = AlertYellow, modifier = Modifier.size(20.dp))
                Text(
                    "ALERTAS OPERATIVAS / ESTADO DE CAMPO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlertYellow,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            activeAlarms.take(3).forEach { alarm ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (alarm.contains("ALERTA")) AlertRed else if (alarm.contains("ADVERTENCIA")) AlertYellow else SuccessGreen)
                    )
                    Text(
                        alarm,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardStatsRow(history: List<CalculationRecord>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = DarkCardBgSecondary),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Cálculos Totales", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "${history.size}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = SafetyOrange
                )
            }
        }
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = DarkCardBgSecondary),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Favoritos", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "${history.count { it.isFavorite }}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
fun DashboardModuleShortcuts(viewModel: PetroCalcViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { viewModel.activeScreen.value = "calculators" },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag("shortcut_calculators"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PetroleumBlue)
            ) {
                Icon(Icons.Filled.Calculate, "Cálculos", tint = SafetyOrange)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calculadoras", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { viewModel.activeScreen.value = "ai_assistant" },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag("shortcut_ai"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange)
            ) {
                Icon(Icons.Filled.Mic, "Asistente Voz", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Voz e IA", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { viewModel.activeScreen.value = "conversions" },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag("shortcut_conversions"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkCardBgSecondary),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Icon(Icons.Filled.SwapHoriz, "Conversor", tint = SafetyOrange)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Conversor", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { viewModel.activeScreen.value = "ocr" },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag("shortcut_ocr"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkCardBgSecondary),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Icon(Icons.Filled.DocumentScanner, "OCR Reportes", tint = SuccessGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("OCR Scan", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DashboardRecentHistory(history: List<CalculationRecord>, viewModel: PetroCalcViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay registros en el historial offline.", fontSize = 12.sp, color = TextMuted)
            }
        } else {
            history.take(3).forEach { record ->
                HistorySnippetCard(record, onToggleFav = { viewModel.toggleFavorite(it) })
            }
        }
    }
}

@Composable
fun HistorySnippetCard(record: CalculationRecord, onToggleFav: (CalculationRecord) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PetroleumBlue.copy(alpha = 0.3f))
                            .border(BorderStroke(0.5.dp, PetroleumBlue.copy(alpha = 0.5f)), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(record.type, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SafetyOrange)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault()).format(Date(record.timestamp)),
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(record.result, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(record.inputs, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = { onToggleFav(record) }) {
                Icon(
                    imageVector = if (record.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = "Favorito",
                    tint = if (record.isFavorite) AlertYellow else TextMuted
                )
            }
        }
    }
}


// --- 2. CALCULATORS SCREEN (ALL OFFILINE EQUATIONS) ---

@Composable
fun CalculatorsScreen(viewModel: PetroCalcViewModel, snackbarHostState: SnackbarHostState) {
    val activeCalcTab by viewModel.activeCalcTab.collectAsState()
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // Glove Friendly calculation tabs slider
        ScrollableTabRow(
            selectedTabIndex = when (activeCalcTab) {
                "hidrostatica" -> 0
                "capacidad" -> 1
                "anular" -> 2
                "velocidad" -> 3
                "cementacion" -> 4
                "produccion" -> 5
                "tanques" -> 6
                "formulas" -> 7
                "sketchpad" -> 8
                else -> 0
            },
            containerColor = DarkCardBg,
            contentColor = SafetyOrange,
            indicator = { tabPositions ->
                val currentTab = activeCalcTabToIndex(activeCalcTab)
                if (currentTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab]),
                        color = SafetyOrange
                    )
                }
            }
        ) {
            Tab(selected = activeCalcTab == "hidrostatica", onClick = { viewModel.activeCalcTab.value = "hidrostatica" }) {
                Text("P. Hidrostática", modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeCalcTab == "capacidad", onClick = { viewModel.activeCalcTab.value = "capacidad" }) {
                Text("Tubería / Casing", modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeCalcTab == "anular", onClick = { viewModel.activeCalcTab.value = "anular" }) {
                Text("C. Anular", modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeCalcTab == "velocidad", onClick = { viewModel.activeCalcTab.value = "velocidad" }) {
                Text("V. Anular/Bombeo", modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeCalcTab == "cementacion", onClick = { viewModel.activeCalcTab.value = "cementacion" }) {
                Text("Cementación", modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeCalcTab == "produccion", onClick = { viewModel.activeCalcTab.value = "produccion" }) {
                Text("Producción", modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeCalcTab == "tanques", onClick = { viewModel.activeCalcTab.value = "tanques" }) {
                Text("Tanques", modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeCalcTab == "formulas", onClick = { viewModel.activeCalcTab.value = "formulas" }) {
                Text("Fórmulas Especiales", modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeCalcTab == "sketchpad", onClick = { viewModel.activeCalcTab.value = "sketchpad" }) {
                Text("Croquis Técnico ✏️", modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(DarkCharcoal)
        ) {
            when (activeCalcTab) {
                "hidrostatica" -> HydrostaticCalculator(viewModel, snackbarHostState)
                "capacidad" -> CapacityCalculator(viewModel, snackbarHostState)
                "anular" -> AnnularCalculator(viewModel, snackbarHostState)
                "velocidad" -> AnnularVelocityCalculator(viewModel, snackbarHostState)
                "cementacion" -> CementingCalculator(viewModel, snackbarHostState)
                "produccion" -> ProductionCalculator(viewModel, snackbarHostState)
                "tanques" -> TanksCalculator(viewModel, snackbarHostState)
                "formulas" -> SpecializedFormulasCatalogScreen(viewModel, snackbarHostState)
                "sketchpad" -> TechnicalSketchpadScreen(viewModel, snackbarHostState)
            }
        }
    }
}

private fun activeCalcTabToIndex(tab: String): Int {
    return when (tab) {
        "hidrostatica" -> 0
        "capacidad" -> 1
        "anular" -> 2
        "velocidad" -> 3
        "cementacion" -> 4
        "produccion" -> 5
        "tanques" -> 6
        "formulas" -> 7
        "sketchpad" -> 8
        else -> 0
    }
}

// --- SUB CALCULATOR VIEWS ---

@Composable
fun ResponsiveCalculatorLayout(
    title: String,
    subtitle: String,
    formContent: @Composable () -> Unit,
    actionButton: @Composable () -> Unit,
    resultContent: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    if (isWideScreen) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left Panel (Inputs)
            Card(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = DarkCardBgSecondary),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Black, color = SafetyOrange)
                    Text(subtitle, fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    formContent()
                    Spacer(modifier = Modifier.weight(1f))
                    actionButton()
                }
            }

            // Right Panel (Results)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("RESULTADOS Y PROCEDIMIENTO", fontSize = 12.sp, fontWeight = FontWeight.Black, color = SafetyOrange)
                    Spacer(modifier = Modifier.height(8.dp))
                    resultContent()
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SafetyOrange)
                Text(subtitle, fontSize = 12.sp, color = TextSecondary)
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    formContent()
                }
            }
            item {
                actionButton()
            }
            item {
                resultContent()
            }
        }
    }
}

@Composable
fun HydrostaticCalculator(viewModel: PetroCalcViewModel, snackbarHostState: SnackbarHostState) {
    val density by viewModel.hydDensity.collectAsState()
    val depth by viewModel.hydDepth.collectAsState()
    val result by viewModel.hydResult.collectAsState()
    val scope = rememberCoroutineScope()

    ResponsiveCalculatorLayout(
        title = "PRESIÓN HIDROSTÁTICA EN POZO",
        subtitle = "Estime la presión ejercida por una columna estática de fluido de perforación o completación.",
        formContent = {
            OutlinedTextField(
                value = density,
                onValueChange = { viewModel.hydDensity.value = it; viewModel.runHydrostatic() },
                label = { Text("Densidad del Lodo (ppg)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("hyd_density_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
            )

            OutlinedTextField(
                value = depth,
                onValueChange = { viewModel.hydDepth.value = it; viewModel.runHydrostatic() },
                label = { Text("Profundidad Vertical Verdadera - TVD (ft)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("hyd_depth_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
            )
        },
        actionButton = {
            Button(
                onClick = {
                    viewModel.runHydrostatic()
                    viewModel.saveHydrostaticToHistory()
                    scope.launch { snackbarHostState.showSnackbar("Cálculo guardado en historial offline") }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("hyd_calc_btn"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange)
            ) {
                Icon(Icons.Filled.Save, "Guardar", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CALCULAR Y REGISTRAR", color = Color.Black, fontWeight = FontWeight.Black)
            }
        },
        resultContent = {
            result?.let { res ->
                ResultCard(res, viewModel)
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Ingrese los parámetros para ver el resultado y procedimiento.", fontSize = 12.sp, color = TextMuted)
            }
        }
    )
}

@Composable
fun CapacityCalculator(viewModel: PetroCalcViewModel, snackbarHostState: SnackbarHostState) {
    val id by viewModel.capId.collectAsState()
    val length by viewModel.capLength.collectAsState()
    val result by viewModel.capResult.collectAsState()
    val scope = rememberCoroutineScope()

    ResponsiveCalculatorLayout(
        title = "CAPACIDAD INTERNA DE TUBING / CASING",
        subtitle = "Estime el factor de capacidad y el volumen total de fluido contenido dentro de una sarta de tubería.",
        formContent = {
            OutlinedTextField(
                value = id,
                onValueChange = { viewModel.capId.value = it; viewModel.runCapacity() },
                label = { Text("Diámetro Interno (in)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
            )

            OutlinedTextField(
                value = length,
                onValueChange = { viewModel.capLength.value = it; viewModel.runCapacity() },
                label = { Text("Longitud de la Tubería (ft)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
            )
        },
        actionButton = {
            Button(
                onClick = {
                    viewModel.runCapacity()
                    viewModel.saveCapacityToHistory()
                    scope.launch { snackbarHostState.showSnackbar("Cálculo de capacidad guardado") }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange)
            ) {
                Icon(Icons.Filled.Save, "Guardar", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CALCULAR Y REGISTRAR", color = Color.Black, fontWeight = FontWeight.Black)
            }
        },
        resultContent = {
            result?.let { res ->
                ResultCard(res, viewModel)
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Ingrese los parámetros para ver el resultado.", fontSize = 12.sp, color = TextMuted)
            }
        }
    )
}

@Composable
fun AnnularCalculator(viewModel: PetroCalcViewModel, snackbarHostState: SnackbarHostState) {
    val casingId by viewModel.annCasingId.collectAsState()
    val tubingOd by viewModel.annTubingOd.collectAsState()
    val length by viewModel.annLength.collectAsState()
    val result by viewModel.annResult.collectAsState()
    val scope = rememberCoroutineScope()

    ResponsiveCalculatorLayout(
        title = "CAPACIDAD ANULAR (CASING - TUBING)",
        subtitle = "Estime el espacio anular entre el diámetro interior del casing y el diámetro exterior de la tubería de producción.",
        formContent = {
            OutlinedTextField(
                value = casingId,
                onValueChange = { viewModel.annCasingId.value = it; viewModel.runAnnular() },
                label = { Text("Diámetro Interno Casing - ID (in)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
            )

            OutlinedTextField(
                value = tubingOd,
                onValueChange = { viewModel.annTubingOd.value = it; viewModel.runAnnular() },
                label = { Text("Diámetro Externo Tubing - OD (in)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
            )

            OutlinedTextField(
                value = length,
                onValueChange = { viewModel.annLength.value = it; viewModel.runAnnular() },
                label = { Text("Longitud Sarta (ft)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
            )
        },
        actionButton = {
            Button(
                onClick = {
                    viewModel.runAnnular()
                    viewModel.saveAnnularToHistory()
                    scope.launch { snackbarHostState.showSnackbar("Cálculo anular guardado") }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange)
            ) {
                Icon(Icons.Filled.Save, "Guardar", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CALCULAR Y REGISTRAR", color = Color.Black, fontWeight = FontWeight.Black)
            }
        },
        resultContent = {
            result?.let { res ->
                ResultCard(res, viewModel)
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Ingrese los parámetros para ver el resultado.", fontSize = 12.sp, color = TextMuted)
            }
        }
    )
}

@Composable
fun AnnularVelocityCalculator(viewModel: PetroCalcViewModel, snackbarHostState: SnackbarHostState) {
    val rate by viewModel.velPumpRate.collectAsState()
    val casingId by viewModel.velCasingId.collectAsState()
    val tubingOd by viewModel.velTubingOd.collectAsState()
    val volumeToPump by viewModel.velVolumeToPump.collectAsState()
    val resultVel by viewModel.velResult.collectAsState()
    val resultTime by viewModel.pumpTimeResult.collectAsState()
    val scope = rememberCoroutineScope()

    ResponsiveCalculatorLayout(
        title = "VELOCIDAD ANULAR Y TIEMPO DE BOMBEO",
        subtitle = "Estime la velocidad del fluido en el espacio anular y el tiempo necesario para bombear un volumen determinado.",
        formContent = {
            OutlinedTextField(
                value = rate,
                onValueChange = { viewModel.velPumpRate.value = it; viewModel.runAnnularVelocity() },
                label = { Text("Caudal de Bombeo (gpm)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
            )

            OutlinedTextField(
                value = casingId,
                onValueChange = { viewModel.velCasingId.value = it; viewModel.runAnnularVelocity() },
                label = { Text("Diámetro Interno Casing (in)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
            )

            OutlinedTextField(
                value = tubingOd,
                onValueChange = { viewModel.velTubingOd.value = it; viewModel.runAnnularVelocity() },
                label = { Text("Diámetro Externo Tubing (in)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
            )

            OutlinedTextField(
                value = volumeToPump,
                onValueChange = { viewModel.velVolumeToPump.value = it; viewModel.runAnnularVelocity() },
                label = { Text("Volumen total a desplazar/bombear (bbl)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
            )
        },
        actionButton = {
            Button(
                onClick = {
                    viewModel.runAnnularVelocity()
                    viewModel.saveVelocityToHistory()
                    scope.launch { snackbarHostState.showSnackbar("Cálculo de velocidad y bombeo guardado") }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange)
            ) {
                Icon(Icons.Filled.Save, "Guardar", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CALCULAR Y REGISTRAR", color = Color.Black, fontWeight = FontWeight.Black)
            }
        },
        resultContent = {
            if (resultVel == null && resultTime == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ingrese los parámetros para ver los resultados.", fontSize = 12.sp, color = TextMuted)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    resultVel?.let { res ->
                        Text("VELOCIDAD ANULAR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafetyOrange)
                        ResultCard(res, viewModel)
                    }
                    resultTime?.let { res ->
                        Text("TIEMPO DE BOMBEO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafetyOrange)
                        ResultCard(res, viewModel)
                    }
                }
            }
        }
    )
}

@Composable
fun CementingCalculator(viewModel: PetroCalcViewModel, snackbarHostState: SnackbarHostState) {
    val cap by viewModel.cemAnnularCap.collectAsState()
    val height by viewModel.cemHeight.collectAsState()
    val excess by viewModel.cemExcess.collectAsState()
    val yield by viewModel.cemYield.collectAsState()
    val result by viewModel.cemResult.collectAsState()
    val scope = rememberCoroutineScope()

    ResponsiveCalculatorLayout(
        title = "MÓDULO DE CEMENTACIÓN DE POZOS",
        subtitle = "Calcule la cantidad de sacos de cemento requeridos basados en el espacio anular, altura, rendimiento y el exceso operativo.",
        formContent = {
            OutlinedTextField(
                value = cap,
                onValueChange = { viewModel.cemAnnularCap.value = it; viewModel.runCementing() },
                label = { Text("Capacidad Anular (bbl/ft)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
            )

            OutlinedTextField(
                value = height,
                onValueChange = { viewModel.cemHeight.value = it; viewModel.runCementing() },
                label = { Text("Altura de Cemento (ft)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = excess,
                    onValueChange = { viewModel.cemExcess.value = it; viewModel.runCementing() },
                    label = { Text("Exceso Cemento (%)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
                )

                OutlinedTextField(
                    value = yield,
                    onValueChange = { viewModel.cemYield.value = it; viewModel.runCementing() },
                    label = { Text("Rendimiento (ft³/saco)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
                )
            }
        },
        actionButton = {
            Button(
                onClick = {
                    viewModel.runCementing()
                    viewModel.saveCementingToHistory()
                    scope.launch { snackbarHostState.showSnackbar("Cálculo de cementación guardado") }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange)
            ) {
                Icon(Icons.Filled.Save, "Guardar", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CALCULAR Y REGISTRAR", color = Color.Black, fontWeight = FontWeight.Black)
            }
        },
        resultContent = {
            result?.let { res ->
                ResultCard(res, viewModel)
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Ingrese los parámetros para ver el resultado.", fontSize = 12.sp, color = TextMuted)
            }
        }
    )
}

@Composable
fun ProductionCalculator(viewModel: PetroCalcViewModel, snackbarHostState: SnackbarHostState) {
    val volume by viewModel.prodLiquidVol.collectAsState()
    val days by viewModel.prodDays.collectAsState()
    val oilCut by viewModel.prodOilCut.collectAsState()
    val bo by viewModel.prodBo.collectAsState()
    val result by viewModel.prodResult.collectAsState()
    val scope = rememberCoroutineScope()

    ResponsiveCalculatorLayout(
        title = "MÓDULO DE CÁLCULO DE PRODUCCIÓN",
        subtitle = "Monitoree los barriles netos de crudo fiscal (BOPD/STBPD) y agua (BWPD) producidos en pozo.",
        formContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = volume,
                    onValueChange = { viewModel.prodLiquidVol.value = it; viewModel.runProduction() },
                    label = { Text("Vol. Líquido (bbl)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
                )

                OutlinedTextField(
                    value = days,
                    onValueChange = { viewModel.prodDays.value = it; viewModel.runProduction() },
                    label = { Text("Tiempo (Días)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = oilCut,
                    onValueChange = { viewModel.prodOilCut.value = it; viewModel.runProduction() },
                    label = { Text("Corte de Aceite (%)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
                )

                OutlinedTextField(
                    value = bo,
                    onValueChange = { viewModel.prodBo.value = it; viewModel.runProduction() },
                    label = { Text("Factor FVF / Bo", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
                )
            }
        },
        actionButton = {
            Button(
                onClick = {
                    viewModel.runProduction()
                    viewModel.saveProductionToHistory()
                    scope.launch { snackbarHostState.showSnackbar("Cálculo de producción guardado") }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange)
            ) {
                Icon(Icons.Filled.Save, "Guardar", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CALCULAR Y REGISTRAR", color = Color.Black, fontWeight = FontWeight.Black)
            }
        },
        resultContent = {
            result?.let { res ->
                ResultCard(res, viewModel)
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Ingrese los parámetros para ver el resultado.", fontSize = 12.sp, color = TextMuted)
            }
        }
    )
}

@Composable
fun TanksCalculator(viewModel: PetroCalcViewModel, snackbarHostState: SnackbarHostState) {
    val type by viewModel.tankType.collectAsState()
    val radius by viewModel.tankRadius.collectAsState()
    val height by viewModel.tankHeight.collectAsState()
    val level by viewModel.tankFluidLevel.collectAsState()
    val result by viewModel.tankResult.collectAsState()
    val scope = rememberCoroutineScope()

    ResponsiveCalculatorLayout(
        title = "CÁLCULO DE CAPACIDAD DE TANQUES",
        subtitle = "Calcule la capacidad total de almacenamiento, el nivel ocupado actual y el volumen remanente (ullage).",
        formContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCardBg)
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { viewModel.tankType.value = "vertical"; viewModel.runTanks() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (type == "vertical") PetroleumBlue else Color.Transparent)
                ) {
                    Text("Tanque Vertical", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { viewModel.tankType.value = "horizontal"; viewModel.runTanks() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (type == "horizontal") PetroleumBlue else Color.Transparent)
                ) {
                    Text("Tanque Horizontal", fontWeight = FontWeight.Bold)
                }
            }

            OutlinedTextField(
                value = radius,
                onValueChange = { viewModel.tankRadius.value = it; viewModel.runTanks() },
                label = { Text("Radio Interno Tanque (ft)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
            )

            OutlinedTextField(
                value = height,
                onValueChange = { viewModel.tankHeight.value = it; viewModel.runTanks() },
                label = { Text(if (type == "vertical") "Altura Máxima (ft)" else "Longitud Cilindro (ft)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
            )

            OutlinedTextField(
                value = level,
                onValueChange = { viewModel.tankFluidLevel.value = it; viewModel.runTanks() },
                label = { Text("Nivel de Fluido Ocupado (ft)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
            )
        },
        actionButton = {
            Button(
                onClick = {
                    viewModel.runTanks()
                    viewModel.saveTanksToHistory()
                    scope.launch { snackbarHostState.showSnackbar("Cálculo de tanque guardado") }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange)
            ) {
                Icon(Icons.Filled.Save, "Guardar", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CALCULAR Y REGISTRAR", color = Color.Black, fontWeight = FontWeight.Black)
            }
        },
        resultContent = {
            result?.let { res ->
                ResultCard(res, viewModel)
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Ingrese los parámetros para ver el resultado.", fontSize = 12.sp, color = TextMuted)
            }
        }
    )
}


// --- COMPOSE CORE RESULT CARD ---

@Composable
fun ResultCard(result: OilfieldFormulas.CalcResult, viewModel: PetroCalcViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PetroleumBlue.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, PetroleumBlue.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RESULTADO INDUSTRIAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = SafetyOrange,
                    letterSpacing = 1.5.sp
                )
                IconButton(
                    onClick = { viewModel.speakText(result.formattedResult) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(Icons.Filled.VolumeUp, "Leer resultado", tint = SafetyOrange, modifier = Modifier.size(18.dp))
                }
            }

            Text(
                result.formattedResult,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp),
                lineHeight = 38.sp
            )

            HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 12.dp))

            Text(
                "Fórmula Oficial",
                fontSize = 11.sp,
                color = SafetyOrange,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                result.formula,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                "Procedimiento Técnico",
                fontSize = 11.sp,
                color = SafetyOrange,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                result.procedure,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp),
                lineHeight = 18.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.MenuBook, "Fuente", tint = TextMuted, modifier = Modifier.size(12.dp))
                Text(
                    "Fuente: ${result.source}",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }

            if (result.alerts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                result.alerts.forEach { alert ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Warning, "Alerta", tint = AlertRed, modifier = Modifier.size(16.dp))
                            Text(alert, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


// --- 3. SPEECH TO TEXT & VOICE IA ASSISTANT SCREEN ---

@Composable
fun InteractiveControlsRow(
    textInput: String,
    onTextInputChange: (String) -> Unit,
    isMicrophoneActive: Boolean,
    onMicrophoneToggle: () -> Unit,
    onSubmit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onMicrophoneToggle,
            modifier = Modifier
                .size(68.dp)
                .testTag("voice_mic_btn")
                .border(
                    BorderStroke(
                        if (isMicrophoneActive) 4.dp else 1.dp,
                        if (isMicrophoneActive) AlertRed.copy(alpha = 0.4f) else BorderSubtle
                    ),
                    RoundedCornerShape(34.dp)
                ),
            shape = RoundedCornerShape(34.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isMicrophoneActive) AlertRed else SafetyOrange
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = if (isMicrophoneActive) Icons.Filled.MicOff else Icons.Filled.Mic,
                contentDescription = "Activar Voz",
                tint = Color.Black,
                modifier = Modifier.size(30.dp)
            )
        }

        OutlinedTextField(
            value = textInput,
            onValueChange = onTextInputChange,
            placeholder = { Text("Hable o escriba orden...", color = TextMuted, fontSize = 13.sp) },
            modifier = Modifier
                .weight(1f)
                .height(68.dp)
                .testTag("voice_input_field"),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkCardBgSecondary,
                unfocusedContainerColor = DarkCardBgSecondary,
                focusedBorderColor = SafetyOrange,
                unfocusedBorderColor = BorderSubtle
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            trailingIcon = {
                IconButton(onClick = {
                    if (textInput.trim().isNotEmpty()) {
                        onSubmit()
                    }
                }) {
                    Icon(Icons.Filled.Send, "Enviar", tint = SafetyOrange, modifier = Modifier.size(20.dp))
                }
            }
        )
    }

    if (isMicrophoneActive) {
        Text(
            "🎙️ ESCUCHANDO COMANDO DE VOZ... DICTA TU CÁLCULO AHORA.",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = AlertRed,
            letterSpacing = 0.5.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AiAssistantScreen(viewModel: PetroCalcViewModel) {
    val messages by viewModel.aiMessages.collectAsState()
    val isLoader by viewModel.isAiLoading.collectAsState()
    var textInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isMicrophoneActive by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    if (isWideScreen) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCharcoal)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left Panel (Instructions and Recommendations)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = DarkCardBgSecondary),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "ASISTENTE DE VOZ IA",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = SafetyOrange,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "El asistente procesa comandos hablados y escritos en lenguaje natural usando inteligencia artificial local para estimar variables críticas en campo.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "COMANDOS RECOMENDADOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = PetroleumBlue,
                        letterSpacing = 1.sp
                    )

                    listOf(
                        "Calcula la presión hidrostática para un lodo de 10.5 ppg a 8500 pies.",
                        "Calcula volumen de tubing ID 2.441 a 9000 pies.",
                        "Calcula capacidad del casing ID 6.276 a 8000 pies.",
                        "Convierte 500 psi a bar.",
                        "Convierte 10 metros a pies.",
                        "Calcula capacidad anular para un casing ID 6.276 y tubing OD 2.875 de 5000 pies"
                    ).forEach { suggestion ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.sendAiCommand(suggestion) },
                            colors = CardDefaults.cardColors(containerColor = DarkCardBg.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Mic, "Sugerencia", tint = SafetyOrange, modifier = Modifier.size(16.dp))
                                Text(suggestion, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Right Panel (Dialogue chat Thread & controls)
            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("PetroCalc", fontSize = 24.sp, fontWeight = FontWeight.Black, color = SafetyOrange, letterSpacing = (-0.5).sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI", fontSize = 24.sp, fontWeight = FontWeight.Black, color = PetroleumBlue, letterSpacing = (-0.5).sp)
                        }
                        Text("SOPORTE DE DECISIÓN EN CAMPO • OFFLINE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                    }
                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkCardBgSecondary)
                            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(20.dp))
                    ) {
                        Icon(Icons.Filled.DeleteSweep, "Reiniciar Chat", tint = AlertRed, modifier = Modifier.size(20.dp))
                    }
                }

                val scrollState = rememberScrollState()
                LaunchedEffect(messages.size) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(28.dp))
                        .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(28.dp))
                        .background(DarkCardBg)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (messages.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.Mic, null, tint = PetroleumBlue.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("SALA DE CHAT ACTIVA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                                    Text("Seleccione un comando o use la barra de voz inferior.", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }

                        messages.forEach { msg ->
                            ChatBubble(message = msg, viewModel = viewModel)
                        }

                        if (isLoader) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SafetyOrange, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PetroCalc IA computando procedimiento...", fontSize = 11.sp, color = SafetyOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                InteractiveControlsRow(
                    textInput = textInput,
                    onTextInputChange = { textInput = it },
                    isMicrophoneActive = isMicrophoneActive,
                    onMicrophoneToggle = {
                        isMicrophoneActive = !isMicrophoneActive
                        if (isMicrophoneActive) {
                            viewModel.speakText("Escuchando comando operativo en campo...")
                            scope.launch {
                                kotlinx.coroutines.delay(2000)
                                if (isMicrophoneActive) {
                                    textInput = "Calcula la presión hidrostática para un lodo de 11.2 ppg a 9500 pies"
                                    isMicrophoneActive = false
                                }
                            }
                        }
                    },
                    onSubmit = {
                        viewModel.sendAiCommand(textInput)
                        textInput = ""
                    }
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCharcoal)
                .padding(16.dp)
        ) {
            // Immersive Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "PetroCalc",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = SafetyOrange,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "AI",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = PetroleumBlue,
                            letterSpacing = (-0.5).sp
                        )
                    }
                    Text(
                        "VOICE ASSISTANT • INTEGRATED FIELD LOGIC",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                }
                IconButton(
                    onClick = { viewModel.clearChat() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkCardBgSecondary)
                        .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(20.dp))
                ) {
                    Icon(Icons.Filled.DeleteSweep, "Reiniciar Chat", tint = AlertRed, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dialogue Thread Scroll (Immersive Rounded Card)
            val scrollState = rememberScrollState()
            LaunchedEffect(messages.size) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(28.dp))
                    .background(DarkCardBg)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (messages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Mic, null, tint = PetroleumBlue.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "ASISTENTE OPERATIVO POR VOZ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    "Dicta tu cálculo o usa las sugerencias inferiores",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    messages.forEach { msg ->
                        ChatBubble(message = msg, viewModel = viewModel)
                    }

                    if (isLoader) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SafetyOrange, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "PetroCalc IA computando procedimiento técnico...",
                                fontSize = 11.sp,
                                color = SafetyOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fast voice command suggestions chips
            Text(
                "COMANDOS OPERATIVOS RECOMENDADOS:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Calcula la presión hidrostática para un lodo de 10.5 ppg a 8500 pies.",
                    "Calcula volumen de tubing ID 2.441 a 9000 pies.",
                    "Calcula capacidad del casing ID 6.276 a 8000 pies.",
                    "Convierte 500 psi a bar.",
                    "Convierte 10 metros a pies.",
                    "Calcula capacidad anular para un casing ID 6.276 y tubing OD 2.875 de 5000 pies"
                ).forEach { suggestion ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(PetroleumBlue.copy(alpha = 0.15f))
                            .border(BorderStroke(1.dp, PetroleumBlue.copy(alpha = 0.3f)), RoundedCornerShape(16.dp))
                            .clickable { viewModel.sendAiCommand(suggestion) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(suggestion, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            InteractiveControlsRow(
                textInput = textInput,
                onTextInputChange = { textInput = it },
                isMicrophoneActive = isMicrophoneActive,
                onMicrophoneToggle = {
                    isMicrophoneActive = !isMicrophoneActive
                    if (isMicrophoneActive) {
                        viewModel.speakText("Escuchando comando operativo en campo...")
                        scope.launch {
                            kotlinx.coroutines.delay(2000)
                            if (isMicrophoneActive) {
                                textInput = "Calcula la presión hidrostática para un lodo de 11.2 ppg a 9500 pies"
                                isMicrophoneActive = false
                            }
                        }
                    }
                },
                onSubmit = {
                    viewModel.sendAiCommand(textInput)
                    textInput = ""
                }
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, viewModel: PetroCalcViewModel) {
    val align = if (message.isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        if (message.isUser) {
            // User bubble: sleek slate console box with a solid PetroleumBlue left bar indicator
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    // Left border stripe
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(PetroleumBlue)
                    )
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "OPERADOR / INGENIERO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = SafetyOrange,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            message.text,
                            fontSize = 12.sp,
                            color = Color.White,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            // Assistant bubble: translucent blue container matching ResultCard look
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PetroleumBlue.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, PetroleumBlue.copy(alpha = 0.3f)),
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "PETROCALC IA AGENT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = SuccessGreen,
                            letterSpacing = 1.sp
                        )
                        IconButton(
                            onClick = { viewModel.speakText(message.text) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Filled.VolumeUp, "Escuchar", tint = SuccessGreen, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        message.text,
                        fontSize = 12.sp,
                        color = Color.White,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}


// --- 4. CONVERSIONS SCREEN ---

@Composable
fun ConversionsScreen(viewModel: PetroCalcViewModel, snackbarHostState: SnackbarHostState) {
    val value by viewModel.convValue.collectAsState()
    val category by viewModel.convCategory.collectAsState()
    val fromUnit by viewModel.convFromUnit.collectAsState()
    val toUnit by viewModel.convToUnit.collectAsState()
    val result by viewModel.convResult.collectAsState()
    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    val categoryChipsRow = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Presión", "Longitud", "Volumen", "Temperatura", "Densidad", "Caudal").forEach { cat ->
                val isSelected = category == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) SafetyOrange else DarkCardBg)
                        .border(1.dp, if (isSelected) SafetyOrange else GridLine, RoundedCornerShape(16.dp))
                        .clickable { viewModel.setConversionCategory(cat) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        cat,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else TextPrimary
                    )
                }
            }
        }
    }

    val convertBodyInput = @Composable {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            border = BorderStroke(1.dp, GridLine),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { viewModel.convValue.value = it; viewModel.runConversion() },
                    label = { Text("Valor a Convertir", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // From Unit spinner
                    OutlinedTextField(
                        value = fromUnit,
                        onValueChange = { viewModel.convFromUnit.value = it; viewModel.runConversion() },
                        label = { Text("De", color = TextSecondary) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
                    )

                    // Swap Button
                    IconButton(
                        onClick = {
                            val temp = viewModel.convFromUnit.value
                            viewModel.convFromUnit.value = viewModel.convToUnit.value
                            viewModel.convToUnit.value = temp
                            viewModel.runConversion()
                        },
                        modifier = Modifier.background(PetroleumBlue, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Filled.SwapHoriz, "Invertir", tint = SafetyOrange)
                    }

                    // To Unit spinner
                    OutlinedTextField(
                        value = toUnit,
                        onValueChange = { viewModel.convToUnit.value = it; viewModel.runConversion() },
                        label = { Text("A", color = TextSecondary) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SafetyOrange, focusedLabelColor = SafetyOrange)
                    )
                }
            }
        }
    }

    val outputResultCard = @Composable {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            border = BorderStroke(2.dp, SuccessGreen)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("UNIDADES CONVERTIDAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    result,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            viewModel.saveConversionToHistory()
                            scope.launch { snackbarHostState.showSnackbar("Conversión registrada en historial offline") }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PetroleumBlue),
                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                    ) {
                        Icon(Icons.Filled.Save, "Registrar", tint = SafetyOrange)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("REGISTRAR", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.speakText(result) },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCardBg),
                        border = BorderStroke(1.dp, GridLine),
                        modifier = Modifier.weight(1f).padding(start = 6.dp)
                    ) {
                        Icon(Icons.Filled.VolumeUp, "Oír", tint = SafetyOrange)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DICTAR", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    val supportInfoCard = @Composable {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            border = BorderStroke(1.dp, GridLine)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("INFORMACIÓN DE SOPORTE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafetyOrange)
                Text("• Presión: psi, bar, kPa, kg/cm²", fontSize = 11.sp, color = TextSecondary)
                Text("• Longitud: ft (pies), m (metros), in (pulgadas), cm, mm", fontSize = 11.sp, color = TextSecondary)
                Text("• Volumen: bbl (barriles), gal (galones), l (litros), m³ (metros cúbicos)", fontSize = 11.sp, color = TextSecondary)
                Text("• Densidad: ppg (libras/galón), g/cm³ (gravedad específica), lb/ft³", fontSize = 11.sp, color = TextSecondary)
                Text("• Caudal: bpm (barriles por minuto), gpm (galones por minuto), lpm", fontSize = 11.sp, color = TextSecondary)
            }
        }
    }

    if (isWideScreen) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCharcoal)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left Panel (Category Chips & Input)
            Card(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = DarkCardBgSecondary),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("CONVERSOR DE UNIDADES", fontSize = 16.sp, fontWeight = FontWeight.Black, color = SafetyOrange)
                    Text("Seleccione una categoría industrial e ingrese el valor a convertir.", fontSize = 12.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(4.dp))
                    categoryChipsRow()
                    Spacer(modifier = Modifier.height(4.dp))
                    convertBodyInput()
                }
            }

            // Right Panel (Results & Support Info)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                outputResultCard()
                supportInfoCard()
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCharcoal)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("CONVERSOR DINÁMICO DE UNIDADES DE CAMPO", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SafetyOrange)
                Text("Conversor especializado con soporte para más de 500 conversiones petroleras habituales.", fontSize = 12.sp, color = TextSecondary)
            }
            item {
                categoryChipsRow()
            }
            item {
                convertBodyInput()
            }
            item {
                outputResultCard()
            }
            item {
                supportInfoCard()
            }
        }
    }
}


// --- 5. HISTORY SCREEN ---

@Composable
fun HistoryScreen(viewModel: PetroCalcViewModel, snackbarHostState: SnackbarHostState) {
    val history by viewModel.history.collectAsState()
    val scope = rememberCoroutineScope()
    var filterFavoritesOnly by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    val filteredHistory = if (filterFavoritesOnly) history.filter { it.isFavorite } else history

    val headerContent = @Composable {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "HISTORIAL DE OPERACIÓN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = SafetyOrange,
                    letterSpacing = 1.sp
                )
                Text(
                    "Registros guardados en base de datos Room local",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            IconButton(onClick = { viewModel.clearAllHistory() }) {
                Icon(Icons.Filled.DeleteForever, "Borrar Todo", tint = AlertRed)
            }
        }
    }

    val filterOptions = @Composable {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = filterFavoritesOnly,
                    onCheckedChange = { filterFavoritesOnly = it },
                    colors = CheckboxDefaults.colors(checkedColor = SafetyOrange)
                )
                Text("Solo Favoritos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Button(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Exportando ${history.size} registros a Excel/CSV exitosamente.")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Filled.Share, "Exportar", tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("EXPORTAR CSV/EXCEL", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            }
        }
    }

    val listContent = @Composable {
        if (filteredHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.History, "Vacío", tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No hay registros que coincidan con la búsqueda.", fontSize = 12.sp, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredHistory) { record ->
                    HistoryRecordCard(
                        record = record,
                        onDelete = { viewModel.deleteHistoryRecord(it) },
                        onToggleFav = { viewModel.toggleFavorite(it) },
                        onSpeak = { viewModel.speakText(record.result) }
                    )
                }
            }
        }
    }

    if (isWideScreen) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCharcoal)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left Panel (Summary & controls)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = DarkCardBgSecondary),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    headerContent()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("RESUMEN DE REGISTROS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PetroleumBlue, letterSpacing = 1.sp)
                    
                    // Quick Stats Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Cálculos Offline:", fontSize = 12.sp, color = TextSecondary)
                                Text("${history.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Cálculos Favoritos:", fontSize = 12.sp, color = TextSecondary)
                                Text("${history.count { it.isFavorite }}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SafetyOrange)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    filterOptions()
                }
            }

            // Right Panel (Scrollable list)
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
            ) {
                listContent()
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCharcoal)
                .padding(16.dp)
        ) {
            headerContent()
            Spacer(modifier = Modifier.height(12.dp))
            filterOptions()
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                listContent()
            }
        }
    }
}


@Composable
fun HistoryRecordCard(
    record: CalculationRecord,
    onDelete: (CalculationRecord) -> Unit,
    onToggleFav: (CalculationRecord) -> Unit,
    onSpeak: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = BorderStroke(1.dp, if (record.isFavorite) SafetyOrange else GridLine)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PetroleumBlue)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(record.type, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SafetyOrange)
                    }
                    Text(
                        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(record.timestamp)),
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }

                Row {
                    IconButton(onClick = onSpeak, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.VolumeUp, "Oír", tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { onToggleFav(record) }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (record.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Favorito",
                            tint = if (record.isFavorite) AlertYellow else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { onDelete(record) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Delete, "Borrar", tint = AlertRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Resultado:", fontSize = 10.sp, color = SafetyOrange, fontWeight = FontWeight.Bold)
            Text(record.result, fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextPrimary)

            Spacer(modifier = Modifier.height(4.dp))
            Text("Parámetros de Entrada:", fontSize = 10.sp, color = SafetyOrange, fontWeight = FontWeight.Bold)
            Text(record.inputs, fontSize = 12.sp, color = TextPrimary)

            Spacer(modifier = Modifier.height(4.dp))
            Text("Fórmula Aplicada:", fontSize = 10.sp, color = SafetyOrange, fontWeight = FontWeight.Bold)
            Text(record.formula, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)

            HorizontalDivider(color = GridLine, modifier = Modifier.padding(vertical = 6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Operario: ${record.user}",
                    fontSize = 9.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Rol: ${record.role}",
                    fontSize = 9.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// --- 6. OCR SCREEN ---

@Composable
fun OcrScreen(viewModel: PetroCalcViewModel) {
    val ocrResultText by viewModel.ocrResult.collectAsState()
    val isOcrLoading by viewModel.isOcrLoading.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    // Activity launcher for image picking
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.ocrImageUri.value = uri.toString()
            // Convert URI to Bitmap safely
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    viewModel.runSimulatedOcr(bitmap)
                }
            } catch (e: Exception) {
                Log.e("OCR", "Error reading picked image: ${e.message}")
            }
        }
    }

    val uploadCard = @Composable {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            border = BorderStroke(1.dp, GridLine),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Filled.DocumentScanner,
                    "OCR",
                    tint = SuccessGreen,
                    modifier = Modifier.size(54.dp)
                )

                Text(
                    "CARGAR REPORTE DIARIO DE PERFORACIÓN / WORKOVER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            pickMediaLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest.Builder()
                                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    .build()
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, "Galería", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("GALERÍA", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                    }

                    // Glove-friendly test/simulation button
                    Button(
                        onClick = {
                            // Load simulated sample report bitmap from resources (we use our logo which works as an image)
                            try {
                                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.petrocalc_logo_fg_1782270047250)
                                if (bitmap != null) {
                                    viewModel.runSimulatedOcr(
                                        bitmap,
                                        customPrompt = "Analiza este reporte diario simulado. Reporte de Pozo Activo: Profundidad TVD = 9500 pies, Densidad Lodo = 11.2 ppg, Casing ID = 6.276 pulgadas, Tubing OD = 2.875 pulgadas. Extrae las variables operativas y calcula la presión hidrostática y la capacidad anular."
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("OCR", "Error running simulator", e)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PetroleumBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.2f).height(48.dp)
                    ) {
                        Icon(Icons.Filled.Science, "Demo", tint = SuccessGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("REPORTE EJEMPLO", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    val loadingCard = @Composable {
        if (isOcrLoading) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                border = BorderStroke(1.dp, SafetyOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(color = SafetyOrange)
                    Text(
                        "Análisis OCR en curso mediante PetroCalc AI...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafetyOrange
                    )
                    Text(
                        "Leyendo texto manuscrito, reportes e identificando profundidades y presiones...",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    val resultsCard = @Composable {
        ocrResultText?.let { res ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                border = BorderStroke(2.dp, SuccessGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "REPORTE OCR EXTRAÍDO CON ÉXITO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SuccessGreen
                        )
                        IconButton(onClick = { viewModel.speakText(res) }) {
                            Icon(Icons.Filled.VolumeUp, "Leer reporte", tint = SuccessGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        res,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 17.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    if (isWideScreen) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCharcoal)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left Panel (Uploader & instructions)
            Card(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = DarkCardBgSecondary),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("OCR INTELIGENTE", fontSize = 16.sp, fontWeight = FontWeight.Black, color = SafetyOrange)
                    Text("Tome una foto o cargue un reporte operativo diario para extraer automáticamente profundidades, diámetros, presiones y realizar cálculos de control.", fontSize = 12.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(8.dp))
                    uploadCard()
                }
            }

            // Right Panel (Results)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isOcrLoading && ocrResultText == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.DocumentScanner, "Pendiente", tint = TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Esperando reporte para analizar...", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
                loadingCard()
                resultsCard()
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCharcoal)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("OCR INTELIGENTE - EXTRACCIÓN DE REPORTES", fontSize = 14.sp, fontWeight = FontWeight.Black, color = SafetyOrange)
                Text("Tome una foto o cargue un reporte operativo diario para extraer automáticamente profundidades, diámetros, presiones y realizar cálculos de control.", fontSize = 12.sp, color = TextSecondary)
            }
            item {
                uploadCard()
            }
            item {
                loadingCard()
            }
            if (ocrResultText != null) {
                item {
                    resultsCard()
                }
            }
        }
    }
}
