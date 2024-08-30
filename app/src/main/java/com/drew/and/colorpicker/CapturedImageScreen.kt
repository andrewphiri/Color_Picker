import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.drew.and.colorpicker.ViewModel.ColorViewModel
import com.drew.and.colorpicker.findClosestColor
import kotlinx.serialization.Serializable

@Serializable
object CapturedImageScreenDestination

@Composable
fun CapturedImageScreen(
    modifier: Modifier = Modifier,
    colorViewModel: ColorViewModel,
    navigateBack: () -> Unit
) {
    val capturedImage by colorViewModel.capturedImage.observeAsState(initial = null)
    var adjustedSelectedColor by remember { mutableStateOf(Color.Transparent) }
    var adjustedPickerPosition by remember { mutableStateOf(Offset.Zero) }
    val selectedColor by colorViewModel.selectedColor.observeAsState(initial = Color.Transparent)
    val pickerPosition by colorViewModel.pickerPosition.observeAsState(initial = Offset.Zero)
    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }

    val closestColor by colorViewModel.findClosestColor.observeAsState(initial = "")

    if (capturedImage != null) {

    }

        Column(modifier = modifier
            .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            // Display captured image or camera preview

            width = LocalContext.current.resources.displayMetrics.widthPixels
            height = LocalContext.current.resources.displayMetrics.heightPixels
            LaunchedEffect(Unit) {
                // Update selected color based on picker position
                adjustedSelectedColor =
                    pickColorFromBitmap(capturedImage!!,   transformCoordinates(adjustedPickerPosition,
                        capturedImage!!,
                        IntSize(width, height)
                    ))
                colorViewModel.setSelectedColor(adjustedSelectedColor)
                colorViewModel.setFindClosestColor(findClosestColor(adjustedSelectedColor.toIntArray()))

                // Initialize picker position when the app starts
                adjustedPickerPosition = Offset(width / 2f, height / 2f) // Initial position anywhere visible
                colorViewModel.setPickerPosition(adjustedPickerPosition)

            }
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(2f, true)
                        .onGloballyPositioned {
                            width = it.size.width
                            height = it.size.height
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                adjustedPickerPosition = Offset(
                                    x = (adjustedPickerPosition.x + dragAmount.x).coerceIn(
                                        0f,
                                        size.width.toFloat()
                                    ),
                                    y = (adjustedPickerPosition.y + dragAmount.y).coerceIn(
                                        0f,
                                        size.height.toFloat()
                                    )
                                )
                                colorViewModel.setPickerPosition(adjustedPickerPosition)
                                // Update selected color based on picker position
                                adjustedSelectedColor =
                                    pickColorFromBitmap(
                                        capturedImage!!,
                                        transformCoordinates(
                                            adjustedPickerPosition,
                                            capturedImage!!,
                                            size
                                        )
                                    )
                                colorViewModel.setSelectedColor(adjustedSelectedColor)
                                colorViewModel.setFindClosestColor(findClosestColor(adjustedSelectedColor.toIntArray()))
                            }
                        }
                ) {
                    Image(
                        bitmap = capturedImage!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                    Canvas(modifier = Modifier
                        .fillMaxSize()) {
                        drawCircle(
                            color = Color.Black,
                            radius = 30f,
                            center = pickerPosition,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.3f, true)
                        .background(
                            shape = RectangleShape,
                            brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)),
                            alpha = 0.8f
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .height(10.dp)
                            .width(50.dp)
                            .background(color = selectedColor, shape = RectangleShape)
                    )

                    Text(
                        text = closestColor,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = selectedColor.toHex(),
                            style = MaterialTheme.typography.bodySmall,
                        )

                        Text(
                            text = selectedColor.toRgb(),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }


                    // Reset button
                    Button(
                        onClick = {
                            navigateBack()
                            colorViewModel.setCapturedImage(null)
                            colorViewModel.setSelectedColor(Color.Transparent)
                            colorViewModel.setPickerPosition(Offset.Zero)

                        }) {
                        Text("Back")
                    }
                }
            }


            //Spacer(modifier = Modifier.height(16.dp))
//        Column(
//            modifier = Modifier
//                .weight(0.5f, true)
//                .padding(8.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//
//        }
        }

}

// Function to pick a color from the bitmap at the given position
private fun pickColorFromBitmap(bitmap: Bitmap, position: Offset): Color {
    val x = position.x.toInt().coerceIn(0, bitmap.width - 1)
    val y = position.y.toInt().coerceIn(0, bitmap.height - 1)
    val pixel = bitmap.getPixel(x, y)
    return Color(pixel)
}

//private fun pickColorFromBitmap(bitmap: Bitmap, position: Offset, size: IntSize): Color {
//    val x = (position.x / size.width * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
//    val y = (position.y / size.height * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
//    val pixel = bitmap.getPixel(x, y)
//    return Color(pixel)
//}

// Extension function to convert Color to HEX string
 private fun Color.toHex(): String {
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()
    return String.format("#%02X%02X%02X", red, green, blue)
}

// Extension function to convert Color to RGB string
private fun Color.toRgb(): String {
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()
    return "RGB($red, $green, $blue)"
}

private fun String.hexToColor(): Color {
    val hexColor = this.replace("#", "")
    val red = hexColor.substring(0, 2).toInt(16) / 255f
    val green = hexColor.substring(2, 4).toInt(16) / 255f
    val blue = hexColor.substring(4, 6).toInt(16)/ 255f
    return Color(red, green, blue)
}

fun Color.toIntArray(): IntArray {
    return intArrayOf(
        (red * 255).toInt(),(green * 255).toInt(),
        (blue * 255).toInt(),
        (alpha * 255).toInt()
    )
}

private fun transformCoordinates(pickerPosition: Offset, imageBitmap: Bitmap, size: IntSize): Offset {
    val imageWidth = imageBitmap.width.toFloat()
    val imageHeight = imageBitmap.height.toFloat()

    // Calculate the aspect ratios
    val viewAspectRatio = size.width.toFloat() / size.height.toFloat()
    val imageAspectRatio = imageWidth / imageHeight

    // Determine scaling and offset
    val scale: Float
    val offsetX: Float
    val offsetY: Float
    if (viewAspectRatio > imageAspectRatio) {
        scale = size.height.toFloat() / imageHeight
        offsetX = (size.width.toFloat() - imageWidth * scale) / 2
        offsetY = 0f
    } else {
        scale = size.width.toFloat() / imageWidth
        offsetX = 0f
        offsetY = (size.height.toFloat() - imageHeight * scale) / 2
    }

    // Transform picker position to bitmap coordinates
    val transformedX = (pickerPosition.x - offsetX) / scale
    val transformedY = (pickerPosition.y - offsetY) / scale

    return Offset(transformedX, transformedY)
}

