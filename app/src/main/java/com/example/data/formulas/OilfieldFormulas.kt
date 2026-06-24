package com.example.data.formulas

import java.util.Locale
import kotlin.math.acos
import kotlin.math.sin

object OilfieldFormulas {

    data class CalcResult(
        val value: Double,
        val formattedResult: String,
        val formula: String,
        val procedure: String,
        val source: String,
        val alerts: List<String> = emptyList()
    )

    // 1. Hydrostatic Pressure
    fun calculateHydrostaticPressure(densityPpg: Double, depthFt: Double): CalcResult {
        val pressurePsi = 0.052 * densityPpg * depthFt
        val formula = "Ph (psi) = 0.052 * Densidad (ppg) * Profundidad (ft)"
        val procedure = """
            1. Multiplicar constante de conversión: 0.052
            2. Multiplicar por Densidad del Lodo: $densityPpg ppg
            3. Multiplicar por Profundidad Vertical Verdadera (TVD): $depthFt ft
            Cálculo: 0.052 * $densityPpg * $depthFt = ${String.format(Locale.US, "%.2f", pressurePsi)} psi
        """.trimIndent()
        
        val alerts = mutableListOf<String>()
        if (pressurePsi > 8000) {
            alerts.add("ALERTA DE ALTA PRESIÓN: Presión calculada > 8000 psi. Verificar especificaciones del cabezal de pozo y sartas.")
        }
        if (densityPpg > 18.0) {
            alerts.add("ADVERTENCIA DE DENSIDAD: Densidad de lodo > 18 ppg es extremadamente alta, verificar control de pozo.")
        }

        return CalcResult(
            value = pressurePsi,
            formattedResult = "${String.format(Locale.US, "%.2f", pressurePsi)} psi",
            formula = formula,
            procedure = procedure,
            source = "API RP 59 / Halliburton Field Booklet",
            alerts = alerts
        )
    }

    // 2. Tubing Capacity
    fun calculateTubingCapacity(innerDiameterIn: Double, lengthFt: Double): CalcResult {
        val capacityBblFt = (innerDiameterIn * innerDiameterIn) / 1029.4
        val totalVolumeBbl = capacityBblFt * lengthFt
        val formula = "Capacidad (bbl/ft) = ID² / 1029.4\nVolumen (bbl) = Capacidad * Longitud (ft)"
        val procedure = """
            1. Elevar diámetro interno (ID) al cuadrado: $innerDiameterIn * $innerDiameterIn = ${String.format(Locale.US, "%.4f", innerDiameterIn * innerDiameterIn)} in²
            2. Dividir entre constante de volumen 1029.4: ${String.format(Locale.US, "%.6f", capacityBblFt)} bbl/ft
            3. Multiplicar por longitud: ${String.format(Locale.US, "%.6f", capacityBblFt)} * $lengthFt = ${String.format(Locale.US, "%.2f", totalVolumeBbl)} bbl
        """.trimIndent()

        val alerts = mutableListOf<String>()
        if (innerDiameterIn <= 0.0 || innerDiameterIn > 12.0) {
            alerts.add("VALOR INUSUAL: Diámetro interno de tubing de $innerDiameterIn in está fuera de rangos de operación estándar.")
        }

        return CalcResult(
            value = totalVolumeBbl,
            formattedResult = "Capacidad: ${String.format(Locale.US, "%.6f", capacityBblFt)} bbl/ft\nVolumen Total: ${String.format(Locale.US, "%.2f", totalVolumeBbl)} bbl",
            formula = formula,
            procedure = procedure,
            source = "Halliburton Red Book - Section 1",
            alerts = alerts
        )
    }

