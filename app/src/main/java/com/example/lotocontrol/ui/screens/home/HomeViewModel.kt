package com.example.lotocontrol.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lotocontrol.data.ClientDao
import com.example.lotocontrol.data.LotterySettingsDao
import com.example.lotocontrol.data.models.Client
import com.example.lotocontrol.data.models.ClientWithBalance
import com.example.lotocontrol.data.models.LotterySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val clients: List<ClientWithBalance> = emptyList(),
    val drawDate: LocalDate = LocalDate.now(),
    val ticketPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val clientDao: ClientDao,
    private val settingsDao: LotterySettingsDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Combine clients and settings flows
            combine(
                clientDao.getAllClientsFlow(),
                settingsDao.getSettingsFlow()
            ) { clients, settings ->
                updateUiState(clients, settings)
            }.collect()
        }
    }

    private fun updateUiState(
        clients: List<Client>,
        settings: LotterySettings?
    ) {
        val currentSettings = settings ?: LotterySettings()
        
        val clientsWithBalance = clients.map { client ->
            val balance = client.calculateBalance(currentSettings.ticketPrice)
            ClientWithBalance(
                client = client,
                currentBalance = balance,
                hasDebt = balance > 0
            )
        }

        _uiState.update { currentState ->
            currentState.copy(
                clients = clientsWithBalance,
                drawDate = currentSettings.drawDate,
                ticketPrice = currentSettings.ticketPrice,
                isLoading = false
            )
        }
    }

    fun updateSettings(drawDate: LocalDate, ticketPrice: Double) {
        viewModelScope.launch {
            try {
                val newSettings = LotterySettings(
                    drawDate = drawDate,
                    ticketPrice = ticketPrice
                )
                settingsDao.insertSettings(newSettings)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun addClient(name: String) {
        viewModelScope.launch {
            try {
                val client = Client(name = name)
                clientDao.insertClient(client)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
