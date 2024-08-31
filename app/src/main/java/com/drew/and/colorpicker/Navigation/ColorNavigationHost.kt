package com.drew.and.colorpicker.Navigation

import CapturedImageScreen
import CapturedImageScreenDestination
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.drew.and.colorpicker.CameraLivePreviewCaptureScreenDestination
import com.drew.and.colorpicker.CameraLivePreviewWithCapture
import com.drew.and.colorpicker.SavedColorsScreen
import com.drew.and.colorpicker.SavedColorsScreenDestination
import com.drew.and.colorpicker.ViewModel.ColorViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
                },
                navigateToSavedColorsList = {
                    navController.navigate(SavedColorsScreenDestination)
                }
            )
            }
        composable<CapturedImageScreenDestination> {
            CapturedImageScreen(
                colorViewModel = colorViewModel,
                navigateBack = {
                    navController.navigateUp()
                },
                navigateToSavedColorsList = {
                    navController.navigate(SavedColorsScreenDestination)
                }
            )
        }
        composable<SavedColorsScreenDestination> {
            SavedColorsScreen(
                navigateBack = { navController.navigateUp() }
            )
        }
    }
}