    // 3. Casing Capacity
    fun calculateCasingCapacity(innerDiameterIn: Double, lengthFt: Double): CalcResult {
        val capacityBblFt = (innerDiameterIn * innerDiameterIn) / 1029.4
        val totalVolumeBbl = capacityBblFt * lengthFt
        val formula = "Capacidad (bbl/ft) = ID² / 1029.4\nVolumen (bbl) = Capacidad * Longitud (ft)"
        val procedure = """
            1. Elevar ID del Casing al cuadrado: $innerDiameterIn * $innerDiameterIn = ${String.format(Locale.US, "%.4f", innerDiameterIn * innerDiameterIn)} in²
            2. Dividir entre 1029.4: ${String.format(Locale.US, "%.6f", capacityBblFt)} bbl/ft
            3. Multiplicar por longitud: ${String.format(Locale.US, "%.6f", capacityBblFt)} * $lengthFt = ${String.format(Locale.US, "%.2f", totalVolumeBbl)} bbl
        """.trimIndent()

        val alerts = mutableListOf<String>()
        if (innerDiameterIn < 4.0 || innerDiameterIn > 30.0) {
            alerts.add("ADVERTENCIA: ID del casing de $innerDiameterIn in es inusual en operaciones estándar.")
        }

        return CalcResult(
            value = totalVolumeBbl,
            formattedResult = "Capacidad: ${String.format(Locale.US, "%.6f", capacityBblFt)} bbl/ft\nVolumen Total: ${String.format(Locale.US, "%.2f", totalVolumeBbl)} bbl",
            formula = formula,
            procedure = procedure,
            source = "Halliburton Red Book",
            alerts = alerts
        )
    }

    // 4. Annular Capacity
    fun calculateAnnularCapacity(casingIdIn: Double, tubingOdIn: Double, lengthFt: Double): CalcResult {
        val capacityBblFt = (casingIdIn * casingIdIn - tubingOdIn * tubingOdIn) / 1029.4
        val totalVolumeBbl = capacityBblFt * lengthFt
        val formula = "Capacidad Anular (bbl/ft) = (Casing ID² - Tubing OD²) / 1029.4\nVolumen Anular (bbl) = Capacidad * Longitud"
        val procedure = """
            1. Casing ID al cuadrado: $casingIdIn² = ${String.format(Locale.US, "%.3f", casingIdIn * casingIdIn)}
            2. Tubing OD al cuadrado: $tubingOdIn² = ${String.format(Locale.US, "%.3f", tubingOdIn * tubingOdIn)}
            3. Restar cuadrados y dividir entre 1029.4: (${String.format(Locale.US, "%.3f", casingIdIn * casingIdIn)} - ${String.format(Locale.US, "%.3f", tubingOdIn * tubingOdIn)}) / 1029.4 = ${String.format(Locale.US, "%.6f", capacityBblFt)} bbl/ft
            4. Multiplicar por longitud: ${String.format(Locale.US, "%.6f", capacityBblFt)} * $lengthFt = ${String.format(Locale.US, "%.2f", totalVolumeBbl)} bbl
        """.trimIndent()

        val alerts = mutableListOf<String>()
        if (casingIdIn <= tubingOdIn) {
            alerts.add("ERROR CRÍTICO: Casing ID debe ser mayor que Tubing OD. El tubing no cabe físicamente en el casing.")
        }

        return CalcResult(
            value = totalVolumeBbl,
            formattedResult = "Capacidad Anular: ${String.format(Locale.US, "%.6f", capacityBblFt)} bbl/ft\nVolumen Anular Total: ${String.format(Locale.US, "%.2f", totalVolumeBbl)} bbl",
            formula = formula,
            procedure = procedure,
            source = "API Drill Design Handbook",
            alerts = alerts
        )
    }

