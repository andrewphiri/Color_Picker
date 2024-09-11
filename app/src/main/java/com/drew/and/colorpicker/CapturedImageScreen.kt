import android.graphics.Bitmap
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.drew.and.colorpicker.R
import com.drew.and.colorpicker.ViewModel.ColorViewModel
import com.drew.and.colorpicker.data.ColorEntity
import com.drew.and.colorpicker.findClosestColor
import com.drew.and.colorpicker.pickColorFromBitmap
import com.drew.and.colorpicker.toHex
import com.drew.and.colorpicker.toIntArray
import com.drew.and.colorpicker.toRgb
import com.drew.and.colorpicker.transformCoordinates
import kotlinx.serialization.Serializable

@Serializable
object CapturedImageScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapturedImageScreen(
    modifier: Modifier = Modifier,
    colorViewModel: ColorViewModel,
    navigateBack: () -> Unit,
    navigateToSavedColorsList: () -> Unit,
) {


    val capturedImage by colorViewModel.capturedImage.observeAsState(initial = null)
    var adjustedSelectedColor by remember { mutableStateOf(Color.Transparent) }
    var adjustedPickerPosition by remember { mutableStateOf(Offset.Zero) }
    val selectedColor by colorViewModel.selectedColor.observeAsState(initial = Color.Transparent)
    val pickerPosition by colorViewModel.pickerPosition.observeAsState(initial = Offset.Zero)
    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }

    val closestColor by colorViewModel.findClosestColor.observeAsState(initial = "")



    Column(modifier = modifier
        .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        TopAppBar(
            title = {
                Text(text = "Pick a Color")
            },
            navigationIcon = {
                IconButton(onClick = {
                    navigateBack()
                    colorViewModel.setCapturedImage(null)
                    colorViewModel.setSelectedColor(Color.Transparent)
                    colorViewModel.setPickerPosition(Offset.Zero)
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back"
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = navigateToSavedColorsList
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_list_alt_24),
                        contentDescription = "My color list" )
                }

                IconButton(
                    onClick = {
                        colorViewModel.saveColor(
                            ColorEntity(
                                hexCode = selectedColor.toHex(),
                                rgbCode = selectedColor.toRgb(),
                                name = closestColor
                            )
                        )
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_save_24),
                        contentDescription = "Save color"
                    )
                }
            }
        )

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
                            colorViewModel.setFindClosestColor(
                                findClosestColor(
                                    adjustedSelectedColor.toIntArray()
                                )
                            )
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
                    .weight(0.2f, true)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .width(50.dp)
                            .background(color = selectedColor, shape = RectangleShape)

                    )

                        Text(
                            text = closestColor,
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center
                        )

                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                        Text(
                            modifier = Modifier
                                .weight(1f, true),
                            text = selectedColor.toHex(),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            modifier = Modifier
                                .weight(1f, true),
                            text = selectedColor.toRgb(),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )

                }

            }
        }

    }
}




