package com.example.compsci399testproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

class MapViewModel : ViewModel() {

    var currentFloor by mutableStateOf(0)
        private set

    var offset by mutableStateOf(Offset.Zero)
        private set

    fun setFloor(floor: Int){
        currentFloor = floor
        offset = Offset.Zero
    }

    fun getFloor(): Int {
        return currentFloor
    }

    fun setOffset(newOffset: Offset) {
        offset = newOffset
    }
}