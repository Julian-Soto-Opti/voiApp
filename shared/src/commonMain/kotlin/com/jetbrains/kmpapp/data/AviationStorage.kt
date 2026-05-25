package com.jetbrains.kmpapp.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

interface AviationStorage {
    fun getThemeColors(): Flow<ThemeColors>
    fun getTheme(): Flow<AppTheme>
    fun updateTheme(theme: AppTheme)
    
    fun getFlights(): Flow<List<Flight>>
    fun getDelayCategories(carrier: String): Flow<List<DelayCategory>>
    fun getStationKPIs(): Flow<List<StationKPI>>
    fun getExecutiveKPIs(): Flow<List<ExecutiveKPI>>
    
    fun getLoggedInUser(): Flow<String?>
    suspend fun signIn(username: String, password: String): Boolean
    suspend fun signOut()
}

class InMemoryAviationStorage : AviationStorage {
    private val _theme = MutableStateFlow(AppTheme.LIQUID_GLASS)
    private val _colors = MutableStateFlow(ThemeColors.getColors(AppTheme.LIQUID_GLASS))
    private val _loggedInUser = MutableStateFlow<String?>(null)
    
    override fun getTheme(): Flow<AppTheme> = _theme.asStateFlow()
    override fun getThemeColors(): Flow<ThemeColors> = _colors.asStateFlow()
    
    override fun updateTheme(theme: AppTheme) {
        _theme.value = theme
        _colors.value = ThemeColors.getColors(theme)
    }

    override fun getFlights(): Flow<List<Flight>> = flow {
        // Genera 10 vuelos mock con datos reales y estructurados
        val flights = listOf(
            Flight("1", "VOI1522", "Y4", "MEX", "CUN", "14:20", "16:30", "ON_AIR", 0, "XA-VRY", "Cap. Sergio Ayala", "P.A. Oscar Flores", "J.C. Sofia Martinez"),
            Flight("2", "VOI382", "Y4", "GDL", "TIJ", "15:45", "17:55", "DELAYED", 35, "XA-VOH", "Cap. Carlos Gomez", "P.A. Roberto Ruiz", "J.C. Lucia Mendez"),
            Flight("3", "VOI891", "Y4", "MEX", "LAX", "16:00", "19:15", "FUTURE", 0, "XA-VRE", "Cap. Alejandro Sanz", "P.A. David Villa", "J.C. Maria Lopez"),
            Flight("4", "VOI441", "Q6", "SJO", "MEX", "13:10", "15:40", "ARRIVED", 0, "XA-VOL", "Cap. Juan Perez", "P.A. Jose Hernandez", "J.C. Ana Gomez"),
            Flight("5", "VOI704", "N3", "MEX", "SAL", "18:30", "20:45", "FUTURE", 0, "XA-VRK", "Cap. Hector Gonzalez", "P.A. Luis Toro", "J.C. Gabriela Diaz"),
            Flight("6", "VOI502", "Y4", "TIJ", "MEX", "12:15", "15:35", "ARRIVED", 15, "XA-VOZ", "Cap. Fernando Torres", "P.A. Javier Mas", "J.C. Elena Perez"),
            Flight("7", "VOI203", "Q6", "GDL", "CUN", "19:00", "21:10", "FUTURE", 0, "XA-VRA", "Cap. Ricardo Rocha", "P.A. Ivan Morales", "J.C. Clara Rojas"),
            Flight("8", "VOI941", "Y4", "MTY", "MEX", "14:50", "16:15", "ON_AIR", 0, "XA-VRB", "Cap. Miguel Angel", "P.A. Arturo Elias", "J.C. Monica Sanchez"),
            Flight("9", "VOI612", "N3", "MEX", "GDL", "15:10", "16:25", "DELAYED", 45, "XA-VRC", "Cap. Eduardo Vargas", "P.A. Pablo Cuevas", "J.C. Andrea B."),
            Flight("10", "VOI104", "Y4", "CUN", "MTY", "17:35", "19:50", "FUTURE", 0, "XA-VRD", "Cap. Gabriel Garcia", "P.A. Tomas Boy", "J.C. Regina Ramos")
        )
        emit(flights)
    }

