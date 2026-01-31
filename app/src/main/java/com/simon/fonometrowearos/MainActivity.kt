package com.simon.fonometrowearos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val audioService = AudioRecorderService()
        val repository = DecibelRepository(audioService)
        val viewModelFactory = DecibelViewModelFactory(repository)

        setContent {
            WearApp(viewModelFactory)
        }
    }
}

@Composable
fun WearApp(viewModelFactory: DecibelViewModelFactory) {
    val viewModel: DecibelViewModel = viewModel(factory = viewModelFactory)
    val decibel by viewModel.decibelFlow.collectAsState()

    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasPermission = isGranted
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            viewModel.startListening()
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasPermission) {
                DecibelDisplay(decibel)
            } else {
                PermissionRequestScreen {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }
}

@Composable
fun DecibelGauge(decibel: Float) {
    val color = when {
        decibel < 60 -> Color(0xFF4CAF50) // Green for safe levels
        decibel < 85 -> Color(0xFFFFC107) // Amber for moderately loud
        else -> Color(0xFFF44336)         // Red for very loud
    }

    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            startAngle = 135f,
            endAngle = 45f,
            progress = decibel / 120f,
            modifier = Modifier.size(180.dp),
            trackColor = color.copy(alpha = 0.3f),
            strokeWidth = 12.dp,
            indicatorColor = color
        )
        Text(
            text = String.format("%.1f", decibel),
            style = MaterialTheme.typography.display1,
            fontSize = 48.sp,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun DecibelDisplay(decibel: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Noise Level",
            style = MaterialTheme.typography.title2,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        DecibelGauge(decibel)
        Text(
            text = "dB",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

@Composable
fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp).fillMaxSize()
    ) {
        Text(
            text = "Microphone access is required to measure noise levels.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}
