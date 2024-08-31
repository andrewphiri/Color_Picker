package com.drew.and.colorpicker

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.hilt.navigation.compose.hiltViewModel
import com.drew.and.colorpicker.ViewModel.ColorViewModel
import com.drew.and.colorpicker.data.ColorEntity
import kotlinx.serialization.Serializable

@Serializable
object SavedColorsScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedColorsScreen(
    modifier: Modifier = Modifier,
    colorViewModel: ColorViewModel = hiltViewModel(),
    navigateBack: () -> Unit
) {
    val colors by colorViewModel.allColors.collectAsState()
    Log.d("SavedColorsScreen", "colors: $colors")
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopAppBar(
            title = {
                Text(text = "Saved Colors")
            },
            navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back"
                    )
                }
            }
        )
        if (colors.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "No colors saved",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {

            SavedColorsList(
                modifier = Modifier.padding(16.dp),
                colors = colors)
        }
    }
}


@Composable
fun SavedColorsList(
    modifier: Modifier = Modifier,
    colors: List<ColorEntity>
) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(colors.size) { index ->
            ColorItem(color = colors[index])
        }
    }
}
@Composable
fun ColorItem(
    modifier: Modifier = Modifier,
    color: ColorEntity
) {
    Row(
      modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
       Box(
           modifier = Modifier
               .size(100.dp)
               .background(color = color.hexCode.hexToColor())
       )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = color.name)
            Text(text = color.hexCode)
            Text(text = color.rgbCode)
        }
    }
}

fun String.hexToColor(): Color {
    val hexColor = this.replace("#", "")
    val red = hexColor.substring(0, 2).toInt(16) / 255f
    val green = hexColor.substring(2, 4).toInt(16) / 255f
    val blue = hexColor.substring(4, 6).toInt(16)/ 255f
    return Color(red, green, blue)
}