    // 5. Displacement Volume
    fun calculateDisplacementVolume(tubingOdIn: Double, tubingIdIn: Double, lengthFt: Double): CalcResult {
        val displacementBblFt = (tubingOdIn * tubingOdIn - tubingIdIn * tubingIdIn) / 1029.4
        val totalVolBbl = displacementBblFt * lengthFt
        val formula = "Desplazamiento (bbl/ft) = (OD² - ID²) / 1029.4\nVolumen Desplazado (bbl) = Desplazamiento * Longitud"
        val procedure = """
            1. Tubing OD al cuadrado: $tubingOdIn² = ${String.format(Locale.US, "%.3f", tubingOdIn * tubingOdIn)}
            2. Tubing ID al cuadrado: $tubingIdIn² = ${String.format(Locale.US, "%.3f", tubingIdIn * tubingIdIn)}
            3. Restar cuadrados y dividir entre 1029.4: (${String.format(Locale.US, "%.3f", tubingOdIn * tubingOdIn)} - ${String.format(Locale.US, "%.3f", tubingIdIn * tubingIdIn)}) / 1029.4 = ${String.format(Locale.US, "%.6f", displacementBblFt)} bbl/ft
            4. Volumen Desplazado: ${String.format(Locale.US, "%.6f", displacementBblFt)} * $lengthFt = ${String.format(Locale.US, "%.2f", totalVolBbl)} bbl
        """.trimIndent()

        val alerts = mutableListOf<String>()
        if (tubingOdIn <= tubingIdIn) {
            alerts.add("ERROR CRÍTICO: Tubing OD debe ser mayor que Tubing ID.")
        }

        return CalcResult(
            value = totalVolBbl,
            formattedResult = "Factor Desp.: ${String.format(Locale.US, "%.6f", displacementBblFt)} bbl/ft\nDesplazamiento Total: ${String.format(Locale.US, "%.2f", totalVolBbl)} bbl",
            formula = formula,
            procedure = procedure,
            source = "IADC Drilling Manual - Displacement Section",
            alerts = alerts
        )
    }

    // 6. Annular Velocity
    fun calculateAnnularVelocity(pumpRateGpm: Double, casingIdIn: Double, tubingOdIn: Double): CalcResult {
        // formula: AV (ft/min) = (24.51 * bpm) / (ID² - OD²)
        // converting pumpRateGpm to bpm: bpm = gpm / 42.0
        val pumpRateBpm = pumpRateGpm / 42.0
        val denominator = casingIdIn * casingIdIn - tubingOdIn * tubingOdIn
        val velocityFtMin = if (denominator > 0) {
            (24.51 * pumpRateBpm) / denominator
        } else {
            0.0
        }
        val formula = "AV (ft/min) = (24.51 * Tasa Bombeo (bpm)) / (Casing ID² - Tubing OD²)\nNota: 1 bpm = 42 gpm"
        val procedure = """
            1. Convertir Caudal a BPM: $pumpRateGpm gpm / 42 = ${String.format(Locale.US, "%.3f", pumpRateBpm)} bpm
            2. Calcular divisor (Casing ID² - Tubing OD²): $casingIdIn² - $tubingOdIn² = ${String.format(Locale.US, "%.3f", denominator)} in²
            3. Calcular velocidad: (24.51 * ${String.format(Locale.US, "%.3f", pumpRateBpm)}) / ${String.format(Locale.US, "%.3f", denominator)} = ${String.format(Locale.US, "%.2f", velocityFtMin)} ft/min
        """.trimIndent()

        val alerts = mutableListOf<String>()
        if (casingIdIn <= tubingOdIn) {
            alerts.add("ERROR CRÍTICO: Diámetro del casing debe ser mayor que el tubing.")
        } else if (velocityFtMin < 100.0 && pumpRateGpm > 0.0) {
            alerts.add("ALERTA DE RETORNO: La velocidad anular calculada ($velocityFtMin ft/min) es menor al mínimo recomendado (100-120 ft/min) para remover recortes efectivamente.")
        }

        return CalcResult(
            value = velocityFtMin,
            formattedResult = "${String.format(Locale.US, "%.2f", velocityFtMin)} ft/min",
            formula = formula,
            procedure = procedure,
            source = "Baker Hughes Drilling Engineering Guidelines",
            alerts = alerts
        )
    }

