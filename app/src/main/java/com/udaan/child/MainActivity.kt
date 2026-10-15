package com.udaan.child

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.udaan.child.data.model.GAResult
import com.udaan.child.ui.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crash handler initialize
        CrashHandler.init(this)

        setContentView(R.layout.activity_main)

        val btnStartOptimization = findViewById<Button>(R.id.btnStartOptimization)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        val icPhone = findViewById<ImageButton>(R.id.icPhone)
        val icMusic = findViewById<ImageButton>(R.id.icMusic)
        val icSettings = findViewById<ImageButton>(R.id.icSettings)

        btnStartOptimization.setOnClickListener {
            viewModel.triggerOptimization()
        }

        icPhone.setOnClickListener { Toast.makeText(this, "Phone clicked", Toast.LENGTH_SHORT).show() }
        icMusic.setOnClickListener { Toast.makeText(this, "Music clicked", Toast.LENGTH_SHORT).show() }
        icSettings.setOnClickListener { Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show() }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is MainViewModel.UiState.Idle -> {
                        progressBar.visibility = View.GONE
                        tvResult.visibility = View.GONE
                        btnStartOptimization.isEnabled = true
                    }
                    is MainViewModel.UiState.Optimizing -> {
                        progressBar.visibility = View.VISIBLE
                        tvResult.visibility = View.VISIBLE
                        tvResult.text = state.progress
                        btnStartOptimization.isEnabled = false
                    }
                    is MainViewModel.UiState.Result -> {
                        progressBar.visibility = View.GONE
                        tvResult.visibility = View.VISIBLE
                        val featuresText = state.result.activeFeatures.joinToString(", ")
                        tvResult.text = "Optimization Complete!\nBest Sharpe: ${state.result.bestSharpe}\nFeatures: $featuresText"
                        btnStartOptimization.isEnabled = true

                        sendOptimizationToServer(state.result)
                    }
                    is MainViewModel.UiState.Error -> {
                        progressBar.visibility = View.GONE
                        tvResult.visibility = View.VISIBLE
                        tvResult.text = "Error: ${state.message}"
                        btnStartOptimization.isEnabled = true
                    }
                }
            }
        }
    }

    private fun sendOptimizationToServer(result: GAResult) {
        lifecycleScope.launch(Dispatchers.IO) {
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
