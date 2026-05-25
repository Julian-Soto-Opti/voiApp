import SwiftUI
import Charts
import Shared

// ==========================================
// 1. PUENTES Y UTILERÍAS REACTIVAS (FlowUtils)
// ==========================================

/// Recolector para flujos de Kotlin (FlowCollector)
class FlowCollector<T>: NSObject, Kotlinx_coroutines_coreFlowCollector {
    private let onEmit: (T) -> Void

    init(onEmit: @escaping (T) -> Void) {
        self.onEmit = onEmit
    }

    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        if let castedValue = value as? T {
            onEmit(castedValue)
        }
        completionHandler(nil)
    }
}

/// Extensión para facilitar la colección de StateFlows de Kotlin directamente en SwiftUI
extension View {
    func collect<T>(_ flow: Kotlinx_coroutines_coreStateFlow, into binding: Binding<T>) -> some View {
        self.onAppear {
            let collector = FlowCollector<T> { newValue in
                DispatchQueue.main.async {
                    binding.wrappedValue = newValue
                }
            }
            flow.collect(collector: collector) { _ in }
        }
    }
    
    // Extensión utilitaria para placeholder en TextField
    func placeholder<Content: View>(
        when shouldShow: Bool,
        alignment: Alignment = .leading,
        @ViewBuilder placeholder: () -> Content
    ) -> some View {
        ZStack(alignment: alignment) {
            placeholder().opacity(shouldShow ? 1 : 0)
            self
        }
    }
}

// ==========================================
// 2. SISTEMA DE TEMAS PREMIUM (AviationThemes)
// ==========================================

/// Extensión para inicializar colores de SwiftUI a partir de strings Hexadecimales de Kotlin
extension Color {
    init(hex: String) {
        var cleanHex = hex.trimmingCharacters(in: .whitespacesAndNewlines)
        if cleanHex.hasPrefix("#") {
            cleanHex.remove(at: cleanHex.startIndex)
        }
        
        var rgb: UInt64 = 0
        Scanner(string: cleanHex).scanHexInt64(&rgb)
        
        let r, g, b: Double
        if cleanHex.count == 6 {
            r = Double((rgb >> 16) & 0xFF) / 255.0
            g = Double((rgb >> 8) & 0xFF) / 255.0
            b = Double(rgb & 0xFF) / 255.0
        } else if cleanHex.count == 8 {
            let a = Double((rgb >> 24) & 0xFF) / 255.0
            r = Double((rgb >> 16) & 0xFF) / 255.0
            g = Double((rgb >> 8) & 0xFF) / 255.0
            b = Double(rgb & 0xFF) / 255.0
            self.init(.sRGB, red: r, green: g, blue: b, opacity: a)
            return
        } else {
            r = 0; g = 0; b = 0
        }
        
        self.init(.sRGB, red: r, green: g, blue: b, opacity: 1.0)
    }
}

/// Representación nativa de SwiftUI de los colores del tema de aviación
struct SwiftUITheme {
    let theme: AppTheme
    let background: Color
    let surface: Color
    let accent: Color
    let textPrimary: Color
    let textSecondary: Color
    let border: Color
    let kpiGood: Color
    let kpiMedium: Color
    let kpiBad: Color

    init(colors: ThemeColors) {
        self.theme = colors.theme
        self.background = Color(hex: colors.background)
        self.surface = Color(hex: colors.surface)
        self.accent = Color(hex: colors.accent)
        self.textPrimary = Color(hex: colors.textPrimary)
        self.textSecondary = Color(hex: colors.textSecondary)
        self.border = Color(hex: colors.border)
        self.kpiGood = Color(hex: colors.kpiGood)
        self.kpiMedium = Color(hex: colors.kpiMedium)
        self.kpiBad = Color(hex: colors.kpiBad)
    }
}

/// Administrador de Temas observable en SwiftUI que se sincroniza con el ThemeViewModel de Kotlin
class ThemeManager: ObservableObject {
    @Published var activeTheme: AppTheme
    @Published var current: SwiftUITheme
    
    private let viewModel: ThemeViewModel
    private var activeThemeCollector: FlowCollector<AppTheme>?
    private var colorsCollector: FlowCollector<ThemeColors>?

    init(viewModel: ThemeViewModel) {
        self.viewModel = viewModel
        
        // Cargar valores iniciales desde Kotlin con as! cast para evitar errores
        let initialTheme = viewModel.theme.value as! AppTheme
        self.activeTheme = initialTheme
        self.current = SwiftUITheme(colors: viewModel.colors.value as! ThemeColors)
        
        // Suscribirse al flujo del tema activo
        self.activeThemeCollector = FlowCollector<AppTheme> { [weak self] newTheme in
            DispatchQueue.main.async {
                self?.activeTheme = newTheme
            }
        }
        viewModel.theme.collect(collector: self.activeThemeCollector!) { _ in }
        
        // Suscribirse al flujo de colores reactivos
        self.colorsCollector = FlowCollector<ThemeColors> { [weak self] newColors in
            DispatchQueue.main.async {
                self?.current = SwiftUITheme(colors: newColors)
            }
        }
        viewModel.colors.collect(collector: self.colorsCollector!) { _ in }
    }
    
    func selectTheme(_ theme: AppTheme) {
        viewModel.selectTheme(theme: theme)
    }
}

// ==========================================
// 3. EFECTOS LIQUID GLASS Y FONDOS (GlassModifiers)
// ==========================================

/// Modificador que transforma cualquier vista en una tarjeta translúcida de estilo Liquid Glass
struct GlassCardModifier: ViewModifier {
    let cornerRadius: CGFloat
    let borderColor: Color
    let surfaceColor: Color
    let shadowColor: Color

    func body(content: Content) -> some View {
        content
            .padding()
            .background(
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(surfaceColor)
                    .background(
                        RoundedRectangle(cornerRadius: cornerRadius)
                            .fill(.ultraThinMaterial)
                    )
            )
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius)
                    .stroke(
                        LinearGradient(
                            colors: [
                                .white.opacity(0.3),
                                borderColor.opacity(0.1),
                                .white.opacity(0.05),
                                borderColor.opacity(0.2)
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        lineWidth: 1
                    )
            )
            .shadow(
                color: shadowColor.opacity(0.15),
                radius: 12,
                x: 0,
                y: 8
            )
    }
}

extension View {
    func glassCard(
        cornerRadius: CGFloat = 20,
        borderColor: Color = .white,
        surfaceColor: Color = .white.opacity(0.1),
        shadowColor: Color = .black
    ) -> some View {
        self.modifier(
            GlassCardModifier(
                cornerRadius: cornerRadius,
                borderColor: borderColor,
                surfaceColor: surfaceColor,
                shadowColor: shadowColor
            )
        )
    }
}

/// Fondo inmersivo con gradientes móviles orgánicos
struct OrganicBackground: View {
    let accentColor: Color
    let isDarkMode: Bool
    @State private var animateCircles = false

    var body: some View {
        ZStack {
            if isDarkMode {
                Color.black.ignoresSafeArea()
            } else {
                Color(hex: "#F9FBFC").ignoresSafeArea()
            }
            
            if isDarkMode {
                ZStack {
                    Circle()
                        .fill(accentColor.opacity(0.3))
                        .frame(width: 400, height: 400)
                        .offset(x: animateCircles ? 100 : -100, y: animateCircles ? -150 : -80)
                    
                    Circle()
                        .fill(Color(hex: "#09254A").opacity(0.4))
                        .frame(width: 500, height: 500)
                        .offset(x: animateCircles ? -120 : 80, y: animateCircles ? 100 : 200)
                }
                .blur(radius: 80)
                .ignoresSafeArea()
            } else {
                ZStack {
                    Circle()
                        .fill(accentColor.opacity(0.1))
                        .frame(width: 300, height: 300)
                        .offset(x: animateCircles ? 80 : -80, y: -200)
                    
                    Circle()
                        .fill(Color.blue.opacity(0.08))
                        .frame(width: 450, height: 450)
                        .offset(x: -100, y: 150)
                }
                .blur(radius: 60)
                .ignoresSafeArea()
            }
        }
        .onAppear {
            withAnimation(
                .easeInOut(duration: 8.0)
                .repeatForever(autoreverses: true)
            ) {
                animateCircles = true
            }
        }
    }
}

