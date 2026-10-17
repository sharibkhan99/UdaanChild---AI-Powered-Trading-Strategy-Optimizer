
package com.udaan.child

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.udaan.child.ui.main.MainViewModel
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                var clientId by remember { mutableStateOf("") }
                var token by remember { mutableStateOf("") }
                var symbol by remember { mutableStateOf("NIFTY") }
                var timeframe by remember { mutableStateOf("5m") }

                var fromDate by remember {
                    mutableStateOf(
                        LocalDate.now().minusDays(30).toString()
                    )
                }

                var toDate by remember {
                    mutableStateOf(LocalDate.now().toString())
                }

                var menuExpanded by remember { mutableStateOf(false) }

                val result by vm.resultText.collectAsState()
                val analysis by vm.analysisText.collectAsState()

                val timeframes =
                    (1..60).map { "${it}m" } +
                    (1..23).map { "${it}H" } +
                    listOf("1D")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Udaan Dhan Forecast & Backtest",
                        style = MaterialTheme.typography.titleLarge
                    )

                    OutlinedTextField(
                        value = clientId,
                        onValueChange = { clientId = it },
                        label = { Text("Dhan Client ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("Dhan Access Token") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = symbol,
                        onValueChange = { symbol = it },
                        label = {
                            Text("Symbol (e.g. NIFTY, BANKNIFTY)")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Button(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Timeframe: $timeframe ▾")
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = {
                                    menuExpanded = false
                                }
                            ) {
                                timeframes.forEach { tf ->
                                    DropdownMenuItem(
                                        text = { Text(tf) },
                                        onClick = {
                                            timeframe = tf
                                            menuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        "Date-range backtest / historical fetch (YYYY-MM-DD)",
                        style = MaterialTheme.typography.labelLarge
                    )

                    OutlinedTextField(
                        value = fromDate,
                        onValueChange = { fromDate = it },
                        label = { Text("Start date") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = toDate,
                        onValueChange = { toDate = it },
                        label = { Text("End date") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            vm.runAnalysis(
                                clientId,
                                token,
                                symbol,
                                timeframe,
                                fromDate,
                                toDate
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fetch Data + Backtest + Kalman Predict")
                    }

                    Text(
                        result,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        analysis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
