package com.udaan.child

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                    OutlinedTextField(clientId, { clientId = it }, label = { Text("Client ID") })
                    OutlinedTextField(token, { token = it }, label = { Text("Token") })
                    OutlinedTextField(symbol, { symbol = it }, label = { Text("Symbol") })
                    OutlinedTextField(timeframe, { timeframe = it }, label = { Text("Timeframe") })
                    Text("Date: $selectedDate")
                    Text("Time: $selectedTime")

                    Button(onClick = {
                        val dt = "\( {selectedDate}T \){selectedTime}"
                        vm.runAnalysis(clientId, token, symbol, timeframe, dt)
                    }) {
                        Text("Run")
                    }

                    val result by vm.resultText.collectAsState()
                    val reversals by vm.reversalsText.collectAsState()
                    Text(result)
                    Text(reversals)
                }
            }
        }
    }
}