    // 7. Pump Time
    fun calculatePumpTime(volumeBbl: Double, pumpRateBpm: Double): CalcResult {
        val timeMin = if (pumpRateBpm > 0) volumeBbl / pumpRateBpm else 0.0
        val formula = "Tiempo de Bombeo (min) = Volumen a Desplazar (bbl) / Caudal de Bombeo (bpm)"
        val procedure = """
            1. Tomar Volumen a Desplazar: $volumeBbl bbl
            2. Dividir entre Caudal de Bombeo: $pumpRateBpm bpm
            3. Tiempo de bombeo: $volumeBbl / $pumpRateBpm = ${String.format(Locale.US, "%.2f", timeMin)} minutos
        """.trimIndent()

        val alerts = mutableListOf<String>()
        if (pumpRateBpm <= 0) {
            alerts.add("ERROR: El caudal de bombeo debe ser mayor a cero.")
        }

        return CalcResult(
            value = timeMin,
            formattedResult = "${String.format(Locale.US, "%.2f", timeMin)} minutos (${String.format(Locale.US, "%.2f", timeMin / 60.0)} hrs)",
            formula = formula,
            procedure = procedure,
            source = "Schlumberger Cementing Field Handbook",
            alerts = alerts
        )
    }

    // 8. Cementing Calculations
    fun calculateCementing(
        annularCapacityBblFt: Double,
        cementHeightFt: Double,
        excessPercent: Double,
        yieldCuFtSk: Double
    ): CalcResult {
        val baseVolumeBbl = annularCapacityBblFt * cementHeightFt
        val totalVolumeBbl = baseVolumeBbl * (1 + (excessPercent / 100.0))
        val totalVolumeCuFt = totalVolumeBbl * 5.6154
        val sacksRequired = if (yieldCuFtSk > 0) totalVolumeCuFt / yieldCuFtSk else 0.0

        val formula = """
            Volumen Base (bbl) = Cap. Anular * Altura
            Volumen con Exceso (bbl) = Vol. Base * (1 + Exceso/100)
            Volumen (cu ft) = Volumen (bbl) * 5.6154
            Sacos = Volumen (cu ft) / Rendimiento (cu ft/sk)
        """.trimIndent()

        val procedure = """
            1. Calcular Volumen Base: $annularCapacityBblFt bbl/ft * $cementHeightFt ft = ${String.format(Locale.US, "%.2f", baseVolumeBbl)} bbl
            2. Aplicar Exceso del $excessPercent%: ${String.format(Locale.US, "%.2f", baseVolumeBbl)} * (1 + ${excessPercent / 100.0}) = ${String.format(Locale.US, "%.2f", totalVolumeBbl)} bbl
            3. Convertir a Pies Cúbicos: ${String.format(Locale.US, "%.2f", totalVolumeBbl)} bbl * 5.6154 = ${String.format(Locale.US, "%.2f", totalVolumeCuFt)} cu ft
            4. Calcular Sacos Requeridos (Rendimiento $yieldCuFtSk cu ft/sk): ${String.format(Locale.US, "%.2f", totalVolumeCuFt)} / $yieldCuFtSk = ${String.format(Locale.US, "%.0f", sacksRequired)} sacos
        """.trimIndent()

        val alerts = mutableListOf<String>()
        if (excessPercent < 0 || excessPercent > 200) {
            alerts.add("ADVERTENCIA: Porcentaje de exceso de cemento ($excessPercent%) fuera del rango operativo típico (10% - 100%).")
        }

        return CalcResult(
            value = sacksRequired,
            formattedResult = "Volumen Cemento: ${String.format(Locale.US, "%.2f", totalVolumeBbl)} bbl (${String.format(Locale.US, "%.2f", totalVolumeCuFt)} ft³)\nSacos Requeridos: ${String.format(Locale.US, "%.0f", sacksRequired)} sacos",
            formula = formula,
            procedure = procedure,
            source = "API Spec 10A / Cementing Guidelines",
            alerts = alerts
        )
    }

