package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.SpecializedFormula
import com.example.ui.theme.*
import com.example.ui.viewmodel.PetroCalcViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecializedFormulasCatalogScreen(
    viewModel: PetroCalcViewModel,
    snackbarHostState: SnackbarHostState
) {
    val formulas by viewModel.specializedFormulas.collectAsState()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Dropdown/Selector states for dialog / form
    var newName by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Perforación") }
    var newMathFormula by remember { mutableStateOf("") }
    var newSource by remember { mutableStateOf("") }
    var newInputUnits by remember { mutableStateOf("") }
    var newOutputUnits by remember { mutableStateOf("") }

    val categories = listOf("Todas", "Perforación", "Cementación", "Workover", "Producción")

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    val filteredFormulas = formulas.filter { formula ->
        val matchesSearch = formula.name.contains(searchQuery, ignoreCase = true) ||
                formula.description.contains(searchQuery, ignoreCase = true) ||
                formula.mathFormula.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "Todas" || formula.category.equals(selectedCategory, ignoreCase = true)
        matchesSearch && matchesCategory
    }

    val formFields = @Composable {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Nombre de la Fórmula", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth().testTag("new_formula_name"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SafetyOrange,
                    unfocusedBorderColor = GridLine
                )
            )

            OutlinedTextField(
                value = newDescription,
                onValueChange = { newDescription = it },
                label = { Text("Descripción Operativa", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SafetyOrange,
                    unfocusedBorderColor = GridLine
                ),
                maxLines = 2
            )

            Text("Categoría Técnica", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafetyOrange)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val activeCategories = listOf("Perforación", "Cementación", "Workover", "Producción")
                activeCategories.forEach { cat ->
                    val isSel = newCategory == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) PetroleumBlue else DarkCardBgSecondary)
                            .border(
                                BorderStroke(1.dp, if (isSel) PetroleumBlue else GridLine),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { newCategory = cat }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(cat, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            OutlinedTextField(
                value = newMathFormula,
                onValueChange = { newMathFormula = it },
                label = { Text("Ecuación Matemática (ej. Ph = 0.052 * D * H)", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SafetyOrange,
                    unfocusedBorderColor = GridLine
                )
            )

            OutlinedTextField(
                value = newSource,
                onValueChange = { newSource = it },
                label = { Text("Fuente Técnica (ej. API Spec 10A)", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SafetyOrange,
                    unfocusedBorderColor = GridLine
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newInputUnits,
                    onValueChange = { newInputUnits = it },
                    label = { Text("U. Entrada", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafetyOrange,
                        unfocusedBorderColor = GridLine
                    )
                )

                OutlinedTextField(
                    value = newOutputUnits,
                    onValueChange = { newOutputUnits = it },
                    label = { Text("U. Salida", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafetyOrange,
                        unfocusedBorderColor = GridLine
                    )
                )
            }
        }
    }

    val saveFormula = {
        if (newName.isBlank() || newMathFormula.isBlank() || newSource.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar("Por favor, rellene Nombre, Fórmula y Fuente Técnica.")
            }
        } else {
            val entity = SpecializedFormula(
                name = newName,
                description = newDescription,
                category = newCategory,
                mathFormula = newMathFormula,
                technicalSource = newSource,
                inputUnits = if (newInputUnits.isBlank()) "No especificadas" else newInputUnits,
                outputUnits = if (newOutputUnits.isBlank()) "No especificadas" else newOutputUnits
            )
            viewModel.insertSpecializedFormula(entity)
            showAddDialog = false
            // Clear inputs
            newName = ""
            newDescription = ""
            newCategory = "Perforación"
            newMathFormula = ""
            newSource = ""
            newInputUnits = ""
            newOutputUnits = ""
            scope.launch {
                snackbarHostState.showSnackbar("Fórmula técnica guardada exitosamente.")
            }
        }
    }

    if (isWideScreen) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left Panel (Search, Filter and Add Form)
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Column {
                    Text(
                        "FÓRMULAS ESPECIALIZADAS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = SafetyOrange,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Catálogo dinámico de ecuaciones de ingeniería petrolera offline.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar fórmula...", color = TextMuted, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Close, null, tint = TextMuted)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("search_formula_field"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkCardBgSecondary,
                        unfocusedContainerColor = DarkCardBgSecondary,
                        focusedBorderColor = SafetyOrange,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    singleLine = true
                )

                // Add formula direct card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBgSecondary),
                    border = BorderStroke(1.dp, BorderSubtle),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "REGISTRAR FÓRMULA TÉCNICA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafetyOrange,
                            letterSpacing = 0.5.sp
                        )
                        formFields()
                        Button(
                            onClick = { saveFormula() },
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text("GUARDAR EN BASE DE DATOS", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Right Panel (Categories & Formula Cards List)
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SafetyOrange else DarkCardBgSecondary)
                                .border(
                                    BorderStroke(1.dp, if (isSelected) SafetyOrange else BorderSubtle),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) Color.Black else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // List of Formulas
                if (filteredFormulas.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.AutoStories,
                                contentDescription = null,
                                tint = PetroleumBlue.copy(alpha = 0.3f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No se encontraron fórmulas",
                                color = TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredFormulas, key = { it.id }) { formula ->
                            FormulaCard(
                                formula = formula,
                                onDelete = {
                                    viewModel.deleteSpecializedFormula(formula)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Fórmula eliminada correctamente")
                                    }
                                },
                                onListen = {
                                    viewModel.speakText("Fórmula especializada ${formula.name}: ${formula.mathFormula}. Categoría: ${formula.category}. Fuente técnica: ${formula.technicalSource}")
                                }
                            )
                        }
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title & Description
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "FÓRMULAS ESPECIALIZADAS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = SafetyOrange,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Catálogo dinámico de ecuaciones de ingeniería petrolera offline.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_formula_btn")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir fórmula", tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("NUEVA", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por nombre, descripción o fórmula...", color = TextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, null, tint = TextMuted)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("search_formula_field"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkCardBgSecondary,
                    unfocusedContainerColor = DarkCardBgSecondary,
                    focusedBorderColor = SafetyOrange,
                    unfocusedBorderColor = BorderSubtle
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SafetyOrange else DarkCardBgSecondary)
                            .border(
                                BorderStroke(1.dp, if (isSelected) SafetyOrange else BorderSubtle),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.Black else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of formulas
            if (filteredFormulas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.AutoStories,
                            contentDescription = null,
                            tint = PetroleumBlue.copy(alpha = 0.3f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No se encontraron fórmulas",
                            color = TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Intente cambiar los filtros o añada una nueva fórmula",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredFormulas, key = { it.id }) { formula ->
                        FormulaCard(
                            formula = formula,
                            onDelete = {
                                viewModel.deleteSpecializedFormula(formula)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Fórmula eliminada correctamente")
                                }
                            },
                            onListen = {
                                viewModel.speakText("Fórmula especializada ${formula.name}: ${formula.mathFormula}. Categoría: ${formula.category}. Fuente técnica: ${formula.technicalSource}")
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Formula Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    "REGISTRAR FÓRMULA TÉCNICA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = SafetyOrange,
                    letterSpacing = 1.sp
                )
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Ingrese los metadatos técnicos oficiales para la nueva ecuación en la base de datos PetroCalc.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        formFields()
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { saveFormula() },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange)
                ) {
                    Text("GUARDAR", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("CANCELAR", color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkCardBg
        )
    }
}

@Composable
fun FormulaCard(
    formula: SpecializedFormula,
    onDelete: () -> Unit,
    onListen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PetroleumBlue.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, PetroleumBlue.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category Badge and voice/delete buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (formula.category.lowercase()) {
                                "perforación" -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                "cementación" -> Color(0xFFA855F7).copy(alpha = 0.2f)
                                "workover" -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                "producción" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                else -> SafetyOrange.copy(alpha = 0.2f)
                            }
                        )
                        .border(
                            BorderStroke(
                                0.5.dp,
                                when (formula.category.lowercase()) {
                                    "perforación" -> Color(0xFF3B82F6)
                                    "cementación" -> Color(0xFFA855F7)
                                    "workover" -> Color(0xFFF59E0B)
                                    "producción" -> Color(0xFF10B981)
                                    else -> SafetyOrange
                                }
                            ),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        formula.category.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = when (formula.category.lowercase()) {
                            "perforación" -> Color(0xFF93C5FD)
                            "cementación" -> Color(0xFFC084FC)
                            "workover" -> Color(0xFFFBBF24)
                            "producción" -> Color(0xFF34D399)
                            else -> SafetyOrange
                        }
                    )
                }

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onListen,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.VolumeUp,
                            "Escuchar Fórmula",
                            tint = SafetyOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            "Eliminar Fórmula",
                            tint = AlertRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Formula Name
            Text(
                formula.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            // Formula Description
            Text(
                formula.description,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mathematical Box (Terminal styling)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    formula.mathFormula,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input / Output units & Technical source
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.Input, "Entrada", tint = TextMuted, modifier = Modifier.size(11.dp))
                        Text(
                            "Entrada: ${formula.inputUnits}",
                            fontSize = 10.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.Output, "Salida", tint = TextMuted, modifier = Modifier.size(11.dp))
                        Text(
                            "Salida: ${formula.outputUnits}",
                            fontSize = 10.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(Icons.Filled.MenuBook, "Fuente", tint = SafetyOrange.copy(alpha = 0.7f), modifier = Modifier.size(11.dp))
                    Text(
                        formula.technicalSource,
                        fontSize = 10.sp,
                        color = SafetyOrange.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
