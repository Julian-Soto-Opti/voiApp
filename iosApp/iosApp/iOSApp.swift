import SwiftUI
import Shared

@main
struct iOSApp: App {
    // Singleton de almacenamiento compartido en memoria de Kotlin para el ciclo de vida de la App
    private static let sharedStorage: AviationStorage = InMemoryAviationStorage()
    
    // Administrador de Temas observable de SwiftUI
    @StateObject private var themeManager: ThemeManager

    init() {
        // Inicializar Koin DI en Kotlin
        KoinKt.doInitKoin()
        
        // Crear el ThemeManager enlazado al almacenamiento singleton
        let themeVM = ThemeViewModel(storage: iOSApp.sharedStorage)
        _themeManager = StateObject(wrappedValue: ThemeManager(viewModel: themeVM))
    }

    var body: some Scene {
        WindowGroup {
            ContentView(storage: iOSApp.sharedStorage)
                .environmentObject(themeManager)
        }
    }
}
