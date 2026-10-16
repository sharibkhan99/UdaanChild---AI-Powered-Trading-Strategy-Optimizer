package com.udaan.child

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import java.time.LocalTime

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var clientId by remember { mutableStateOf("") }
                var token by remember { mutableStateOf("") }
                var symbol by remember { mutableStateOf("NIFTY") }
                var timeframe by remember { mutableStateOf("1D") }
                var selectedDate by remember { mutableStateOf(LocalDate.now()) }
                var selectedTime by remember { mutableStateOf(LocalTime.now()) }

                Column(Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = clientId,
                        onValueChange = { clientId = it },
                        label = { Text("Client ID") }
                    )
                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("Token") }
                    )
                    OutlinedTextField(
                        value = symbol,
                        onValueChange = { symbol = it },
                        label = { Text("Symbol") }
                    )
                    OutlinedTextField(
                        value = timeframe,
                        onValueChange = { timeframe = it },
                        label = { Text("Timeframe") }
                    )
                    Text(text = "Date: " + selectedDate.toString())
                    Text(text = "Time: " + selectedTime.toString())

                    Button(onClick = {
                        val dt = selectedDate.toString() + "T" + selectedTime.toString()
                        vm.runAnalysis(clientId, token, symbol, timeframe, dt)
                    }) {
                        Text(text = "Run")
                    }

                    val result by vm.resultText.collectAsState()
                    val reversals by vm.reversalsText.collectAsState()
                    Text(text = result)
                    Text(text = reversals)
                }
            }
        }
    }
}
