package com.example.lotocontrol.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lotocontrol.data.ClientDao
import com.example.lotocontrol.data.LotterySettingsDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientBalanceInfo(
    val name: String,
    val balance: Double,
    val hasDebt: Boolean
)

data class SummaryUiState(
    val clientBalances: List<ClientBalanceInfo> = emptyList(),
    val totalPendingAmount: Double = 0.0,
    val clientsWithDebt: Int = 0,
    val clientsWithoutDebt: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val clientDao: ClientDao,
    private val settingsDao: LotterySettingsDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                clientDao.getAllClientsFlow(),
                settingsDao.getSettingsFlow()
            ) { clients, settings ->
                val ticketPrice = settings?.ticketPrice ?: 0.0
                
                val balances = clients.map { client ->
                    val balance = client.calculateBalance(ticketPrice)
                    ClientBalanceInfo(
                        name = client.name,
                        balance = balance,
                        hasDebt = balance > 0
                    )
                }.sortedByDescending { it.balance }

                val totalPending = balances.sumOf { it.balance }
                val withDebt = balances.count { it.hasDebt }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        clientBalances = balances,
                        totalPendingAmount = totalPending,
                        clientsWithDebt = withDebt,
                        clientsWithoutDebt = clients.size - withDebt,
                        isLoading = false
                    )
                }
            }.catch { e ->
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar los datos: ${e.message}",
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
