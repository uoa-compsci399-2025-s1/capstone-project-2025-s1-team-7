package com.example.compsci399testproject.viewmodel

import android.app.Application
import android.content.Context
import android.net.wifi.ScanResult
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.compsci399testproject.utils.WifiScanner

class WifiViewModel(application: Context) : AndroidViewModel(application as Application) {
    private val scanner = WifiScanner(application.applicationContext, this)

    private val _lastScanTime = mutableStateOf<Long?>(null)
    val lastScanTime: State<Long?> = _lastScanTime
    var scanResults = scanner.scanResults
    private var lastUploadedResults: Map<String, Int>? = null

    fun updateScanResults() {
        _lastScanTime.value = System.currentTimeMillis()
        this.scanResults = scanner.scanResults
    }

    fun scan() {
        scanner.scanWifi()
    }

    override fun onCleared() {
        super.onCleared()
        scanner.cleanup()
    }

    fun getResults(): List<ScanResult> {
        return this.scanResults.value
    }


    fun hasScanChanged(newResults: List<ScanResult>): Boolean {
        val currentMap = newResults.associate { it.BSSID to it.level }

        val changed = currentMap != lastUploadedResults
        if (changed) {
            lastUploadedResults = currentMap
        }
        return changed
    }
}