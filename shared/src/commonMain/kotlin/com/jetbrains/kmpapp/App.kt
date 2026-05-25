package com.jetbrains.kmpapp

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.data.*
import com.jetbrains.kmpapp.viewmodels.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ==========================================
// 1. SISTEMA DE TEMAS Y ENLAZADO DE COLORES
// ==========================================

fun parseColor(hex: String): Int {
    val cleanHex = hex.trim().replace("#", "")
    return when (cleanHex.length) {
        6 -> (0xFF000000 or cleanHex.toLong(16)).toInt()
        8 -> cleanHex.toLong(16).toInt()
        else -> 0xFF000000.toInt()
    }
}

@Composable
fun AviationTheme(
    colors: ThemeColors,
    content: @Composable () -> Unit
) {
    val surface = Color(parseColor(colors.surface))
    val background = Color(parseColor(colors.background))
    val accent = Color(parseColor(colors.accent))
    val textPrimary = Color(parseColor(colors.textPrimary))
    val textSecondary = Color(parseColor(colors.textSecondary))

    val colorScheme = darkColorScheme(
        primary = accent,
        surface = surface,
        background = background,
        onPrimary = Color.White,
        onSurface = textPrimary,
        onBackground = textPrimary
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// Modificador utilitario para tarjetas con efecto cristal / Liquid Glass
@Composable
fun Modifier.glassCard(
    cornerRadius: Dp = 24.dp,
    borderColor: Color,
    surfaceColor: Color
): Modifier = this
    .background(
        color = surfaceColor,
        shape = RoundedCornerShape(cornerRadius)
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.2f),
                borderColor.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.03f),
                borderColor.copy(alpha = 0.15f)
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )

// ==========================================
// 2. ENTRY POINT PRINCIPAL DE COMPOSE
// ==========================================

@Composable
fun App() {
    val themeViewModel: ThemeViewModel = koinViewModel()
    val loginViewModel: LoginViewModel = koinViewModel()
    
    val themeColors by themeViewModel.colors.collectAsState()
    val loginState by loginViewModel.state.collectAsState()
    
    AviationTheme(colors = themeColors) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo Orgánico de Degradados Animados
            OrganicBackgroundAnim(
                accentColor = Color(parseColor(themeColors.accent)),
                isDarkMode = themeColors.theme != AppTheme.LIGHT
            )

            AnimatedContent(
                targetState = loginState.isSuccess,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                }
            ) { loggedIn ->
                if (loggedIn) {
                    MainNavigator(
                        themeColors = themeColors,
                        themeViewModel = themeViewModel,
                        onSignOut = { loginViewModel.signOut() }
                    )
                } else {
                    LoginView(
                        loginViewModel = loginViewModel,
                        themeColors = themeColors
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. FONDOS ORGÁNICOS FLUIDOS EN COMPOSE
// ==========================================

@Composable
fun OrganicBackgroundAnim(accentColor: Color, isDarkMode: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) Color(0xFF020912) else Color(0xFFF6F8FA))
    ) {
        if (isDarkMode) {
            Canvas(modifier = Modifier.fillMaxSize().blur(80.dp)) {
                val rad = angle.toDouble() * (kotlin.math.PI / 180.0)
                val centerOffset = Offset(
                    x = size.width / 2 + (size.width / 4 * kotlin.math.cos(rad)).toFloat(),
                    y = size.height / 3 + (size.height / 6 * kotlin.math.sin(rad)).toFloat()
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.25f), Color.Transparent),
                        center = centerOffset,
                        radius = size.width * 0.5f
                    ),
                    radius = size.width * 0.5f,
                    center = centerOffset
                )

                val altCenter = Offset(
                    x = size.width / 3 - (size.width / 5 * kotlin.math.sin(rad)).toFloat(),
                    y = size.height * 0.7f + (size.height / 8 * kotlin.math.cos(rad)).toFloat()
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF09254A).copy(alpha = 0.35f), Color.Transparent),
                        center = altCenter,
                        radius = size.width * 0.6f
                    ),
                    radius = size.width * 0.6f,
                    center = altCenter
                )
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize().blur(60.dp)) {
                val rad = angle.toDouble() * (kotlin.math.PI / 180.0)
                val centerOffset = Offset(
                    x = size.width / 2 + (size.width * 0.2f * kotlin.math.cos(rad)).toFloat(),
                    y = size.height / 4
                )
                drawCircle(
                    color = accentColor.copy(alpha = 0.08f),
                    radius = size.width * 0.4f,
                    center = centerOffset
                )
            }
        }
    }
}

// ==========================================
// 4. PANTALLA: ACCESO SEGURO (LoginView)
// ==========================================

