package com.example.compsci399testproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compsci399testproject.ui.theme.COMPSCI399TestProjectTheme

@Composable
fun WifiSignalList()
{
    Column(modifier = Modifier.fillMaxWidth().background(color = colorResource(id = R.color.lighter_grey)),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text(text = "Wi-Fi Scans",
            color = colorResource(id = R.color.dark_grey),
            fontWeight = FontWeight(600),
            style = TextStyle(
                fontSize = 38.sp
            ),
            modifier = Modifier.padding(0.dp, 60.dp, 0.dp, 6.dp),
            textAlign = TextAlign.Right
        )
        Text(text = "Last scan was 3 seconds ago",
            color = colorResource(id = R.color.dark_grey),
            fontWeight = FontWeight(400),
            style = TextStyle(
                fontSize = 20.sp
            ),
            modifier = Modifier.padding(0.dp, 6.dp, 0.dp, 36.dp),
            textAlign = TextAlign.Right
        )


        LazyColumn(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            items(20) {
                WifiSignalBox("UoA WIFI", "MAC: 1d:1d:1d:1d:1d:1d", "-50dBm")
            }
        }
    }
}

@Composable
fun WifiSignalBox(SSID: String, MAC: String, signalStrength: String) {
    Column(modifier = Modifier.fillMaxWidth()
        .padding(24.dp, 0.dp, 24.dp, 10.dp)
        .background(color = colorResource(id = R.color.light_grey), shape = RoundedCornerShape(14.dp))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column() {
                WifiSignalBoxText(SSID, TextAlign.Left, Modifier.padding(16.dp, 10.dp, 0.dp, 6.dp))
                WifiSignalBoxText(MAC, TextAlign.Left, Modifier.padding(16.dp, 2.dp, 0.dp, 10.dp))
            }
            WifiSignalBoxText(signalStrength, TextAlign.Right, Modifier.padding(0.dp, 6.dp, 16.dp, 6.dp).fillMaxWidth().fillMaxHeight())
        }
    }
}

@Composable
fun WifiSignalBoxText(s: String, ta: TextAlign, modifier: Modifier) {
    Text(text = s,
        color = colorResource(id = R.color.dark_grey),
        fontWeight = FontWeight(600),
        fontFamily = FontFamily.SansSerif,
        style = TextStyle(
            fontSize = 16.sp
        ),
        modifier = modifier,
        textAlign = ta
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewFunc() {
    WifiSignalList()
}