// ==========================================
// 4. VISTA: ACCESO SEGURO (LoginView)
// ==========================================

class iOSLoginViewModel: ObservableObject {
    let viewModel: LoginViewModel
    @Published var state: LoginState
    private var stateCollector: FlowCollector<LoginState>?

    init(storage: AviationStorage) {
        let vm = LoginViewModel(storage: storage)
        self.viewModel = vm
        self.state = vm.state.value as! LoginState
        
        self.stateCollector = FlowCollector<LoginState> { [weak self] newState in
            DispatchQueue.main.async {
                self?.state = newState
            }
        }
        vm.state.collect(collector: self.stateCollector!) { _ in }
    }

    func signIn(username: String, password: String) {
        viewModel.signIn(username: username, password: password)
    }

    func signOut() {
        viewModel.signOut()
    }
}

struct LoginView: View {
    @EnvironmentObject var themeManager: ThemeManager
    @StateObject private var loginVM: iOSLoginViewModel
    @State private var username = ""
    @State private var password = ""
    
    init(storage: AviationStorage) {
        _loginVM = StateObject(wrappedValue: iOSLoginViewModel(storage: storage))
    }

    var body: some View {
        ZStack {
            OrganicBackground(accentColor: themeManager.current.accent, isDarkMode: true)
                .ignoresSafeArea()
            
            VStack(spacing: 30) {
                Spacer()
                
                VStack(spacing: 12) {
                    Image(systemName: "airplane.circle.fill")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 80, height: 80)
                        .foregroundColor(themeManager.current.accent)
                        .shadow(color: themeManager.current.accent.opacity(0.5), radius: 15)
                    
                    Text("ORKA DEMO")
                        .font(.system(size: 32, weight: .bold, design: .rounded))
                        .foregroundColor(.white)
                        .tracking(3)
                    
                    Text("Plataforma Multiplataforma Premium de Operaciones")
                        .font(.system(size: 14, weight: .regular, design: .rounded))
                        .foregroundColor(themeManager.current.textSecondary)
                        .multilineTextAlignment(.center)
                }
                .padding(.bottom, 20)
                
                VStack(spacing: 20) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Usuario")
                            .font(.system(size: 12, weight: .semibold, design: .rounded))
                            .foregroundColor(themeManager.current.textSecondary)
                        
                        HStack {
                            Image(systemName: "envelope.fill")
                                .foregroundColor(themeManager.current.accent)
                            TextField("", text: $username)
                                .foregroundColor(.white)
                                .autocapitalization(.none)
                                .disableAutocorrection(true)
                                .keyboardType(.emailAddress)
                                .placeholder(when: username.isEmpty) {
                                    Text("julian@orka.com").foregroundColor(.white.opacity(0.3))
                                }
                        }
                        .padding()
                        .background(Color.white.opacity(0.05))
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.white.opacity(0.1), lineWidth: 1)
                        )
                    }
                    
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Contraseña")
                            .font(.system(size: 12, weight: .semibold, design: .rounded))
                            .foregroundColor(themeManager.current.textSecondary)
                        
                        HStack {
                            Image(systemName: "lock.fill")
                                .foregroundColor(themeManager.current.accent)
                            SecureField("", text: $password)
                                .foregroundColor(.white)
                                .placeholder(when: password.isEmpty) {
                                    Text("••••••••").foregroundColor(.white.opacity(0.3))
                                }
                        }
                        .padding()
                        .background(Color.white.opacity(0.05))
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.white.opacity(0.1), lineWidth: 1)
                        )
                    }
                }
                .glassCard(cornerRadius: 24, borderColor: themeManager.current.accent)
                .padding(.horizontal, 24)
                
                if let errorMessage = loginVM.state.errorMessage {
                    Text(errorMessage)
                        .font(.system(size: 13, weight: .medium, design: .rounded))
                        .foregroundColor(.white)
                        .padding()
                        .frame(maxWidth: .infinity)
                        .background(Color.red.opacity(0.2))
                        .background(.ultraThinMaterial)
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.red.opacity(0.3), lineWidth: 1)
                        )
                        .padding(.horizontal, 24)
                        .transition(.move(edge: .top).combined(with: .opacity))
                }
                
                Button(action: {
                    guard !username.isEmpty && !password.isEmpty else { return }
                    UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
                    loginVM.signIn(username: username, password: password)
                }) {
                    HStack {
                        if loginVM.state.isLoading {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Text("Iniciar Sesión")
                                .font(.system(size: 16, weight: .bold, design: .rounded))
                                .foregroundColor(.white)
                                .tracking(1)
                        }
                    }
                    .frame(width: loginVM.state.isLoading ? 50 : 250, height: 50)
                    .background(
                        LinearGradient(
                            colors: [themeManager.current.accent, themeManager.current.accent.opacity(0.6)],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .clipShape(RoundedRectangle(cornerRadius: loginVM.state.isLoading ? 25 : 14, style: .continuous))
                    .shadow(color: themeManager.current.accent.opacity(0.4), radius: 10, x: 0, y: 5)
                }
                .disabled(loginVM.state.isLoading || username.isEmpty || password.isEmpty)
                .animation(.spring(response: 0.4, dampingFraction: 0.75, blendDuration: 0), value: loginVM.state.isLoading)
                
                Spacer()
            }
        }
    }
}

// ==========================================
// 5. VISTA: DASHBOARD EJECUTIVO (DashboardView)
// ==========================================

class iOSExecutiveViewModel: ObservableObject {
    let viewModel: ExecutiveViewModel
    @Published var kpis: [ExecutiveKPI] = []
    private var kpisCollector: FlowCollector<NSArray>?

    init(storage: AviationStorage) {
        let vm = ExecutiveViewModel(storage: storage)
        self.viewModel = vm
        if let initialKpis = vm.kpis.value as? [ExecutiveKPI] {
            self.kpis = initialKpis
        }
        
        self.kpisCollector = FlowCollector<NSArray> { [weak self] newList in
            if let list = newList as? [ExecutiveKPI] {
                DispatchQueue.main.async {
                    self?.kpis = list
                }
            }
        }
        vm.kpis.collect(collector: self.kpisCollector!) { _ in }
    }
}

struct DashboardView: View {
    @EnvironmentObject var themeManager: ThemeManager
    @StateObject private var execVM: iOSExecutiveViewModel
    @State private var pulseSphere = false
    @State private var dragOffset = CGSize.zero
    
