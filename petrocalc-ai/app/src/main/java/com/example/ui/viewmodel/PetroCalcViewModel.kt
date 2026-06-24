package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.database.AppDatabase
import com.example.data.database.CalculationRecord
import com.example.data.database.SpecializedFormula
import com.example.data.formulas.OilfieldFormulas
import com.example.data.repository.CalculationRepository
import com.example.data.repository.SpecializedFormulaRepository
import com.example.ui.voice.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class PetroCalcViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = CalculationRepository(database.calculationDao())
    private val formulaRepository = SpecializedFormulaRepository(database.specializedFormulaDao())

    val history: StateFlow<List<CalculationRecord>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val specializedFormulas: StateFlow<List<SpecializedFormula>> = formulaRepository.allFormulas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Voice Engine
    private var voiceManager: VoiceManager? = null

    // User Profile
    val currentUser = MutableStateFlow("Ing. Mendoza")
    val currentRole = MutableStateFlow("Ingeniero de Campo") // Operador, Supervisor, Ingeniero, Administrador

    // Active Screen (Navigation fallback)
    val activeScreen = MutableStateFlow("dashboard") // dashboard, calculators, ai_assistant, conversions, history, ocr

    // Active Tab inside Calculators
    val activeCalcTab = MutableStateFlow("hidrostatica") // hidrostatica, capacidad, anular, velocidad, cementacion, produccion, tanques

    // AI Chat Thread
    val aiMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "¡Saludos! Soy PetroCalc AI, tu asistente experto de campo. Puedo interpretar tus comandos por voz, dar soporte en cálculos, cementaciones, producción y OCR. ¿En qué pozo trabajaremos hoy?",
                isUser = false
            )
        )
    )
    val isAiLoading = MutableStateFlow(false)

    // Manual Calculations Inputs & Results
    // Hydrostatic
    val hydDensity = MutableStateFlow("10.5")
    val hydDepth = MutableStateFlow("8500")
    val hydResult = MutableStateFlow<OilfieldFormulas.CalcResult?>(null)

    // Tubing/Casing Capacity
    val capId = MutableStateFlow("2.441") // ID standard 2-7/8 tubing
    val capLength = MutableStateFlow("9000")
    val capResult = MutableStateFlow<OilfieldFormulas.CalcResult?>(null)

    // Annular Capacity
    val annCasingId = MutableStateFlow("6.276") // ID standard 7 in casing
    val annTubingOd = MutableStateFlow("2.875") // OD 2-7/8 tubing
    val annLength = MutableStateFlow("9000")
    val annResult = MutableStateFlow<OilfieldFormulas.CalcResult?>(null)

    // Annular Velocity / Pump Time
    val velPumpRate = MutableStateFlow("150") // gpm
    val velCasingId = MutableStateFlow("6.276")
    val velTubingOd = MutableStateFlow("2.875")
    val velVolumeToPump = MutableStateFlow("400") // bbl
    val velResult = MutableStateFlow<OilfieldFormulas.CalcResult?>(null)
    val pumpTimeResult = MutableStateFlow<OilfieldFormulas.CalcResult?>(null)

    // Cementing
    val cemAnnularCap = MutableStateFlow("0.0326") // bbl/ft
    val cemHeight = MutableStateFlow("2000") // ft
    val cemExcess = MutableStateFlow("20") // %
    val cemYield = MutableStateFlow("1.18") // cu ft/sk
    val cemResult = MutableStateFlow<OilfieldFormulas.CalcResult?>(null)

    // Production
    val prodLiquidVol = MutableStateFlow("1200") // bbl
    val prodDays = MutableStateFlow("1.0") // day
    val prodOilCut = MutableStateFlow("45.0") // %
    val prodBo = MutableStateFlow("1.12") // formation volume factor
    val prodResult = MutableStateFlow<OilfieldFormulas.CalcResult?>(null)

    // Tanks
    val tankType = MutableStateFlow("vertical") // vertical or horizontal
    val tankRadius = MutableStateFlow("10.0") // ft
    val tankHeight = MutableStateFlow("20.0") // ft or length
    val tankFluidLevel = MutableStateFlow("14.5") // ft
    val tankResult = MutableStateFlow<OilfieldFormulas.CalcResult?>(null)

    // Unit Converter
    val convValue = MutableStateFlow("1000")
    val convCategory = MutableStateFlow("Presión") // Presión, Longitud, Volumen, Temperatura, Densidad, Caudal
    val convFromUnit = MutableStateFlow("psi")
    val convToUnit = MutableStateFlow("bar")
    val convResult = MutableStateFlow("68.95 bar")

    // OCR simulated state
    val ocrImageUri = MutableStateFlow<String?>(null)
    val isOcrLoading = MutableStateFlow(false)
    val ocrResult = MutableStateFlow<String?>(null)

    // Active Alerts / Alarms List
    val activeAlarms = MutableStateFlow<List<String>>(
        listOf(
            "SISTEMA OK: PetroCalc AI operando en modo campo seguro offline.",
            "AVISO DE RANGO: Lodo base agua en pozo activo registrado en 10.5 ppg."
        )
    )

    init {
        // Initialize Speech Manager
        viewModelScope.launch {
            try {
                voiceManager = VoiceManager(application)
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to start TTS", e)
            }
        }

        // Initialize Specialized Formulas database with technical standard defaults
        viewModelScope.launch {
            try {
                formulaRepository.populateDefaultFormulasIfNeeded()
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to populate specialized formulas", e)
            }
        }
        
        // Execute initial calculations to populate defaults
        calculateAllDefaults()
    }

    private fun calculateAllDefaults() {
        runHydrostatic()
        runCapacity()
        runAnnular()
        runAnnularVelocity()
        runCementing()
        runProduction()
        runTanks()
        runConversion()
    }

    // Speak helper
    fun speakText(text: String) {
        voiceManager?.speak(text)
    }

    // Alert manager
    private fun updateActiveAlarms(newAlerts: List<String>) {
        val current = activeAlarms.value.toMutableList()
        // Remove old warnings, keep system logs, and add new ones
        current.removeAll { it.startsWith("ALERTA") || it.startsWith("ADVERTENCIA") }
        current.addAll(0, newAlerts)
        activeAlarms.value = current
    }

    // --- MANAGE ROLES ---
    fun setRole(role: String) {
        currentRole.value = role
        currentUser.value = when (role) {
            "Operador" -> "Operador Gomez"
            "Supervisor" -> "Supervisor Silva"
            "Ingeniero de Campo" -> "Ing. Mendoza"
            "Administrador" -> "Admin Petro"
            else -> "Personal Petro"
        }
    }

    // --- RUN CALC MODULES & SAVE TO ROOM ---
    fun runHydrostatic() {
        val density = hydDensity.value.toDoubleOrNull() ?: 0.0
        val depth = hydDepth.value.toDoubleOrNull() ?: 0.0
        val res = OilfieldFormulas.calculateHydrostaticPressure(density, depth)
        hydResult.value = res
        updateActiveAlarms(res.alerts)
    }

    fun saveHydrostaticToHistory() {
        val res = hydResult.value ?: return
        viewModelScope.launch {
            val record = CalculationRecord(
                user = currentUser.value,
                role = currentRole.value,
                type = "Presión Hidrostática",
                formula = res.formula,
                inputs = "Densidad: ${hydDensity.value} ppg, Profundidad: ${hydDepth.value} ft",
                result = res.formattedResult
            )
            repository.insert(record)
        }
    }

    fun runCapacity() {
        val id = capId.value.toDoubleOrNull() ?: 0.0
        val length = capLength.value.toDoubleOrNull() ?: 0.0
        val res = OilfieldFormulas.calculateTubingCapacity(id, length)
        capResult.value = res
        updateActiveAlarms(res.alerts)
    }

    fun saveCapacityToHistory() {
        val res = capResult.value ?: return
        viewModelScope.launch {
            val record = CalculationRecord(
                user = currentUser.value,
                role = currentRole.value,
                type = "Capacidad de Tubing",
                formula = res.formula,
                inputs = "Diámetro Interno: ${capId.value} in, Longitud: ${capLength.value} ft",
                result = res.formattedResult
            )
            repository.insert(record)
        }
    }

    fun runAnnular() {
        val casingId = annCasingId.value.toDoubleOrNull() ?: 0.0
        val tubingOd = annTubingOd.value.toDoubleOrNull() ?: 0.0
        val length = annLength.value.toDoubleOrNull() ?: 0.0
        val res = OilfieldFormulas.calculateAnnularCapacity(casingId, tubingOd, length)
        annResult.value = res
        updateActiveAlarms(res.alerts)
    }

    fun saveAnnularToHistory() {
        val res = annResult.value ?: return
        viewModelScope.launch {
            val record = CalculationRecord(
                user = currentUser.value,
                role = currentRole.value,
                type = "Capacidad Anular",
                formula = res.formula,
                inputs = "Casing ID: ${annCasingId.value} in, Tubing OD: ${annTubingOd.value} in, Longitud: ${annLength.value} ft",
                result = res.formattedResult
            )
            repository.insert(record)
        }
    }

    fun runAnnularVelocity() {
        val gpm = velPumpRate.value.toDoubleOrNull() ?: 0.0
        val casingId = velCasingId.value.toDoubleOrNull() ?: 0.0
        val tubingOd = velTubingOd.value.toDoubleOrNull() ?: 0.0
        val volume = velVolumeToPump.value.toDoubleOrNull() ?: 0.0

        val resVel = OilfieldFormulas.calculateAnnularVelocity(gpm, casingId, tubingOd)
        val pumpRateBpm = gpm / 42.0
        val resTime = OilfieldFormulas.calculatePumpTime(volume, pumpRateBpm)

        velResult.value = resVel
        pumpTimeResult.value = resTime

        val combinedAlerts = resVel.alerts + resTime.alerts
        updateActiveAlarms(combinedAlerts)
    }

    fun saveVelocityToHistory() {
        val resVel = velResult.value ?: return
        val resTime = pumpTimeResult.value ?: return
        viewModelScope.launch {
            val record = CalculationRecord(
                user = currentUser.value,
                role = currentRole.value,
                type = "Velocidad Anular y Bombeo",
                formula = resVel.formula,
                inputs = "Caudal: ${velPumpRate.value} gpm, Casing ID: ${velCasingId.value} in, Tubing OD: ${velTubingOd.value} in, Vol: ${velVolumeToPump.value} bbl",
                result = "Vel: ${resVel.formattedResult} | Tiempo: ${resTime.formattedResult}"
            )
            repository.insert(record)
        }
    }

    fun runCementing() {
        val cap = cemAnnularCap.value.toDoubleOrNull() ?: 0.0
        val height = cemHeight.value.toDoubleOrNull() ?: 0.0
        val excess = cemExcess.value.toDoubleOrNull() ?: 0.0
        val yield = cemYield.value.toDoubleOrNull() ?: 0.0
        val res = OilfieldFormulas.calculateCementing(cap, height, excess, yield)
        cemResult.value = res
        updateActiveAlarms(res.alerts)
    }

    fun saveCementingToHistory() {
        val res = cemResult.value ?: return
        viewModelScope.launch {
            val record = CalculationRecord(
                user = currentUser.value,
                role = currentRole.value,
                type = "Cementación",
                formula = res.formula,
                inputs = "Capacidad Anular: ${cemAnnularCap.value} bbl/ft, Altura: ${cemHeight.value} ft, Exceso: ${cemExcess.value}%, Rend.: ${cemYield.value} cu ft/sk",
                result = res.formattedResult
            )
            repository.insert(record)
        }
    }

    fun runProduction() {
        val volume = prodLiquidVol.value.toDoubleOrNull() ?: 0.0
        val days = prodDays.value.toDoubleOrNull() ?: 1.0
        val oilCut = prodOilCut.value.toDoubleOrNull() ?: 0.0
        val bo = prodBo.value.toDoubleOrNull() ?: 1.0
        val res = OilfieldFormulas.calculateProduction(volume, days, oilCut, bo)
        prodResult.value = res
        updateActiveAlarms(res.alerts)
    }

    fun saveProductionToHistory() {
        val res = prodResult.value ?: return
        viewModelScope.launch {
            val record = CalculationRecord(
                user = currentUser.value,
                role = currentRole.value,
                type = "Cálculo de Producción",
                formula = res.formula,
                inputs = "Vol Líquido: ${prodLiquidVol.value} bbl, Días: ${prodDays.value}, Corte Aceite: ${prodOilCut.value}%, Factor Bo: ${prodBo.value}",
                result = res.formattedResult
            )
            repository.insert(record)
        }
    }

    fun runTanks() {
        val radius = tankRadius.value.toDoubleOrNull() ?: 0.0
        val height = tankHeight.value.toDoubleOrNull() ?: 0.0
        val fluidLevel = tankFluidLevel.value.toDoubleOrNull() ?: 0.0

        val res = if (tankType.value == "vertical") {
            OilfieldFormulas.calculateVerticalTank(radius, height, fluidLevel)
        } else {
            OilfieldFormulas.calculateHorizontalTank(radius, height, fluidLevel)
        }
        tankResult.value = res
        updateActiveAlarms(res.alerts)
    }

    fun saveTanksToHistory() {
        val res = tankResult.value ?: return
        viewModelScope.launch {
            val record = CalculationRecord(
                user = currentUser.value,
                role = currentRole.value,
                type = "Cálculo de Tanques (${tankType.value.replaceFirstChar { it.uppercase() }})",
                formula = res.formula,
                inputs = "Radio: ${tankRadius.value} ft, Altura/Largo: ${tankHeight.value} ft, Nivel Fluido: ${tankFluidLevel.value} ft",
                result = res.formattedResult
            )
            repository.insert(record)
        }
    }

    fun runConversion() {
        val value = convValue.value.toDoubleOrNull() ?: 0.0
        val from = convFromUnit.value
        val to = convToUnit.value
        val converted = OilfieldFormulas.convertUnits(value, from, to)
        convResult.value = "${String.format(Locale.US, "%.4f", converted)} $to"
    }

    fun saveConversionToHistory() {
        viewModelScope.launch {
            val record = CalculationRecord(
                user = currentUser.value,
                role = currentRole.value,
                type = "Conversión de Unidades",
                formula = "${convFromUnit.value} -> ${convToUnit.value}",
                inputs = "Valor: ${convValue.value} ${convFromUnit.value}",
                result = convResult.value
            )
            repository.insert(record)
        }
    }

    fun setConversionCategory(category: String) {
        convCategory.value = category
        when (category) {
            "Presión" -> {
                convFromUnit.value = "psi"
                convToUnit.value = "bar"
            }
            "Longitud" -> {
                convFromUnit.value = "pies"
                convToUnit.value = "metros"
            }
            "Volumen" -> {
                convFromUnit.value = "barriles"
                convToUnit.value = "litros"
            }
            "Temperatura" -> {
                convFromUnit.value = "°F"
                convToUnit.value = "°C"
            }
            "Densidad" -> {
                convFromUnit.value = "ppg"
                convToUnit.value = "g/cm³"
            }
            "Caudal" -> {
                convFromUnit.value = "bpm"
                convToUnit.value = "gpm"
            }
        }
        runConversion()
    }

    // --- GEMINI AI ASSISTANT CHAT & SPEECH ---
    fun sendAiCommand(commandText: String) {
        if (commandText.trim().isEmpty()) return

        // Append user message
        val updated = aiMessages.value.toMutableList()
        updated.add(ChatMessage(text = commandText, isUser = true))
        aiMessages.value = updated

        isAiLoading.value = true

        viewModelScope.launch {
            val systemPrompt = """
                Eres PetroCalc AI, una asistente de software senior y de ingeniería de petróleo especializada en operaciones de campo (workover, producción, perforación, cementación y fluidos).
                
                REGLAS DE OPERACIÓN:
                1. Nunca inventes fórmulas petroleras. Usa las estándar (API, Halliburton, Schlumberger, Baker Hughes, IADC).
                2. Si el usuario te hace una orden de cálculo por voz o texto, debes realizar el cálculo y mostrar de forma clara:
                   - El Resultado destacado
                   - La Fórmula empleada
                   - Las Variables interpretadas
                   - El Procedimiento paso a paso detallado
                   - Fuente técnica oficial de referencia
                3. Mantén tus explicaciones breves, en un español técnico de campo de Latinoamérica.
                4. Si el usuario te pasa valores fuera de rango, incluye una sección de 'ALERTA DE SEGURIDAD INDUSTRIAL'.
                5. Puedes responder a preguntas operativas sobre control de brotes, densidades de lodo, cementaciones y tanques.
                
                FÓRMULAS OFICIALES:
                - Hidrostática: Ph (psi) = 0.052 * Densidad (ppg) * Profundidad (ft)
                - Capacidad Tubing/Casing: Cap (bbl/ft) = ID² / 1029.4
                - Anular: CapAnular (bbl/ft) = (CasingID² - TubingOD²) / 1029.4
                - Desplazamiento Tubing: Desp (bbl/ft) = (TubingOD² - TubingID²) / 1029.4
                - Velocidad Anular: AV (ft/min) = (24.51 * bpm) / (CasingID² - TubingOD²)
                - Tiempo Bombeo: Tiempo (min) = Volumen (bbl) / Caudal (bpm)
                - Tanques: Capacidad Vertical (bbl) = (π * Radio² * Altura) / 5.6154
            """.trimIndent()

            val response = GeminiClient.generateContent(
                systemInstruction = systemPrompt,
                prompt = commandText
            )

            isAiLoading.value = false

            val currentMessages = aiMessages.value.toMutableList()
            currentMessages.add(ChatMessage(text = response, isUser = false))
            aiMessages.value = currentMessages

            // Save this AI interaction to history for tracking!
            val record = CalculationRecord(
                user = currentUser.value,
                role = currentRole.value,
                type = "Asistente de Voz IA",
                formula = "Interpretación Lenguaje Natural (Gemini AI)",
                inputs = commandText,
                result = response.take(150) + if (response.length > 150) "..." else ""
            )
            repository.insert(record)

            // TTS Read Aloud the main result safely!
            speakResultFromResponse(response)
        }
    }

    private fun speakResultFromResponse(response: String) {
        // Simple heuristic: extract line containing "igual a" or "Presión hidrostática =" or similar
        // Or just read the first two lines which usually have the main answer
        val lines = response.split("\n")
        val mainLine = lines.firstOrNull { 
            it.lowercase().contains("igual a") || 
            it.lowercase().contains("=") || 
            it.lowercase().contains("resultado") 
        } ?: lines.firstOrNull { it.trim().isNotEmpty() } ?: "Cálculo procesado correctamente."
        speakText(mainLine)
    }

    fun clearChat() {
        aiMessages.value = listOf(
            ChatMessage(
                text = "Chat reiniciado. Estoy lista para escuchar tus órdenes de cálculo.",
                isUser = false
            )
        )
    }

    // --- OCR SIMULATION VIA GEMINI ---
    // Takes a photo (bitmap), converts to base64, and sends to Gemini to extract variables!
    fun runSimulatedOcr(bitmap: Bitmap, customPrompt: String = "Analiza este reporte operativo de campo petrolero. Extrae diámetros, profundidades, densidades de lodos y presiones. Realiza los cálculos automáticos requeridos basándote en ellos.") {
        isOcrLoading.value = true
        ocrResult.value = null

        viewModelScope.launch {
            // Convert Bitmap to base64
            val base64 = bitmapToBase64(bitmap)
            
            val ocrSystemPrompt = """
                Eres un extractor inteligente OCR de reportes y hojas operativas de perforación y workover petrolero.
                Analiza la imagen provista (que representa un reporte de campo) y:
                1. Extrae todas las variables operativas críticas (Profundidades, Diámetros de tuberías ID/OD, Densidad del lodo, Presión, Volumen de tanques, Caudales).
                2. Formatea el extracto en una tabla Markdown limpia de alta visibilidad.
                3. Realiza de forma automática cálculos operativos recomendados usando las fórmulas API oficiales (ej. calcular presión hidrostática si tienes profundidad y densidad de lodo; o capacidad si tienes ID y profundidad).
                4. Advierte sobre cualquier anomalía o valor que parezca fuera de los rangos seguros en campo.
                5. Responde en español operativo.
            """.trimIndent()

            val response = GeminiClient.generateContent(
                systemInstruction = ocrSystemPrompt,
                prompt = customPrompt,
                imageBase64 = base64
            )

            isOcrLoading.value = false
            ocrResult.value = response

            // Also save the OCR event to local history database
            val record = CalculationRecord(
                user = currentUser.value,
                role = currentRole.value,
                type = "Análisis OCR Reportes",
                formula = "Análisis Multimodal (Gemini 3.5 Flash)",
                inputs = "Imagen de Reporte Operativo cargada",
                result = "Variables extraídas. Presión hidrostática y capacidades calculadas."
            )
            repository.insert(record)
            
            speakText("Reporte analizado. Variables operativas extraídas y calculadas.")
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    // --- RECORD ACTIONS FOR HISTORY SCREEN ---
    fun deleteHistoryRecord(record: CalculationRecord) {
        viewModelScope.launch {
            repository.delete(record)
        }
    }

    fun toggleFavorite(record: CalculationRecord) {
        viewModelScope.launch {
            repository.update(record.copy(isFavorite = !record.isFavorite))
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    // --- SPECIALIZED FORMULAS CRUD ---
    fun insertSpecializedFormula(formula: SpecializedFormula) {
        viewModelScope.launch {
            formulaRepository.insert(formula)
        }
    }

    fun deleteSpecializedFormula(formula: SpecializedFormula) {
        viewModelScope.launch {
            formulaRepository.delete(formula)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager?.shutdown()
    }
}
