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

class  BiometricPromptManager(
    /**
     * This class allows us to show this biometric prompt,
     * to configure it and also to observe its results
     *
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
    /**
     * Channel
     * interface Channel<E> : SendChannel<E> , ReceiveChannel<E>
     * Channel is a non-blocking primitive for communication between
     * a sender (via SendChannel) and a receiver (via ReceiveChannel).
     * Conceptually, a channel is similar to Java's java.util.concurrent.BlockingQueue,
     * but it has suspending operations instead of blocking ones and can be closed.
     *
     * We can send the events, receive them and observe them
     */
    // In order to return a result to the UI, we use a channel
    // We receive and send BiometricResult objects
    private val resultChannel = Channel<BiometricResult>()
    // We listen to the result and we observe it
    // An attribute that can be accessed from external classes
    val promptResults = resultChannel.receiveAsFlow()

    fun showBiometricPrompt( //Configure Biometric Prompt
        title: String,
        description: String
    ) {
        //Set an activity as the context of the biometric manager
        val manager = BiometricManager.from(activity)

        //Authenticators are ways how we can authenticate user with the biometric prompt
        // by using 'or', we can set multiple ways the user can authenticate itself
        //val authenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        val authenticators = if (Build.VERSION.SDK_INT >= 30) {
            /**
             * Build.VERSION.SDK_INT: This checks the Android SDK version of the device at runtime.
             * Build.VERSION.SDK_INT returns the API level of the current Android operating system.
             *
             * >=30: Android API level 30 corresponds to Android 11 (R). If the device is running
             * Android 11 or higher, this block of code will execute the logic inside the if statement.
             */
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
            /**
             * BIOMERIC_STRONG -> a biometric quality
             * DEVICE_CREDENTIAL -> a pattern or a passkey
             */
        } else BIOMETRIC_STRONG

        // Building the prompt
        val promptInfo = PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setAllowedAuthenticators(authenticators)

        //True -> Gives the biometric manager a hint that users can confirm they want to authenticate
        // meaning the user needs to click something to begin the scan
        //False -> the biometric evaluation begins immediately
        //.setConfirmationRequired(false)

        if (Build.VERSION.SDK_INT < 30) {
            promptInfo.setNegativeButtonText("Cancel")
        }


        when (manager.canAuthenticate(authenticators)) {
            // We bind the received options to the ones we've created with the sealed class
            // Here we send the results
            /**
             * We separate these options from the ones found in the prompt
             * because these options prevent the prompt from initially
             * display itself
             */

            //Hardware unavailable
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                resultChannel.trySend(BiometricResult.HardwareUnavailable) //We send the event
                return //To ask the user to try again, it does not continue here
            }

            // Feature unavailable, there's no hardware that can support the feature
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                resultChannel.trySend(BiometricResult.FeatureUnavailable)
                return
            }

            // Biometrics haven't been set
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                resultChannel.trySend(BiometricResult.AuthenticationNotSet)
                return
            }

            //When security update is available
            /**
             * Unit
             * The type with only one value: the Unit object.
             * This type corresponds to the void type in Java.
             */
            else -> Unit
        }

        //The actual prompt
        val prompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                /**
                 * Ctrl + O ->
                 * Override or select:
                 * onAuthenticationError
                 * onAuthenticationSucceeeded
                 * onAuthenticationFailed
                 */

                // Something failed with the biometricAuth-mechanisim itself
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // The only option that sends a string
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

        // Perform the autentication
        /**
         * Here we can pass a crypto object to provide security
         * to decrypt and encrypt that information
         */
        prompt.authenticate(promptInfo.build())
    }

    /**
     * Sealed interface
     *
     * Sealed classes and interfaces provide controlled
     * inheritance of your class hierarchies. All direct subclasses
     * of a sealed class are known at compile time. No other subclasses
     * may appear outside the module and package within which the sealed
     * class is defined.
     *
     * The same logic applies to sealed interfaces and their
     * implementations: once a module with a sealed interface
     * is compiled, no new implementations can be created.
     *
     * When you combine sealed classes and interfaces with the when
     * expression, you can cover the behavior of all possible subclasses
     * and ensure that no new subclasses are created to affect your code adversely.
     *
     * Enum classes
     *
     * enum class is a special type of class used to define a set of constants.
     * It is often used when you need a predefined set of possible values for a
     * particular variable, like states, types, or categories. Enum classes can
     * contain constants, properties, and methods.
     *
     *
     * Since in this example we need objects to be able to behave different from other
     * objects (use arguments or not), we used a sealed interface
     */
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