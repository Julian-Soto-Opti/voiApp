package com.jetbrains.kmpapp.data

import kotlinx.serialization.Serializable

@Serializable
enum class AppTheme {
    LIQUID_GLASS,
    VOLARIS_PREMIUM,
    LIGHT,
    DARK
}

@Serializable
data class ThemeColors(
    val theme: AppTheme,
    val background: String,       // Hex color: e.g. "#000000"
    val surface: String,          // Hex color for cards/surfaces
    val accent: String,           // Predominant theme color (purple, pink, etc.)
    val textPrimary: String,      // Primary text color
    val textSecondary: String,    // Secondary text color
    val border: String,           // Border color
    val kpiGood: String,          // Success green
    val kpiMedium: String,        // Warning yellow
    val kpiBad: String            // Danger red
) {
    companion object {
        fun getColors(theme: AppTheme): ThemeColors {
            return when (theme) {
                AppTheme.LIQUID_GLASS -> ThemeColors(
                    theme = AppTheme.LIQUID_GLASS,
                    background = "#020D1D",      // Deep fluid space navy
                    surface = "#10FFFFFF",         // Semi-transparent white for glass
                    accent = "#0E758F",            // Cyber teal/crystal blue
                    textPrimary = "#FFFFFF",       // White
                    textSecondary = "#8FA3BC",     // Light grayish blue
                    border = "#20FFFFFF",          // Glass border reflection
                    kpiGood = "#74B02B",
                    kpiMedium = "#F39C12",
                    kpiBad = "#C0392B"
                )
                AppTheme.VOLARIS_PREMIUM -> ThemeColors(
                    theme = AppTheme.VOLARIS_PREMIUM,
                    background = "#0B020F",      // Obsidian purple
                    surface = "#1A0825",          // Deep obsidian magenta surface
                    accent = "#A12885",            // Volaris Pink/Purple
                    textPrimary = "#FFFFFF",
                    textSecondary = "#C2B2D1",
                    border = "#3D1350",
                    kpiGood = "#74B02B",
                    kpiMedium = "#F39C12",
                    kpiBad = "#C0392B"
                )
                AppTheme.LIGHT -> ThemeColors(
                    theme = AppTheme.LIGHT,
                    background = "#F9FBFC",      // Clean light gray/white
                    surface = "#FFFFFF",          // Pure white
                    accent = "#0E758F",            // Minimalist ocean blue
                    textPrimary = "#1C2D37",       // Charcoal
                    textSecondary = "#7F8C8D",     // Cool gray
                    border = "#E2E8F0",            // Soft border
                    kpiGood = "#27AE60",
                    kpiMedium = "#F39C12",
                    kpiBad = "#C0392B"
                )
                AppTheme.DARK -> ThemeColors(
                    theme = AppTheme.DARK,
                    background = "#000000",      // Pure OLED black
                    surface = "#121212",          // Dark gray surface
                    accent = "#74B02B",            // Neon green accent
                    textPrimary = "#FFFFFF",       // White
                    textSecondary = "#9E9E9E",     // Gray
                    border = "#1F1F1F",
                    kpiGood = "#2ECC71",
                    kpiMedium = "#F1C40F",
                    kpiBad = "#E74C3C"
                )
            }
        }
    }
}

@Serializable
data class Carrier(
    val code: String,              // Y4, Q6, N3
    val name: String
)

@Serializable
data class Flight(
    val id: String,
    val flightNumber: String,      // e.g. VOI1522
    val carrier: String,           // Y4, Q6, N3
    val origin: String,            // MEX, GDL, TIJ
    val destination: String,       // CUN, MTY, LAX
    val departureTime: String,     // ISO Time string
    val arrivalTime: String,       // ISO Time string
    val status: String,            // ON_AIR, DELAYED, ARRIVED, CANCELLED, FUTURE
    val delayMinutes: Int,
    val registration: String,      // e.g. XA-VRY
    val crewPilot: String,
    val crewCopilot: String,
    val crewChief: String
)

@Serializable
data class DelayCategory(
    val code: String,              // C/R, C/NR, NC/R, NC/NR, TBD
    val label: String,             // Controlable/Reclamable, etc.
    val flightCount: Int,
    val percentage: Double
)

@Serializable
data class StationKPI(
    val station: String,           // MEX, GDL, TIJ, CUN, MTY
    val flightVolume: Int,
    val otp15: Double,             // On-Time Performance (e.g. 84.2)
    val btp0: Double,              // Block-Time Performance (e.g. 91.5)
    val atd5: Double,              // Actual Time Departure +5 (e.g. 78.4)
    val gtp5: Double,              // Ground Time Performance (e.g. 88.0)
    val statusColor: String        // "GOOD", "MEDIUM", "BAD" depending on average KPIs
)

@Serializable
data class ExecutiveKPI(
    val name: String,              // OTP+15, OTP+15 Exc, BTP+0, ATD+0, GTP+0, GTP+5, ATD+5, BTP+5, PAX OTP
    val valueY4: Double,           // KPI value for Carrier Y4
    val valueQ6: Double,           // KPI value for Carrier Q6
    val valueN3: Double            // KPI value for Carrier N3
)
