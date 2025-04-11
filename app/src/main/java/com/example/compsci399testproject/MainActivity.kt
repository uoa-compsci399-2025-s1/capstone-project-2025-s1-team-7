package com.example.compsci399testproject

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compsci399testproject.ui.theme.COMPSCI399TestProjectTheme
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sign In ------------------------------
        val credentialManager = CredentialManager.create(this)

        val getSignInWithGoogle  = GetSignInWithGoogleOption.Builder(
            "145717740654-30d6vl1g1nh2e0v5ehfvl9bbijoc18i9.apps.googleusercontent.com"
        )
        .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(getSignInWithGoogle)
            .build()

        var account: GoogleIdTokenCredential? = null

        CoroutineScope(Dispatchers.Main).launch {
            account = signIn(this@MainActivity, credentialManager, request)
        }

        print("""
            
            Main
        
            $account
        
        """)

        // -------------------------------------

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "HomeMenu", builder = {
                composable("HomeMenu") {
                    Menu(navController)
                }

                composable("WifiSignals") {
                    WifiSignalList()
                }

                composable("MapLocation") {
                    FindingLocation()
                }

                composable("ScanTool") {
                    ScanTool(account = account)
                }
            })
        }
    }
}

@Composable
fun Menu(navController: NavController)
{
    Column(modifier = Modifier.fillMaxSize().background(color = colorResource(id = R.color.lighter_grey)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        MenuButton(onClick = {navController.navigate("WifiSignals")}, text = "Wi-Fi Signals")
        MenuButton(onClick = {navController.navigate("MapLocation")}, text = "Map Location")
        MenuButton(onClick = {navController.navigate("ScanTool")}, text = "Scan Tool")
    }
}

@Composable
fun MenuButton(onClick: () -> Unit, text: String) {
    Button(onClick = { onClick()},
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.darker_white))) {
        Text(text, color = colorResource(R.color.light_blue))
    }
}

@Preview
@Composable
fun PreviewFun() {
    Button(onClick = {},
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.darker_white))
    ) {
        Text("aaaa", color = colorResource(R.color.light_blue))
    }
}