package com.example.compsci399testproject

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.compsci399testproject.viewmodel.MapViewModel

@Composable
fun MainAppEntry() {
    Text(
        text = "Main App",
        color = colorResource(id = R.color.dark_blue),
        fontWeight = FontWeight(600),
        fontFamily = FontFamily.SansSerif,
        style = TextStyle(
            fontSize = 24.sp
        ),
        modifier = Modifier.padding(0.dp, 50.dp, 0.dp, 0.dp)
    )
}

@Composable
fun MapView(viewModel: MapViewModel){

}