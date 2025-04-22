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
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.compsci399testproject.viewmodel.MapViewModel
import kotlin.math.floor


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

@Composable
fun ChangeFloorScrollPosition(state : ScrollState, amount : Int) {
    LaunchedEffect(Unit) { state.animateScrollTo(amount) }
}

// For Eric - this is a placeholder, feel free to overwrite however you see fit.
@Composable
fun FloorSelectorList(selectedFloor : Int, onSelect: (Int) -> Unit, visible: Boolean, changeFloorVisibility: (Boolean) -> Unit){
    val state : ScrollState = rememberScrollState()
    ChangeFloorScrollPosition(state, 900)

    Box(modifier = Modifier.fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                changeFloorVisibility(false)
                Log.d("FLOOR SELECTOR", "Tapped outside area")
            })
        }) {
        Column(modifier = Modifier.align(Alignment.BottomEnd)
            .offset(x = -20.dp, y = if (visible) -200.dp else -260.dp)
            .animateContentSize()
            .background(color = colorResource(id = R.color.darker_white), shape = RoundedCornerShape(6.dp))
            .border(width = 2.dp, color = colorResource(id = R.color.light_blue), shape = RoundedCornerShape(6.dp))
            .width(60.dp)
            .height(if (visible) 180.dp else 60.dp)
            .clip(shape = RoundedCornerShape(6.dp))
            .verticalScroll(state, visible),
            verticalArrangement = Arrangement.spacedBy(0.dp)) {
            for (floor in 5 downTo 0) {
                Button(
                    onClick = { if (visible) {onSelect(floor)} else {changeFloorVisibility(true); }},
                    modifier = Modifier.padding(0.dp).fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedFloor == floor && visible) colorResource(id = R.color.dark_grey) else colorResource(id = R.color.darker_white)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("$floor",
                        color = if (selectedFloor != floor || !visible) colorResource(id = R.color.light_blue) else colorResource(R.color.darker_white),
                        modifier = Modifier.fillMaxSize().wrapContentHeight(align = Alignment.CenterVertically),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun FloorSelectorButton(selectedFloor : Int, visible: Boolean, onFloorVisibilityChange: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onFloorVisibilityChange,
            modifier = Modifier.width(60.dp).height(60.dp)
                .align(Alignment.BottomEnd)
                .offset(x = -20.dp, y = -260.dp)
                .border(width = 2.dp, color = colorResource(id = R.color.light_blue), shape = RoundedCornerShape(6.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.darker_white)
            ),
            shape = RoundedCornerShape(6.dp)

        ) {
            Text("$selectedFloor",
                color = colorResource(id = R.color.light_blue),
                modifier = Modifier.fillMaxSize().wrapContentHeight(align = Alignment.CenterVertically),
                textAlign = TextAlign.Center
            )
        }
    }
}

//Main screen for map view.
@Composable
@Preview
fun MapView(viewModel: MapViewModel = viewModel()) {
    var floorSelectorVisible:Boolean by remember { mutableStateOf(false) }

    Column {
        Box(modifier = Modifier.fillMaxSize()) {
            MapImageView(
                floor = viewModel.currentFloor,
                offset = viewModel.offset,
                onOffsetChange = viewModel::updateOffset
            )
        }
    }
    //FloorSelectorButton(selectedFloor = viewModel.currentFloor, visible = floorSelectorVisible, onFloorVisibilityChange = {floorSelectorVisible = !floorSelectorVisible})
    FloorSelectorList(
        selectedFloor = viewModel.currentFloor,
        onSelect = { viewModel.setFloor(it)},
        visible = floorSelectorVisible,
        changeFloorVisibility = {floorSelectorVisible = it}
    )
}

