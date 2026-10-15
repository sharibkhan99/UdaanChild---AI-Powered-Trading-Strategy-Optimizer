class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1️⃣ Start optimisation
        findViewById<Button>(R.id.btnStartOptimization)
            .setOnClickListener { viewModel.triggerOptimization() }

        // 2️⃣ Right‑hand icons – wire them later when you create those screens
        findViewById<ImageButton>(R.id.icPhone)
            .setOnClickListener { /* start CallLogActivity */ }

        findViewById<ImageButton>(R.id.icMusic)
            .setOnClickListener { /* start MediaPlayerActivity */ }

        findViewById<ImageButton>(R.id.icSettings)
            .setOnClickListener { /* start SettingsActivity */ }

        // 3️⃣ Observe UI state from ViewModel (progress / result)
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is MainViewModel.UiState.Idle -> {
                    progressBar.visibility = View.GONE
                    tvResult.visibility = View.GONE
                    btnStartOptimization.isEnabled = true
                }
                is MainViewModel.UiState.Optimizing -> {
                    progressBar.visibility = View.VISIBLE
                    tvResult.visibility = View.GONE
                    btnStartOptimization.isEnabled = false
                }
                is MainViewModel.UiState.Result -> {
                    progressBar.visibility = View.GONE
                    tvResult.visibility = View.VISIBLE
                    tvResult.text = "Forecast: ${state.forecast}  (confidence ${state.confidence}%)"
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