    init(storage: AviationStorage) {
        _execVM = StateObject(wrappedValue: iOSExecutiveViewModel(storage: storage))
    }

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 25) {
                HStack {
                    VStack(alignment: .leading, spacing: 5) {
                        Text("Dashboard Ejecutivo")
                            .font(.system(size: 26, weight: .bold, design: .rounded))
                            .foregroundColor(themeManager.current.textPrimary)
                        Text("Monitoreo Operativo en Tiempo Real")
                            .font(.system(size: 14, weight: .medium, design: .rounded))
                            .foregroundColor(themeManager.current.textSecondary)
                    }
                    Spacer()
                }
                .padding(.horizontal, 20)
                .padding(.top, 20)
                
                if let otpKPI = execVM.kpis.first(where: { $0.name == "OTP + 15" }) {
                    VStack(spacing: 10) {
                        ZStack {
                            Circle()
                                .fill(
                                    RadialGradient(
                                        colors: [themeManager.current.accent.opacity(0.3), .clear],
                                        center: .center,
                                        startRadius: 5,
                                        endRadius: 130
                                    )
                                )
                                .frame(width: 260, height: 260)
                                .scaleEffect(pulseSphere ? 1.08 : 0.95)
                            
                            Circle()
                                .fill(
                                    LinearGradient(
                                        colors: [
                                            .white.opacity(0.15),
                                            themeManager.current.surface.opacity(0.05)
                                        ],
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .frame(width: 220, height: 220)
                                .overlay(
                                    Circle()
                                        .stroke(
                                            LinearGradient(
                                                colors: [.white.opacity(0.6), .clear, themeManager.current.accent.opacity(0.3)],
                                                startPoint: .topLeading,
                                                endPoint: .bottomTrailing
                                            ),
                                            lineWidth: 1.5
                                        )
                                )
                                .shadow(color: themeManager.current.accent.opacity(0.3), radius: 25, x: 0, y: 15)
                            
                            Ellipse()
                                .fill(
                                    LinearGradient(
                                        colors: [.white.opacity(0.35), .clear],
                                        startPoint: .top,
                                        endPoint: .bottom
                                    )
                                )
                                .frame(width: 150, height: 70)
                                .offset(y: -65)
                            
                            VStack(spacing: 4) {
                                Text("OTP + 15")
                                    .font(.system(size: 14, weight: .bold, design: .rounded))
                                    .foregroundColor(themeManager.current.textSecondary)
                                    .tracking(2)
                                
                                Text("\(String(format: "%.1f", otpKPI.valueY4))%")
                                    .font(.system(size: 40, weight: .heavy, design: .rounded))
                                    .foregroundColor(.white)
                                
                                Text("Promedio Global")
                                    .font(.system(size: 11, weight: .semibold, design: .rounded))
                                    .foregroundColor(themeManager.current.kpiGood)
                            }
                        }
                        .frame(width: 260, height: 260)
                        .rotation3DEffect(.degrees(Double(dragOffset.width / 6)), axis: (x: 0, y: 1, z: 0))
                        .rotation3DEffect(.degrees(Double(-dragOffset.height / 6)), axis: (x: 1, y: 0, z: 0))
                        .gesture(
                            DragGesture()
                                .onChanged { gesture in dragOffset = gesture.translation }
                                .onEnded { _ in
                                    withAnimation(.spring(response: 0.5, dampingFraction: 0.6)) { dragOffset = .zero }
                                }
                        )
                        
                        Text("Mantén y arrastra la esfera para rotar en 3D")
                            .font(.system(size: 11, weight: .regular, design: .rounded))
                            .foregroundColor(themeManager.current.textSecondary)
                    }
                    .padding(.vertical, 10)
                }
                
                if let btpKPI = execVM.kpis.first(where: { $0.name == "BTP + 0" }) {
                    VStack(alignment: .leading, spacing: 15) {
                        Text("BTP + 0 (Block Time Performance)")
                            .font(.system(size: 16, weight: .bold, design: .rounded))
                            .foregroundColor(themeManager.current.textPrimary)
                        
                        HStack(spacing: 20) {
                            ZStack {
                                Circle().stroke(Color.white.opacity(0.05), lineWidth: 12).frame(width: 140, height: 140)
                                Circle()
                                    .trim(from: 0, to: CGFloat(btpKPI.valueY4 / 100.0))
                                    .stroke(LinearGradient(colors: [themeManager.current.accent, themeManager.current.accent.opacity(0.4)], startPoint: .top, endPoint: .bottom), style: StrokeStyle(lineWidth: 12, lineCap: .round))
                                    .frame(width: 140, height: 140).rotationEffect(.degrees(-90))
                                
                                Circle().stroke(Color.white.opacity(0.05), lineWidth: 10).frame(width: 110, height: 110)
                                Circle()
                                    .trim(from: 0, to: CGFloat(btpKPI.valueN3 / 100.0))
                                    .stroke(LinearGradient(colors: [Color.blue, Color.blue.opacity(0.4)], startPoint: .top, endPoint: .bottom), style: StrokeStyle(lineWidth: 10, lineCap: .round))
                                    .frame(width: 110, height: 110).rotationEffect(.degrees(-90))
                                
                                Circle().stroke(Color.white.opacity(0.05), lineWidth: 8).frame(width: 82, height: 82)
                                Circle()
                                    .trim(from: 0, to: CGFloat(btpKPI.valueQ6 / 100.0))
                                    .stroke(LinearGradient(colors: [Color.purple, Color.purple.opacity(0.4)], startPoint: .top, endPoint: .bottom), style: StrokeStyle(lineWidth: 8, lineCap: .round))
                                    .frame(width: 82, height: 82).rotationEffect(.degrees(-90))
                            }
                            .frame(width: 155, height: 155)
                            
                            VStack(alignment: .leading, spacing: 10) {
                                LegendItem(carrier: "Y4 (Volaris)", value: btpKPI.valueY4, color: themeManager.current.accent)
                                LegendItem(carrier: "N3 (Volaris S.)", value: btpKPI.valueN3, color: .blue)
                                LegendItem(carrier: "Q6 (Volaris CR)", value: btpKPI.valueQ6, color: .purple)
                            }
                        }
                    }
                    .padding(20)
                    .glassCard(borderColor: themeManager.current.border)
                    .padding(.horizontal, 20)
                }
                
                VStack(alignment: .leading, spacing: 15) {
                    Text("Comparativo de KPIs por Carrier")
                        .font(.system(size: 16, weight: .bold, design: .rounded))
                        .foregroundColor(themeManager.current.textPrimary)
                    
                    VStack(spacing: 12) {
                        HStack {
                            Text("Indicador").font(.caption.bold()).foregroundColor(themeManager.current.textSecondary).frame(width: 100, alignment: .leading)
                            Spacer()
                            Text("Y4").font(.caption.bold()).foregroundColor(themeManager.current.textSecondary).frame(width: 50, alignment: .center)
                            Text("Q6").font(.caption.bold()).foregroundColor(themeManager.current.textSecondary).frame(width: 50, alignment: .center)
                            Text("N3").font(.caption.bold()).foregroundColor(themeManager.current.textSecondary).frame(width: 50, alignment: .center)
                        }
                        Divider().background(themeManager.current.border)
                        
                        ForEach(execVM.kpis.filter({ $0.name != "OTP + 15" && $0.name != "BTP + 0" }), id: \.name) { kpi in
                            HStack {
                                Text(kpi.name).font(.system(size: 13, weight: .semibold, design: .rounded)).foregroundColor(.white).frame(width: 100, alignment: .leading)
                                Spacer()
                                KPIBadge(value: kpi.valueY4, theme: themeManager).frame(width: 50)
                                KPIBadge(value: kpi.valueQ6, theme: themeManager).frame(width: 50)
                                KPIBadge(value: kpi.valueN3, theme: themeManager).frame(width: 50)
                            }
                            .padding(.vertical, 4)
                        }
                    }
                }
                .padding(20)
                .glassCard(borderColor: themeManager.current.border)
                .padding(.horizontal, 20)
                .padding(.bottom, 30)
            }
        }
        .background(OrganicBackground(accentColor: themeManager.current.accent, isDarkMode: themeManager.current.theme != .light))
        .onAppear {
            withAnimation(.easeInOut(duration: 2.0).repeatForever(autoreverses: true)) { pulseSphere = true }
        }
    }
}

struct LegendItem: View {
    let carrier: String
    let value: Double
    let color: Color
    var body: some View {
        HStack(spacing: 8) {
            Circle().fill(color).frame(width: 10, height: 10)
            VStack(alignment: .leading, spacing: 2) {
                Text(carrier).font(.system(size: 11, weight: .semibold, design: .rounded)).foregroundColor(.white.opacity(0.7))
                Text("\(String(format: "%.1f", value))%").font(.system(size: 14, weight: .bold, design: .rounded)).foregroundColor(.white)
            }
        }
    }
}

