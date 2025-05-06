package com.example.lotocontrol.ui.screens.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lotocontrol.data.ClientDao
import com.example.lotocontrol.data.LotterySettingsDao
import com.example.lotocontrol.data.models.Client
import com.example.lotocontrol.data.models.LotterySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientUiState(
    val client: Client? = null,
    val ticketPrice: Double = 0.0,
    val returnedTickets: Int = 0,
    val amountPaid: Double = 0.0,
    val currentBalance: Double = 0.0,
    val nextClientId: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val clientDao: ClientDao,
    private val settingsDao: LotterySettingsDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientUiState())
    val uiState: StateFlow<ClientUiState> = _uiState.asStateFlow()

    private var settings: LotterySettings? = null
    private var allClients: List<Client> = emptyList()

    init {
        viewModelScope.launch {
            settingsDao.getSettingsFlow().collect { newSettings ->
                settings = newSettings
                updateBalance()
            }
        }

        viewModelScope.launch {
            clientDao.getAllClientsFlow().collect { clients ->
                allClients = clients
                updateNextClientId()
            }
        }
    }

    fun loadClient(clientId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val client = clientDao.getClient(clientId)
                _uiState.update { currentState ->
                    currentState.copy(
                        client = client,
                        returnedTickets = 0,
                        amountPaid = 0.0,
                        isLoading = false
                    )
                }
                updateBalance()
                updateNextClientId()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar el cliente: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateReturnedTickets(count: Int) {
        _uiState.update { 
            it.copy(returnedTickets = count.coerceAtMost(it.client?.ticketsDelivered ?: 0))
        }
        updateBalance()
    }

    fun updateAmountPaid(amount: Double) {
        _uiState.update { it.copy(amountPaid = amount) }
        updateBalance()
    }

    private fun updateBalance() {
        val currentState = _uiState.value
        val client = currentState.client ?: return
        val ticketPrice = settings?.ticketPrice ?: 0.0
        
        val totalDue = (client.ticketsDelivered - currentState.returnedTickets) * ticketPrice
        val newBalance = totalDue + client.previousDebt - currentState.amountPaid

        _uiState.update { it.copy(
            ticketPrice = ticketPrice,
            currentBalance = newBalance
        )}
    }

    private fun updateNextClientId() {
        val currentClient = _uiState.value.client ?: return
        val currentIndex = allClients.indexOfFirst { it.id == currentClient.id }
        val nextClient = allClients.getOrNull(currentIndex + 1)
        
        _uiState.update { it.copy(nextClientId = nextClient?.id) }
    }

    fun saveChanges(onComplete: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val client = currentState.client ?: return@launch
            
            try {
                val updatedClient = client.copy(
                    ticketsReturned = currentState.returnedTickets,
                    amountPaid = client.amountPaid + currentState.amountPaid,
                    previousDebt = if (currentState.currentBalance > 0) 
                        currentState.currentBalance else 0.0
                )
                
                clientDao.updateClient(updatedClient)
                onComplete()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
