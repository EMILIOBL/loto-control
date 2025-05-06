package com.example.lotocontrol

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.lotocontrol.data.ClientDao
import com.example.lotocontrol.data.LotterySettingsDao
import com.example.lotocontrol.data.import.DataImporter
import com.example.lotocontrol.data.import.ImportResult
import com.example.lotocontrol.ui.LotoControlNavigation
import com.example.lotocontrol.ui.theme.LotoControlTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var clientDao: ClientDao

    @Inject
    lateinit var settingsDao: LotterySettingsDao

    private val dataImporter = DataImporter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            LotoControlTheme {
                var showImportError by remember { mutableStateOf<String?>(null) }

                val importLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let { handleImport(it) }
                }

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("LotoControl") },
                            actions = {
                                IconButton(
                                    onClick = { importLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Upload,
                                        contentDescription = "Importar datos"
                                    )
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        LotoControlNavigation()

                        // Show error snackbar if needed
                        showImportError?.let { error ->
                            LaunchedEffect(error) {
                                // Reset error after showing
                                showImportError = null
                            }
                        }
                    }
                }

                // Error dialog
                showImportError?.let { error ->
                    AlertDialog(
                        onDismissRequest = { showImportError = null },
                        title = { Text("Error de importación") },
                        text = { Text(error) },
                        confirmButton = {
                            TextButton(onClick = { showImportError = null }) {
                                Text("Aceptar")
                            }
                        }
                    )
                }
            }
        }
    }

    private fun handleImport(uri: Uri) {
        lifecycleScope.launch {
            if (!dataImporter.validateExcelFormat(this@MainActivity, uri)) {
                showError("El formato del archivo no es válido. Por favor, use la plantilla correcta.")
                return@launch
            }

            when (val result = dataImporter.importFromExcel(this@MainActivity, uri)) {
                is ImportResult.Success -> {
                    try {
                        // Insert settings
                        settingsDao.insertSettings(result.settings)
                        
                        // Insert all clients
                        result.clients.forEach { client ->
                            clientDao.insertClient(client)
                        }
                        
                        showSuccess()
                    } catch (e: Exception) {
                        showError("Error al guardar los datos: ${e.message}")
                    }
                }
                is ImportResult.Error -> {
                    showError(result.message)
                }
            }
        }
    }

    private fun showError(message: String) {
        lifecycleScope.launch {
            // Show error dialog
            setContent {
                var showDialog by remember { mutableStateOf(true) }
                
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Error") },
                        text = { Text(message) },
                        confirmButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Aceptar")
                            }
                        }
                    )
                }
            }
        }
    }

    private fun showSuccess() {
        lifecycleScope.launch {
            // Show success snackbar
            setContent {
                var showDialog by remember { mutableStateOf(true) }
                
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Éxito") },
                        text = { Text("Datos importados correctamente") },
                        confirmButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Aceptar")
                            }
                        }
                    )
                }
            }
        }
    }
}
