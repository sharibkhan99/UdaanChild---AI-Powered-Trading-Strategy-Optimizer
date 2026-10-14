package com.udaan.child

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.udaan.child.ui.main.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // ViewModel obtained via the activity‑ktx delegate
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ← this line needs a real layout file at res/layout/activity_main.xml
        setContentView(R.layout.activity_main)

        // Example: observe the UI‑state coming from the ViewModel
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is MainViewModel.UiState.Idle -> { /* nothing */ }
                    is MainViewModel.UiState.Optimizing -> {
                        // show progress, e.g. a ProgressBar
                    }
                    is MainViewModel.UiState.Result -> {
                        // show the optimisation result
                    }
                    is MainViewModel.UiState.Error -> {
                        // show error message
                    }
                }
            }
        }

        // If you have a button that should start the optimisation, wire it here:
        // findViewById<Button>(R.id.btnStart).setOnClickListener { viewModel.triggerOptimization() }
    }
}
