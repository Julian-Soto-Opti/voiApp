package com.jetbrains.kmpapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// 1. ThemeViewModel
class ThemeViewModel(private val storage: AviationStorage) : ViewModel() {
    val theme: StateFlow<AppTheme> = storage.getTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.LIQUID_GLASS)

    val colors: StateFlow<ThemeColors> = storage.getThemeColors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeColors.getColors(AppTheme.LIQUID_GLASS))

    fun selectTheme(theme: AppTheme) {
        storage.updateTheme(theme)
    }
}

// 2. LoginViewModel
data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val loggedInUser: String? = null
)

class LoginViewModel(private val storage: AviationStorage) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            storage.getLoggedInUser().collect { user ->
                _state.value = _state.value.copy(loggedInUser = user, isSuccess = user != null)
            }
        }
    }

    fun signIn(username: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val success = storage.signIn(username, password)
            if (success) {
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = "Credenciales incorrectas. Intenta con julian@orka.com / admin123"
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            storage.signOut()
            _state.value = LoginState()
        }
    }
}

// 3. FollowUpViewModel
class FollowUpViewModel(private val storage: AviationStorage) : ViewModel() {
    private val _selectedCarrier = MutableStateFlow("Y4")
    val selectedCarrier: StateFlow<String> = _selectedCarrier.asStateFlow()

    private val _delays = MutableStateFlow<List<DelayCategory>>(emptyList())
    val delays: StateFlow<List<DelayCategory>> = _delays.asStateFlow()

    init {
        // Carga inicial
        loadDelays("Y4")
    }

    fun selectCarrier(code: String) {
        _selectedCarrier.value = code
        loadDelays(code)
    }

    private fun loadDelays(carrier: String) {
        viewModelScope.launch {
            storage.getDelayCategories(carrier).collect { list ->
                _delays.value = list
            }
        }
    }
}

// 4. ExecutiveViewModel
class ExecutiveViewModel(private val storage: AviationStorage) : ViewModel() {
    val kpis: StateFlow<List<ExecutiveKPI>> = storage.getExecutiveKPIs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

// 5. CCOViewModel
class CCOViewModel(private val storage: AviationStorage) : ViewModel() {
    val stationKPIs: StateFlow<List<StationKPI>> = storage.getStationKPIs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

// 6. GanttViewModel
class GanttViewModel(private val storage: AviationStorage) : ViewModel() {
    val flights: StateFlow<List<Flight>> = storage.getFlights()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
