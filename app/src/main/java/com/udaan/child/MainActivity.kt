package com.udaan.child

import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.udaan.child.ui.main.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etClientId = findViewById<EditText>(R.id.etClientId)
        val etAccessToken = findViewById<EditText>(R.id.etAccessToken)
        val etSymbol = findViewById<EditText>(R.id.etSymbol)
        val spTimeframe = findViewById<Spinner>(R.id.spTimeframe)
        val etAsOfDate = findViewById<EditText>(R.id.etAsOfDate)
        val btnRun = findViewById<Button>(R.id.btnRun)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val tvReversals = findViewById<TextView>(R.id.tvReversals)

        val timeframes = arrayOf("1m", "2m", "3m", "5m", "15m", "1H", "1D", "Custom")
        spTimeframe.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, timeframes)
        etAsOfDate.setText(LocalDate.now().toString())

        btnRun.setOnClickListener {
            val clientId = etClientId.text.toString()
            val token = etAccessToken.text.toString()
            val symbol = etSymbol.text.toString().ifBlank { "NIFTY" }
            val tf = spTimeframe.selectedItem.toString()
            val date = etAsOfDate.text.toString()

            if (clientId.isBlank() || token.isBlank()) {
                tvResult.text = "Client ID aur Token daalo"
                return@setOnClickListener
            }

            viewModel.runAnalysis(clientId, token, symbol, tf, date)
        }

        lifecycleScope.launch {
            viewModel.resultText.collectLatest { tvResult.text = it }
        }
        lifecycleScope.launch {
            viewModel.reversalsText.collectLatest { tvReversals.text = it }
        }
    }
}
