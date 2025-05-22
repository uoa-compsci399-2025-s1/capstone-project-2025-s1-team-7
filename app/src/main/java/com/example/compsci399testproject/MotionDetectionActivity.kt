package com.example.compsci399testproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.compsci399testproject.sensors.RotationSensorService
import com.example.compsci399testproject.sensors.StepDetectionService
import kotlinx.coroutines.*

//import android.os.StrictMode
//import java.io.OutputStreamWriter
//import java.net.HttpURLConnection
//import java.net.URL
//import org.json.JSONObject


@Composable
fun SensorTool() {
    val context = LocalContext.current

    // Step detection method toggle
    var useRhythm by remember { mutableStateOf(true) }
    var stepPulse by remember { mutableStateOf(false) }
    // Rotation service
    val rotationService = remember { RotationSensorService(context) }

    // Step detection
    val coroutineScope = rememberCoroutineScope()
    val stepService = remember {
        StepDetectionService(context, {
            coroutineScope.launch {
                stepPulse = true
                delay(1000)
                stepPulse = false
            }
        }, useRhythm)
    }
//    println("Heading: ${rotationService.azimuthCompass.toInt()}")
//    println("stepPulse: $stepPulse")
//    println()

//    LaunchedEffect(rotationService.azimuthCompass, stepPulse) {
//        withContext(Dispatchers.IO) {
////            sendJsonToServer(heading = rotationService.azimuthCompass.toInt(), step = stepPulse)
//            sendJsonToServer(heading = 1,  step = true)
//        }
//        delay(1500)
//    }

    // Start/stop listeners
    DisposableEffect(Unit) {
        rotationService.startListening()
        stepService.startListening()
        onDispose {
            rotationService.stopListening()
            stepService.stopListening()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Compass image (rotates with azimuthCompass)
        Text("Heading: ${rotationService.azimuthCompass.toInt()}°", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.compass),
            contentDescription = "Compass",
            modifier = Modifier
                .size(150.dp)
                .rotate(rotationService.azimuthCompass)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Toggle detection strategy
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Use Rhythm Detection")
            Switch(
                checked = useRhythm,
                onCheckedChange = { useRhythm = it }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Step pulse visual (glowing dot)
        Box(
            modifier = Modifier
                .size(64.dp)
                .alpha(if (stepPulse) 1f else 0f)
                .background(color = Color.Green, shape = CircleShape)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Step Detected", modifier = Modifier.alpha(if (stepPulse) 1f else 0f))
    }
}

// Straight from ChatGPT
//fun sendJsonToServer(heading: Int, step: Boolean) {
//    // Optional: Allow network on main thread for testing (not recommended for production)
////    val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
////    StrictMode.setThreadPolicy(policy)
//
//    println("Sending POST: heading=$heading, step=$step")
//
//    // Doesn't work -> Edit from next day: I think I used wrong IP but i already removed port from Firewall so I don't wanna try again
//    val url = URL("http://10.x.x.x:x0x0")  // Replace with your server's IP
//    val jsonBody = JSONObject()
//    jsonBody.put("heading", heading)
//    jsonBody.put("step", step)
//
//    with(url.openConnection() as HttpURLConnection) {
//        requestMethod = "POST"
//        setRequestProperty("Content-Type", "application/json")
//        doOutput = true
//
//        OutputStreamWriter(outputStream).use { writer ->
//            writer.write(jsonBody.toString())
//        }
//
//        // Read response
//        val responseCode = responseCode
//        println("Response Code: $responseCode")
//        inputStream.bufferedReader().use {
//            val response = it.readText()
//            println("Response from server: $response")
//        }
//    }
//}