package com.example.compsci399testproject

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset

import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory

import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.compsci399testproject.viewmodel.MapViewModel


@Composable
fun MapImageView(
    floor: Int,
    offset: Offset,
    onOffsetChange: (Offset) -> Unit
) {
    val context = LocalContext.current
    val imageBitmap = remember(floor) {
        val assetManager = context.assets
        val path = "Building 302/Tiles/Floor ${floor}/Floor${floor}.png"
        val inputStream = assetManager.open(path)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        bitmap.asImageBitmap()
    }

    val gestureModifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                onOffsetChange(offset + dragAmount)
            }
        }

    Canvas(modifier = gestureModifier) {
        drawImage(
            image = imageBitmap,
            topLeft = offset
        )
    }
}

// For Eric - this is a placeholder, feel free to overwrite however you see fit.
@Composable
fun FloorSelector(selectedFloor : Int, onSelect: (Int) -> Unit){
    Row(modifier = Modifier.padding(8.dp)) {
        (0..2).forEach { floor ->
            Button(
                onClick = { onSelect(floor) },
                modifier = Modifier.padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFloor == floor) colorResource(id = R.color.dark_grey) else colorResource(id=R.color.dark_blue)
                )
            ) {
                Text("Floor $floor")
            }
        }
    }
}

//Main screen for map view.
@Composable
fun MapView(viewModel: MapViewModel = viewModel()) {
    Column {
        FloorSelector(
            selectedFloor = viewModel.currentFloor,
            onSelect = { viewModel.setFloor(it) }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            MapImageView(
                floor = viewModel.currentFloor,
                offset = viewModel.offset,
                onOffsetChange = viewModel::updateOffset
            )
        }
    }
}