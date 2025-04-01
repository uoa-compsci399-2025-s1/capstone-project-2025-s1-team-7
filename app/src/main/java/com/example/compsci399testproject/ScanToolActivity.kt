package com.example.compsci399testproject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScanTool() {
    Column(modifier = Modifier
        .fillMaxSize()
        .background(colorResource(id = R.color.lighter_grey))
        .padding(0.dp, 120.dp, 0.dp, 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Finding your location",
            color = colorResource(id = R.color.dark_blue),
            fontWeight = FontWeight(600),
            fontFamily = FontFamily.SansSerif,
            style = TextStyle(
                fontSize = 24.sp
            ),
            modifier = Modifier.padding(0.dp, 50.dp, 0.dp, 0.dp)
        )

        Box(
            modifier = Modifier
                .padding(0.dp, 200.dp, 0.dp, 0.dp)
                .size(14.dp)
                .clip(CircleShape)
                .background(color = colorResource(id = R.color.dark_blue))
        )
    }
}