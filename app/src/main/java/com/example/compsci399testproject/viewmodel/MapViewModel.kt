package com.example.compsci399testproject.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compsci399testproject.machinelearning.LocationPredictor
import com.example.compsci399testproject.utils.Node
import com.example.compsci399testproject.utils.NodeType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class UIState {
    MAIN,
    NAVIGATION_PREVIEW,
    NAVIGATING
}

class MapViewModel(wifiViewModel: WifiViewModel) : ViewModel() {

    var currentFloor by mutableStateOf(0)
        private set

    var offset by mutableStateOf(Offset.Zero)
        private set

    var zoom by mutableStateOf(3.5f)
        private set

    var angle by mutableStateOf(0f)
        private set

    var lockedOnPosition by mutableStateOf(true)
        private set

    var uiState by mutableStateOf(UIState.MAIN)

    // Screen Size
    var screenSizeWidth by mutableStateOf(0f)
    var screenSizeHeight by mutableStateOf(0f)

    // Map Image Size
    var mapImageSizeWidth by mutableStateOf(0f)
    var mapImageSizeHeight by mutableStateOf(0f)

    // Navigation
    var currentNavDestinationNode by mutableStateOf(Node("", 0, 0, 0, NodeType.ROOM, mutableListOf()))

    // Position
    // Position X and Y take percentage values
    // This is because the image scaling is different and can't use the raw pixel values
    private val _positionX = MutableStateFlow((754f) / 1536f)
    val positionX: StateFlow<Float> = _positionX.asStateFlow()

    private val _positionY = MutableStateFlow((1330f) / 1536f)
    val positionY: StateFlow<Float> = _positionY.asStateFlow()

    private val _positionFloor = MutableStateFlow(0)
    val positionFloor: StateFlow<Int> = _positionFloor.asStateFlow()

    private val _rotation = MutableStateFlow(180f)
    val rotation: StateFlow<Float> = _rotation.asStateFlow()

    private val _wifiViewModel = wifiViewModel

    init {
        startPredictingLocation()
    }

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

    fun updateLockedOnPosition(value: Boolean) {
        lockedOnPosition = value
    }

    fun updateUiState(state: UIState) {
        uiState = state
    }

    // Screen Size Functions
    fun updateScreenSize(width: Float, height: Float) {
        screenSizeWidth = width
        screenSizeHeight = height
    }

    fun updateMapImageSize(width: Float, height: Float) {
        mapImageSizeWidth = width
        mapImageSizeHeight = height
    }

    // Navigation functions
    fun updateNavDestinationNode(n: Node) {
        currentNavDestinationNode = n
    }

    fun viewDestinationNode(node: Node) {
        updateLockedOnPosition(false)
        updateUiState(UIState.NAVIGATION_PREVIEW)
        updateNavDestinationNode(node)
        setFloor(node.floor)

        val newZoom = 6f

        val widthOffset = (screenSizeWidth / 2) / newZoom
        val heightOffset = (screenSizeHeight / 2) / newZoom

        val x = (((754f + node.x) / 1536f) * mapImageSizeWidth) - widthOffset
        val y = (((1330f - node.y) / 1536f) * mapImageSizeHeight) - heightOffset

        val localOffset = Offset(x, y)
        val localZoom = newZoom
        val localAngle = 0f

        updateOffset(localOffset)
        updateZoom(localZoom)
        updateAngle(localAngle)

        Log.d("MAP VIEWMODEL", "VIEW DESTINATION ${localOffset} ${localZoom} ${localAngle} | NODE ${node.id} ${node.x}, ${node.y}")
    }

    // Wifi Location Prediction Function
    private fun startPredictingLocation() {
        viewModelScope.launch {
            while (true) {
                _wifiViewModel.scan()

                val success = withTimeoutOrNull(10_000) {
                    _wifiViewModel.scanResults.firstOrNull { results ->
                        if (results.isNotEmpty()) {
                            _wifiViewModel.updateScanResults()

                            val strengthArray = _wifiViewModel.getStrengthArray()
                            val floor = LocationPredictor.predictFloor(strengthArray.toFloatArray())
                            val x = LocationPredictor.predictX(strengthArray.toFloatArray())
                            val y = LocationPredictor.predictY(strengthArray.toFloatArray())

                            Log.d("predictor", "Predicted X: $x, Y: $y, Floor: $floor")

                            _positionX.value = (754f + x) / 1536f
                            _positionY.value = (1330f - y) / 1536f
                            _positionFloor.value = floor

                            if (lockedOnPosition) {setFloor(floor)}
                            true
                        } else false
                    }
                }

                delay(30_000)
            }
        }
    }


}