struct KPIBadge: View {
    let value: Double
    let theme: ThemeManager
    var body: some View {
        Text("\(String(format: "%.1f", value))%")
            .font(.system(size: 12, weight: .bold, design: .rounded))
            .foregroundColor(value >= 85.0 ? theme.current.kpiGood : (value >= 80.0 ? theme.current.kpiMedium : theme.current.kpiBad))
    }
}

// ==========================================
// 6. VISTA: FOLLOW UP (FollowUpView)
// ==========================================

class iOSFollowUpViewModel: ObservableObject {
    let viewModel: FollowUpViewModel
    @Published var selectedCarrier: String = "Y4"
    @Published var delays: [DelayCategory] = []
    private var carrierCollector: FlowCollector<NSString>?
    private var delaysCollector: FlowCollector<NSArray>?

    init(storage: AviationStorage) {
        let vm = FollowUpViewModel(storage: storage)
        self.viewModel = vm
        self.selectedCarrier = vm.selectedCarrier.value as! String
        if let initialDelays = vm.delays.value as? [DelayCategory] {
            self.delays = initialDelays
        }
        
        self.carrierCollector = FlowCollector<NSString> { [weak self] newCarrier in
            DispatchQueue.main.async { self?.selectedCarrier = newCarrier as String }
        }
        vm.selectedCarrier.collect(collector: self.carrierCollector!) { _ in }
        
        self.delaysCollector = FlowCollector<NSArray> { [weak self] newList in
            if let list = newList as? [DelayCategory] {
                DispatchQueue.main.async { self?.delays = list }
            }
        }
        vm.delays.collect(collector: self.delaysCollector!) { _ in }
    }
    
    func selectCarrier(code: String) { viewModel.selectCarrier(code: code) }
}

struct DelayChartData: Identifiable {
    let id: String
    let code: String
    let label: String
    let flightCount: Double
    let percentage: Double
}

struct FollowUpView: View {
    @EnvironmentObject var themeManager: ThemeManager
    @StateObject private var followUpVM: iOSFollowUpViewModel
    @State private var hoveredBar: String? = nil
    
    init(storage: AviationStorage) {
        _followUpVM = StateObject(wrappedValue: iOSFollowUpViewModel(storage: storage))
    }

    private var chartData: [DelayChartData] {
        followUpVM.delays.map { delay in
            DelayChartData(
                id: delay.code,
                code: delay.code,
                label: delay.label,
                flightCount: Double(delay.flightCount),
                percentage: delay.percentage
            )
        }
    }

    private func barGradient(for delayCode: String) -> LinearGradient {
        let isHovered = hoveredBar == delayCode
        let accent = themeManager.current.accent
        return LinearGradient(
            colors: [accent.opacity(isHovered ? 1.0 : 0.8), accent.opacity(isHovered ? 0.4 : 0.2)],
            startPoint: .top,
            endPoint: .bottom
        )
    }

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 25) {
                HStack {
                    VStack(alignment: .leading, spacing: 5) {
                        Text("Follow Up de Demoras")
                            .font(.system(size: 26, weight: .bold, design: .rounded))
                            .foregroundColor(themeManager.current.textPrimary)
                        Text("Análisis por Categoría de Retraso")
                            .font(.system(size: 14, weight: .medium, design: .rounded))
                            .foregroundColor(themeManager.current.textSecondary)
                    }
                    Spacer()
                }
                .padding(.horizontal, 20)
                .padding(.top, 20)
                
                HStack(spacing: 0) {
                    CarrierTabButton(code: "Y4", label: "Y4", selected: followUpVM.selectedCarrier, theme: themeManager) { followUpVM.selectCarrier(code: "Y4"); hoveredBar = nil }
                    CarrierTabButton(code: "Q6", label: "Q6", selected: followUpVM.selectedCarrier, theme: themeManager) { followUpVM.selectCarrier(code: "Q6"); hoveredBar = nil }
                    CarrierTabButton(code: "N3", label: "N3", selected: followUpVM.selectedCarrier, theme: themeManager) { followUpVM.selectCarrier(code: "N3"); hoveredBar = nil }
                }
                .padding(6)
                .background(Color.white.opacity(0.05))
                .cornerRadius(16)
                .overlay(RoundedRectangle(cornerRadius: 16).stroke(themeManager.current.border, lineWidth: 1))
                .padding(.horizontal, 20)
                
                VStack(alignment: .leading, spacing: 15) {
                    Text("Distribución de Vuelos Demorados").font(.system(size: 16, weight: .bold, design: .rounded)).foregroundColor(themeManager.current.textPrimary)
                    
                    Chart(chartData) { delay in
                        BarMark(x: .value("Categoría", delay.code), y: .value("Vuelos", delay.flightCount))
                            .foregroundStyle(barGradient(for: delay.code))
                            .cornerRadius(8)
                            .annotation(position: .top) {
                                Text("\(Int(delay.flightCount))").font(.system(size: 11, weight: .bold, design: .rounded)).foregroundColor(.white)
                            }
                    }
                    .chartYAxis {
                        AxisMarks(position: .leading) { _ in
                            AxisGridLine(stroke: StrokeStyle(lineWidth: 1, dash: [4, 4])).foregroundStyle(themeManager.current.border)
                            AxisValueLabel().foregroundStyle(themeManager.current.textSecondary)
                        }
                    }
                    .chartXAxis {
                        AxisMarks { _ in AxisValueLabel().foregroundStyle(themeManager.current.textSecondary) }
                    }
                    .frame(height: 220)
                    .chartOverlay { proxy in
                        GeometryReader { geo in
                            Rectangle()
                                .fill(.clear)
                                .contentShape(Rectangle())
                                .gesture(
                                    DragGesture(minimumDistance: 0)
                                        .onChanged { value in
                                            let location = value.location
                                            if let category: String = proxy.value(atX: location.x) {
                                                if followUpVM.delays.contains(where: { $0.code == category }) {
                                                    hoveredBar = category
                                                }
                                            }
                                        }
                                )
                        }
                    }
                    
                    if let categoryCode = hoveredBar, let selectedDelay = followUpVM.delays.first(where: { $0.code == categoryCode }) {
                        HStack(spacing: 12) {
                            Circle().fill(themeManager.current.accent).frame(width: 8, height: 8)
                            VStack(alignment: .leading, spacing: 3) {
                                Text(selectedDelay.label).font(.system(size: 12, weight: .bold, design: .rounded)).foregroundColor(.white)
                                Text("\(selectedDelay.flightCount) vuelos retrasados (\(String(format: "%.1f", selectedDelay.percentage))%)").font(.system(size: 11, weight: .semibold, design: .rounded)).foregroundColor(themeManager.current.textSecondary)
                            }
                            Spacer()
                            Button(action: { hoveredBar = nil }) {
                                Image(systemName: "xmark.circle.fill").foregroundColor(themeManager.current.textSecondary)
                            }
                        }
                        .padding().glassCard(cornerRadius: 14, borderColor: themeManager.current.accent)
                    } else {
                        Text("Toca cualquier barra del gráfico para ver detalles").font(.system(size: 11, weight: .regular, design: .rounded)).foregroundColor(themeManager.current.textSecondary).frame(maxWidth: .infinity, alignment: .center)
                    }
                }
                .padding(20).glassCard(borderColor: themeManager.current.border).padding(.horizontal, 20)
                
                VStack(alignment: .leading, spacing: 15) {
                    Text("Detalle de Afectaciones").font(.system(size: 16, weight: .bold, design: .rounded)).foregroundColor(themeManager.current.textPrimary)
                    VStack(spacing: 12) {
                        ForEach(followUpVM.delays, id: \.code) { delay in
                            HStack(spacing: 12) {
                                Text(delay.code).font(.system(size: 11, weight: .bold, design: .rounded)).foregroundColor(.white).frame(width: 45, height: 26).background(themeManager.current.accent.opacity(0.2)).cornerRadius(8).overlay(RoundedRectangle(cornerRadius: 8).stroke(themeManager.current.accent.opacity(0.3), lineWidth: 1))
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(delay.label).font(.system(size: 13, weight: .bold, design: .rounded)).foregroundColor(.white)
                                    Text("\(String(format: "%.1f", delay.percentage))% del total").font(.system(size: 11, weight: .medium, design: .rounded)).foregroundColor(themeManager.current.textSecondary)
                                }
                                Spacer()
                                Text("\(delay.flightCount)").font(.system(size: 16, weight: .bold, design: .rounded)).foregroundColor(.white).frame(width: 40, alignment: .trailing)
                            }
                            if delay.code != followUpVM.delays.last?.code { Divider().background(themeManager.current.border) }
                        }
                    }
                }
                .padding(20).glassCard(borderColor: themeManager.current.border).padding(.horizontal, 20).padding(.bottom, 30)
            }
        }
        .background(OrganicBackground(accentColor: themeManager.current.accent, isDarkMode: themeManager.current.theme != .light))
    }
}