    // 9. Production Calculations
    fun calculateProduction(
        liquidVolumeBbl: Double,
        timeDays: Double,
        oilCutPercent: Double,
        shrinkageFactor: Double // Bo (Formation Volume Factor, e.g. 1.15)
    ): CalcResult {
        val days = if (timeDays > 0) timeDays else 1.0
        val bpd = liquidVolumeBbl / days
        val bopd = bpd * (oilCutPercent / 100.0)
        val bwpd = bpd * (1 - (oilCutPercent / 100.0))
        val monthlyBbl = bpd * 30.4
        val annualBbl = bpd * 365.0
        
        // Stock Tank Barrels considering shrinkage factor (Bo)
        val stbpd = if (shrinkageFactor >= 1.0) bopd / shrinkageFactor else bopd

        val formula = """
            BPD (bbl/día) = Vol. Líquido (bbl) / Tiempo (días)
            BOPD (Petróleo) = BPD * Oil Cut (%) / 100
            BWPD (Agua) = BPD - BOPD
            STBPD (Petróleo Estabilizado) = BOPD / Factor Bo
        """.trimIndent()

        val procedure = """
            1. Calcular BPD total: $liquidVolumeBbl bbl / $days días = ${String.format(Locale.US, "%.2f", bpd)} bbl/día
            2. Calcular BOPD (Corte Crudo del $oilCutPercent%): ${String.format(Locale.US, "%.2f", bopd)} bbl/día
            3. Calcular BWPD (Corte de Agua: ${100.0 - oilCutPercent}%): ${String.format(Locale.US, "%.2f", bwpd)} bbl/día
            4. Aplicar Factor de Encogimiento (Bo = $shrinkageFactor): ${String.format(Locale.US, "%.2f", bopd)} / $shrinkageFactor = ${String.format(Locale.US, "%.2f", stbpd)} STBPD en tanque fiscal.
            5. Proyección: Mensual = ${String.format(Locale.US, "%.1f", monthlyBbl)} bbl; Anual = ${String.format(Locale.US, "%.1f", annualBbl)} bbl
        """.trimIndent()

        val alerts = mutableListOf<String>()
        if (oilCutPercent < 0 || oilCutPercent > 100) {
            alerts.add("ERROR: El corte de aceite debe estar entre 0% y 100%.")
        }
        if (shrinkageFactor < 1.0) {
            alerts.add("VALOR INVÁLIDO: El factor de encogimiento Bo (FVF) debe ser mayor o igual a 1.0.")
        }

        return CalcResult(
            value = bpd,
            formattedResult = "BPD Total: ${String.format(Locale.US, "%.2f", bpd)} bpd\nBOPD (Crudo): ${String.format(Locale.US, "%.2f", bopd)} bopd\nSTBPD (Fiscal): ${String.format(Locale.US, "%.2f", stbpd)} stbpd",
            formula = formula,
            procedure = procedure,
            source = "SPE Petroleum Engineering Handbook (Section 4)",
            alerts = alerts
        )
    }

