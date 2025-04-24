package com.example.compsci399testproject.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {

    var currentFloor by mutableStateOf(0)
        private set

    var offset by mutableStateOf(Offset.Zero)
        private set

    var zoom by mutableStateOf(3.5f)
        private set

    var angle by mutableStateOf(0f)
        private set

    fun setFloor(floor: Int){
        currentFloor = floor
    }

    fun getFloor(): Int {
        return currentFloor
    }

    fun updateOffset(newOffset : Offset){
        offset = newOffset
    }

    fun updateZoom(newZoom : Float){
        zoom = newZoom
    }

    fun updateAngle(newAngle : Float){
        angle = newAngle
    }
}