// ==========================================
// 7. VISTA: GANTT INTERACTIVO (GanttView)
// ==========================================

class iOSGanttViewModel: ObservableObject {
    let viewModel: GanttViewModel
    @Published var flights: [Flight] = []
    private var flightsCollector: FlowCollector<NSArray>?

    init(storage: AviationStorage) {
        let vm = GanttViewModel(storage: storage)
        self.viewModel = vm
        if let initialFlights = vm.flights.value as? [Flight] { self.flights = initialFlights }
        self.flightsCollector = FlowCollector<NSArray> { [weak self] newList in
            if let list = newList as? [Flight] { DispatchQueue.main.async { self?.flights = list } }
        }
        vm.flights.collect(collector: self.flightsCollector!) { _ in }
    }
}

struct GanttView: View {
    @EnvironmentObject var themeManager: ThemeManager
    @StateObject private var ganttVM: iOSGanttViewModel
    @State private var searchText = ""
    @State private var selectedFlight: Flight? = nil
    @State private var filterStatus: String = "ALL"
    
    private let hours = ["12:00", "14:00", "16:00", "18:00", "20:00", "22:00"]
    
    init(storage: AviationStorage) {
        _ganttVM = StateObject(wrappedValue: iOSGanttViewModel(storage: storage))
    }

    var body: some View {
        VStack(spacing: 20) {
            VStack(spacing: 12) {
                HStack {
                    Image(systemName: "magnifyingglass").foregroundColor(themeManager.current.textSecondary)
                    TextField("", text: $searchText).foregroundColor(.white).placeholder(when: searchText.isEmpty) {
                        Text("Buscar por vuelo, origen o matrícula...").foregroundColor(.white.opacity(0.3))
                    }
                }
                .padding().background(Color.white.opacity(0.05)).cornerRadius(14).overlay(RoundedRectangle(cornerRadius: 14).stroke(themeManager.current.border, lineWidth: 1))
                
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        StatusFilterButton(label: "Todos", value: "ALL", active: filterStatus) { filterStatus = "ALL" }
                        StatusFilterButton(label: "En Aire", value: "ON_AIR", active: filterStatus) { filterStatus = "ON_AIR" }
                        StatusFilterButton(label: "Demorados", value: "DELAYED", active: filterStatus) { filterStatus = "DELAYED" }
                        StatusFilterButton(label: "Arribados", value: "ARRIVED", active: filterStatus) { filterStatus = "ARRIVED" }
                    }
                }
            }
            .padding(.horizontal, 20).padding(.top, 20)
            
            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: 20) {
                    HStack(spacing: 0) {
                        Text("Matrícula").font(.system(size: 11, weight: .bold, design: .rounded)).foregroundColor(themeManager.current.textSecondary).frame(width: 80, alignment: .leading)
                        HStack(spacing: 0) {
                            ForEach(hours, id: \.self) { hour in
                                Text(hour).font(.system(size: 11, weight: .semibold, design: .rounded)).foregroundColor(themeManager.current.textSecondary).frame(maxWidth: .infinity)
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                    
                    let groupedFlights = Dictionary(grouping: filteredFlights, by: { $0.registration })
                    ForEach(groupedFlights.keys.sorted(), id: \.self) { reg in
                        let regFlights = groupedFlights[reg] ?? []
                        HStack(spacing: 0) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(reg).font(.system(size: 13, weight: .bold, design: .rounded)).foregroundColor(.white)
                                Text("A320neo").font(.system(size: 9, weight: .semibold, design: .rounded)).foregroundColor(themeManager.current.textSecondary)
                            }
                            .frame(width: 80, alignment: .leading)
                            
                            ZStack(alignment: .leading) {
                                HStack(spacing: 0) {
                                    ForEach(0..<hours.count, id: \.self) { _ in
                                        Divider().background(themeManager.current.border.opacity(0.3)).frame(maxWidth: .infinity)
                                    }
                                }
                                ForEach(regFlights, id: \.id) { flight in
                                    FlightCapsule(flight: flight, theme: themeManager) { selectedFlight = flight }
                                        .offset(x: getTimelineOffset(for: flight))
                                }
                            }
                            .frame(height: 50)
                        }
                        .padding(.vertical, 8).padding(.horizontal, 15).glassCard(borderColor: themeManager.current.border).padding(.horizontal, 15)
                    }
                }
                .padding(.bottom, 30)
            }
        }
        .background(OrganicBackground(accentColor: themeManager.current.accent, isDarkMode: themeManager.current.theme != .light))
        .sheet(item: $selectedFlight) { flight in FlightDetailView(flight: flight, theme: themeManager) }
    }
    
    private var filteredFlights: [Flight] {
        ganttVM.flights.filter { flight in
            let matchesSearch = searchText.isEmpty || flight.flightNumber.localizedCaseInsensitiveContains(searchText) || flight.registration.localizedCaseInsensitiveContains(searchText)
            let matchesStatus = filterStatus == "ALL" || flight.status == filterStatus
            return matchesSearch && matchesStatus
        }
    }
    
    private func getTimelineOffset(for flight: Flight) -> CGFloat {
        let hour = Int(flight.departureTime.split(separator: ":").first ?? "12") ?? 12
        let minute = Double(flight.departureTime.split(separator: ":").last ?? "00") ?? 0.0
        let totalMinutes = Double((hour - 12) * 60) + minute
        let scale = 250.0 / 720.0
        return CGFloat(totalMinutes * scale)
    }
}

struct FlightCapsule: View {
    let flight: Flight
    let theme: ThemeManager
    let action: () -> Void
    var body: some View {
        Button(action: action) {
            HStack(spacing: 4) {
                Text(flight.flightNumber).font(.system(size: 9, weight: .bold, design: .rounded)).foregroundColor(.white)
                Image(systemName: "chevron.right.circle.fill").font(.system(size: 10)).foregroundColor(.white.opacity(0.8))
                Text(flight.destination).font(.system(size: 9, weight: .heavy, design: .rounded)).foregroundColor(.white)
            }
            .padding(.horizontal, 8).padding(.vertical, 6)
            .background(Capsule().fill(statusColor.opacity(0.2)).background(Capsule().fill(.ultraThinMaterial)))
            .overlay(Capsule().stroke(statusColor.opacity(0.4), lineWidth: 1))
        }
    }
    private var statusColor: Color {
        switch flight.status {
        case "ON_AIR": return theme.current.accent
        case "DELAYED": return theme.current.kpiBad
        case "ARRIVED": return theme.current.kpiGood
        default: return Color.gray
        }
    }
}

// ==========================================
// 8. VISTA: CENTRO OPERATIVO CCO (CCOView)
// ==========================================

class iOSCCOViewModel: ObservableObject {
    let viewModel: CCOViewModel
    @Published var stationKPIs: [StationKPI] = []
    private var stationKPIsCollector: FlowCollector<NSArray>?

