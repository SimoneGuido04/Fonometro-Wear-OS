package com.simon.fonometrowearos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.simon.fonometrowearos.ui.theme.FonometroTheme
import com.simon.fonometrowearos.ui.theme.NeonGreen
import com.simon.fonometrowearos.ui.theme.NeonAmber
import com.simon.fonometrowearos.ui.theme.NeonRed
import com.simon.fonometrowearos.ui.theme.SurfaceGray
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val audioService = AudioRecorderService()
        val repository = DecibelRepository(audioService)
        val viewModelFactory = DecibelViewModelFactory(repository)

        setContent {
            FonometroTheme {
                WearApp(viewModelFactory)
            }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
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

@Composable
fun DecibelGauge(decibel: Float) {
    val animatedDecibel by animateFloatAsState(
        targetValue = decibel,
        animationSpec = tween(durationMillis = 300),
        label = "Decibel Animation"
    )

    val maxDecibel = 120f
    val sweepAngle = 270f
    val startAngle = 135f

    // Calculate progress (0.0 to 1.0)
    val progress = (animatedDecibel / maxDecibel).coerceIn(0f, 1f)
    
    // Determine color based on level
    val gaugeColor = when {
        animatedDecibel < 60 -> NeonGreen
        animatedDecibel < 85 -> NeonAmber
        else -> NeonRed
    }

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val strokeWidth = 15.dp.toPx()
            
            // Draw background track
            drawArc(
                color = SurfaceGray,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Draw progress arc with gradient
            drawArc(
                brush = Brush.sweepGradient(
                    0.0f to NeonGreen,
                    0.5f to NeonAmber,
                    1.0f to NeonRed,
                    center = center
                ),
                startAngle = startAngle,
                sweepAngle = sweepAngle * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
             // Draw indicator glow
             // Very subtle glow effect simulated by drawing a wider, transparent arc behind the main one? 
             // Or just simple indicator. Material 3 is clean. Let's stick to the gradient arc.
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%.1f", animatedDecibel),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 52.sp,
                    color = gaugeColor
                )
            )
            Text(
                text = "dB",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )
        }
    }
}


@Composable
fun DecibelDisplay(decibel: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        DecibelGauge(decibel)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Optional status text
        val statusText = when {
            decibel < 60 -> "Safe"
            decibel < 85 -> "Moderate"
            else -> "Loud"
        }
        
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "Microphone Needed",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "To measure noise levels, we need access to your microphone.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Grant Access", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
