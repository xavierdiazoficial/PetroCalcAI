package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.PetroCalcViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- STYLUS SKETCHPAD STRUCTURES ---

data class DrawingPoint(
    val offset: Offset,
    val pressure: Float
)

data class DrawingStroke(
    val points: List<DrawingPoint>,
    val color: Color,
    val strokeWidth: Float,
    val isEraser: Boolean = false,
    val toolType: String = "Finger"
)

data class SavedDrawing(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: String,
    val templateName: String,
    val strokes: List<DrawingStroke>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalSketchpadScreen(
    viewModel: PetroCalcViewModel,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    // Local canvas states
    var strokes by remember { mutableStateOf(listOf<DrawingStroke>()) }
    var undoneStrokes by remember { mutableStateOf(listOf<DrawingStroke>()) }
    
    var currentColor by remember { mutableStateOf(SafetyOrange) }
    var currentWidth by remember { mutableStateOf(4f) }
    var isEraserMode by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf("Vertical Wellbore") } // None, Vertical Wellbore, Cementing Job, Storage Tank, Pressure Grid

    // Saved sketches repository (live view state, saved to a list)
    var savedDrawings by remember {
        mutableStateOf(
            listOf(
                SavedDrawing(
                    id = "demo-1",
                    title = "Croquis Completación Pozo L-20",
                    date = "Hoy, 10:45 AM",
                    templateName = "Vertical Wellbore",
                    strokes = listOf(
                        DrawingStroke(
                            points = listOf(
                                DrawingPoint(Offset(250f, 400f), 1f),
                                DrawingPoint(Offset(320f, 410f), 1f)
                            ),
                            color = SafetyOrange,
                            strokeWidth = 6f
                        )
                    )
                ),
                SavedDrawing(
                    id = "demo-2",
                    title = "Monitoreo Nivel de Tanque B-4",
                    date = "Ayer, 03:20 PM",
                    templateName = "Storage Tank",
                    strokes = emptyList()
                )
            )
        )
    }

    // Active tool telemetry
    var lastToolUsed by remember { mutableStateOf("Ninguno") }
    var lastPressureUsed by remember { mutableStateOf(0f) }

    // Dialog for saving
    var showSaveDialog by remember { mutableStateOf(false) }
    var newDrawingTitle by remember { mutableStateOf("") }

    // Preset options for canvas overlays
    val templates = listOf(
        "Vertical Wellbore" to "Esquema Pozo",
        "Cementing Job" to "Cementación",
        "Storage Tank" to "Tanque Horizontal",
        "Pressure Grid" to "Grilla de Presión",
        "None" to "Lienzo Blanco"
    )

    val colorsList = listOf(
        SafetyOrange to "Naranja",
        SuccessGreen to "Verde",
        PetroleumBlue to "Azul",
        Color.White to "Blanco",
        AlertYellow to "Amarillo",
        AlertRed to "Rojo"
    )

    val brushSizes = listOf(
        2f to "Fino",
        4f to "Medio",
        8f to "Grueso",
        15f to "Marcador"
    )

    // Layout controls
    val controlPanel = @Composable {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stylus / Hardware Telemetry Header
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBgSecondary),
                border = BoxBorderSubtle(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Gesture, "Lápiz", tint = SafetyOrange, modifier = Modifier.size(18.dp))
                        Text(
                            "TELEMETRÍA DE COMPATIBILIDAD DE LÁPIZ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SafetyOrange
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Admite lápices digitales de alta precisión (Samsung S-Pen, Lenovo Pen, USI) con reconocimiento de presión física y modo borrador integrado.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ÚLTIMO DISPOSITIVO DETECTADO", fontSize = 8.sp, color = TextMuted)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    when (lastToolUsed) {
                                        "Stylus" -> Icons.Filled.Edit
                                        "Eraser" -> Icons.Filled.CleaningServices
                                        "Finger" -> Icons.Filled.TouchApp
                                        else -> Icons.Filled.DeviceUnknown
                                    },
                                    null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    if (lastToolUsed == "Ninguno") "Esperando trazo..." else "Lápiz/Pantalla: $lastToolUsed",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("PRESIÓN COMPATIBLE", fontSize = 8.sp, color = TextMuted)
                            Text(
                                if (lastPressureUsed == 0f) "Pasiva (0.0)" else "Activa (${String.format("%.2f", lastPressureUsed)} G-Force)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (lastPressureUsed > 0f) SuccessGreen else TextPrimary
                            )
                        }
                    }
                }
            }

            // Canvas Templates Grid Selector
            Text("PLANTILLA TÉCNICA DE FONDO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafetyOrange)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                templates.forEach { (id, label) ->
                    val isSelected = selectedTemplate == id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PetroleumBlue else DarkCardBgSecondary)
                            .border(BoxBorder(isSelected))
                            .clickable { selectedTemplate = id }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Brush tool options
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBgSecondary),
                border = BoxBorderSubtle(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("HERRAMIENTAS DE EDICIÓN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafetyOrange)
                    
                    // Eraser vs Pen Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isEraserMode = false },
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isEraserMode) PetroleumBlue else DarkCardBg
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BoxBorder(!isEraserMode)
                        ) {
                            Icon(Icons.Filled.Brush, "Lápiz", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DIBUJAR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { isEraserMode = true },
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEraserMode) AlertYellow else DarkCardBg
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BoxBorder(isEraserMode, color = AlertYellow)
                        ) {
                            Icon(Icons.Filled.CleaningServices, "Borrador", tint = if (isEraserMode) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BORRADOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isEraserMode) Color.Black else Color.White)
                        }
                    }

                    if (!isEraserMode) {
                        // Brush Colors List
                        Text("Color del Trazo", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            colorsList.forEach { (color, name) ->
                                val isSelected = currentColor == color
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(color)
                                        .border(
                                            BorderStroke(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) Color.White else Color.Transparent
                                            ),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { currentColor = color }
                                )
                            }
                        }

                        // Brush Width
                        Text("Grosor del Trazo", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            brushSizes.forEach { (size, label) ->
                                val isSelected = currentWidth == size
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) SafetyOrange else DarkCardBg)
                                        .border(BoxBorder(isSelected))
                                        .clickable { currentWidth = size }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Canvas Actions (Undo, Redo, Clear, Save)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (strokes.isNotEmpty()) {
                            undoneStrokes = undoneStrokes + strokes.last()
                            strokes = strokes.dropLast(1)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(DarkCardBgSecondary, RoundedCornerShape(8.dp))
                        .border(BoxBorderSubtle(), RoundedCornerShape(8.dp)),
                    enabled = strokes.isNotEmpty()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Undo, "Deshacer", tint = if (strokes.isNotEmpty()) Color.White else TextMuted, modifier = Modifier.size(16.dp))
                        Text("Deshacer", fontSize = 11.sp, color = if (strokes.isNotEmpty()) Color.White else TextMuted, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(
                    onClick = {
                        if (undoneStrokes.isNotEmpty()) {
                            strokes = strokes + undoneStrokes.last()
                            undoneStrokes = undoneStrokes.dropLast(1)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(DarkCardBgSecondary, RoundedCornerShape(8.dp))
                        .border(BoxBorderSubtle(), RoundedCornerShape(8.dp)),
                    enabled = undoneStrokes.isNotEmpty()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Redo, "Rehacer", tint = if (undoneStrokes.isNotEmpty()) Color.White else TextMuted, modifier = Modifier.size(16.dp))
                        Text("Rehacer", fontSize = 11.sp, color = if (undoneStrokes.isNotEmpty()) Color.White else TextMuted, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(
                    onClick = {
                        strokes = emptyList()
                        undoneStrokes = emptyList()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(DarkCardBgSecondary, RoundedCornerShape(8.dp))
                        .border(BoxBorderSubtle(), RoundedCornerShape(8.dp)),
                    enabled = strokes.isNotEmpty()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Delete, "Limpiar", tint = if (strokes.isNotEmpty()) AlertRed else TextMuted, modifier = Modifier.size(16.dp))
                        Text("Limpiar", fontSize = 11.sp, color = if (strokes.isNotEmpty()) AlertRed else TextMuted, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Save Drawing trigger
            Button(
                onClick = {
                    newDrawingTitle = "Croquis Pozo " + SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())
                    showSaveDialog = true
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Save, "Guardar", tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("GUARDAR CROQUIS EN HISTORIAL", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
            }
        }
    }

    // List of saved sketches
    val savedSketchesList = @Composable {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "MIS CROQUIS GUARDADOS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SafetyOrange,
                modifier = Modifier.padding(top = 10.dp)
            )
            
            if (savedDrawings.isEmpty()) {
                Text("No hay croquis guardados aún.", fontSize = 11.sp, color = TextMuted)
            } else {
                savedDrawings.forEach { drawing ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Load this drawing back into canvas!
                                strokes = drawing.strokes
                                selectedTemplate = drawing.templateName
                                undoneStrokes = emptyList()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Croquis '${drawing.title}' cargado en el lienzo.")
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = DarkCardBgSecondary),
                        border = BoxBorderSubtle(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(drawing.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(
                                    "Plantilla: ${drawing.templateName} • ${drawing.date}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        savedDrawings = savedDrawings.filter { it.id != drawing.id }
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Croquis eliminado.")
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.DeleteOutline, "Borrar", tint = AlertRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // THE CANVAS CARD - Full vector overlay capabilities
    val canvasCard = @Composable {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .border(BorderStroke(1.dp, GridLine), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Interactive draw Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("stylus_drawing_canvas")
                        .pointerInput(currentColor, currentWidth, isEraserMode) {
                            // High precision stylus, eraser & multitouch input loop
                            awaitEachGesture {
                                val firstDown = awaitFirstDown(requireUnconsumed = false)
                                val toolType = firstDown.type
                                val isEraser =
                                    isEraserMode || toolType == PointerType.Eraser

                                lastToolUsed = when (toolType) {
                                    PointerType.Stylus -> "Stylus"
                                    PointerType.Eraser -> "Eraser"
                                    PointerType.Touch -> "Finger"
                                    PointerType.Mouse -> "Mouse"
                                    else -> "Unknown"
                                }
                                lastPressureUsed = firstDown.pressure

                                var strokePoints = mutableListOf(
                                    DrawingPoint(
                                        firstDown.position,
                                        firstDown.pressure
                                    )
                                )
                                val newStroke = DrawingStroke(
                                    points = strokePoints,
                                    color = if (isEraser) Color.Black else currentColor,
                                    strokeWidth = if (isEraser) 30f else currentWidth,
                                    isEraser = isEraser,
                                    toolType = lastToolUsed
                                )
                                strokes = strokes + newStroke

                                val currentStrokeIndex = strokes.lastIndex

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val anyPressed =
                                        event.changes.any { it.pressed }
                                    if (!anyPressed) {
                                        break
                                    }

                                    val change =
                                        event.changes.firstOrNull { it.pressed }
                                    if (change != null) {
                                        lastPressureUsed = change.pressure
                                        val point = DrawingPoint(
                                            change.position,
                                            change.pressure
                                        )

                                        strokes =
                                            strokes
                                                .toMutableList()
                                                .apply {
                                                    val lastStroke =
                                                        this[currentStrokeIndex]
                                                    this[currentStrokeIndex] =
                                                        lastStroke.copy(
                                                            points = lastStroke.points + point
                                                        )
                                                }
                                        change.consume()
                                    }
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    // 1. Draw static grid under everything
                    val gridSpacing = 60f
                    for (x in 0..(width / gridSpacing).toInt()) {
                        drawLine(
                            color = GridLine.copy(alpha = 0.4f),
                            start = Offset(x * gridSpacing, 0f),
                            end = Offset(x * gridSpacing, height),
                            strokeWidth = 1f
                        )
                    }
                    for (y in 0..(height / gridSpacing).toInt()) {
                        drawLine(
                            color = GridLine.copy(alpha = 0.4f),
                            start = Offset(0f, y * gridSpacing),
                            end = Offset(width, y * gridSpacing),
                            strokeWidth = 1f
                        )
                    }

                    // 2. Draw chosen technical background template
                    when (selectedTemplate) {
                        "Vertical Wellbore" -> {
                            val cx = width / 2f
                            
                            // Drill hole (open hole / mud column background)
                            drawRect(
                                color = Color(0xFF1E1E1C),
                                topLeft = Offset(cx - 110f, 60f),
                                size = Size(220f, height - 120f)
                            )
                            
                            // Casing (Thick steel container)
                            drawLine(color = Color.Gray, start = Offset(cx - 100f, 60f), end = Offset(cx - 100f, height - 150f), strokeWidth = 8f)
                            drawLine(color = Color.Gray, start = Offset(cx + 100f, 60f), end = Offset(cx + 100f, height - 150f), strokeWidth = 8f)
                            // Bottom of casing shoe
                            drawLine(color = Color.Gray, start = Offset(cx - 100f, height - 150f), end = Offset(cx - 80f, height - 130f), strokeWidth = 8f)
                            drawLine(color = Color.Gray, start = Offset(cx + 100f, height - 150f), end = Offset(cx + 80f, height - 130f), strokeWidth = 8f)

                            // Tubing (Internal green string)
                            drawRect(
                                color = PetroleumBlue.copy(alpha = 0.3f),
                                topLeft = Offset(cx - 30f, 40f),
                                size = Size(60f, height - 100f)
                            )
                            drawLine(color = SuccessGreen, start = Offset(cx - 30f, 40f), end = Offset(cx - 30f, height - 100f), strokeWidth = 4f)
                            drawLine(color = SuccessGreen, start = Offset(cx + 30f, 40f), end = Offset(cx + 30f, height - 100f), strokeWidth = 4f)

                            // Center axis line
                            drawLine(
                                color = SafetyOrange.copy(alpha = 0.5f),
                                start = Offset(cx, 20f),
                                end = Offset(cx, height - 40f),
                                strokeWidth = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            )
                        }
                        "Cementing Job" -> {
                            val cx = width / 2f
                            
                            // Borehole Background
                            drawRect(
                                color = Color(0xFF151515),
                                topLeft = Offset(cx - 120f, 50f),
                                size = Size(240f, height - 100f)
                            )
                            
                            // Cement slurry in the lower annulus (concrete grey/greenish)
                            drawRect(
                                color = Color(0xFF555A5C),
                                topLeft = Offset(cx - 110f, height - 350f),
                                size = Size(220f, 300f)
                            )

                            // Inner casing
                            drawRect(
                                color = Color(0xFF1C2224),
                                topLeft = Offset(cx - 80f, 50f),
                                size = Size(160f, height - 100f)
                            )
                            drawLine(color = Color.LightGray, start = Offset(cx - 80f, 50f), end = Offset(cx - 80f, height - 50f), strokeWidth = 6f)
                            drawLine(color = Color.LightGray, start = Offset(cx + 80f, 50f), end = Offset(cx + 80f, height - 50f), strokeWidth = 6f)

                            // Cement Plug representation
                            drawRect(
                                color = AlertYellow,
                                topLeft = Offset(cx - 78f, height - 380f),
                                size = Size(156f, 40f)
                            )
                        }
                        "Storage Tank" -> {
                            val cy = height / 2f
                            val cx = width / 2f

                            // Draw cylindrical tank shell
                            drawRoundRect(
                                color = Color.DarkGray,
                                topLeft = Offset(cx - 220f, cy - 120f),
                                size = Size(440f, 240f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(40f, 40f),
                                style = Stroke(width = 6f)
                            )

                            // Fluid Level Overlay representation
                            drawRoundRect(
                                color = PetroleumBlue.copy(alpha = 0.25f),
                                topLeft = Offset(cx - 217f, cy + 20f),
                                size = Size(434f, 96f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(37f, 37f)
                            )

                            // Annotations levels
                            drawLine(color = AlertRed, start = Offset(cx - 240f, cy - 120f), end = Offset(cx - 220f, cy - 120f), strokeWidth = 2f)
                            drawLine(color = AlertYellow, start = Offset(cx - 240f, cy), end = Offset(cx - 220f, cy), strokeWidth = 2f)
                            drawLine(color = SuccessGreen, start = Offset(cx - 240f, cy + 120f), end = Offset(cx - 220f, cy + 120f), strokeWidth = 2f)
                        }
                        "Pressure Grid" -> {
                            // Coordinate labels
                            drawLine(color = Color.White, start = Offset(60f, 40f), end = Offset(60f, height - 60f), strokeWidth = 4f)
                            drawLine(color = Color.White, start = Offset(60f, height - 60f), end = Offset(width - 40f, height - 60f), strokeWidth = 4f)
                        }
                    }

                    // 3. Render strokes with dynamic stylus pressure sensitivity
                    strokes.forEach { stroke ->
                        if (stroke.points.size > 1) {
                            for (i in 0 until stroke.points.size - 1) {
                                val p1 = stroke.points[i]
                                val p2 = stroke.points[i + 1]
                                val avgPressure = (p1.pressure + p2.pressure) / 2f
                                
                                // Dynamic width scaling based on actual stylus pressure
                                val adjustedWidth = stroke.strokeWidth * (0.4f + avgPressure * 1.6f)
                                
                                drawLine(
                                    color = stroke.color,
                                    start = p1.offset,
                                    end = p2.offset,
                                    strokeWidth = adjustedWidth,
                                    cap = StrokeCap.Round
                                )
                            }
                        } else if (stroke.points.size == 1) {
                            val p = stroke.points.first()
                            val adjustedWidth = stroke.strokeWidth * (0.4f + p.pressure * 1.6f)
                            drawCircle(
                                color = stroke.color,
                                center = p.offset,
                                radius = adjustedWidth / 2f
                            )
                        }
                    }
                }

                // Transparent card title/labels overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "CUADRICULA TÉCNICA ACTIVA: ${selectedTemplate.uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafetyOrange,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(SuccessGreen))
                            Text("LIENZO LISTO PARA LÁPIZ", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = SuccessGreen)
                        }
                    }
                }
            }
        }
    }

    // MAIN LAYOUT
    if (isWideScreen) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCharcoal)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left Toolbar Panel
            Card(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = DarkCardBgSecondary),
                shape = RoundedCornerShape(24.dp),
                border = BoxBorderSubtle()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "DISEÑO Y CROQUIS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = SafetyOrange
                    )
                    Text(
                        "Dibuje diagramas de pozos, registre completaciones y anote esquemas directamente en el campo de forma offline.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    
                    Divider(color = GridLine)
                    controlPanel()
                    Divider(color = GridLine)
                    savedSketchesList()
                }
            }

            // Right Giant Canvas
            Box(
                modifier = Modifier
                    .weight(1.4f)
                    .fillMaxHeight()
            ) {
                canvasCard()
            }
        }
    } else {
        // Vertical Mobile Scroll
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCharcoal)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "DISEÑO Y CROQUIS TÉCNICOS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = SafetyOrange
                )
                Text(
                    "Lienzo de dibujo de alta sensibilidad optimizado para lápices ópticos (Stylus/Samsung S-Pen) y pantallas táctiles industriales.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            item {
                Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                    canvasCard()
                }
            }

            item {
                controlPanel()
            }

            item {
                savedSketchesList()
            }
        }
    }

    // Save Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    "GUARDAR CROQUIS TÉCNICO",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafetyOrange
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Asigne un nombre a este esquema de pozo para guardarlo de manera offline.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = newDrawingTitle,
                        onValueChange = { newDrawingTitle = it },
                        label = { Text("Nombre del Croquis", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SafetyOrange,
                            unfocusedBorderColor = GridLine
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newDrawing = SavedDrawing(
                            title = newDrawingTitle,
                            date = "Hoy, " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
                            templateName = selectedTemplate,
                            strokes = strokes
                        )
                        savedDrawings = listOf(newDrawing) + savedDrawings
                        showSaveDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Croquis técnico guardado exitosamente.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange)
                ) {
                    Text("GUARDAR", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("CANCELAR", color = TextSecondary)
                }
            },
            containerColor = DarkCardBg
        )
    }
}

// Helpers for borders matching existing styling
@Composable
fun BoxBorder(isSelected: Boolean, color: Color = SafetyOrange): BorderStroke {
    return BorderStroke(
        width = if (isSelected) 1.5.dp else 1.dp,
        color = if (isSelected) color else BorderSubtle.copy(alpha = 0.5f)
    )
}

@Composable
fun BoxBorderSubtle(): BorderStroke {
    return BorderStroke(1.dp, BorderSubtle)
}
