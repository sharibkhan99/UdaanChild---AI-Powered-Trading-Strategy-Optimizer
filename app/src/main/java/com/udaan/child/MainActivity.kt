package com.udaan.child

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
import com.udaan.child.ui.main.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crash reporting start karein
        CrashHandler.init(this)

        setContentView(R.layout.activity_main)

        // XML ke sabhi views ko connect karein
        val btnStartOptimization = findViewById<Button>(R.id.btnStartOptimization)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        val icPhone = findViewById<ImageButton>(R.id.icPhone)
        val icMusic = findViewById<ImageButton>(R.id.icMusic)
        val icSettings = findViewById<ImageButton>(R.id.icSettings)

        // Start Optimization button click
        btnStartOptimization.setOnClickListener {
            viewModel.triggerOptimization()
        }

        // Side icons click listeners
        icPhone.setOnClickListener {
            Toast.makeText(this, "Phone clicked", Toast.LENGTH_SHORT).show()
        }
        icMusic.setOnClickListener {
            Toast.makeText(this, "Music clicked", Toast.LENGTH_SHORT).show()
        }
        icSettings.setOnClickListener {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
        }

        // ViewModel state observe karein
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
                        tvResult.text = "Optimization Complete!\nBest Sharpe: ${state.result.bestSharpe}"
                        btnStartOptimization.isEnabled = true
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
}
