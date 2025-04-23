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
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp

import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.compsci399testproject.viewmodel.MapViewModel
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin


@Composable
fun MapImageView(
    floor: Int,
    offset: Offset,
    onOffsetChange: (Offset) -> Unit,
    positionX: Float,
    positionY: Float,
    positionFloor: Int,
    rotation: Float
) {
    val context = LocalContext.current
    val imageBitmap = remember(floor) {
        try {
            val assetManager = context.assets
            val path = "Building 302/Tiles/Floor ${floor}/Floor${floor}.png"
            val inputStream = assetManager.open(path)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            Toast.makeText(context, "Could not find floor image", Toast.LENGTH_SHORT).show()
            val assetManager = context.assets
            val path = "Building 302/Tiles/Floor ${0}/Floor${0}.png"
            val inputStream = assetManager.open(path)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bitmap.asImageBitmap()
        }
    }

    fun Offset.rotateBy(angle: Float): Offset {
        val angleInRadians = angle * (PI / 180)
        val cos = cos(angleInRadians)
        val sin = sin(angleInRadians)
        return Offset((x * cos - y * sin).toFloat(), (x * sin + y * cos).toFloat())
    }

    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableStateOf(3f) }
    var angle by remember { mutableStateOf(0f) }

    val floor_image_size_width : Dp  = 300.dp
    val floor_image_size_height : Dp = 300.dp

    val size_x : Dp  = 4.dp
    val size_y : Dp = 4.dp
    val position_x : Dp = (floor_image_size_width * positionX) - (size_x / 2)
    val position_y : Dp = (floor_image_size_height * positionY) - (size_y / 2)


    Box(modifier = Modifier.pointerInput(Unit) {
        detectTransformGestures(panZoomLock = true,
            onGesture = { centroid, pan, gestureZoom, gestureRotate ->
                val oldScale = zoom
                val newScale = Math.max(zoom * gestureZoom, 1.5f)

                // For natural zooming and rotating, the centroid of the gesture should
                // be the fixed point where zooming and rotating occurs.
                // We compute where the centroid was (in the pre-transformed coordinate
                // space), and then compute where it will be after this delta.
                // We then compute what the new offset should be to keep the centroid
                // visually stationary for rotating and zooming, and also apply the pan.
                offset =
                    (offset + centroid / oldScale).rotateBy(gestureRotate) -
                            (centroid / newScale + pan / oldScale)
                zoom = newScale
                angle += gestureRotate
            }
        )
        }
        .graphicsLayer {
            translationX = -offset.x * zoom
            translationY = -offset.y * zoom
            scaleX = zoom
            scaleY = zoom
            rotationZ = angle
            transformOrigin = TransformOrigin(0f, 0f)
        }
        .fillMaxSize()) {

        Log.d("MAP", "Zoom ${zoom} | Rotation ${angle} | Offset ${offset} ")

        Image(bitmap = imageBitmap,
            contentDescription = "${floor} image",
            modifier = Modifier
                .size(width = floor_image_size_width, height = floor_image_size_height))

        Image(painter = painterResource(id = R.drawable.position_icon),
            contentDescription = "${floor} image",
            modifier = Modifier
                .alpha(if (floor != positionFloor) 0f else 1f)
                .size(size_x, size_y)
                .offset(position_x, position_y)
                .graphicsLayer {
                    rotationZ = rotation })
    }
}

// For Eric - this is a placeholder, feel free to overwrite however you see fit.
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloorSelectorList(selectedFloor : Int, onSelect: (Int) -> Unit, visible: Boolean, changeFloorVisibility: (Boolean) -> Unit, modifier: Modifier){
    val state : ScrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        if (!visible) state.animateScrollTo(900); Log.d("FLOOR SELECTOR", "Scroll function")
    }

    Column(modifier = modifier
        .offset(x = -90.dp, y = -200.dp)
        .animateContentSize()
        .background(color = colorResource(id = R.color.darker_white), shape = RoundedCornerShape(6.dp))
        .border(width = 2.dp, color = colorResource(id = R.color.light_blue), shape = RoundedCornerShape(6.dp))
        .width(60.dp)
        .height(if (visible) 180.dp else 0.dp)
        .clip(shape = RoundedCornerShape(6.dp))
        .verticalScroll(state, visible),
        verticalArrangement = Arrangement.spacedBy(0.dp)) {
        for (floor in 5 downTo 0) {
            Button(
                onClick = { onSelect(floor) },
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

@Composable
fun FloorSelectorButton(selectedFloor : Int, visible: Boolean, changeFloorVisibility: (Boolean) -> Unit, modifier: Modifier) {
    Button(
        onClick = {changeFloorVisibility(!visible)},
        modifier = modifier.width(60.dp).height(60.dp)
            .offset(x = -20.dp, y = -260.dp)
            .border(width = 2.dp, color = colorResource(id = R.color.light_blue), shape = RoundedCornerShape(6.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (visible) colorResource(id = R.color.light_blue) else colorResource(id = R.color.darker_white)
        ),
        shape = RoundedCornerShape(6.dp)

    ) {
        Text("$selectedFloor",
            color = if (visible) colorResource(id = R.color.darker_white) else colorResource(id = R.color.light_blue),
            modifier = Modifier.fillMaxSize().wrapContentHeight(align = Alignment.CenterVertically),
            textAlign = TextAlign.Center
        )
    }
}

//Main screen for map view.
@Composable
@Preview
fun MapView(viewModel: MapViewModel = viewModel()) {
    var floorSelectorVisible:Boolean by remember { mutableStateOf(false) }

    // Position X and Y take percentage values
    // This is because the image scaling is different and can't use the raw pixel values
    var positionX:Float by remember { mutableFloatStateOf(754f / 1536f) }
    var positionY:Float by remember { mutableFloatStateOf(1330f / 1536f) }
    var positionFloor: Int by remember { mutableIntStateOf(0) }
    var rotation:Float by remember { mutableFloatStateOf(180f) }

    Column {
        Box(modifier = Modifier.fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    floorSelectorVisible = false
                })
            }) {

            MapImageView(
                floor = viewModel.currentFloor,
                offset = viewModel.offset,
                onOffsetChange = viewModel::updateOffset,
                positionX = positionX,
                positionY = positionY,
                positionFloor = positionFloor,
                rotation = rotation
            )

            FloorSelectorButton(
                selectedFloor = viewModel.currentFloor,
                visible = floorSelectorVisible,
                changeFloorVisibility = {floorSelectorVisible = it},
                modifier = Modifier.align(Alignment.BottomEnd)
            )

            FloorSelectorList(
                selectedFloor = viewModel.currentFloor,
                onSelect = { viewModel.setFloor(it)},
                visible = floorSelectorVisible,
                changeFloorVisibility = {floorSelectorVisible = it},
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

