package com.udaan.child

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.udaan.child.ui.main.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etCustomInput = findViewById<EditText>(R.id.etCustomInput)
        val btnCustomForecast = findViewById<Button>(R.id.btnCustomForecast)
        val tvCustomForecastResult = findViewById<TextView>(R.id.tvCustomForecastResult)
        val btnRunAnalysis = findViewById<Button>(R.id.btnRunAnalysis)

        // Custom Forecast Button Click
        btnCustomForecast.setOnClickListener {
            val textInput = etCustomInput.text.toString()
            if (textInput.isNotBlank()) {
                viewModel.runCustomForecast(textInput)
            }
        }

        // Live Market Analysis Button Click
        btnRunAnalysis.setOnClickListener {
            viewModel.runAnalysis("NIFTY")
        }

        // Observe Custom Forecast Result
        lifecycleScope.launch {
            viewModel.customForecastResult.collectLatest { result ->
                if (result != null) {
                    tvCustomForecastResult.text = "Predicted Value: %.2f (StdDev: %.4f)".format(result.value, result.stdDev)
                }
            }
        }
    }
}