@Composable
fun LoginView(
    loginViewModel: LoginViewModel,
    themeColors: ThemeColors
) {
    val state by loginViewModel.state.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val surfaceColor = Color(parseColor(themeColors.surface)).copy(alpha = 0.3f)
    val accentColor = Color(parseColor(themeColors.accent))
    val textSec = Color(parseColor(themeColors.textSecondary))

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerRadius = 28.dp, borderColor = accentColor, surfaceColor = surfaceColor)
                .padding(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Aviación Logo",
                tint = accentColor,
                modifier = Modifier
                    .size(65.dp)
                    .scale(1.2f)
                    .background(accentColor.copy(alpha = 0.15f), shape = CircleShape)
                    .padding(14.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ORKA COMPOSE",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Text(
                text = "Control Premium Multiplataforma de Operaciones",
                fontSize = 12.sp,
                color = textSec,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = accentColor) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = textSec,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = accentColor) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = textSec,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Color(0xFFE53935),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE53935).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE53935).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        loginViewModel.signIn(username, password)
                    }
                },
                enabled = !state.isLoading && username.isNotEmpty() && password.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Iniciar Sesión",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. NAVEGADOR PRINCIPAL DE PESTAÑAS (TABS)
// ==========================================

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainNavigator(
    themeColors: ThemeColors,
    themeViewModel: ThemeViewModel,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val accentColor = Color(parseColor(themeColors.accent))
    val surfaceColor = Color(parseColor(themeColors.surface)).copy(alpha = 0.25f)
    val textSec = Color(parseColor(themeColors.textSecondary))

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 15.dp)
                    .glassCard(cornerRadius = 28.dp, borderColor = accentColor, surfaceColor = surfaceColor)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        Triple(0, Icons.Default.Menu, "KPIs"),
                        Triple(1, Icons.Default.DateRange, "Demoras"),
                        Triple(2, Icons.Default.List, "Vuelos"),
                        Triple(3, Icons.Default.LocationOn, "CCO"),
                        Triple(4, Icons.Default.Settings, "Ajustes")
                    )

                    tabs.forEach { (index, icon, label) ->
                        val isActive = selectedTab == index
                        val bgAlpha by animateFloatAsState(if (isActive) 0.15f else 0f)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(accentColor.copy(alpha = bgAlpha))
                                .clickable { selectedTab = index }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isActive) Color.White else textSec,
                                    modifier = Modifier.size(if (isActive) 22.dp else 19.dp)
                               )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    fontSize = 8.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isActive) Color.White else textSec
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                }
            ) { tab ->
                when (tab) {
                    0 -> DashboardScreen(themeColors = themeColors)
                    1 -> FollowUpScreen(themeColors = themeColors)
                    2 -> VuelosScreen(themeColors = themeColors)
                    3 -> CCOScreen(themeColors = themeColors)
                    4 -> SettingsScreen(themeColors = themeColors, themeViewModel = themeViewModel, onSignOut = onSignOut)
                    else -> DashboardScreen(themeColors = themeColors)
                }
            }
        }
    }
}

// ==========================================
// 6. DASHBOARD EJECUTIVO COMPCompose (KPIs)
// ==========================================

