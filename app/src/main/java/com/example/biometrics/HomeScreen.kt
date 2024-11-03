package com.example.biometrics

import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.biometrics.BiometricPromptManager.BiometricResult

@Composable
fun HomeScreen(navController : NavController,promptManager:BiometricPromptManager){

    Column(
        modifier = Modifier.fillMaxSize()
    ){
        LazyColumn{
            item {
                profileRow(navController,promptManager)

            }
            items(2){
                Image(
                    painter = painterResource(id = R.drawable.android),
                    contentDescription = "android logo",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

    }
}

@Composable
fun profileRow(navController : NavController,promptManager:BiometricPromptManager){

    //----------------- VARIABLES -------------------------
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
    // ----------------- LAUNCH BIOMETRIC SET ---------------------
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
    //-------------------------------------------------------
    Row(
        modifier = Modifier
            .padding(10.dp)
            .border(
                width = 1.dp,
                color = Color.Black,
                shape = RoundedCornerShape(12.dp)
            )
            .fillMaxWidth(),

        horizontalArrangement = Arrangement.SpaceBetween
    ){
        profileInfo()
        TextButton(
            onClick = {
                promptManager.showBiometricPrompt(
                    title = "Authenticate",
                    description = "Please, identify yourself"
                )
            }
        ) {
            Text("Edit")
        }

        // ---------------- EVENT ON BIOMETRICS ------------------
        biometricResult?.let{ //like an if after the event
                result -> // Do something according to the result
            when(result){
                    //Alt + Enter -> Add remaining branches
                    //Select the first sentence before the dot
                    // Alt+ Enter -> import memembers from ....
                    is BiometricResult.AuthenticationSuccess -> {
                        /**
                         * Your code is on the right track! To ensure smooth navigation,
                         * consider handling any potential issues with the navigation
                         * process and managing any redundant navigation calls that might
                         * happen due to UI recomposition.
                         *
                         * This checks the current route before navigating to "profile,"
                         * which helps prevent redundant navigation due to recomposition.
                         */
                        if (navController.currentDestination?.route != "profile") {
                            navController.navigate("profile")
                        }
                    }
                    else ->{
                        // DISPALY ERROR
                        //Toast.makeText(context, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show()
                    }

                }


        }
        // --------------------------------------------------------
    }
}

@Composable
fun profileInfo(){
    Row(
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "Profile icon",
            modifier=Modifier.size(50.dp)

        )
        Text(
            "Profile",
            modifier = Modifier.padding(horizontal = 10.dp),
            fontSize = 18.sp
        )
    }
}

@Preview(showBackground=true)
@Composable
fun HomeScreenPreview(){
    //HomeScreen(navController = rememberNavController(),BiometricPromptManager(this))
}
