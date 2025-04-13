package com.example.compsci399testproject

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.compsci399testproject.utils.GoogleSheetsService
import com.example.compsci399testproject.viewmodel.WifiViewModel
import kotlinx.coroutines.delay


@Composable
fun ScanTool(wifiViewModel: WifiViewModel, googleSheetsService: GoogleSheetsService) {

    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var floorNumber by remember { mutableStateOf("") }

    val lastScanTime by wifiViewModel.lastScanTime

    var timeSinceLastScan by remember { mutableStateOf("Never") }
    var bestSignal by remember { mutableStateOf("") }
    var timeSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(lastScanTime) {
        while (true) {
            val now = System.currentTimeMillis()
            timeSinceLastScan = if (lastScanTime != null) {
                val seconds = (now - lastScanTime!!) / 1000.0
                timeSeconds = seconds.toInt()
                "Last scanned %.1f seconds ago".format(seconds)
            } else {
                "Last scanned: never"
            }
            delay(100)
        }
    }


    //Fun and improved.
    val introMessage =
        if (timeSeconds > 60) {
            "Hey, you. Finally awake. You were trying to cross the border, right? Walked into that Imperial ambush, same as us."
        } else {
            "Where are you?"
        }

    //Error handling.
    val context = LocalContext.current
    val showToast: (String) -> Unit = { msg ->
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(colorResource(id = R.color.lighter_grey))
        .padding(0.dp, 10.dp, 0.dp, 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = introMessage,
            color = colorResource(id = R.color.dark_blue),
            fontWeight = FontWeight(600),
            fontFamily = FontFamily.SansSerif,
            style = TextStyle(
                fontSize = 24.sp
            ),
            modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 0.dp)
        )


        val wifiSignals = wifiViewModel.getResults()
        val strongestSignal = wifiSignals.maxByOrNull { it.level }
        bestSignal = if (strongestSignal != null) {
            """Best:
                SSID: ${strongestSignal.SSID}
                Signal Strength: ${strongestSignal.level} dBm
            """.trimIndent()
        } else {
            "No WiFi signals found."
        }


        Text(
            text = bestSignal,
            style = TextStyle(fontSize = 16.sp),
            color = colorResource(id = R.color.dark_blue),
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp)
        )


        Text(
            text = timeSinceLastScan,
            style = TextStyle(fontSize = 16.sp),
            color = colorResource(id = R.color.dark_blue),
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp)
        )

        Spacer(modifier = Modifier.height(0.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal=32.dp)
        ) {
            OutlinedTextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text("Longitude (X)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = { Text("Latitude (Y)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = floorNumber,
                onValueChange = { floorNumber = it },
                label = { Text("Room Number") },
                modifier = Modifier.fillMaxWidth()
            )
        }


        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = {
                captureData(
                    context,
                    latitude,
                    longitude,
                    floorNumber,
                    showToast,
                    wifiViewModel,
                    googleSheetsService
                )
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.dark_blue),
                contentColor = colorResource(id = R.color.darker_white)
            ),
            modifier = Modifier
                .height(50.dp)
                .width(250.dp)
        ) {
            Text(text = "Capture",
                style = TextStyle(fontSize = 24.sp)
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        when (googleSheetsService.successOrFail) {
            "Success" -> {
                Text(
                    text = googleSheetsService.successOrFail!!,
                    color = Color.Green,
                    fontWeight = FontWeight(600),
                    fontFamily = FontFamily.SansSerif,
                    style = TextStyle(
                        fontSize = 30.sp
                    ),
                    modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 0.dp)
                )
            }
            "Fail" -> {
                Text(
                    text = googleSheetsService.successOrFail!!,
                    color = Color.Red,
                    fontWeight = FontWeight(600),
                    fontFamily = FontFamily.SansSerif,
                    style = TextStyle(
                        fontSize = 30.sp
                    ),
                    modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 0.dp)
                )
            }
        }
    }


}

fun captureData(
    context: Context,
    latitudeInput: String,
    longitudeInput: String,
    floorNumberInput: String,
    onError: (String) -> Unit,
    wifiViewModel: WifiViewModel,
    googleSheetsService: GoogleSheetsService
) {
    val longitude: Float = longitudeInput.toFloatOrNull() ?: run {
        onError("Invalid Longitude. Please enter a number.")
        return
    }

    val latitude: Float = latitudeInput.toFloatOrNull() ?: run {
        onError("Invalid Latitude. Please enter a number.")
        return
    }

    val floor = floorNumberInput.toIntOrNull() ?: run {
        onError("Please enter a floor number.")
        return
    }

    wifiViewModel.scan()

    Handler(Looper.getMainLooper()).postDelayed({
        val wifiSignals = wifiViewModel.getResults()
        wifiViewModel.updateScanResults(wifiSignals)

        Log.d("wifiScan", "Scan results: $wifiSignals")

        if (wifiSignals.isNotEmpty()) {
            val strongestSignal = wifiSignals.maxByOrNull { it.level }
            val scanTime = System.currentTimeMillis()

            val signalDetails = """
                Best:
                SSID: ${strongestSignal?.SSID}
                Signal Strength: ${strongestSignal?.level} dBm
                Last Scan Time: $scanTime
            """.trimIndent()

            Toast.makeText(context, signalDetails, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "No WiFi signals found.", Toast.LENGTH_SHORT).show()
        }
    }, 8000)


    // API takes a list of strings -> values: [ ["latitude", "longitude", "floorNumberInput",...] ]
    // Google Sheets should differentiate between value types when input

    val positionInfoToList = listOf<String>(latitudeInput, longitudeInput, floorNumberInput, "wifiSignals")
    googleSheetsService.storePositionInformation(positionInfoToList)

}