    init(storage: AviationStorage) {
        let vm = CCOViewModel(storage: storage)
        self.viewModel = vm
        if let initialKpis = vm.stationKPIs.value as? [StationKPI] { self.stationKPIs = initialKpis }
        
        self.stationKPIsCollector = FlowCollector<NSArray> { [weak self] newList in
            if let list = newList as? [StationKPI] { DispatchQueue.main.async { self?.stationKPIs = list } }
        }
        vm.stationKPIs.collect(collector: self.stationKPIsCollector!) { _ in }
    }
}

struct CCOView: View {
    @EnvironmentObject var themeManager: ThemeManager
    @StateObject private var ccoVM: iOSCCOViewModel
    @State private var zoomScale: CGFloat = 1.0
    @State private var selectedStation: StationKPI? = nil
    
    private let stationCoordinates: [String: CGPoint] = [
        "MEX": CGPoint(x: 180, y: 220),
        "CUN": CGPoint(x: 290, y: 150),
        "GDL": CGPoint(x: 90, y: 190),
        "TIJ": CGPoint(x: 60, y: 80),
        "MTY": CGPoint(x: 190, y: 100)
    ]
    
    init(storage: AviationStorage) {
        _ccoVM = StateObject(wrappedValue: iOSCCOViewModel(storage: storage))
    }

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                VStack(alignment: .leading, spacing: 5) {
                    Text("Centro de Control CCO").font(.system(size: 26, weight: .bold, design: .rounded)).foregroundColor(themeManager.current.textPrimary)
                    Text("Salud Operativa por Estación").font(.system(size: 14, weight: .medium, design: .rounded)).foregroundColor(themeManager.current.textSecondary)
                }
                Spacer()
            }
            .padding(.horizontal, 20).padding(.top, 20)
            
            ZStack(alignment: .bottom) {
                GeometryReader { geometry in
                    ZStack {
                        GridBackgroundPattern(theme: themeManager).opacity(0.15)
                        ForEach(ccoVM.stationKPIs, id: \.station) { kpi in
                            let coordinate = stationCoordinates[kpi.station] ?? CGPoint(x: 150, y: 150)
                            let bubbleSize = getBubbleSize(volume: Int(kpi.flightVolume))
                            
                            StationBubbleView(kpi: kpi, size: bubbleSize, theme: themeManager, isSelected: selectedStation?.station == kpi.station) {
                                withAnimation(.spring(response: 0.4, dampingFraction: 0.7)) { selectedStation = kpi }
                            }
                            .position(coordinate).scaleEffect(zoomScale)
                        }
                    }
                    .frame(width: geometry.size.width, height: geometry.size.height)
                    .gesture(MagnificationGesture().onChanged { value in zoomScale = max(0.8, min(value, 2.0)) })
                }
                .frame(maxHeight: .infinity).clipped()
                
                if let station = selectedStation {
                    VStack(spacing: 15) {
                        HStack {
                            Text("Estación \(station.station)").font(.system(size: 18, weight: .bold, design: .rounded)).foregroundColor(.white)
                            Spacer()
                            Button(action: { withAnimation(.easeOut(duration: 0.2)) { selectedStation = nil } }) {
                                Image(systemName: "xmark.circle.fill").foregroundColor(themeManager.current.textSecondary).font(.system(size: 18))
                            }
                        }
                        
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Volumen Diario").font(.caption).foregroundColor(themeManager.current.textSecondary)
                                Text("\(station.flightVolume) Vuelos").font(.system(size: 15, weight: .bold, design: .rounded)).foregroundColor(.white)
                            }
                            Spacer()
                            Text(station.statusColor).font(.system(size: 10, weight: .bold, design: .rounded)).foregroundColor(.white).padding(.horizontal, 8).padding(.vertical, 4).background(statusColor(color: station.statusColor).opacity(0.3)).cornerRadius(6)
                        }
                        
                        Divider().background(themeManager.current.border)
                        VStack(spacing: 10) {
                            MiniProgressMetric(label: "OTP + 15", value: station.otp15, color: themeManager.current.accent)
                            MiniProgressMetric(label: "BTP + 0", value: station.btp0, color: .blue)
                            MiniProgressMetric(label: "GTP + 5", value: station.gtp5, color: .purple)
                        }
                    }
                    .padding(20).glassCard(borderColor: statusColor(color: station.statusColor)).padding(.horizontal, 20).padding(.bottom, 25).transition(.move(edge: .bottom).combined(with: .opacity))
                } else {
                    Text("Toca una burbuja de estación para ver el desglose operativo").font(.system(size: 11, weight: .semibold, design: .rounded)).foregroundColor(themeManager.current.textSecondary).padding(.bottom, 25)
                }
            }
        }
        .background(OrganicBackground(accentColor: themeManager.current.accent, isDarkMode: themeManager.current.theme != .light))
    }
    
    private func getBubbleSize(volume: Int) -> CGFloat {
        let minSize: CGFloat = 65
        let maxSize: CGFloat = 110
        let scale = CGFloat(volume - 50) / 80.0
        return minSize + (scale * (maxSize - minSize))
    }
    
    private func statusColor(color: String) -> Color {
        switch color {
        case "GOOD": return themeManager.current.kpiGood
        case "MEDIUM": return themeManager.current.kpiMedium
        default: return themeManager.current.kpiBad
        }
    }
}

struct StationBubbleView: View {
    let kpi: StationKPI
    let size: CGFloat
    let theme: ThemeManager
    let isSelected: Bool
    let action: () -> Void
    @State private var floatingOffset = CGFloat.zero

    var body: some View {
        Button(action: action) {
            ZStack {
                Circle().fill(statusColor.opacity(isSelected ? 0.35 : 0.15)).frame(width: size + 15, height: size + 15)
                Circle()
                    .fill(LinearGradient(colors: [.white.opacity(0.12), .white.opacity(0.02)], startPoint: .topLeading, endPoint: .bottomTrailing))
                    .frame(width: size, height: size)
                    .overlay(Circle().stroke(isSelected ? theme.current.accent : statusColor.opacity(0.4), lineWidth: isSelected ? 2.5 : 1))
                
                VStack(spacing: 2) {
                    Text(kpi.station).font(.system(size: 16, weight: .black, design: .rounded)).foregroundColor(.white)
                    Text("\(String(format: "%.0f", kpi.otp15))%").font(.system(size: 12, weight: .bold, design: .rounded)).foregroundColor(statusColor)
                }
            }
        }
        .offset(y: floatingOffset)
        .onAppear {
            withAnimation(.easeInOut(duration: 4.0).repeatForever(autoreverses: true)) { floatingOffset = 6 }
        }
    }
    private var statusColor: Color {
        switch kpi.statusColor {
        case "GOOD": return theme.current.kpiGood
        case "MEDIUM": return theme.current.kpiMedium
        default: return theme.current.kpiBad
        }
    }
}

// ==========================================
// 9. VISTA: CONFIGURACIÓN Y TEMAS (SettingsView)
// ==========================================

