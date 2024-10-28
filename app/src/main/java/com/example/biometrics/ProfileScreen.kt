package com.example.biometrics

import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ProfileScreen(navController: NavController){
    /*Variables*/
    var username by remember { mutableStateOf("username._.") }
    var email by remember { mutableStateOf("user1@gmail.com") }
    var phone by remember { mutableStateOf("(+52) 449 212 1234") }
    var country by remember {mutableStateOf("Mexico") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ){
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "Profile icon",
            modifier=Modifier.size(100.dp)
        )

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("User") }
        )
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        TextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") }
        )
        TextField(
            value = country,
            onValueChange = { country = it },
            label = { Text("Country") }
        )
        Button(onClick = { navController.popBackStack() }) {
            Text("Return")
        }

    }

}