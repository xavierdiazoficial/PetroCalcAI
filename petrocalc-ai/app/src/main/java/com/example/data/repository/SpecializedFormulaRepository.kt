package com.example.data.repository

import com.example.data.database.SpecializedFormula
import com.example.data.database.SpecializedFormulaDao
import kotlinx.coroutines.flow.Flow

class SpecializedFormulaRepository(private val specializedFormulaDao: SpecializedFormulaDao) {

    val allFormulas: Flow<List<SpecializedFormula>> = specializedFormulaDao.getAllFormulas()

    fun getFormulasByCategory(category: String): Flow<List<SpecializedFormula>> {
        return specializedFormulaDao.getFormulasByCategory(category)
    }

    suspend fun insert(formula: SpecializedFormula): Long {
        return specializedFormulaDao.insertFormula(formula)
    }

    suspend fun insertAll(formulas: List<SpecializedFormula>) {
        specializedFormulaDao.insertFormulas(formulas)
    }

    suspend fun delete(formula: SpecializedFormula) {
        specializedFormulaDao.deleteFormula(formula)
    }

    suspend fun clearAll() {
        specializedFormulaDao.clearAll()
    }

    suspend fun populateDefaultFormulasIfNeeded() {
        val count = specializedFormulaDao.getCount()
        if (count == 0) {
            val defaults = listOf(
                // Perforación
                SpecializedFormula(
                    name = "Gradiente de Presión Hidrostática",
                    description = "Determina la tasa de aumento de presión por unidad de profundidad vertical.",
                    category = "Perforación",
                    mathFormula = "Ph_Grad (psi/ft) = 0.052 * Densidad (ppg)",
                    technicalSource = "API RP 59 / Halliburton Red Book",
                    inputUnits = "Densidad del lodo (ppg)",
                    outputUnits = "Gradiente de presión (psi/ft)"
                ),
                SpecializedFormula(
                    name = "Densidad de Control (Kill Mud)",
                    description = "Densidad requerida para restablecer el control hidrostático del pozo después de una surgencia.",
                    category = "Perforación",
                    mathFormula = "KMW (ppg) = SIDP (psi) / (0.052 * TVD (ft)) + OMW (ppg)",
                    technicalSource = "IADC Well Control Manual",
                    inputUnits = "SIDP (psi), TVD (ft), Densidad Actual OMW (ppg)",
                    outputUnits = "Densidad de lodo de control (ppg)"
                ),
                SpecializedFormula(
                    name = "Presión de Circulación Crítica",
                    description = "Presión máxima de seguridad admisible en el espacio anular durante la remoción de un brote.",
                    category = "Perforación",
                    mathFormula = "P_crit (psi) = MASP (psi) + Ph_anular (psi)",
                    technicalSource = "Well Control API Standard 53",
                    inputUnits = "MASP (psi), Presión Hidrostática Anular (psi)",
                    outputUnits = "Presión de circulación límite (psi)"
                ),

                // Cementación
                SpecializedFormula(
                    name = "Rendimiento de Lechada",
                    description = "Volumen de lechada húmeda producida por saco de cemento seco mezclado.",
                    category = "Cementación",
                    mathFormula = "Rendimiento (cu ft/sk) = (Vol_Agua_Gal + 0.478 * Saco_Lbs) / 7.48",
                    technicalSource = "API Spec 10A / Schlumberger",
                    inputUnits = "Agua de mezcla (gal/sk), Peso del saco (lbs/sk)",
                    outputUnits = "Rendimiento de lechada (cu ft/saco)"
                ),
                SpecializedFormula(
                    name = "Agua de Mezcla Requerida",
                    description = "Volumen total de agua dulce necesaria para la correcta hidratación de la lechada de cemento.",
                    category = "Cementación",
                    mathFormula = "Agua (gal) = Agua_sk (gal/sk) * Sacos_Totales",
                    technicalSource = "Schlumberger Cementing Manual",
                    inputUnits = "Agua de diseño (gal/sk), Número total de sacos",
                    outputUnits = "Agua de mezcla requerida (gal)"
                ),
                SpecializedFormula(
                    name = "Presión Hidrostática de Lechada",
                    description = "Presión hidrostática ejercida por la lechada líquida en el anular antes del fraguado.",
                    category = "Cementación",
                    mathFormula = "Ph_cem (psi) = 0.052 * Densidad_cem (ppg) * Altura_cem (ft)",
                    technicalSource = "API RP 10B-2 / Halliburton",
                    inputUnits = "Densidad lechada (ppg), Altura de la columna (ft)",
                    outputUnits = "Presión hidrostática líquida (psi)"
                ),

                // Workover
                SpecializedFormula(
                    name = "Densidad de Salmuera de Completación",
                    description = "Densidad del fluido libre de sólidos requerida para balancear la presión del yacimiento con un margen seguro.",
                    category = "Workover",
                    mathFormula = "Densidad (ppg) = BHP (psi) / (0.052 * TVD (ft)) + Margen_Seguridad (ppg)",
                    technicalSource = "Well Workover Principles - H. Gerhardt",
                    inputUnits = "Presión de yacimiento BHP (psi), TVD (ft), Margen (ppg)",
                    outputUnits = "Densidad de salmuera de control (ppg)"
                ),
                SpecializedFormula(
                    name = "Límite de Tensión de Tubería",
                    description = "Tensión mecánica máxima admisible en la sarta durante trabajos de pesca o liberación de empacadores.",
                    category = "Workover",
                    mathFormula = "Tensión_Max (lbs) = Límite_Fluencia (lbs) * Factor_Diseño (0.8)",
                    technicalSource = "API Bulletin 7G / Pipestring Limits",
                    inputUnits = "Carga de fluencia teórica (lbs)",
                    outputUnits = "Tensión mecánica máxima segura (lbs)"
                ),
                SpecializedFormula(
                    name = "Presión de Prueba de Casing",
                    description = "Presión de prueba máxima de integridad hidráulica en tuberías de revestimiento envejecidas.",
                    category = "Workover",
                    mathFormula = "P_prueba (psi) = Presión_Operación (psi) * 1.15",
                    technicalSource = "API RP 5C1 - Casing Integrity",
                    inputUnits = "Presión de servicio nominal máxima (psi)",
                    outputUnits = "Presión de prueba de hermeticidad (psi)"
                ),

                // Producción
                SpecializedFormula(
                    name = "Índice de Productividad (IP)",
                    description = "Capacidad del yacimiento para entregar fluidos al pozo por cada unidad de diferencial de presión (drawdown).",
                    category = "Producción",
                    mathFormula = "IP (bpd/psi) = Caudal_Liquido (bpd) / (BHP_Estatica (psi) - BHP_Fluyente (psi))",
                    technicalSource = "Petroleum Production Systems - Economides",
                    inputUnits = "Caudal total (bpd), Presión estática (psi), Presión de fondo fluyente (psi)",
                    outputUnits = "Índice de Productividad (bpd/psi)"
                ),
                SpecializedFormula(
                    name = "Eficiencia de Bombeo Mecánico",
                    description = "Relación de rendimiento entre el caudal real medido en tanques y el desplazamiento teórico de la bomba de varillas.",
                    category = "Producción",
                    mathFormula = "Eficiencia (%) = (Producción Real (bpd) / Desplazamiento Teórico (bpd)) * 100",
                    technicalSource = "API RP 11L - Sucker Rod Pumping",
                    inputUnits = "Caudal de producción real (bpd), Desplazamiento teórico (bpd)",
                    outputUnits = "Eficiencia de bombeo (%)"
                ),
                SpecializedFormula(
                    name = "Relación Gas-Aceite (GOR)",
                    description = "Volumen de gas producido por unidad de volumen de petróleo en condiciones estándar.",
                    category = "Producción",
                    mathFormula = "GOR (scf/bbl) = Gas_Producido (scf) / Aceite_Producido (bbl)",
                    technicalSource = "SPE Reservoir Engineering Handbook",
                    inputUnits = "Gas diario producido (scf/día), Aceite diario producido (bpd)",
                    outputUnits = "Relación Gas-Aceite GOR (scf/bbl)"
                )
            )
            specializedFormulaDao.insertFormulas(defaults)
        }
    }
}