struct SettingsView: View {
    @EnvironmentObject var themeManager: ThemeManager
    let storage: AviationStorage
    let onSignOut: () -> Void
    
    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 25) {
                HStack {
                    VStack(alignment: .leading, spacing: 5) {
                        Text("Configuración").font(.system(size: 26, weight: .bold, design: .rounded)).foregroundColor(themeManager.current.textPrimary)
                        Text("Personalización de la Experiencia").font(.system(size: 14, weight: .medium, design: .rounded)).foregroundColor(themeManager.current.textSecondary)
                    }
                    Spacer()
                }
                .padding(.horizontal, 20).padding(.top, 20)
                
                VStack(spacing: 15) {
                    HStack(spacing: 15) {
                        Image(systemName: "person.crop.circle.fill.badge.checkmark").resizable().frame(width: 50, height: 45).foregroundColor(themeManager.current.accent)
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Julian Soto").font(.system(size: 18, weight: .bold, design: .rounded)).foregroundColor(.white)
                            Text("Administrador de Operaciones").font(.system(size: 12, weight: .semibold, design: .rounded)).foregroundColor(themeManager.current.textSecondary)
                        }
                        Spacer()
                    }
                }
                .padding(20).glassCard(borderColor: themeManager.current.border).padding(.horizontal, 20)
                
                VStack(alignment: .leading, spacing: 15) {
                    Text("Seleccionar Tema Visual").font(.system(size: 16, weight: .bold, design: .rounded)).foregroundColor(themeManager.current.textPrimary)
                    VStack(spacing: 12) {
                        ThemeOptionRow(theme: .liquidGlass, name: "Liquid Glass 💎", description: "Vidrio templado inmersivo y profundidad 3D", isActive: themeManager.activeTheme == .liquidGlass, themeManager: themeManager)
                        Divider().background(themeManager.current.border)
                        ThemeOptionRow(theme: .volarisPremium, name: "Volaris Premium 🪻", description: "Color predominante rosa/morado de marca", isActive: themeManager.activeTheme == .volarisPremium, themeManager: themeManager)
                        Divider().background(themeManager.current.border)
                        ThemeOptionRow(theme: .light, name: "Modo Claro Minimalista ☀️", description: "Superficies limpias e interfaces claras", isActive: themeManager.activeTheme == .light, themeManager: themeManager)
                        Divider().background(themeManager.current.border)
                        ThemeOptionRow(theme: .dark, name: "Modo Oscuro Obsidian 🌙", description: "Negros puros optimizados para pantallas OLED", isActive: themeManager.activeTheme == .dark, themeManager: themeManager)
                    }
                }
                .padding(20).glassCard(borderColor: themeManager.current.border).padding(.horizontal, 20)
                
                Button(action: onSignOut) {
                    HStack(spacing: 10) {
                        Image(systemName: "power")
                        Text("Cerrar Sesión").font(.system(size: 15, weight: .bold, design: .rounded))
                    }
                    .foregroundColor(.white).frame(maxWidth: .infinity).padding().background(Color.red.opacity(0.2)).background(.ultraThinMaterial).cornerRadius(14).overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.red.opacity(0.3), lineWidth: 1))
                }
                .padding(.horizontal, 20).padding(.top, 10).padding(.bottom, 30)
            }
        }
        .background(OrganicBackground(accentColor: themeManager.current.accent, isDarkMode: themeManager.current.theme != .light))
    }
}

// ==========================================
// 10. VISTA: TAB NAVIGATOR (MainView)
// ==========================================

struct MainView: View {
    @EnvironmentObject var themeManager: ThemeManager
    let storage: AviationStorage
    let onSignOut: () -> Void
    @State private var selectedTab = 0
    
    var body: some View {
        ZStack(alignment: .bottom) {
            Group {
                switch selectedTab {
                case 0: DashboardView(storage: storage)
                case 1: FollowUpView(storage: storage)
                case 2: GanttView(storage: storage)
                case 3: CCOView(storage: storage)
                case 4: SettingsView(storage: storage, onSignOut: onSignOut)
                default: DashboardView(storage: storage)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(.bottom, 80)
            
            HStack(spacing: 0) {
                TabBarItem(index: 0, icon: "chart.pie.fill", label: "KPIs", activeTab: $selectedTab, theme: themeManager)
                TabBarItem(index: 1, icon: "hourglass.badge.plus", label: "Follow Up", activeTab: $selectedTab, theme: themeManager)
                TabBarItem(index: 2, icon: "calendar.day.timeline.left", label: "Gantt", activeTab: $selectedTab, theme: themeManager)
                TabBarItem(index: 3, icon: "network", label: "CCO", activeTab: $selectedTab, theme: themeManager)
                TabBarItem(index: 4, icon: "gearshape.fill", label: "Ajustes", activeTab: $selectedTab, theme: themeManager)
            }
            .padding(.horizontal, 10).padding(.vertical, 8).glassCard(cornerRadius: 28, borderColor: themeManager.current.border.opacity(0.4)).padding(.horizontal, 20).padding(.bottom, 15)
        }
        .ignoresSafeArea(.keyboard, edges: .bottom)
    }
}

struct TabBarItem: View {
    let index: Int
    let icon: String
    let label: String
    @Binding var activeTab: Int
    let theme: ThemeManager
    
    var body: some View {
        Button(action: {
            withAnimation(.spring(response: 0.35, dampingFraction: 0.75)) { activeTab = index }
        }) {
            VStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.system(size: activeTab == index ? 20 : 18, weight: activeTab == index ? .bold : .regular))
                    .foregroundColor(activeTab == index ? .white : theme.current.textSecondary)
                Text(label).font(.system(size: 9, weight: activeTab == index ? .bold : .medium, design: .rounded)).foregroundColor(activeTab == index ? .white : theme.current.textSecondary)
            }
            .frame(maxWidth: .infinity).padding(.vertical, 6)
            .background(RoundedRectangle(cornerRadius: 18).fill(activeTab == index ? theme.current.accent.opacity(0.15) : .clear).frame(width: 55, height: 48))
        }
    }
}

// ==========================================
// 10.5. STRUCTS COMPLEMENTARIOS Y EXTENSIONES
// ==========================================

extension Flight: Identifiable {}

struct CarrierTabButton: View {
    let code: String
    let label: String
    let selected: String
    let theme: ThemeManager
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: 13, weight: .bold, design: .rounded))
                .foregroundColor(selected == code ? .white : theme.current.textSecondary)
                .padding(.vertical, 8)
                .frame(maxWidth: .infinity)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(selected == code ? theme.current.accent.opacity(0.2) : .clear)
                )
                .animation(.spring(response: 0.3, dampingFraction: 0.7), value: selected)
        }
    }
}

struct StatusFilterButton: View {
    let label: String
    let value: String
    let active: String
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: 12, weight: .bold, design: .rounded))
                .foregroundColor(active == value ? .white : .white.opacity(0.6))
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(
                    Capsule()
                        .fill(active == value ? Color.white.opacity(0.15) : Color.white.opacity(0.03))
                )
                .overlay(
                    Capsule()
                        .stroke(active == value ? Color.white.opacity(0.2) : Color.white.opacity(0.05), lineWidth: 1)
                )
        }
    }
}

struct CrewItemRow: View {
    let role: String
    let name: String
    let icon: String
    let theme: ThemeManager
    
    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(theme.current.accent.opacity(0.15))
                    .frame(width: 38, height: 38)
                Image(systemName: icon)
                    .foregroundColor(theme.current.accent)
                    .font(.system(size: 16))
            }
            VStack(alignment: .leading, spacing: 2) {
                Text(role)
                    .font(.system(size: 11, weight: .bold, design: .rounded))
                    .foregroundColor(theme.current.textSecondary)
                Text(name)
                    .font(.system(size: 14, weight: .semibold, design: .rounded))
                    .foregroundColor(.white)
            }
            Spacer()
        }
        .padding(.vertical, 4)
    }
}

struct FlightDetailView: View {
    let flight: Flight
    let theme: ThemeManager
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        ZStack {
            OrganicBackground(accentColor: theme.current.accent, isDarkMode: theme.activeTheme != .light)
                .ignoresSafeArea()
            
            VStack(spacing: 20) {
                // Header
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Detalle de Vuelo")
                            .font(.system(size: 24, weight: .bold, design: .rounded))
                            .foregroundColor(.white)
                        Text(flight.flightNumber)
                            .font(.system(size: 16, weight: .medium, design: .rounded))
                            .foregroundColor(theme.current.accent)
                    }
                    Spacer()
                    Button(action: { presentationMode.wrappedValue.dismiss() }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(theme.current.textSecondary)
                            .font(.system(size: 24))
                    }
                }
                .padding(.horizontal, 24)
                .padding(.top, 24)
                
