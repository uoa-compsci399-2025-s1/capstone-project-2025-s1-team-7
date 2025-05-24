package com.example.compsci399testproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.compsci399testproject.sensors.RotationSensorService
import com.example.compsci399testproject.sensors.StepDetectionService

class MapViewModelFactory(
    private val wifiViewModel: WifiViewModel,
    private val rotationSensorService: RotationSensorService,
    private val stepDetectionService: StepDetectionService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(wifiViewModel, rotationSensorService, stepDetectionService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}