    // 10. Vertical Tank Capacity
    fun calculateVerticalTank(radiusFt: Double, heightFt: Double, fluidLevelFt: Double): CalcResult {
        // V = pi * r^2 * h in cu ft. Convert to bbl by dividing by 5.6154
        val totalCapacityBbl = (Math.PI * radiusFt * radiusFt * heightFt) / 5.6154
        val actualLevel = if (fluidLevelFt > heightFt) heightFt else fluidLevelFt
        val fluidVolumeBbl = (Math.PI * radiusFt * radiusFt * actualLevel) / 5.6154
        val remainingVolumeBbl = totalCapacityBbl - fluidVolumeBbl

        val formula = """
            Capacidad Total (bbl) = (π * Radio² * Altura) / 5.6154
            Volumen Fluido (bbl) = (π * Radio² * Nivel Fluido) / 5.6154
            Volumen Restante = Capacidad Total - Volumen Fluido
        """.trimIndent()

        val procedure = """
            1. Calcular área circular base: π * $radiusFt² = ${String.format(Locale.US, "%.2f", Math.PI * radiusFt * radiusFt)} ft²
            2. Capacidad Total (Altura $heightFt ft): ${String.format(Locale.US, "%.2f", Math.PI * radiusFt * radiusFt * heightFt)} ft³ / 5.6154 = ${String.format(Locale.US, "%.2f", totalCapacityBbl)} bbl
            3. Volumen de Fluido Actual (Nivel $actualLevel ft): ${String.format(Locale.US, "%.2f", Math.PI * radiusFt * radiusFt * actualLevel)} ft³ / 5.6154 = ${String.format(Locale.US, "%.2f", fluidVolumeBbl)} bbl
            4. Espacio Libre (Ullage): ${String.format(Locale.US, "%.2f", totalCapacityBbl)} - ${String.format(Locale.US, "%.2f", fluidVolumeBbl)} = ${String.format(Locale.US, "%.2f", remainingVolumeBbl)} bbl
        """.trimIndent()

        val alerts = mutableListOf<String>()
        if (fluidLevelFt > heightFt) {
            alerts.add("ALERTA DE DESBORDE: El nivel de fluido ($fluidLevelFt ft) excede la altura del tanque ($heightFt ft).")
        } else if (fluidLevelFt > heightFt * 0.9) {
            alerts.add("ADVERTENCIA DE SEGURIDAD: Nivel de llenado > 90% de capacidad máxima. Riesgo de sobrellenado.")
        }

        return CalcResult(
            value = fluidVolumeBbl,
            formattedResult = "Capacidad Máx: ${String.format(Locale.US, "%.2f", totalCapacityBbl)} bbl\nContenido: ${String.format(Locale.US, "%.2f", fluidVolumeBbl)} bbl\nVacío: ${String.format(Locale.US, "%.2f", remainingVolumeBbl)} bbl",
            formula = formula,
            procedure = procedure,
            source = "API MPMS Chapter 2 - Vertical Tank Calibration",
            alerts = alerts
        )
    }

    // 11. Horizontal Tank Capacity
    fun calculateHorizontalTank(radiusFt: Double, lengthFt: Double, fluidLevelFt: Double): CalcResult {
        val totalCapacityBbl = (Math.PI * radiusFt * radiusFt * lengthFt) / 5.6154
        val h = if (fluidLevelFt > radiusFt * 2) radiusFt * 2 else fluidLevelFt
        val r = radiusFt

        // Cylinder volume segment: Area = r^2 * acos((r - h)/r) - (r - h)*sqrt(2*r*h - h^2)
        val fluidVolumeBbl = if (h >= r * 2) {
            totalCapacityBbl
        } else if (h <= 0) {
            0.0
        } else {
            val term1 = r * r * acos((r - h) / r)
            val term2 = (r - h) * kotlin.math.sqrt(2 * r * h - h * h)
            val segmentArea = term1 - term2
            (segmentArea * lengthFt) / 5.6154
        }
        val remainingVolumeBbl = totalCapacityBbl - fluidVolumeBbl

        val formula = """
            Capacidad Total (bbl) = (π * Radio² * Longitud) / 5.6154
            Volumen Fluido = [Radio² * acos((Radio-y)/Radio) - (Radio-y)*√(2*Radio*y - y²)] * Longitud / 5.6154
            Donde y = nivel de fluido, acos() en radianes.
        """.trimIndent()

        val procedure = """
            1. Capacidad Máxima: (π * $r² * $lengthFt) / 5.6154 = ${String.format(Locale.US, "%.2f", totalCapacityBbl)} bbl
            2. Área de segmento para nivel $h ft: ${String.format(Locale.US, "%.3f", (fluidVolumeBbl * 5.6154) / lengthFt)} ft²
            3. Volumen de Fluido Actual: ${String.format(Locale.US, "%.2f", fluidVolumeBbl)} bbl
            4. Espacio Libre (Vacío): ${String.format(Locale.US, "%.2f", remainingVolumeBbl)} bbl
        """.trimIndent()

        val alerts = mutableListOf<String>()
        if (fluidLevelFt > radiusFt * 2) {
            alerts.add("ALERTA DE DESBORDE: El nivel de fluido ($fluidLevelFt ft) excede el diámetro del tanque (${radiusFt * 2} ft).")
        } else if (fluidLevelFt > radiusFt * 2 * 0.9) {
            alerts.add("ADVERTENCIA DE CRITICIDAD: Tanque horizontal lleno a más del 90%.")
        }

        return CalcResult(
            value = fluidVolumeBbl,
            formattedResult = "Capacidad Máx: ${String.format(Locale.US, "%.2f", totalCapacityBbl)} bbl\nContenido: ${String.format(Locale.US, "%.2f", fluidVolumeBbl)} bbl\nVacío: ${String.format(Locale.US, "%.2f", remainingVolumeBbl)} bbl",
            formula = formula,
            procedure = procedure,
            source = "API MPMS Chapter 2.2E - Horizontal Cylindrical Tanks",
            alerts = alerts
        )
    }