                ScrollView(showsIndicators: false) {
                    VStack(spacing: 20) {
                        // Route Info Card
                        VStack(spacing: 15) {
                            HStack {
                                VStack(alignment: .leading) {
                                    Text("Origen")
                                        .font(.system(size: 11, weight: .bold, design: .rounded))
                                        .foregroundColor(theme.current.textSecondary)
                                    Text(flight.origin)
                                        .font(.system(size: 28, weight: .black, design: .rounded))
                                        .foregroundColor(.white)
                                }
                                Spacer()
                                VStack {
                                    Image(systemName: "airplane")
                                        .resizable()
                                        .aspectRatio(contentMode: .fit)
                                        .frame(width: 24, height: 24)
                                        .foregroundColor(theme.current.accent)
                                        .rotationEffect(.degrees(90))
                                    Text(flight.status)
                                        .font(.system(size: 10, weight: .bold, design: .rounded))
                                        .foregroundColor(statusColor)
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 4)
                                        .background(statusColor.opacity(0.15))
                                        .cornerRadius(6)
                                }
                                Spacer()
                                VStack(alignment: .trailing) {
                                    Text("Destino")
                                        .font(.system(size: 11, weight: .bold, design: .rounded))
                                        .foregroundColor(theme.current.textSecondary)
                                    Text(flight.destination)
                                        .font(.system(size: 28, weight: .black, design: .rounded))
                                        .foregroundColor(.white)
                                }
                            }
                            
                            Divider().background(theme.current.border)
                            
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text("Salida Programada")
                                        .font(.system(size: 11, weight: .semibold, design: .rounded))
                                        .foregroundColor(theme.current.textSecondary)
                                    Text(flight.departureTime)
                                        .font(.system(size: 15, weight: .bold, design: .rounded))
                                        .foregroundColor(.white)
                                }
                                Spacer()
                                VStack(alignment: .trailing, spacing: 4) {
                                    Text("Llegada Programada")
                                        .font(.system(size: 11, weight: .semibold, design: .rounded))
                                        .foregroundColor(theme.current.textSecondary)
                                    Text(flight.arrivalTime)
                                        .font(.system(size: 15, weight: .bold, design: .rounded))
                                        .foregroundColor(.white)
                                }
                            }
                        }
                        .padding(20)
                        .glassCard(borderColor: theme.current.border)
                        
                        // Delay and Aircraft details
                        VStack(spacing: 15) {
                            HStack {
                                Image(systemName: "clock.badge.exclamationmark.fill")
                                    .foregroundColor(flight.delayMinutes > 0 ? theme.current.kpiBad : theme.current.kpiGood)
                                Text("Demora:")
                                    .font(.system(size: 14, weight: .bold, design: .rounded))
                                    .foregroundColor(.white)
                                Spacer()
                                Text(flight.delayMinutes > 0 ? "\(flight.delayMinutes) mins" : "Sin Demora")
                                    .font(.system(size: 14, weight: .bold, design: .rounded))
                                    .foregroundColor(flight.delayMinutes > 0 ? theme.current.kpiBad : theme.current.kpiGood)
                            }
                            
                            Divider().background(theme.current.border)
                            
                            HStack {
                                Image(systemName: "airplane")
                                    .foregroundColor(theme.current.accent)
                                Text("Matrícula:")
                                    .font(.system(size: 14, weight: .bold, design: .rounded))
                                    .foregroundColor(.white)
                                Spacer()
                                Text(flight.registration)
                                    .font(.system(size: 14, weight: .bold, design: .rounded))
                                    .foregroundColor(.white)
                            }
                        }
                        .padding(20)
                        .glassCard(borderColor: theme.current.border)
                        
                        // Crew members Card
                        VStack(alignment: .leading, spacing: 15) {
                            Text("Tripulación Asignada")
                                .font(.system(size: 15, weight: .bold, design: .rounded))
                                .foregroundColor(.white)
                            
                            VStack(spacing: 10) {
                                CrewItemRow(role: "Capitán / Piloto", name: flight.crewPilot, icon: "person.badge.shield.checkmark.fill", theme: theme)
                                Divider().background(theme.current.border)
                                CrewItemRow(role: "Primer Oficial / Copiloto", name: flight.crewCopilot, icon: "person.fill", theme: theme)
                                Divider().background(theme.current.border)
                                CrewItemRow(role: "Sobrecargo Mayor / Jefe de Cabina", name: flight.crewChief, icon: "person.2.fill", theme: theme)
                            }
                        }
                        .padding(20)
                        .glassCard(borderColor: theme.current.border)
                    }
                    .padding(.horizontal, 24)
                }
            }
        }
    }
    
    private var statusColor: Color {
        switch flight.status {
        case "ON_AIR": return theme.current.accent
        case "DELAYED": return theme.current.kpiBad
        case "ARRIVED": return theme.current.kpiGood
        default: return Color.gray
        }
    }
}

struct GridBackgroundPattern: View {
    let theme: ThemeManager
    
    var body: some View {
        GeometryReader { geometry in
            Path { path in
                let step: CGFloat = 30
                for x in stride(from: 0, to: geometry.size.width, by: step) {
                    path.move(to: CGPoint(x: x, y: 0))
                    path.addLine(to: CGPoint(x: x, y: geometry.size.height))
                }
                for y in stride(from: 0, to: geometry.size.height, by: step) {
                    path.move(to: CGPoint(x: 0, y: y))
                    path.addLine(to: CGPoint(x: geometry.size.width, y: y))
                }
            }
            .stroke(theme.current.border.opacity(0.5), lineWidth: 0.5)
        }
    }
}

struct MiniProgressMetric: View {
    let label: String
    let value: Double
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(label)
                    .font(.system(size: 12, weight: .bold, design: .rounded))
                    .foregroundColor(.white)
                Spacer()
                Text("\(String(format: "%.1f", value))%")
                    .font(.system(size: 12, weight: .bold, design: .rounded))
                    .foregroundColor(color)
            }
            GeometryReader { geometry in
                ZStack(alignment: .leading) {
                    Capsule()
                        .fill(Color.white.opacity(0.05))
                        .frame(height: 6)
                    Capsule()
                        .fill(color)
                        .frame(width: CGFloat(value / 100.0) * geometry.size.width, height: 6)
                }
            }
            .frame(height: 6)
        }
    }
}

struct ThemeOptionRow: View {
    let theme: AppTheme
    let name: String
    let description: String
    let isActive: Bool
    let themeManager: ThemeManager
    
    var body: some View {
        Button(action: {
            withAnimation(.spring(response: 0.35, dampingFraction: 0.75)) {
                themeManager.selectTheme(theme)
            }
        }) {
            HStack(spacing: 15) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(name)
                        .font(.system(size: 15, weight: .bold, design: .rounded))
                        .foregroundColor(.white)
                    Text(description)
                        .font(.system(size: 11, weight: .medium, design: .rounded))
                        .foregroundColor(themeManager.current.textSecondary)
                        .multilineTextAlignment(.leading)
                }
                Spacer()
                if isActive {
                    Circle()
                        .fill(themeManager.current.accent)
                        .frame(width: 14, height: 14)
                        .overlay(
                            Circle()
                                .stroke(Color.white, lineWidth: 2)
                        )
                } else {
                    Circle()
                        .stroke(themeManager.current.border, lineWidth: 2)
                        .frame(width: 14, height: 14)
                }
            }
            .padding(.vertical, 4)
        }
    }
}

// ==========================================
// 11. VISTA CONTENEDORA FINAL (ContentView)
// ==========================================

struct ContentView: View {
    @EnvironmentObject var themeManager: ThemeManager
    let storage: AviationStorage
    @StateObject private var loginVM: iOSLoginViewModel
    
    init(storage: AviationStorage) {
        self.storage = storage
        _loginVM = StateObject(wrappedValue: iOSLoginViewModel(storage: storage))
    }

    var body: some View {
        Group {
            if loginVM.state.isSuccess {
                MainView(storage: storage) { loginVM.signOut() }
                    .transition(.asymmetric(insertion: .move(edge: .trailing), removal: .move(edge: .leading)))
            } else {
                LoginView(storage: storage)
                    .transition(.opacity)
            }
        }
        .animation(.spring(response: 0.45, dampingFraction: 0.8), value: loginVM.state.isSuccess)
    }
}
