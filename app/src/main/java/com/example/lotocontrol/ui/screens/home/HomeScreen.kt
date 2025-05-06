package com.example.lotocontrol.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lotocontrol.data.models.Client
import com.example.lotocontrol.ui.theme.DebtColor
import com.example.lotocontrol.ui.theme.PaidColor
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onClientClick: (Long) -> Unit,
    onSummaryClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LotoControl") },
                actions = {
                    IconButton(onClick = onSummaryClick) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Ver resumen"
                        )
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
            // Header section with draw date and ticket price
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sorteo del ${uiState.drawDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es")))}",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Precio del décimo: ${String.format("%.2f€", uiState.ticketPrice)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Client list
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.clients) { clientWithBalance ->
                    ClientCard(
                        client = clientWithBalance.client,
                        balance = clientWithBalance.currentBalance,
                        hasDebt = clientWithBalance.hasDebt,
                        onClick = { onClientClick(clientWithBalance.client.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClientCard(
    client: Client,
    balance: Double,
    hasDebt: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                text = client.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = String.format("%.2f€", balance),
                style = MaterialTheme.typography.bodyLarge,
                color = if (hasDebt) DebtColor else PaidColor
            )
        }
    }
}
