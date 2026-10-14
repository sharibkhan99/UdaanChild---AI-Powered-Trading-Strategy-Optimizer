package com.udaan.child

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity          // <-- now resolvable
import androidx.activity.viewModels                     // optional, if you use view‑models
import com.udaan.child.ui.main.MainViewModel            // adjust if your VM lives elsewhere
import kotlinx.android.synthetic.main.activity_main.*   // only if you still use synthetic; otherwise use view‑binding / data‑binding

class MainActivity : AppCompatActivity() {

    // Example: obtain the ViewModel (requires the `activity-ktx` dependency you already have)
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)          // <-- now resolvable

        // ---- your UI initialisation code here ----
        // e.g. observe viewModel.uiState, set click listeners, etc.
    }
}
