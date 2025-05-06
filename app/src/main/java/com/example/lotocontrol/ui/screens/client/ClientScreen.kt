package com.example.lotocontrol.ui.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lotocontrol.ui.theme.DebtColor
import com.example.lotocontrol.ui.theme.PaidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(
    clientId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToNextClient: (Long) -> Unit,
    viewModel: ClientViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(clientId) {
        viewModel.loadClient(clientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.client?.name ?: "Cliente") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            uiState.nextClientId?.let { onNavigateToNextClient(it) }
                        },
                        enabled = uiState.nextClientId != null
                    ) {
                        Icon(Icons.Default.ArrowForward, "Siguiente cliente")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current balance card
            BalanceCard(
                ticketsDelivered = uiState.client?.ticketsDelivered ?: 0,
                ticketsReturned = uiState.returnedTickets,
                ticketPrice = uiState.ticketPrice,
                previousDebt = uiState.client?.previousDebt ?: 0.0,
                amountPaid = uiState.amountPaid,
                currentBalance = uiState.currentBalance
            )

            // Returned tickets input
            OutlinedTextField(
                value = if (uiState.returnedTickets > 0) 
                    uiState.returnedTickets.toString() else "",
                onValueChange = { value ->
                    viewModel.updateReturnedTickets(value.toIntOrNull() ?: 0)
                },
                label = { Text("Décimos devueltos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Amount paid input
            OutlinedTextField(
                value = if (uiState.amountPaid > 0.0) 
                    String.format("%.2f", uiState.amountPaid) else "",
                onValueChange = { value ->
                    viewModel.updateAmountPaid(value.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Cantidad pagada (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Save button
            Button(
                onClick = { 
                    viewModel.saveChanges {
                        onNavigateToNextClient(uiState.nextClientId ?: 0)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar y continuar")
            }
        }
    }
}

@Composable
private fun BalanceCard(
    ticketsDelivered: Int,
    ticketsReturned: Int,
    ticketPrice: Double,
    previousDebt: Double,
    amountPaid: Double,
    currentBalance: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BalanceRow("Décimos entregados", ticketsDelivered.toString())
            BalanceRow("Décimos devueltos", ticketsReturned.toString())
            BalanceRow("Precio por décimo", String.format("%.2f€", ticketPrice))
            Divider()
            BalanceRow("Importe total", String.format("%.2f€", 
                (ticketsDelivered - ticketsReturned) * ticketPrice))
            if (previousDebt > 0) {
                BalanceRow("Deuda anterior", String.format("%.2f€", previousDebt))
            }
            BalanceRow("Cantidad pagada", String.format("%.2f€", amountPaid))
            Divider()
            BalanceRow(
                "Balance actual",
                String.format("%.2f€", currentBalance),
                if (currentBalance > 0) DebtColor else PaidColor
            )
        }
    }
}

@Composable
private fun BalanceRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = LocalContentColor.current
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor
        )
    }
}