@Composable
fun DashboardScreen(themeColors: ThemeColors) {
    val execVM: ExecutiveViewModel = koinViewModel()
    val kpis by execVM.kpis.collectAsState()
    
    val accentColor = Color(parseColor(themeColors.accent))
    val surfaceColor = Color(parseColor(themeColors.surface)).copy(alpha = 0.2f)
    val textPrimary = Color(parseColor(themeColors.textPrimary))
    val textSec = Color(parseColor(themeColors.textSecondary))

    // Control Físico de Rotación de la Esfera 3D
    var sphereOffset by remember { mutableStateOf(Offset.Zero) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Dashboard Ejecutivo",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = "Monitoreo Operativo en Tiempo Real",
                    fontSize = 12.sp,
                    color = textSec,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Esfera 3D Cristalina Interactiva (OTP)
        kpis.firstOrNull { it.name == "OTP + 15" }?.let { otp ->
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        try {
                                            awaitRelease()
                                        } finally {
                                            // Efecto muelle al soltar
                                            sphereOffset = Offset.Zero
                                        }
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragEnd = { sphereOffset = Offset.Zero },
                                    onDragCancel = { sphereOffset = Offset.Zero },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        sphereOffset = Offset(
                                            x = (sphereOffset.x + dragAmount.x).coerceIn(-100f, 100f),
                                            y = (sphereOffset.y + dragAmount.y).coerceIn(-100f, 100f)
                                        )
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Sombra radial base
                        Canvas(modifier = Modifier.size(240.dp)) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(accentColor.copy(alpha = 0.2f), Color.Transparent),
                                    radius = size.width * 0.45f
                                ),
                                radius = size.width * 0.45f
                            )
                        }

                        // Esfera Glassmórfica Principal
                        Box(
                            modifier = Modifier
                                .size(190.dp)
                                .offset(x = (sphereOffset.x / 4).dp, y = (sphereOffset.y / 4).dp)
                                .glassCard(cornerRadius = 95.dp, borderColor = accentColor, surfaceColor = Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Destello y brillo de refracción
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = Color.White.copy(alpha = 0.15f),
                                    startAngle = -135f,
                                    sweepAngle = 90f,
                                    useCenter = false,
                                    style = Stroke(3.dp.toPx())
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "OTP + 15",
                                    fontSize = 11.sp,
                                    color = textSec,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "${otp.valueY4.toString().take(4)}%",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Promedio Global",
                                    fontSize = 10.sp,
                                    color = Color(parseColor(themeColors.kpiGood)),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = "Arrastra para inclinar la esfera 3D",
                        fontSize = 10.sp,
                        color = textSec,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Concentric Rings Chart: Performance de Bloque (BTP)
        kpis.firstOrNull { it.name == "BTP + 0" }?.let { btp ->
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(cornerRadius = 24.dp, borderColor = accentColor, surfaceColor = surfaceColor)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "BTP + 0 (Block Time Performance)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Canvas(modifier = Modifier.size(130.dp)) {
                            val stroke = 10.dp.toPx()
                            val center = this.center
                            val r1 = (115.dp / 2).toPx()
                            val r2 = (90.dp / 2).toPx()
                            val r3 = (65.dp / 2).toPx()

                            // Tracks
                            drawCircle(color = Color.White.copy(alpha = 0.03f), radius = r1, center = center, style = Stroke(stroke))
                            drawCircle(color = Color.White.copy(alpha = 0.03f), radius = r2, center = center, style = Stroke(stroke))
                            drawCircle(color = Color.White.copy(alpha = 0.03f), radius = r3, center = center, style = Stroke(stroke))

                            // Rings
                            drawArc(
                                color = accentColor,
                                startAngle = -90f,
                                sweepAngle = (btp.valueY4 / 100.0 * 360f).toFloat(),
                                useCenter = false,
                                style = Stroke(stroke, cap = StrokeCap.Round),
                                size = Size(r1 * 2, r1 * 2),
                                topLeft = Offset(center.x - r1, center.y - r1)
                            )
                            drawArc(
                                color = Color(0xFF2196F3),
                                startAngle = -90f,
                                sweepAngle = (btp.valueN3 / 100.0 * 360f).toFloat(),
                                useCenter = false,
                                style = Stroke(stroke, cap = StrokeCap.Round),
                                size = Size(r2 * 2, r2 * 2),
                                topLeft = Offset(center.x - r2, center.y - r2)
                            )
                            drawArc(
                                color = Color(0xFF9C27B0),
                                startAngle = -90f,
                                sweepAngle = (btp.valueQ6 / 100.0 * 360f).toFloat(),
                                useCenter = false,
                                style = Stroke(stroke, cap = StrokeCap.Round),
                                size = Size(r3 * 2, r3 * 2),
                                topLeft = Offset(center.x - r3, center.y - r3)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            DashboardLegendItem("Y4 Volaris", btp.valueY4, accentColor)
                            DashboardLegendItem("N3 Volaris S.", btp.valueN3, Color(0xFF2196F3))
                            DashboardLegendItem("Q6 Volaris CR", btp.valueQ6, Color(0xFF9C27B0))
                        }
                    }
                }
            }
        }

        // Comparativa de KPIs por Carrier
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerRadius = 24.dp, borderColor = accentColor, surfaceColor = surfaceColor)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Comparativo de KPIs por Carrier",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Indicador", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textSec, modifier = Modifier.weight(1.5f))
                    Text("Y4", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textSec, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    Text("Q6", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textSec, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    Text("N3", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textSec, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                kpis.filter { it.name != "OTP + 15" && it.name != "BTP + 0" }.forEach { kpi ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(kpi.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1.5f))
                        Text("${kpi.valueY4.toString().take(4)}%", fontSize = 12.sp, color = getKpiValueColor(kpi.valueY4, themeColors), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        Text("${kpi.valueQ6.toString().take(4)}%", fontSize = 12.sp, color = getKpiValueColor(kpi.valueQ6, themeColors), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        Text("${kpi.valueN3.toString().take(4)}%", fontSize = 12.sp, color = getKpiValueColor(kpi.valueN3, themeColors), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun DashboardLegendItem(label: String, value: Double, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Column {
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold)
            Text("${value.toString().take(4)}%", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

fun getKpiValueColor(valeur: Double, theme: ThemeColors): Color {
    return when {
        valeur >= 85.0 -> Color(parseColor(theme.kpiGood))
        valeur >= 80.0 -> Color(parseColor(theme.kpiMedium))
        else -> Color(parseColor(theme.kpiBad))
    }
}

// ==========================================
// 7. PANTALLA: FOLLOW UP DE DEMORAS (Canvas)
// ==========================================

@Composable
fun FollowUpScreen(themeColors: ThemeColors) {
    val followUpVM: FollowUpViewModel = koinViewModel()
    val delays by followUpVM.delays.collectAsState()
    val selectedCarrier by followUpVM.selectedCarrier.collectAsState()
    
    val accentColor = Color(parseColor(themeColors.accent))
    val surfaceColor = Color(parseColor(themeColors.surface)).copy(alpha = 0.2f)
    val textPrimary = Color(parseColor(themeColors.textPrimary))
    val textSec = Color(parseColor(themeColors.textSecondary))

    var hoveredBarCode by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Follow Up de Demoras",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = "Distribución de Afectaciones por Carrier",
                    fontSize = 12.sp,
                    color = textSec,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Filtro Horizontal de Carriers
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerRadius = 16.dp, borderColor = accentColor, surfaceColor = Color.White.copy(alpha = 0.03f))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    listOf("Y4", "Q6", "N3").forEach { code ->
                        val isSelected = selectedCarrier == code
                        val bgAlpha by animateFloatAsState(if (isSelected) 0.2f else 0f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentColor.copy(alpha = bgAlpha))
                                .clickable { 
                                    followUpVM.selectCarrier(code)
                                    hoveredBarCode = null
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = code,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else textSec
                            )
                        }
                    }
                }
            }
        }

        // Gráfico de Barras Táctil Avanzado sobre Canvas
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerRadius = 24.dp, borderColor = accentColor, surfaceColor = surfaceColor)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Demoras del Carrier $selectedCarrier",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Canvas con lógica interactiva de coordenadas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .pointerInput(delays) {
                            detectTapGestures { tapOffset ->
                                val totalBars = delays.size
                                if (totalBars > 0) {
                                    val barWidthWithSpacing = size.width / totalBars
                                    val tappedIndex = (tapOffset.x / barWidthWithSpacing).toInt().coerceIn(0, totalBars - 1)
                                    hoveredBarCode = delays.getOrNull(tappedIndex)?.code
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxCount = delays.maxOfOrNull { it.flightCount }?.toFloat() ?: 10f
                        val numBars = delays.size
                        if (numBars > 0) {
                            val spacing = 20.dp.toPx()
                            val widthPerBar = (size.width - (spacing * (numBars - 1))) / numBars
                            
                            // Líneas de cuadrícula horizontal
                            val gridLines = 4
                            for (i in 0..gridLines) {
                                val yCoord = size.height * (i.toFloat() / gridLines)
                                drawLine(
                                    color = Color.White.copy(alpha = 0.05f),
                                    start = Offset(0f, yCoord),
                                    end = Offset(size.width, yCoord),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                            }

                            delays.forEachIndexed { index, delay ->
                                val barHeight = size.height * (delay.flightCount.toFloat() / maxCount)
                                val xCoord = index * (widthPerBar + spacing)
                                val isHovered = hoveredBarCode == delay.code

                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            accentColor.copy(alpha = if (isHovered) 1f else 0.8f),
                                            accentColor.copy(alpha = if (isHovered) 0.4f else 0.2f)
                                        )
                                    ),
                                    topLeft = Offset(xCoord, size.height - barHeight),
                                    size = Size(widthPerBar, barHeight),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Detalle interactivo al presionar una barra
                val selectedDelay = delays.firstOrNull { it.code == hoveredBarCode }
                if (selectedDelay != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassCard(cornerRadius = 14.dp, borderColor = accentColor, surfaceColor = Color.White.copy(alpha = 0.04f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(accentColor, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(selectedDelay.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${selectedDelay.flightCount} vuelos retrasados (${selectedDelay.percentage.toString().take(4)}%)", fontSize = 10.sp, color = textSec, fontWeight = FontWeight.SemiBold)
                        }
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpiar selección",
                            tint = textSec,
                            modifier = Modifier.size(16.dp).clickable { hoveredBarCode = null }
                        )
                    }
                } else {
                    Text(
                        text = "Toca cualquier barra del gráfico para ver los detalles",
                        fontSize = 11.sp,
                        color = textSec,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Detalle de Afectaciones en Lista Plana
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerRadius = 24.dp, borderColor = accentColor, surfaceColor = surfaceColor)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Detalle de Afectaciones",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                delays.forEach { delay ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = delay.code,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .size(width = 42.dp, height = 24.dp)
                                .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .wrapContentHeight()
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(delay.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${delay.percentage.toString().take(4)}% del total de retrasos", fontSize = 10.sp, color = textSec, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            text = "${delay.flightCount}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ==========================================
// 8. PANTALLA: VUELOS EN TIEMPO REAL / GANTT
// ==========================================

@Composable
fun FlightSliderTimeline(
    progress: Float,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxWidth().height(16.dp)) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        // Track line
        drawLine(
            color = inactiveColor.copy(alpha = 0.15f),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 2.dp.toPx()
        )
        
        // Progress active line
        drawLine(
            color = activeColor,
            start = Offset(0f, centerY),
            end = Offset(width * progress, centerY),
            strokeWidth = 3.dp.toPx()
        )
        
        // Tick marks
        val tickCount = 25
        val spacing = width / (tickCount - 1)
        for (i in 0 until tickCount) {
            val x = i * spacing
            val isPassed = x <= width * progress
            val tickHeight = if (i % 5 == 0) 8.dp.toPx() else 4.dp.toPx()
            drawLine(
                color = if (isPassed) activeColor else inactiveColor.copy(alpha = 0.25f),
                start = Offset(x, centerY - tickHeight / 2),
                end = Offset(x, centerY + tickHeight / 2),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // Dot thumb
        drawCircle(
            color = activeColor,
            radius = 6.dp.toPx(),
            center = Offset(width * progress, centerY)
        )
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = Offset(width * progress, centerY)
        )
    }
}

@Composable
fun VuelosScreen(themeColors: ThemeColors) {
    val ganttVM: GanttViewModel = koinViewModel()
    val flights by ganttVM.flights.collectAsState()
    
    val accentColor = Color(parseColor(themeColors.accent))
    val surfaceColor = Color(parseColor(themeColors.surface)).copy(alpha = 0.2f)
    val textPrimary = Color(parseColor(themeColors.textPrimary))
    val textSec = Color(parseColor(themeColors.textSecondary))

    var searchText by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("ALL") }
    var selectedFlightForDetail by remember { mutableStateOf<Flight?>(null) }
    
    // viewMode: 0 = Live List (mockup style), 1 = Gantt Timeline
    var viewMode by remember { mutableStateOf(0) }
    var subTabFilterTransit by remember { mutableStateOf(0) } // 0 = En Tránsito (ON_AIR/DELAYED), 1 = Todos

    val filteredFlights = remember(flights, searchText, filterStatus, subTabFilterTransit, viewMode) {
        flights.filter { flight ->
            val matchesSearch = searchText.isEmpty() || 
                    flight.flightNumber.contains(searchText, ignoreCase = true) ||
                    flight.registration.contains(searchText, ignoreCase = true)
            
            val matchesStatus = if (viewMode == 0) {
                if (subTabFilterTransit == 0) {
                    flight.status == "ON_AIR" || flight.status == "DELAYED"
                } else {
                    true
                }
            } else {
                filterStatus == "ALL" || flight.status == filterStatus
            }
            
            matchesSearch && matchesStatus
        }
    }

    val groupedFlights = remember(filteredFlights) { filteredFlights.groupBy { it.registration } }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. HEADER PREMIUM
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.15f))
                                .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = "Hola, Julian!",
                                fontSize = 12.sp,
                                color = textSec,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "MEX - Ciudad de México",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                    
                    // Notification Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                            .clickable { }
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            // Red Dot Badge
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(parseColor(themeColors.kpiBad)), CircleShape)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }

            // 2. TOGGLE DE VISTA (Live List vs Gantt)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .glassCard(cornerRadius = 24.dp, borderColor = accentColor, surfaceColor = Color.White.copy(alpha = 0.03f))
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        listOf("Vuelos en Vivo ✈️", "Línea Gantt 📊").forEachIndexed { index, title ->
                            val isSelected = viewMode == index
                            val bgAlpha by animateFloatAsState(if (isSelected) 1f else 0f)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) accentColor else Color.Transparent)
                                    .clickable { viewMode = index }
                                    .wrapContentSize(Alignment.Center)
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else textSec
                                )
                            }
                        }
                    }
                }
            }

            // VISTA 0: VUELOS EN VIVO (MOCKUP STYLE)
            if (viewMode == 0) {
                // Curved Arc Flight Route Monitoring
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassCard(cornerRadius = 24.dp, borderColor = accentColor, surfaceColor = surfaceColor)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val arcPath = Path().apply {
                                    moveTo(20.dp.toPx(), size.height - 10.dp.toPx())
                                    quadraticTo(
                                        size.width / 2,
                                        -20.dp.toPx(),
                                        size.width - 20.dp.toPx(),
                                        size.height - 10.dp.toPx()
                                    )
                                }
                                drawPath(
                                    path = arcPath,
                                    color = accentColor.copy(alpha = 0.4f),
                                    style = Stroke(
                                        width = 1.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                                        cap = StrokeCap.Round
                                    )
                                )
                            }
                            
                            // Plane flying in center
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(18.dp)
                                    .align(Alignment.TopCenter)
                                    .offset(y = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text("Origen", fontSize = 9.sp, color = textSec, fontWeight = FontWeight.Bold)
                                    Text("MEX", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                                
                                Text(
                                    text = "25 de Mayo 2026",
                                    fontSize = 11.sp,
                                    color = textSec,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Destino", fontSize = 9.sp, color = textSec, fontWeight = FontWeight.Bold)
                                    Text("CUN", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Inner sub-tab toggle "En Tránsito" vs "Todos"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(21.dp))
                                .padding(3.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                listOf("En Tránsito", "Todos los Vuelos").forEachIndexed { index, label ->
                                    val isSelected = subTabFilterTransit == index
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(if (isSelected) Color(parseColor(themeColors.accent)).copy(alpha = 0.2f) else Color.Transparent)
                                            .border(1.dp, if (isSelected) Color(parseColor(themeColors.accent)).copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(18.dp))
                                            .clickable { subTabFilterTransit = index }
                                            .wrapContentSize(Alignment.Center)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else textSec
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Search Box
                item {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Buscar por vuelo, matrícula...", color = Color.White.copy(alpha = 0.3f)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accentColor) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Flight List Cards
                if (filteredFlights.isEmpty()) {
                    item {
                        Text(
                            text = "No hay vuelos en tránsito en este momento.",
                            color = textSec,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(40.dp)
                        )
                    }
                } else {
                    items(filteredFlights) { flight ->
                        // Calculate simulated flight progress
                        val progress = when (flight.status) {
                            "ARRIVED" -> 1.0f
                            "FUTURE" -> 0.0f
                            "ON_AIR" -> 0.65f
                            "DELAYED" -> 0.35f
                            else -> 0.5f
                        }
                        
                        val statusColor = when (flight.status) {
                            "ON_AIR" -> Color(parseColor(themeColors.accent))
                            "DELAYED" -> Color(parseColor(themeColors.kpiBad))
                            "ARRIVED" -> Color(parseColor(themeColors.kpiGood))
                            else -> Color.Gray
                        }

                        val statusLabel = when (flight.status) {
                            "ON_AIR" -> "EN VUELO"
                            "DELAYED" -> "RETRASADO"
                            "ARRIVED" -> "ARRIBADO"
                            else -> "PLANEADO"
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassCard(cornerRadius = 24.dp, borderColor = accentColor, surfaceColor = surfaceColor)
                                .clickable { selectedFlightForDetail = flight }
                                .padding(20.dp)
                        ) {
                            // Card Header: CDG -> LON style
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = flight.origin,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (flight.origin == "MEX") "Ciudad de México" else "Origen",
                                        fontSize = 10.sp,
                                        color = textSec,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "1h 45m", // Mock duration
                                        fontSize = 9.sp,
                                        color = textSec,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = flight.destination,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (flight.destination == "CUN") "Cancún" else "Destino",
                                        fontSize = 10.sp,
                                        color = textSec,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Timeline slider
                            FlightSliderTimeline(
                                progress = progress,
                                activeColor = statusColor,
                                inactiveColor = textSec,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Bottom info row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(statusColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${flight.flightNumber} • ${flight.registration}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                
                                // Beautiful Status Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(statusColor.copy(alpha = 0.15f))
                                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = statusLabel,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = statusColor
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // VISTA 1: TIMELINE GANTT (EXISTENTE REESTRUCTURADA)
                item {
                    Text(
                        text = "Movimientos de la Flota por Matrícula",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            placeholder = { Text("Buscar por vuelo o matrícula...", color = Color.White.copy(alpha = 0.3f)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accentColor) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Estatus filters
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val filters = listOf(
                                "ALL" to "Todos",
                                "ON_AIR" to "En Aire",
                                "DELAYED" to "Demorados",
                                "ARRIVED" to "Arribados"
                            )
                            items(filters) { (status, label) ->
                                val isActive = filterStatus == status
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isActive) accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.03f))
                                        .border(1.dp, if (isActive) accentColor else Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                        .clickable { filterStatus = status }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isActive) Color.White else textSec)
                                }
                            }
                        }
                    }
                }

                if (groupedFlights.isEmpty()) {
                    item {
                        Text(
                            text = "No se encontraron vuelos asignados",
                            color = textSec,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(40.dp)
                        )
                    }
                } else {
                    items(groupedFlights.keys.sorted()) { registration ->
                        val fleetFlights = groupedFlights[registration] ?: emptyList()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassCard(cornerRadius = 20.dp, borderColor = accentColor, surfaceColor = surfaceColor)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.width(75.dp)) {
                                Text(registration, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("A320neo", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = textSec)
                            }

                            Box(modifier = Modifier.weight(1f).height(46.dp)) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.05f),
                                        start = Offset(0f, size.height / 2),
                                        end = Offset(size.width, size.height / 2),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    fleetFlights.forEach { flight ->
                                        val statusColor = when (flight.status) {
                                            "ON_AIR" -> accentColor
                                            "DELAYED" -> Color(parseColor(themeColors.kpiBad))
                                            "ARRIVED" -> Color(parseColor(themeColors.kpiGood))
                                            else -> Color.Gray
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(statusColor.copy(alpha = 0.15f))
                                                .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                .clickable { selectedFlightForDetail = flight }
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${flight.flightNumber} ➔ ${flight.destination}",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Details Modal
        selectedFlightForDetail?.let { flight ->
            FlightDetailModal(
                flight = flight,
                themeColors = themeColors,
                onDismiss = { selectedFlightForDetail = null }
            )
        }
    }
}

@Composable
fun FlightDetailModal(
    flight: Flight,
    themeColors: ThemeColors,
    onDismiss: () -> Unit
) {
    val accentColor = Color(parseColor(themeColors.accent))
    val surfaceColor = Color(parseColor(themeColors.surface))
    val textSec = Color(parseColor(themeColors.textSecondary))

    val statusColor = when (flight.status) {
        "ON_AIR" -> accentColor
        "DELAYED" -> Color(parseColor(themeColors.kpiBad))
        "ARRIVED" -> Color(parseColor(themeColors.kpiGood))
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerRadius = 28.dp, borderColor = accentColor, surfaceColor = surfaceColor)
                .clickable(enabled = false, onClick = {}) // Evitar cerrar al tocar dentro
                .padding(24.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Detalle de Vuelo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(flight.flightNumber, fontSize = 13.sp, color = accentColor, fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = textSec)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Ruta
            Row(
                modifier = Modifier.fillMaxWidth().glassCard(cornerRadius = 16.dp, borderColor = accentColor, surfaceColor = Color.White.copy(alpha = 0.03f)).padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Origen", fontSize = 10.sp, color = textSec, fontWeight = FontWeight.Bold)
                    Text(flight.origin, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(statusColor.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(flight.status, fontSize = 8.sp, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Destino", fontSize = 10.sp, color = textSec, fontWeight = FontWeight.Bold)
                    Text(flight.destination, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detalles
            Column(
                modifier = Modifier.fillMaxWidth().glassCard(cornerRadius = 16.dp, borderColor = accentColor, surfaceColor = Color.White.copy(alpha = 0.03f)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow("Matrícula", flight.registration, textSec)
                DetailRow("Demora", if (flight.delayMinutes > 0) "${flight.delayMinutes} minutos" else "Sin demoras", if (flight.delayMinutes > 0) Color(parseColor(themeColors.kpiBad)) else Color(parseColor(themeColors.kpiGood)))
                DetailRow("Piloto", flight.crewPilot, textSec)
                DetailRow("Primer Oficial", flight.crewCopilot, textSec)
                DetailRow("Jefe de Cabina", flight.crewChief, textSec)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DetailRow(label: String, valText: String, valColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold)
        Text(valText, fontSize = 12.sp, color = valColor, fontWeight = FontWeight.Bold)
    }
}

// ==========================================
// 9. PANTALLA: CCO MAPA DE BUBBLES
// ==========================================

@Composable
fun CCOScreen(themeColors: ThemeColors) {
    val ccoVM: CCOViewModel = koinViewModel()
    val stationKPIs by ccoVM.stationKPIs.collectAsState()
    
    val accentColor = Color(parseColor(themeColors.accent))
    val surfaceColor = Color(parseColor(themeColors.surface)).copy(alpha = 0.2f)
    val textPrimary = Color(parseColor(themeColors.textPrimary))
    val textSec = Color(parseColor(themeColors.textSecondary))

    var selectedStation by remember { mutableStateOf<StationKPI?>(null) }

    val coordinates = remember {
        mapOf(
            "MEX" to Offset(0.5f, 0.62f),
            "CUN" to Offset(0.85f, 0.44f),
            "GDL" to Offset(0.24f, 0.54f),
            "TIJ" to Offset(0.14f, 0.22f),
            "MTY" to Offset(0.54f, 0.28f)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Control CCO",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = "Salud y Rendimiento Operativo por Estación",
                    fontSize = 12.sp,
                    color = textSec,
                    fontWeight = FontWeight.Medium
                )
            }

            // Mapa de burbujas dinámicas dibujado en Canvas con gestos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .glassCard(cornerRadius = 24.dp, borderColor = accentColor, surfaceColor = surfaceColor)
                    .pointerInput(stationKPIs) {
                        detectTapGestures { tapOffset ->
                            val width = size.width
                            val height = size.height
                            
                            // Buscar la burbuja más cercana al tap
                            stationKPIs.forEach { kpi ->
                                val coord = coordinates[kpi.station] ?: Offset(0.5f, 0.5f)
                                val xPos = coord.x * width
                                val yPos = coord.y * height
                                val sizeScale = 45.dp.toPx()
                                
                                val distance = kotlin.math.sqrt(
                                    (tapOffset.x - xPos) * (tapOffset.x - xPos) +
                                    (tapOffset.y - yPos) * (tapOffset.y - yPos)
                                )
                                if (distance <= sizeScale) {
                                    selectedStation = kpi
                                }
                            }
                        }
                    }
            ) {
                // Dibujo técnico de la malla y las burbujas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Malla técnica
                    val step = 30.dp.toPx()
                    for (x in 0..(size.width / step).toInt()) {
                        drawLine(Color.White.copy(alpha = 0.02f), Offset(x * step, 0f), Offset(x * step, size.height))
                    }
                    for (y in 0..(size.height / step).toInt()) {
                        drawLine(Color.White.copy(alpha = 0.02f), Offset(0f, y * step), Offset(size.width, y * step))
                    }

                    // Burbujas
                    stationKPIs.forEach { kpi ->
                        val coord = coordinates[kpi.station] ?: Offset(0.5f, 0.5f)
                        val xPos = coord.x * size.width
                        val yPos = coord.y * size.height
                        
                        val statusColor = when (kpi.statusColor) {
                            "GOOD" -> Color(parseColor(themeColors.kpiGood))
                            "MEDIUM" -> Color(parseColor(themeColors.kpiMedium))
                            else -> Color(parseColor(themeColors.kpiBad))
                        }

                        // Aura pulsante
                        drawCircle(
                            color = statusColor.copy(alpha = 0.12f),
                            radius = 45.dp.toPx(),
                            center = Offset(xPos, yPos)
                        )

                        // Núcleo translúcido
                        drawCircle(
                            color = Color.White.copy(alpha = 0.05f),
                            radius = 32.dp.toPx(),
                            center = Offset(xPos, yPos)
                        )
                        drawCircle(
                            color = statusColor.copy(alpha = 0.5f),
                            radius = 32.dp.toPx(),
                            center = Offset(xPos, yPos),
                            style = Stroke(1.dp.toPx())
                        )
                    }
                }

                // Superposición de etiquetas sobre el Canvas en Compose
                Box(modifier = Modifier.fillMaxSize()) {
                    stationKPIs.forEach { kpi ->
                        val coord = coordinates[kpi.station] ?: Offset(0.5f, 0.5f)
                        val statusColor = when (kpi.statusColor) {
                            "GOOD" -> Color(parseColor(themeColors.kpiGood))
                            "MEDIUM" -> Color(parseColor(themeColors.kpiMedium))
                            else -> Color(parseColor(themeColors.kpiBad))
                        }
                        
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(
                                    x = (coord.x * 290.dp.value).dp, // Coordenada burda escalada
                                    y = (coord.y * 380.dp.value).dp
                                )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(kpi.station, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("${kpi.otp15.toString().take(4)}%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor)
                            }
                        }
                    }
                }
            }

            // Detalle Operativo en la Base al seleccionar una burbuja
            val currentStation = selectedStation
            if (currentStation != null) {
                val statusColor = when (currentStation.statusColor) {
                    "GOOD" -> Color(parseColor(themeColors.kpiGood))
                    "MEDIUM" -> Color(parseColor(themeColors.kpiMedium))
                    else -> Color(parseColor(themeColors.kpiBad))
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(cornerRadius = 24.dp, borderColor = statusColor, surfaceColor = surfaceColor)
                        .padding(18.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Estación ${currentStation.station}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        IconButton(onClick = { selectedStation = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = textSec, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Volumen Diario", fontSize = 11.sp, color = textSec, fontWeight = FontWeight.SemiBold)
                        Text("${currentStation.flightVolume} Vuelos", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProgressIndicatorCCO("OTP + 15", currentStation.otp15, accentColor)
                        ProgressIndicatorCCO("BTP + 0", currentStation.btp0, Color(0xFF2196F3))
                        ProgressIndicatorCCO("GTP + 5", currentStation.gtp5, Color(0xFF9C27B0))
                    }
                }
            } else {
                Text(
                    text = "Toca una burbuja de estación para ver el desglose operativo",
                    fontSize = 11.sp,
                    color = textSec,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ProgressIndicatorCCO(label: String, value: Double, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold)
            Text("${value.toString().take(4)}%", fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (value / 100.0).toFloat())
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}

// ==========================================
// 10. PANTALLA: CONFIGURACIÓN Y TEMAS
// ==========================================

@Composable
fun SettingsScreen(
    themeColors: ThemeColors,
    themeViewModel: ThemeViewModel,
    onSignOut: () -> Unit
) {
    val accentColor = Color(parseColor(themeColors.accent))
    val surfaceColor = Color(parseColor(themeColors.surface)).copy(alpha = 0.2f)
    val textPrimary = Color(parseColor(themeColors.textPrimary))
    val textSec = Color(parseColor(themeColors.textSecondary))

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Configuración",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = "Personalización de la Experiencia Visual",
                    fontSize = 12.sp,
                    color = textSec,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Info de Perfil de Usuario
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerRadius = 24.dp, borderColor = accentColor, surfaceColor = surfaceColor)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBox,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(45.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Julian Soto", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Administrador de Operaciones", fontSize = 11.sp, color = textSec, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Selector de Temas Visuales
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerRadius = 24.dp, borderColor = accentColor, surfaceColor = surfaceColor)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Seleccionar Tema Visual",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ThemeOption(
                        title = "Liquid Glass 💎",
                        desc = "Vidrio templado inmersivo y profundidad 3D",
                        isActive = themeColors.theme == AppTheme.LIQUID_GLASS,
                        accentColor = accentColor,
                        textSec = textSec,
                        onClick = { themeViewModel.selectTheme(AppTheme.LIQUID_GLASS) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    ThemeOption(
                        title = "Volaris Premium 🪻",
                        desc = "Color predominante rosa/morado de marca",
                        isActive = themeColors.theme == AppTheme.VOLARIS_PREMIUM,
                        accentColor = accentColor,
                        textSec = textSec,
                        onClick = { themeViewModel.selectTheme(AppTheme.VOLARIS_PREMIUM) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    ThemeOption(
                        title = "Modo Claro Minimalista ☀️",
                        desc = "Superficies limpias e interfaces claras",
                        isActive = themeColors.theme == AppTheme.LIGHT,
                        accentColor = accentColor,
                        textSec = textSec,
                        onClick = { themeViewModel.selectTheme(AppTheme.LIGHT) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    ThemeOption(
                        title = "Modo Oscuro Obsidian 🌙",
                        desc = "Negros puros optimizados para pantallas OLED",
                        isActive = themeColors.theme == AppTheme.DARK,
                        accentColor = accentColor,
                        textSec = textSec,
                        onClick = { themeViewModel.selectTheme(AppTheme.DARK) }
                    )
                }
            }
        }

        // Botón Cerrar Sesión
        item {
            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B).copy(alpha = 0.2f)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFC0392B).copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                    Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ThemeOption(
    title: String,
    desc: String,
    isActive: Boolean,
    accentColor: Color,
    textSec: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(desc, fontSize = 10.sp, color = textSec, fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier
                .size(16.dp)
                .border(2.dp, if (isActive) accentColor else Color.White.copy(alpha = 0.2f), CircleShape)
                .padding(3.dp)
        ) {
            if (isActive) {
                Box(modifier = Modifier.fillMaxSize().background(accentColor, CircleShape))
            }
        }
    }
}
