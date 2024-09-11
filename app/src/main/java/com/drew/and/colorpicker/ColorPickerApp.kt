package com.drew.and.colorpicker

import android.Manifest
import android.content.pm.PackageManager
import android.view.Window
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.drew.and.colorpicker.Navigation.ColorAppNavHost

@Composable
fun ColorPickerApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    window: Window
) {


    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        ColorAppNavHost(
            navController = navController,
            window = window
        )
    }
}

@Composable
fun CameraPermissionRequest(modifier: Modifier = Modifier, window: Window) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    if (hasCameraPermission) {
        ColorPickerApp(
            modifier = modifier,
            window = window
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Camera permission is required to use this app.",
                textAlign = TextAlign.Center
            )
        }
    }
}