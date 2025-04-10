package com.example.compsci399testproject

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight


@Composable
fun ScanTool() {

    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var floorNumber by remember { mutableStateOf("") }

    //Fun.
    val introMessage = if ((0..10).random() == 10){
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
        .padding(0.dp, 200.dp, 0.dp, 0.dp),
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
            modifier = Modifier.padding(0.dp, 50.dp, 0.dp, 0.dp)
        )

        Spacer(modifier = Modifier.height(100.dp))

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
            onClick = { captureData(context, latitude, longitude, floorNumber, showToast) },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.dark_blue),
                contentColor = colorResource(id = R.color.darker_white)
            ),
            modifier = Modifier.height(50.dp).width(250.dp)
        ) {
            Text(text = "Capture",
                style = TextStyle(fontSize = 24.sp)
            )
        }
    }
}

fun captureData(
    context: Context,
    latitudeInput: String,
    longitudeInput: String,
    floorNumberInput: String,
    onError: (String) -> Unit
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

    // val wifiSignals = getWifiSignals() - Connor to implement.

    //storePositionInformation() - Renesh to implement.
}