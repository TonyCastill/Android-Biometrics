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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
                    //Access the flow to collect events
                    val biometricResult by promptManager.promptResults.collectAsState(initial = null)

                    // In order to fire and launch the activity
                    // to set a biometric or enroll it
                    val enrollLauncher = rememberLauncherForActivityResult(
                        // We fire the StartActivityFor result to pop up an
                        // activity where the user can choose a pattern
                        contract = ActivityResultContracts.StartActivityForResult(),
                        onResult = {
                            println("Activity result: $it")
                        }
                    )

                    // To prompt the user to set a biometric
                    // signature in case it hasn't been set
                    LaunchedEffect(biometricResult) {
                        if(biometricResult is BiometricResult.AuthenticationNotSet){
                            if(Build.VERSION.SDK_INT >= 30){
                                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                    putExtra(
                                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                                        // Alt + Enter -> import that
                                    )
                                }
                                // Fire the activity
                                enrollLauncher.launch(enrollIntent)
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        //Simple button
                        Button(onClick = {
                            promptManager.showBiometricPrompt(
                                title = "Sample prompt",
                                description = "Sample prompt description"
                            )
                        }) {
                            Text(text = "Authenticate")
                        }
                        biometricResult?.let{ //like an if after the event
                            result -> // Do something according to the result
                            Text(
                                text = when(result){
                                    //Alt + Enter -> Add remaining branches

                                    //Select the first sentence before the dot
                                    // Alt+ Enter -> import memembers from ....
                                    is BiometricResult.AuthenticationError -> {
                                        result.error
                                    }
                                    BiometricResult.AuthenticationFailed -> {
                                        "Authentication failed"
                                    }
                                    BiometricResult.AuthenticationNotSet -> {
                                        "Authentication not set"
                                    }
                                    BiometricResult.AuthenticationSuccess -> {
                                        "Authentication success"
                                    }
                                    BiometricResult.FeatureUnavailable -> {
                                        "Feature unavailable"
                                    }
                                    BiometricResult.HardwareUnavailable -> {
                                        "Hardware unavailable"
                                    }
                                }
                            )

                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BiometricsTheme {
        Greeting("Android")
    }
}