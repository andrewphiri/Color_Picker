package com.drew.and.colorpicker.Navigation

import CapturedImageScreen
import CapturedImageScreenDestination
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.drew.and.colorpicker.CameraLivePreviewCaptureScreenDestination
import com.drew.and.colorpicker.CameraLivePreviewWithCapture
import com.drew.and.colorpicker.ViewModel.ColorViewModel

@Composable
fun ColorAppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
 val colorViewModel: ColorViewModel = hiltViewModel()
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = CameraLivePreviewCaptureScreenDestination
    ) {
        composable<CameraLivePreviewCaptureScreenDestination> {
            CameraLivePreviewWithCapture(
                colorViewModel = colorViewModel,
                navigateToCapturedImage = {
                    navController.navigate(CapturedImageScreenDestination)
                }
            )
            }
        composable<CapturedImageScreenDestination> {
            CapturedImageScreen(
                colorViewModel = colorViewModel,
                navigateBack = {
                    navController.navigateUp()
                })

        }
    }
}