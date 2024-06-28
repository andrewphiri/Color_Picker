import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.drew.and.colorpicker.CameraLivePreviewWithCapture
import com.drew.and.colorpicker.CameraPreviewWithCapture

@Composable
fun ColorPickerApp(modifier: Modifier) {
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var selectedColor by remember { mutableStateOf(Color.Transparent) }
    var pickerPosition by remember { mutableStateOf(Offset.Zero) }
    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }

    Column(modifier = modifier
        .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        // Display captured image or camera preview
        width = LocalContext.current.resources.displayMetrics.widthPixels
        height = LocalContext.current.resources.displayMetrics.heightPixels

        if (capturedImage != null) {
            LaunchedEffect(Unit) {
                // Update selected color based on picker position
                selectedColor =
                    pickColorFromBitmap(capturedImage!!, pickerPosition, IntSize(width = width, height = height))

                // Initialize picker position when the app starts
                    pickerPosition = Offset(width / 2f, height / 2f) // Initial position anywhere visible

            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(2.5f)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            pickerPosition = Offset(
                                x = (pickerPosition.x + dragAmount.x).coerceIn(
                                    0f,
                                    size.width.toFloat()
                                ),
                                y = (pickerPosition.y + dragAmount.y).coerceIn(
                                    0f,
                                    size.height.toFloat()
                                )
                            )
                            // Update selected color based on picker position
                            selectedColor =
                                pickColorFromBitmap(capturedImage!!, pickerPosition, size)

                        }
                    }
            ) {
                Image(
                    bitmap = capturedImage!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                Canvas(modifier = Modifier
                    .fillMaxSize()) {
                    drawCircle(
                        color = Color.Black,
                        radius = 20f,
                        center = pickerPosition,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

            }
        } else {
            // Show camera preview and capture image
            CameraLivePreviewWithCapture(
                modifier = Modifier
                    .fillMaxSize()
            ) { bitmap ->
                capturedImage = bitmap
            }
        }

        //Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.weight(1f, true)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display selected color
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(color = selectedColor, shape = CircleShape)

            )

          //  Spacer(modifier = Modifier.height(16.dp))

            // Display selected color's HEX and RGB values
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(0.4f, true)
                        .padding(8.dp),
                    text = "HEX:",
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    modifier = Modifier
                        .weight(2f, true)
                        .padding(8.dp),
                    text = selectedColor.toHex(),
                    style = MaterialTheme.typography.titleMedium,

                )
            }


            //Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(0.4f, true)
                        .padding(8.dp),
                    text = "RGB:",
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    modifier = Modifier
                        .weight(2f, true)
                        .padding(8.dp),
                    text = selectedColor.toRgb(),
                    style = MaterialTheme.typography.titleMedium,

                )
            }


           // Spacer(modifier = Modifier.height(16.dp))

            // Reset button
            Button(onClick = {
                capturedImage = null
                selectedColor = Color.Transparent
                pickerPosition = Offset.Zero
            }) {
                Text("Reset")
            }
        }
    }
}

// Function to pick a color from the bitmap at the given position
fun pickColorFromBitmap(bitmap: Bitmap, position: Offset, size: IntSize): Color {
    val x = (position.x / size.width * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
    val y = (position.y / size.height * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
    val pixel = bitmap.getPixel(x, y)
    return Color(pixel)
}

// Extension function to convert Color to HEX string
 fun Color.toHex(): String {
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()
    return String.format("#%02X%02X%02X", red, green, blue)
}

// Extension function to convert Color to RGB string
fun Color.toRgb(): String {
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()
    return "RGB($red, $green, $blue)"
}
