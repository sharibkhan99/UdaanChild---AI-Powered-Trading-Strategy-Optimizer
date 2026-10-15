package com.udaan.child

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udaan.child.data.model.GAResult
import com.udaan.child.ui.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crash reporting initialize karein
        CrashHandler.init(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        viewModel = viewModel,
                        onIconClick = { iconName ->
                            Toast.makeText(this, "$iconName clicked", Toast.LENGTH_SHORT).show()
                        },
                        onResultReady = { result ->
                            sendOptimizationToServer(result)
                        }
                    )
                }
            }
        }
    }

    private fun sendOptimizationToServer(result: GAResult) {
        val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val url = URL("http://139.59.12.88:3000/api/optimization")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    doOutput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                val payload = JSONObject().apply {
                    put("deviceName", "${Build.MANUFACTURER} ${Build.MODEL}")
                    put("sharpe", result.bestSharpe.toString())
                    put("features", result.activeFeatures.joinToString(", "))
                    put("chromosome", result.bestChromosome.toString())
                }

                conn.outputStream.use { os ->
                    os.write(payload.toString().toByteArray(Charsets.UTF_8))
                }
                conn.responseCode
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onIconClick: (String) -> Unit,
    onResultReady: (GAResult) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var lastSentResult by remember { mutableStateOf<GAResult?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16dp)
    ) {
        // Main Content (Title, Button, Progress, Result)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 48dp), // Right icon strip ke liye jagah chhodi hai
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32dp))

            Text(
                text = "UdaanChild – AI Trading Optimizer",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(40dp))

            val isOptimizing = uiState is MainViewModel.UiState.Optimizing

            Button(
                onClick = { viewModel.triggerOptimization() },
                enabled = !isOptimizing
            ) {
                Text(text = "START OPTIMIZATION")
            }

            Spacer(modifier = Modifier.height(24dp))

            when (val state = uiState) {
                is MainViewModel.UiState.Optimizing -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16dp))
                    Text(text = state.progress, fontSize = 16.sp)
                }
                is MainViewModel.UiState.Result -> {
                    if (lastSentResult != state.result) {
                        lastSentResult = state.result
                        onResultReady(state.result)
                    }
                    val featuresText = state.result.activeFeatures.joinToString(", ")
                    Text(
                        text = "Optimization Complete!\nBest Sharpe: ${state.result.bestSharpe}\nFeatures: $featuresText",
                        fontSize = 16.sp
                    )
                }
                is MainViewModel.UiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 16.sp
                    )
                }
                is MainViewModel.UiState.Idle -> {
                    // Do nothing
                }
            }
        }

        // Right-hand vertical icon strip (Phone, Play, Settings)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(8dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16dp)
        ) {
            IconButton(onClick = { onIconClick("Phone") }) {
                Icon(Icons.Default.Call, contentDescription = "Phone")
            }
            HorizontalDivider(modifier = Modifier.width(32dp))
            IconButton(onClick = { onIconClick("Music") }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Music")
            }
            HorizontalDivider(modifier = Modifier.width(32dp))
            IconButton(onClick = { onIconClick("Settings") }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    }
}
