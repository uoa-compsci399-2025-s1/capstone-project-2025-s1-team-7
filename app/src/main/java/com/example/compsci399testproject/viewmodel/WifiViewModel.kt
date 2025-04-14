package com.example.compsci399testproject.viewmodel

import android.app.Application
import android.content.Context
import android.net.wifi.ScanResult
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.compsci399testproject.utils.WifiScanner

class WifiViewModel(application: Context) : AndroidViewModel(application as Application) {
    private val scanner = WifiScanner(application.applicationContext)

    private val _lastScanTime = mutableStateOf<Long?>(null)
    val lastScanTime: State<Long?> = _lastScanTime
    val scanResults = scanner.scanResults

    fun updateScanResults(results: List<ScanResult>) {
        _lastScanTime.value = System.currentTimeMillis()
        scanner.updateScanResults(results)
    }

    fun scan() {
        scanner.scanWifi()
    }

    override fun onCleared() {
        super.onCleared()
        scanner.cleanup()
    }

    fun getResults(): List<ScanResult> {
        return scanner.scanResults.value
    }
}