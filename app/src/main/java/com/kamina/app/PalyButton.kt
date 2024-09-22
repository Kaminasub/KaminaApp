package com.kamina.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext

@Composable
fun PlayButton(navController: NavController) {
    val context = LocalContext.current
    Button(onClick = {
        // Start EmbeddedVideoPage for testing
        val intent = Intent(context, EmbeddedVideoPage::class.java)
        context.startActivity(intent)
    }) {
        BasicText("Play Movie (Test)")
    }
}