    // 12. Unit Conversions (Helper for 500+ dynamic conversions or direct calculations)
    // We provide direct high precision math for oilfield units.
    fun convertUnits(value: Double, fromUnit: String, toUnit: String): Double {
        val f = fromUnit.lowercase(Locale.ROOT).trim()
        val t = toUnit.lowercase(Locale.ROOT).trim()

        if (f == t) return value

        // Pressure
        if (f == "psi" && t == "bar") return value * 0.0689476
        if (f == "bar" && t == "psi") return value / 0.0689476
        if (f == "psi" && t == "kpa") return value * 6.89476
        if (f == "kpa" && t == "psi") return value / 6.89476
        if (f == "bar" && t == "kpa") return value * 100.0
        if (f == "kpa" && t == "bar") return value / 100.0

        // Length
        if (f == "ft" || f == "pies") {
            if (t == "m" || t == "metros") return value * 0.3048
        }
        if (f == "m" || f == "metros") {
            if (t == "ft" || t == "pies") return value / 0.3048
        }
        if (f == "in" || f == "pulgadas") {
            if (t == "cm") return value * 2.54
            if (t == "mm") return value * 25.4
        }
        if (f == "cm") {
            if (t == "in" || t == "pulgadas") return value / 2.54
        }
        if (f == "mm") {
            if (t == "in" || t == "pulgadas") return value / 25.4
        }

        // Volume
        if (f == "bbl" || f == "barriles") {
            if (t == "gal" || t == "galones") return value * 42.0
            if (t == "l" || t == "litros") return value * 158.987
            if (t == "m³" || t == "m3" || t == "metros cubicos") return value * 0.158987
            if (t == "ft³" || t == "ft3" || t == "pies cubicos") return value * 5.6154
        }
        if (f == "gal" || f == "galones") {
            if (t == "bbl" || t == "barriles") return value / 42.0
            if (t == "l" || t == "litros") return value * 3.78541
            if (t == "m³" || t == "m3") return value * 0.00378541
        }
        if (f == "l" || f == "litros") {
            if (t == "bbl" || t == "barriles") return value / 158.987
            if (t == "gal" || t == "galones") return value / 3.78541
        }

        // Density
        if (f == "ppg") {
            if (t == "g/cm³" || t == "sg") return value * 0.119826
            if (t == "lb/ft³" || t == "lb/ft3") return value * 7.48052
            if (t == "kg/m³" || t == "kg/m3") return value * 119.826
        }
        if (f == "g/cm³" || f == "sg") {
            if (t == "ppg") return value / 0.119826
        }

        // Flow rate
        if (f == "bpm") {
            if (t == "gpm") return value * 42.0
            if (t == "lpm") return value * 158.987
        }
        if (f == "gpm") {
            if (t == "bpm") return value / 42.0
            if (t == "lpm") return value * 3.78541
        }

        // Temperature
        if (f == "f" || f == "°f") {
            if (t == "c" || t == "°c") return (value - 32.0) * 5.0 / 9.0
        }
        if (f == "c" || f == "°c") {
            if (t == "f" || t == "°f") return (value * 9.0 / 5.0) + 32.0
        }

        return value // Default returns original if unmapped
    }
}
