package com.udaan.child.ui.main

import androidx.lifecycle.ViewModel
import com.udaan.child.forecast.DhanRepository
import com.udaan.child.forecast.ForecastUseCase

class MainViewModel : ViewModel() {
    private val repo = DhanRepository()
    private val useCase = ForecastUseCase(repo)

    // Temporary stubs
    fun getAllForecasts() = emptyList<String>()
}