    override fun getDelayCategories(carrier: String): Flow<List<DelayCategory>> = flow {
        // Categorías de demora dinámicas según la aerolínea seleccionada
        val delayData = when (carrier) {
            "Y4" -> listOf(
                DelayCategory("C/R", "Controlable Reclamable", 14, 40.0),
                DelayCategory("C/NR", "Controlable No Reclamable", 7, 20.0),
                DelayCategory("NC/R", "No Controlable Reclamable", 5, 14.3),
                DelayCategory("NC/NR", "No Controlable No Reclamable", 6, 17.1),
                DelayCategory("TBD", "Por Determinar", 3, 8.6)
            )
            "Q6" -> listOf(
                DelayCategory("C/R", "Controlable Reclamable", 3, 25.0),
                DelayCategory("C/NR", "Controlable No Reclamable", 5, 41.7),
                DelayCategory("NC/R", "No Controlable Reclamable", 1, 8.3),
                DelayCategory("NC/NR", "No Controlable No Reclamable", 2, 16.7),
                DelayCategory("TBD", "Por Determinar", 1, 8.3)
            )
            else -> listOf(
                DelayCategory("C/R", "Controlable Reclamable", 8, 32.0),
                DelayCategory("C/NR", "Controlable No Reclamable", 6, 24.0),
                DelayCategory("NC/R", "No Controlable Reclamable", 4, 16.0),
                DelayCategory("NC/NR", "No Controlable No Reclamable", 5, 20.0),
                DelayCategory("TBD", "Por Determinar", 2, 8.0)
            )
        }
        emit(delayData)
    }

    override fun getStationKPIs(): Flow<List<StationKPI>> = flow {
        val stations = listOf(
            StationKPI("MEX", 124, 82.4, 89.1, 76.5, 84.0, "GOOD"),
            StationKPI("CUN", 89, 87.6, 92.5, 81.0, 90.2, "GOOD"),
            StationKPI("GDL", 76, 74.5, 82.0, 71.2, 79.5, "MEDIUM"),
            StationKPI("TIJ", 65, 68.2, 75.4, 62.0, 71.0, "BAD"),
            StationKPI("MTY", 54, 85.0, 90.0, 80.2, 86.5, "GOOD")
        )
        emit(stations)
    }

    override fun getExecutiveKPIs(): Flow<List<ExecutiveKPI>> = flow {
        val executiveKPIs = listOf(
            ExecutiveKPI("OTP + 15", 84.5, 81.2, 83.0),
            ExecutiveKPI("OTP + 15 Exc", 87.2, 83.5, 85.1),
            ExecutiveKPI("BTP + 0", 92.1, 89.0, 90.5),
            ExecutiveKPI("ATD + 0", 78.4, 75.0, 76.9),
            ExecutiveKPI("GTP + 0", 81.0, 79.2, 80.1),
            ExecutiveKPI("GTP + 5", 86.5, 84.0, 85.0),
            ExecutiveKPI("ATD + 5", 83.2, 80.5, 81.8),
            ExecutiveKPI("BTP + 5", 94.6, 92.1, 93.4),
            ExecutiveKPI("PAX OTP", 85.4, 82.3, 84.0)
        )
        emit(executiveKPIs)
    }

    override fun getLoggedInUser(): Flow<String?> = _loggedInUser.asStateFlow()

    override suspend fun signIn(username: String, password: String): Boolean {
        delay(800) // Simula la latencia de la red de Amplify Cognito
        return if (username.lowercase() == "julian@orka.com" && password == "admin123") {
            _loggedInUser.value = "Julian Soto"
            true
        } else {
            false
        }
    }

    override suspend fun signOut() {
        _loggedInUser.value = null
    }
}
