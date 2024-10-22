package com.example.biometrics

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class BiometricPromptManager(
    /**
     * The Biometric manager needs an activity
     * to show its own fragment, like a little sheet
     * that pops up, which contains the security information
     *
     *
     * We can only show this in activities that are called
     * AppCompat activities. So MainActivity needs to be:
     *
     * class MainActivity : AppCompatActivity() {...}
     *
     *
     */
    private val activity : AppCompatActivity
) {

    private val resultChannel = Channel<BiometricResult>()
    val promptResults = resultChannel.receiveAsFlow()

    fun showBiometricPrompt( //Configure Biometric Prompt
        title: String,
        description: String
    ){
        //Set an activity as the context of the biometric manager
        val manager = BiometricManager.from(activity)
        //Authenticators are ways how we can authenticate user with the biometric prompt
        // by using 'or', we can set multiple ways the user can authenticate itself
        //val authenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL


        val authenticators = if(Build.VERSION.SDK_INT>=30){
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        }else BIOMETRIC_STRONG


        val promptInfo = PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setAllowedAuthenticators(authenticators)
            //True -> Gives the biometric manager a hint that users can confirm they want to authenticate
            // meaning the user needs to click something to begin the scan
            //False -> the biometric evaluation begins immediately
            //.setConfirmationRequired(false)

        if(Build.VERSION.SDK_INT < 30){
            promptInfo.setNegativeButtonText("Cancel")
        }


        when(manager.canAuthenticate(authenticators)){
            //We bind the received options to the ones we've created with the sealed class
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                resultChannel.trySend(BiometricResult.HardwareUnavailable) //We send the event
                return //To ask the user to try again
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->{
                resultChannel.trySend(BiometricResult.FeatureUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->{
                resultChannel.trySend(BiometricResult.AuthenticationNotSet)
                return
            }
            else  -> Unit
        }

        val prompt  = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback(){
                //Ctrl + O
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    resultChannel.trySend(BiometricResult.AuthenticationError(errString.toString()))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    resultChannel.trySend(BiometricResult.AuthenticationSuccess)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    resultChannel.trySend(BiometricResult.AuthenticationFailed)
                }
            }
        )

        prompt.authenticate(promptInfo.build())
    }

    sealed interface BiometricResult{ // Posible results
        //In case the evaluation can be perfomed, but hardware is busy
        data object HardwareUnavailable : BiometricResult

        //Biometric evaluation isn't avaialable in the device
        data object FeatureUnavailable : BiometricResult

        //Failed not beacause the users fault
        data class AuthenticationError(val error: String): BiometricResult

        //The wrong face or the wrong finger
        data object AuthenticationFailed : BiometricResult

        // Correctly recognized
        data object AuthenticationSuccess : BiometricResult

        //In case the user doesn't have set up authentication
        data object AuthenticationNotSet : BiometricResult
    }
}