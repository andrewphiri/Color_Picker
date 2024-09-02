package com.drew.and.colorpicker


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopAppBar(
            modifier = Modifier.padding(top = 16.dp),
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
                colors = colors,
                onDelete = { color ->
                    colorViewModel.deleteColor(color)
                }
            )
        }
    }
}


@Composable
fun SavedColorsList(
    modifier: Modifier = Modifier,
    colors: List<ColorEntity>,
    onDelete: (ColorEntity) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(colors.size) { index ->
            ColorItem(
                color = colors[index],
                onDelete = onDelete
                )
        }
    }
}
@Composable
fun ColorItem(
    modifier: Modifier = Modifier,
    color: ColorEntity,
    onDelete: (ColorEntity) -> Unit
) {
    var isOverflowMenuExpanded by remember { mutableStateOf(false) }
    var isAlertDialogShowing by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ){
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(color = color.hexCode.hexToColor())
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = color.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = color.hexCode,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = color.rgbCode,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            ShowOverflowMenu(
                modifier = Modifier.align(Alignment.TopEnd),
                color = color,
                onShowAlertDialog = { isAlertDialogShowing = true },
                onDismiss = { isOverflowMenuExpanded = false  },
                title = "Delete color" ,
                message = "Are you sure you want to delete this color?" ,
                isAlertDialogShowing = isAlertDialogShowing,
                isOverflowMenuExpanded = isOverflowMenuExpanded,
                onDismissAlertDialog = { isAlertDialogShowing = false },
                onShowMenu = { isOverflowMenuExpanded = !isOverflowMenuExpanded },
                onDelete = {
                    onDelete(color)
                    isAlertDialogShowing = false
                    isOverflowMenuExpanded = false
                }
            )
        }
    }

   
}

@Composable
fun ShowOverflowMenu(
    modifier: Modifier = Modifier,
    color: ColorEntity,
    isOverflowMenuExpanded: Boolean = false,
    isAlertDialogShowing: Boolean = false,
    onDismissAlertDialog: () -> Unit = {},
    onShowMenu: (ColorEntity) -> Unit = {},
    onShowAlertDialog: () -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit = {},
    title: String,
    message: String,
) {
    ShowAlertDialog(
        onDismissAlertDialog = onDismissAlertDialog,
        onConfirm = onDelete,
        isAlertDialogShowing = isAlertDialogShowing,
        title = title,
        message = message
    )
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopEnd
    ) {
        IconButton(
            onClick = {
                onShowMenu(color)
            }
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Show overflow menu"
            )
        }
        DropdownMenu(
            modifier = modifier,
            expanded = isOverflowMenuExpanded,
            onDismissRequest = onDismiss
        ) {
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    onShowAlertDialog()
                    onDismiss()
                }
            )
        }
    }
}

/**
 * Alert dialog when trying to delete an item
 */
@Composable
fun ShowAlertDialog(
    modifier: Modifier = Modifier,
    onDismissAlertDialog: () -> Unit,
    onConfirm: () -> Unit,
    isAlertDialogShowing: Boolean,
    title: String,
    message: String,
    confirmButtonText: String = "Delete",
    dismissButtonText: String = "Cancel"
) {
    if (isAlertDialogShowing) {
        AlertDialog(
            modifier = modifier,
            shape = ShapeDefaults.Medium,
            title = { Text(text = title) },
            text = { Text(text = message) },
            onDismissRequest = onDismissAlertDialog,
            dismissButton = {
                OutlinedButton(onClick = onDismissAlertDialog) { Text(text = dismissButtonText) }
            },
            confirmButton = {
                Button(onClick = onConfirm) {
                    Text(confirmButtonText)
                }
            }
        )
    }
}

fun String.hexToColor(): Color {
    val hexColor = this.replace("#", "")
    val red = hexColor.substring(0, 2).toInt(16) / 255f
    val green = hexColor.substring(2, 4).toInt(16) / 255f
    val blue = hexColor.substring(4, 6).toInt(16)/ 255f
    return Color(red, green, blue)
}