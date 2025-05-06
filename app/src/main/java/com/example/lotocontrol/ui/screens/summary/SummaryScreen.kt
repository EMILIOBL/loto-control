package com.example.lotocontrol.ui.screens.summary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lotocontrol.ui.theme.DebtColor
import com.example.lotocontrol.ui.theme.PaidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: SummaryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen de Saldos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total pendiente de cobro",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = String.format("%.2f€", uiState.totalPendingAmount),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (uiState.totalPendingAmount > 0) DebtColor else PaidColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Clientes con deuda",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${uiState.clientsWithDebt}",
                                style = MaterialTheme.typography.titleLarge,
                                color = DebtColor
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Clientes al día",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${uiState.clientsWithoutDebt}",
                                style = MaterialTheme.typography.titleLarge,
                                color = PaidColor
                            )
                        }
                    }
                }
            }

            // Client balances list
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.clientBalances) { clientBalance ->
                    ClientBalanceCard(
                        name = clientBalance.name,
                        balance = clientBalance.balance,
                        hasDebt = clientBalance.hasDebt
                    )
                }
            }
        }
    }
}

@Composable
private fun ClientBalanceCard(
    name: String,
    balance: Double,
    hasDebt: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = String.format("%.2f€", balance),
                style = MaterialTheme.typography.bodyLarge,
                color = if (hasDebt) DebtColor else PaidColor,
                textAlign = TextAlign.End
            )
        }
    }
}
