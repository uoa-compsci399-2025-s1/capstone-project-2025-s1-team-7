package com.example.compsci399testproject

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset

import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compsci399testproject.utils.NavigationGraph
import com.example.compsci399testproject.utils.Node
import com.example.compsci399testproject.utils.NodeType
import com.example.compsci399testproject.utils.getRoomNodes
import com.example.compsci399testproject.utils.initialiseGraph

import com.example.compsci399testproject.viewmodel.MapViewModel
import com.example.compsci399testproject.viewmodel.UIState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MapImageView(
    floor: Int,
    offset: Offset,
    zoom: Float,
    angle: Float,
    updateOffset: (Offset) -> Unit,
    updateZoom: (Float) -> Unit,
    updateAngle: (Float) -> Unit,
    positionXPercentage: Float,
    positionYPercentage: Float,
    positionFloor: Int,
    rotation: Float,
    lockedOnPosition: Boolean,
    changeLockOnPosition: (Boolean) -> Unit,
    uiState: UIState,
    navigationNode: Node,
    mapImageSizeWidth: Dp,
    mapImageSizeHeight: Dp
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

    // Had to create local variables as there was some weird cache issue with the gesture code using old values
    // This meant that the variables were never updated and always used the default values below
    var localOffset:Offset by remember { mutableStateOf(Offset.Zero) }
    var localZoom:Float by remember { mutableFloatStateOf(3.5f) }
    var localAngle:Float by remember { mutableFloatStateOf(0f) }

    val floorImageSizeWidth : Dp  = mapImageSizeWidth
    val floorImageSizeHeight : Dp = mapImageSizeHeight

    val rawPositionX: Dp = (floorImageSizeWidth * positionXPercentage)
    val rawPositionY: Dp = (floorImageSizeHeight * positionYPercentage)

    val positionIconSizeX : Dp  = 4.dp
    val positionIconSizeY : Dp = 4.dp
    val positionIconPosX : Dp = rawPositionX - (positionIconSizeX / 2)
    val positionIconPosY : Dp = rawPositionY - (positionIconSizeY / 2)


    Box(modifier = Modifier
        .pointerInput(Unit) {
            detectTransformGestures(
                panZoomLock = true,
                onGesture = { centroid, pan, gestureZoom, gestureRotate ->
                    val oldScale = localZoom
                    val newScale = Math.max(localZoom * gestureZoom, 1.5f)

                    // For natural zooming and rotating, the centroid of the gesture should
                    // be the fixed point where zooming and rotating occurs.
                    // We compute where the centroid was (in the pre-transformed coordinate
                    // space), and then compute where it will be after this delta.
                    // We then compute what the new offset should be to keep the centroid
                    // visually stationary for rotating and zooming, and also apply the pan.
                    localOffset =
                        (localOffset + centroid / oldScale).rotateBy(gestureRotate) -
                                (centroid / newScale + pan / oldScale)
                    localZoom = newScale
                    localAngle += gestureRotate

                    updateOffset(localOffset)
                    updateZoom(localZoom)
                    updateAngle(localAngle)
                    changeLockOnPosition(false)
                }
            )
        }
        .graphicsLayer {
            if (lockedOnPosition) {
                val width = this.size.width
                val height = this.size.height

                val newZoom = 6f

                val widthOffset = (width / 2) / newZoom
                val heightOffset = (height / 2) / newZoom

                val x = rawPositionX.toPx() - widthOffset
                val y = rawPositionY.toPx() - heightOffset

                localOffset = Offset(x, y)
                localZoom = newZoom
                localAngle = 0f

                updateOffset(localOffset)
                updateZoom(localZoom)
                updateAngle(localAngle)
                //Log.d("MAP", "Pixel position of User | ${x}, ${y} | Pos ${width} ${height}")
            } else if (!uiState.equals(UIState.MAIN)) {
                // This is a very hacky method to update the local position and should be changed later
                localOffset = offset
                localZoom = zoom
                localAngle = angle

            }

            translationX = -offset.x * zoom
            translationY = -offset.y * zoom
            scaleX = zoom
            scaleY = zoom
            rotationZ = angle
            transformOrigin = TransformOrigin(0f, 0f)


        }
        .fillMaxSize()
    ) {

        Log.d("MAP", "Zoom ${zoom} | Rotation ${angle} | Offset ${offset},  ")

        Image(bitmap = imageBitmap,
            contentDescription = "${floor} image",
            modifier = Modifier
                .size(width = floorImageSizeWidth, height = floorImageSizeHeight)
                //.background(color = Color.Blue)
        )

        Spacer(modifier = Modifier.fillMaxSize().drawWithCache {
            onDrawBehind {
                //drawPath(path, Color.Green, style = Stroke(2.dp.toPx()))
            }
        })



        Image(painter = painterResource(id = R.drawable.position_icon),
            contentDescription = "${floor} image",
            modifier = Modifier
                .alpha(if (floor != positionFloor) 0f else 1f)
                .size(positionIconSizeX, positionIconSizeY)
                .offset(positionIconPosX, positionIconPosY)
                .graphicsLayer {
                    rotationZ = rotation
                }
        )

        Box(modifier = Modifier
            .width(4.dp)
            .height(if (uiState.equals(UIState.NAVIGATION_PREVIEW) && floor == navigationNode.floor) 4.dp else 0.dp)
            .offset(x = (((754f + navigationNode.x) / 1536f) * floorImageSizeWidth) - 2.dp,
                y = (((1330f - navigationNode.y) / 1536f) * floorImageSizeHeight) - 2.dp)
            .background(color = colorResource(id = R.color.light_blue), shape = RoundedCornerShape(6.dp))
        )
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
        .background(
            color = colorResource(id = R.color.darker_white),
            shape = RoundedCornerShape(6.dp)
        )
        .border(
            width = 2.dp,
            color = colorResource(id = R.color.light_blue),
            shape = RoundedCornerShape(6.dp)
        )
        .width(60.dp)
        .height(if (visible) 180.dp else 0.dp)
        .clip(shape = RoundedCornerShape(6.dp))
        .verticalScroll(state, visible),
        verticalArrangement = Arrangement.spacedBy(0.dp)) {
        for (floor in 5 downTo 0) {
            Button(
                onClick = { onSelect(floor) },
                modifier = Modifier
                    .padding(0.dp)
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFloor == floor && visible) colorResource(id = R.color.dark_grey) else colorResource(id = R.color.darker_white)
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("$floor",
                    color = if (selectedFloor != floor || !visible) colorResource(id = R.color.light_blue) else colorResource(R.color.darker_white),
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(align = Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun FloorSelectorButton(selectedFloor : Int, visible: Boolean, changeFloorVisibility: (Boolean) -> Unit, modifier: Modifier, uiState: UIState) {
    Button(
        onClick = {changeFloorVisibility(!visible)},
        modifier = modifier
            .offset(x = -20.dp, y = -260.dp)
            .border(
                width = 2.dp,
                color = colorResource(id = R.color.light_blue),
                shape = RoundedCornerShape(6.dp)
            )
            .width(60.dp)
            .height(if (uiState.equals(UIState.MAIN) || uiState.equals(UIState.NAVIGATING)) 60.dp else 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (visible) colorResource(id = R.color.light_blue) else colorResource(id = R.color.darker_white)
        ),
        shape = RoundedCornerShape(6.dp)

    ) {
        Text("$selectedFloor",
            color = if (visible) colorResource(id = R.color.darker_white) else colorResource(id = R.color.light_blue),
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight(align = Alignment.CenterVertically),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ResetPositionButton(selectedFloor: Int, positionFloor: Int, modifier: Modifier, changeFloor: (Int) -> Unit,
                        lockedOnPosition: Boolean, changeLockedOnPosition: (Boolean) -> Unit,
                        uiState: UIState
) {
    Box(
        modifier = modifier
            .width(60.dp)
            .height(if ((uiState.equals(UIState.MAIN) || uiState.equals(UIState.NAVIGATING)) && (selectedFloor != positionFloor || !lockedOnPosition)) 60.dp else 0.dp)
            .offset(x = -20.dp, y = -180.dp)
            .background(
                color = colorResource(id = R.color.darker_white),
                shape = RoundedCornerShape(60.dp)
            )
            .border(
                width = 2.dp,
                color = colorResource(id = R.color.light_blue),
                shape = RoundedCornerShape(60.dp)
            )
            .clickable {
                changeFloor(positionFloor)
                changeLockedOnPosition(true)
            }
    )
    {
        Box(modifier = modifier
            .width(24.dp)
            .height(24.dp)
            .offset(x = 10.dp, y = -40.dp)
            .background(
                color = colorResource(id = R.color.light_blue),
                shape = RoundedCornerShape(60.dp)
            )
        ) {
            Text(text = positionFloor.toString(),
                color = colorResource(R.color.darker_white),
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight(align = Alignment.CenterVertically),
                textAlign = TextAlign.Center
            )
        }

    }

}

@Composable
fun SearchBar(modifier: Modifier, searchText: String, updateSearchText: (String) -> Unit, searchResults : List<Node>,
              uiState: UIState,
              viewDestinationNode: (Node) -> Unit){
    val singleSearchResultHeight: Dp = 40.dp
    val totalSearchHeight = singleSearchResultHeight * 4

    val focusManager = LocalFocusManager.current

    var searchFocused by remember { mutableStateOf(false) }

    Box(modifier = modifier
        .offset(x = 0.dp, y = 30.dp)
        .width(if (uiState.equals(UIState.MAIN)) 280.dp else 0.dp)
    ) {
        OutlinedTextField(value = searchText,
            onValueChange = { updateSearchText(it) },
            placeholder = { Text("Search") },
            modifier = Modifier.fillMaxWidth().height(56.dp)
                .onFocusChanged {
                    searchFocused = it.isFocused
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.dark_blue),
                unfocusedBorderColor = colorResource(id = R.color.light_blue),
                cursorColor = colorResource(id = R.color.dark_blue),
                focusedContainerColor = colorResource(id = R.color.darker_white),
                unfocusedContainerColor = colorResource(id = R.color.darker_white)
            ),
            leadingIcon = {Icon(imageVector = Icons.Filled.Search, contentDescription = "")},
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        LazyColumn (modifier = Modifier.fillMaxWidth()
            .heightIn(0.dp, totalSearchHeight)
            .height(if (uiState.equals(UIState.MAIN) && searchFocused) singleSearchResultHeight * searchResults.size else 0.dp)
            .offset(x = 0.dp, y = 62.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .background(color = colorResource(id = R.color.darker_white), shape = RoundedCornerShape(8.dp))
            .border(2.dp, color = colorResource(id = R.color.dark_blue), shape = RoundedCornerShape(8.dp)),
        ) {

            items(searchResults) { node ->
                Box(modifier = Modifier.fillMaxWidth().height(singleSearchResultHeight)
                    .clickable {
                        viewDestinationNode(node)
                        focusManager.clearFocus()
                    }) {
                    Text(text = node.id, modifier = Modifier.fillMaxSize()
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .padding(horizontal = 12.dp))
                }
            }
        }
    }
}

@Composable
fun PreviewNavigationSearchBar(modifier: Modifier, destinationNode: Node,
                               uiState: UIState, updateUiState: (UIState) -> Unit,
                               startNavigation: () -> Unit) {

    var destinationSearchTextFocused: Boolean by remember { mutableStateOf(false) }

    Column(modifier = modifier
        .offset(x = 0.dp, y = 30.dp)
        .width(if (uiState.equals(UIState.NAVIGATION_PREVIEW)) 280.dp else 0.dp)
        .height(120.dp)
        .background(color = colorResource(id = R.color.darker_white), shape = RoundedCornerShape(6.dp))
        .border(color = colorResource(id = R.color.dark_blue), width = 2.dp, shape = RoundedCornerShape(6.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("From Current Location", modifier.fillMaxWidth().offset(x = 0.dp, y = 10.dp), textAlign = TextAlign.Center)
        Text("To " + destinationNode.id, modifier.fillMaxWidth().offset(x = 0.dp, y = 20.dp), textAlign = TextAlign.Center)

        Row(modifier = Modifier.offset(0.dp, 40.dp)) {
            Button(onClick = {updateUiState(UIState.MAIN)}, modifier = Modifier.width(120.dp).height(36.dp).padding(0.dp, 0.dp, 10.dp, 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.red_cancel)
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Cancel")
            }

            Button(onClick = {startNavigation()}, modifier = Modifier.width(120.dp).height(36.dp).padding(10.dp, 0.dp, 0.dp, 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.light_blue)
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Navigate")
            }
        }
    }
}

@Composable
fun NavigationTopBar(modifier: Modifier, destinationNode: Node,
                               uiState: UIState, updateUiState: (UIState) -> Unit) {
    Column(modifier = modifier
        .offset(x = 0.dp, y = 30.dp)
        .width(if (uiState.equals(UIState.NAVIGATING)) 280.dp else 0.dp)
        .height(100.dp)
        .background(color = colorResource(id = R.color.darker_white), shape = RoundedCornerShape(6.dp))
        .border(color = colorResource(id = R.color.dark_blue), width = 2.dp, shape = RoundedCornerShape(6.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Navigating to " + destinationNode.id, modifier.fillMaxWidth().offset(x = 0.dp, y = 20.dp), textAlign = TextAlign.Center)

        Button(onClick = {updateUiState(UIState.MAIN)}, modifier = Modifier.width(120.dp).height(36.dp).offset(x = 0.dp, y = 30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.red_cancel)
            ),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text("Cancel")
        }
    }
}

//Main screen for map view.
@Composable
fun MapView(viewModel: MapViewModel = viewModel()) {
    val context = LocalContext.current
    val currentFocusManager = LocalFocusManager.current
    val displayMetrics = context.resources.displayMetrics

    viewModel.updateScreenSize(displayMetrics.widthPixels.toFloat(), displayMetrics.heightPixels.toFloat())

    var floorImageSizeWidth = 316.dp
    var floorImageSizeHeight = 316.dp

    viewModel.updateMapImageSize(with(LocalDensity.current) {floorImageSizeWidth.toPx()}, with(LocalDensity.current) {floorImageSizeHeight.toPx()})


    var floorSelectorVisible:Boolean by remember { mutableStateOf(false) }

    val positionX by viewModel.positionX.collectAsState()
    val positionY by viewModel.positionY.collectAsState()
    val positionFloor by viewModel.positionFloor.collectAsState()
    val rotation by viewModel.rotation.collectAsState()

    val navigationGraph: NavigationGraph = remember {initialiseGraph()}
    val rooms: List<Node> = remember { getRoomNodes(navigationGraph) }

    var searchText: String by remember { mutableStateOf("") }
    var searchResults = remember { mutableStateListOf<Node>() }

    fun getSearchResults(query: String) {
        searchText = query
        searchResults.clear()

        if (searchText.trim() == "") return

        for (room: Node in rooms) {
            if (room.id.lowercase().contains(query.lowercase())) {
                searchResults.add(room)
            }
        }
        Log.d("MAP", searchResults.toString())
    }


    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                floorSelectorVisible = false
                currentFocusManager.clearFocus()
            })
        }) {

        MapImageView(
            floor = viewModel.currentFloor,
            offset = viewModel.offset,
            zoom = viewModel.zoom,
            angle = viewModel.angle,
            updateOffset = {viewModel.updateOffset(it)},
            updateZoom = {viewModel.updateZoom(it)},
            updateAngle = {viewModel.updateAngle(it)},
            positionXPercentage = positionX,
            positionYPercentage = positionY,
            positionFloor = positionFloor,
            rotation = rotation,
            lockedOnPosition = viewModel.lockedOnPosition,
            changeLockOnPosition = {viewModel.updateLockedOnPosition(it)},
            uiState = viewModel.uiState,
            navigationNode = viewModel.currentNavDestinationNode,
            mapImageSizeWidth = floorImageSizeWidth,
            mapImageSizeHeight = floorImageSizeHeight
        )

        SearchBar(
            modifier = Modifier.align(Alignment.TopCenter),
            searchText = searchText,
            updateSearchText = {getSearchResults(it)},
            searchResults = searchResults,
            uiState = viewModel.uiState,
            viewDestinationNode = {viewModel.viewDestinationNode(it)}
        )

        PreviewNavigationSearchBar(modifier = Modifier.align(Alignment.TopCenter),
            destinationNode = viewModel.currentNavDestinationNode,
            uiState = viewModel.uiState,
            updateUiState = {viewModel.updateUiState(it)},
            startNavigation = {viewModel.startNavigation(navigationGraph)}
        )

        NavigationTopBar (modifier = Modifier.align(Alignment.TopCenter),
            destinationNode = viewModel.currentNavDestinationNode,
            uiState = viewModel.uiState,
            updateUiState = {viewModel.updateUiState(it)}
        )

        ResetPositionButton(
            selectedFloor = viewModel.currentFloor,
            positionFloor = positionFloor,
            modifier = Modifier.align(Alignment.BottomEnd),
            changeFloor = { viewModel.setFloor(it)},
            lockedOnPosition = viewModel.lockedOnPosition,
            changeLockedOnPosition = {viewModel.updateLockedOnPosition(it)},
            uiState = viewModel.uiState
        )

        FloorSelectorButton(
            selectedFloor = viewModel.currentFloor,
            visible = floorSelectorVisible,
            changeFloorVisibility = {floorSelectorVisible = it},
            modifier = Modifier.align(Alignment.BottomEnd),
            uiState = viewModel.uiState
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

@Composable
@Preview
fun MapPreviewFun() {
    var arrayList: ArrayList<Node> = ArrayList<Node>()

    arrayList.add(Node("Room 1", 30, 30, 0, NodeType.ROOM, mutableListOf()))
    arrayList.add(Node("Room 2", 30, 30, 0, NodeType.ROOM, mutableListOf()))
    arrayList.add(Node("Room 3", 30, 30, 0, NodeType.ROOM, mutableListOf()))
    arrayList.add(Node("Room 4", 30, 30, 0, NodeType.ROOM, mutableListOf()))
    arrayList.add(Node("Room 5", 30, 30, 0, NodeType.ROOM, mutableListOf()))


    //SearchBar(Modifier, "", {}, arrayList)

    PreviewNavigationSearchBar(Modifier,
        Node("Room 1", 30, 30, 0, NodeType.ROOM, mutableListOf()),
        UIState.NAVIGATION_PREVIEW,
        {},
        {}
    )

    //NavigationTopBar (Modifier,
    //    Node("Room 1", 30, 30, 0, NodeType.ROOM, mutableListOf()),
    //    UIState.NAVIGATING,
    //    {}
    //)
}

