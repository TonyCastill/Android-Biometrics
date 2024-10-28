package com.example.biometrics

import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.biometrics.BiometricPromptManager.*
import com.example.biometrics.ui.theme.BiometricsTheme

/**
 * AppCompatActivity() -> Base class for activities that wish
 * to use some of the newer platform features on older Android devices.
 *
 * ComponentActivity() -> Base class for activities that enables composition of higher level components.
 * Rather than all functionality being built directly into this class, only the minimal set of lower
 * level building blocks are included. Higher level components can then be used as needed without
 * enforcing a deep Activity class hierarchy or strong coupling between components.
 */
class MainActivity : AppCompatActivity() {



    private  val promptManager  by lazy{
        /**
         * by lazy means we initialize the value as soon
         * as we access it the fist time
         */
        BiometricPromptManager(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiometricsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // ------------------- MAIN CONTENT -------------------
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        //Simple button
                        /*Button(onClick = {
                            promptManager.showBiometricPrompt(
                                title = "Sample prompt",
                                description = "Sample prompt description"
                            )
                        }) {
                            Text(text = "Authenticate")
                        }*/
                        ComposeMultiScreenApp(promptManager)


                    }
                }
            }
        }
    }
}

@Composable
fun ComposeMultiScreenApp(promptManager:BiometricPromptManager){
    val navController = rememberNavController()
    Surface(color= MaterialTheme.colorScheme.surface){
        SetupNavGraph(navController=navController,promptManager) //función propia //crea el grafo recordando el navcontroller donde nos encontramos
    }
}

@Composable
fun SetupNavGraph(navController: NavHostController,promptManager:BiometricPromptManager) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController,promptManager) }
        composable("profile") { ProfileScreen(navController) }

    }
}