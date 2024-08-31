package com.drew.and.colorpicker

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.drew.and.colorpicker.ViewModel.ColorViewModel
import com.drew.and.colorpicker.data.ColorEntity
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import java.util.concurrent.Executors


var emaRed = Color.Transparent.red.toDouble()
var emaGreen = Color.Transparent.green.toDouble()
var emaBlue = Color.Transparent.blue.toDouble()
var initialized = false

@Serializable
object CameraLivePreviewCaptureScreenDestination

@ExperimentalMaterial3Api
@SuppressLint("RestrictedApi")
@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun CameraLivePreviewWithCapture(
    modifier: Modifier = Modifier,
    colorViewModel: ColorViewModel,
    navigateToCapturedImage: () -> Unit,
    navigateToSavedColorsList: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
   //var capturedLiveImage by remember { mutableStateOf<Bitmap?>(null) }
    var hasStoragePermission by remember { mutableStateOf( if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    )
}

    val selectedColor by colorViewModel.selectedColor.observeAsState(initial = Color.Transparent)
    val pickerPosition by colorViewModel.pickerPosition.observeAsState(initial = Offset.Zero)
    val pickerSize by colorViewModel.pickerSize.observeAsState(initial = 25f)
    val areaSize by colorViewModel.areaSize.observeAsState(initial = 3)
    var adjustableAreaSize by remember{ mutableStateOf(3) }
//    var pickerPosition by remember { mutableStateOf(Offset.Zero) }
    var adjustablePickerSize by remember{ mutableStateOf(25f) }
    var width by rememberSaveable { mutableStateOf(0) }
    var height by rememberSaveable { mutableStateOf(0) }
    width = LocalContext.current.resources.displayMetrics.widthPixels
    height = LocalContext.current.resources.displayMetrics.heightPixels
    var screenSize = IntSize(width = width, height = height)

    val closestColor by colorViewModel.findClosestColor.observeAsState(initial = "")

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                colorViewModel.setCapturedImage(bitmap)
                navigateToCapturedImage()
            }
        }
    }

    val requestStoragePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, do something
            val intent = Intent(Intent.ACTION_PICK)
                .apply {
                    type = "image/*"
                }
            hasStoragePermission = true
            launcher.launch(intent)
        } else {
            // Permission denied, show an error message or take appropriate action
            hasStoragePermission = false
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Obtain the camera provider
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraProvider = cameraProviderFuture.get()
    val executor: Executor = ContextCompat.getMainExecutor(context)



    LaunchedEffect(Unit) {
        // Initialize picker position when the app starts
        //pickerPosition = Offset(width / 2f, height / 2f) // Initial position anywhere visible
        colorViewModel.setPickerPosition(Offset(width / 2f, height / 2f))
    }



    DisposableEffect(Unit) {
        cameraProviderFuture.addListener({
            val previewBuilder = Preview.Builder()
                val preview = previewBuilder
                .setTargetRotation(previewView.display.rotation)
                .build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetRotation(previewView.display.rotation)
                .build()

           val imageAnalysisBuilder = ImageAnalysis.Builder()
                .setTargetRotation(previewView.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)


            val imageAnalysisExtender = Camera2Interop.Extender(imageAnalysisBuilder)
            imageAnalysisExtender
                .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
                .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                .setCaptureRequestOption(CaptureRequest.NOISE_REDUCTION_MODE, CameraMetadata.NOISE_REDUCTION_MODE_HIGH_QUALITY)
                .setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)


          val imageAnalysis =   imageAnalysisBuilder.build().also {
                it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    val bitmap = imageProxy.toBitmap() //.toBitmap()

                    // Transform picker position to image coordinates
                    val transformedPosition =  transformCoordinates(
                        pickerPosition = pickerPosition,
                        imageBitmap = imageProxy,
                        size = screenSize
                    )

                    if (bitmap != null) {

                        val color = pickColorFromBitmap(
                            bitmap,
                            transformCoordinates(
                                pickerPosition = pickerPosition,
                                imageBitmap = imageProxy,
                                size = screenSize
                            )
                        )

//            val colorArray = getPixelColorAtOffset(imageProxy, transformedPosition)
//            val clr = Color(colorArray[0], colorArray[1], colorArray[2])
//            selectedColor = updateEMAColor(imageProxy, transformedPosition, alpha = 0.1f, selectedColor = clr)

                        coroutineScope.launch {
                            val colorArray =
                                getAverageColorAtOffset(image = imageProxy, offset =  transformedPosition, areaSize = areaSize)
                            val clr = updateEmaColor(colorArray, threshold = 10)
                            colorViewModel.setSelectedColor(Color(clr[0], clr[1], clr[2]))
                            colorViewModel.setFindClosestColor(findClosestColor(clr))
                            imageProxy.close()
                        }

            //selectedColor = Color(clr[0], clr[1], clr[2])
                    }

                }
            }

            val previewExtender = Camera2Interop.Extender(previewBuilder)
            previewExtender
                .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
                .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                .setCaptureRequestOption(CaptureRequest.NOISE_REDUCTION_MODE, CameraMetadata.NOISE_REDUCTION_MODE_HIGH_QUALITY)


            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                //val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
              cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalysis
                )

            } catch (exc: Exception) {
                Toast.makeText(context, "Failed to bind camera", Toast.LENGTH_SHORT).show()
            }
        }, executor)

        onDispose {
            val cameraProviders = cameraProviderFuture.get()
            cameraProviders.unbindAll()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    adjustablePickerSize += dragAmount
                    adjustableAreaSize += dragAmount.toInt()
                    adjustablePickerSize = adjustablePickerSize.coerceIn(25f, 200f)
                    adjustableAreaSize = adjustableAreaSize.coerceIn(1, 20)
                    colorViewModel.setPickerSize(adjustablePickerSize)
                    colorViewModel.setAreaSize(adjustableAreaSize)
                }
            }
    ) {

        // Show the CameraX preview
        AndroidView(
            factory = {
                previewView
                      },
            modifier = Modifier
                .fillMaxSize()
        )

        Canvas(modifier = Modifier
            .fillMaxSize()) {
            drawCircle(
                color = Color.Black,
                radius = pickerSize,
                center = pickerPosition,
                style = Stroke(width = 2.dp.toPx())
            )
        }



        TopAppBar(
            modifier = Modifier.background(color = Color.Transparent),
            colors = TopAppBarColors(containerColor = Color.Transparent,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White,
                scrolledContainerColor = Color.Transparent),
            title = {
                Text(text = "")
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
                .fillMaxWidth()
                .height(110.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier.fillMaxWidth()
            ){
                // Capture button
                IconButton(
                    onClick = {
                        val imageCaptureInstance = imageCapture ?: return@IconButton
                        // val photoFile = createTempFile(context)

                        // val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                        imageCaptureInstance.takePicture(
                            executor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val bitmap = imageProxyToBitmap(image)
                                    if (bitmap != null) {
                                        colorViewModel.setCapturedImage(bitmap)
                                        navigateToCapturedImage()
                                    }
                                    image.close()
                                }
                                override fun onError(exception: ImageCaptureException) {
                                    Toast.makeText(context, "Image capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }

                            }
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Transparent)
                        .padding(16.dp)
                ) {
                    Image(
                        modifier = Modifier.size(48.dp),
                        painter = painterResource(R.drawable.sharp_camera_24),
                        contentDescription = "Capture Color"
                    )
                }

                // Insert photo from gallery
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_PICK)
                            .apply {
                                type = "image/*"
                            }
                        if (hasStoragePermission) {
                            launcher.launch(intent)
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                requestStoragePermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                            } else {
                                requestStoragePermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        }

                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .background(Color.Transparent)
                        .padding(16.dp)
                ) {
                    Image(
                        modifier = Modifier.size(48.dp),
                        painter = painterResource(R.drawable.outline_insert_photo_24),
                        contentDescription = "Pick a picture from gallery"
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .padding(4.dp),
                    verticalArrangement =  Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = closestColor,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                        )

                    Text(
                        modifier = Modifier
                            .align(Alignment.Start),
                        text = selectedColor.toHex(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                        )

                    Text(
                        modifier = Modifier
                            .align(Alignment.Start),
                        text = selectedColor.toRgb(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                        )
                    Box(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .height(10.dp)
                            .width(50.dp)
                            .background(color = selectedColor, shape = RectangleShape)
                    )
                }
            }
        }
    }
}

private fun createTempFile(context: android.content.Context): java.io.File {
    val storageDir = context.getExternalFilesDir(null)
    return java.io.File.createTempFile("JPEG_${System.currentTimeMillis()}_", ".jpg", storageDir)
}

//Helper function to convert ImageProxy to Bitmap
private fun imageProxyToBitmap(image: ImageProxy) : Bitmap? {
    val buffer: ByteBuffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap: Bitmap? = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return bitmap?.let { rotateBitmap(it, image.imageInfo.rotationDegrees) }
}

//Helper function to convert ImageProxy to Bitmap
private fun imageProxyToBitmap1(image: ImageProxy) : Bitmap? {
    val buffer: ByteBuffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap: Bitmap? = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return bitmap
}

private fun imageProxyToBitmapLive(image: ImageProxy): Bitmap? {
    val yBuffer = image.planes[0].buffer // Y
    val uBuffer = image.planes[1].buffer // U
    val vBuffer = image.planes[2].buffer // V

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    // U and V are swapped
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
    val imageBytes = out.toByteArray()
    val bitmap: Bitmap? = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    return bitmap?.let { Bitmap.createBitmap(it) }
}


private fun imageProxyToBitmapLiveRGBA(image: ImageProxy): Bitmap? {
    val planes = image.planes
    val buffer = planes[0].buffer
    val pixelStride = planes[0].pixelStride
    val rowStride = planes[0].rowStride
    val rowPadding = rowStride - pixelStride * image.width

    val bitmap = Bitmap.createBitmap(image.width + rowPadding / pixelStride, image.height, Bitmap.Config.ARGB_8888)
    bitmap.copyPixelsFromBuffer(buffer)

    val rotationDegrees = image.imageInfo.rotationDegrees
    Log.d("ColorPicker", "Image rotation degrees: $rotationDegrees")

    return rotateBitmap(bitmap, rotationDegrees)
}

// Helper function to rotate a Bitmap
private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(bitmap,0,0,bitmap.width, bitmap.height, matrix, true)
}

fun getSmoothedPixelColorAtOffset(image: ImageProxy, offset: Offset, smoothingFactor: Double, sampleRadius: Int): IntArray {
    val sampledColors = mutableListOf<IntArray>()

    // Sample multiple points around the offset
    for (dx in -sampleRadius..sampleRadius) {
        for (dy in -sampleRadius..sampleRadius) {
            val sampledOffset = Offset(offset.x + dx, offset.y + dy)
            sampledColors.add(getPixelColorAtOffset1(image, sampledOffset))
        }
    }

    // Calculate the average color of the sampled points
    val avgRed = sampledColors.map { it[0] }.average()
    val avgGreen = sampledColors.map { it[1] }.average()
    val avgBlue = sampledColors.map { it[2] }.average()

    if (smoothingFactor == 1.0) {
        // Directly use the average color when smoothingFactor is 1.0
        emaRed = avgRed
        emaGreen = avgGreen
        emaBlue = avgBlue
    } else {
        // Apply EMA for smoothing
        emaRed = (smoothingFactor * avgRed) + ((1 - smoothingFactor) * emaRed)
        emaGreen = (smoothingFactor * avgGreen) + ((1 - smoothingFactor) * emaGreen)
        emaBlue = (smoothingFactor * avgBlue) + ((1 - smoothingFactor) * emaBlue)
    }

    // Apply EMA for smoothing
    emaRed = (smoothingFactor * avgRed) + ((1 - smoothingFactor) * emaRed)
    emaGreen = (smoothingFactor * avgGreen) + ((1 - smoothingFactor) * emaGreen)
    emaBlue = (smoothingFactor * avgBlue) + ((1 - smoothingFactor) * emaBlue)

    return intArrayOf(emaRed.toInt(), emaGreen.toInt(), emaBlue.toInt())
}

//Function to smooth color detection. The lower the alpha the smoother the detection
fun updateEmaColor(newColor: IntArray, alpha: Double = 1.0, threshold: Int = 5): IntArray {
    val (newRed, newGreen, newBlue) = newColor

    if (!initialized) {
        // Initialize EMA with the first color
        emaRed = newRed.toDouble()
        emaGreen = newGreen.toDouble()
        emaBlue = newBlue.toDouble()
        initialized = true
    } else {
        // Calculate the difference
        val deltaRed = Math.abs(newRed - emaRed)
        val deltaGreen = Math.abs(newGreen - emaGreen)
        val deltaBlue = Math.abs(newBlue - emaBlue)

        // Update EMA only if the change is significant
        if (deltaRed > threshold || deltaGreen > threshold || deltaBlue > threshold) {
            emaRed = alpha * newRed + (1 - alpha) * emaRed
            emaGreen = alpha * newGreen + (1 - alpha) * emaGreen
            emaBlue = alpha * newBlue + (1 - alpha) * emaBlue
        }
    }

    // Return the smoothed color
    return intArrayOf(emaRed.toInt(), emaGreen.toInt(), emaBlue.toInt())
}


// Function to retrieve the color at a specific offset
fun getPixelColorAtOffset1(image: ImageProxy, offset: Offset): IntArray {
    val planes = image.planes

    val height = image.height
    val width = image.width

    val x = offset.x.coerceIn(0f, (width - 1).toFloat()).toInt()
    val y = offset.y.coerceIn(0f, (height - 1).toFloat()).toInt()

    fun byteBufferToByteArray(buffer: ByteBuffer): ByteArray {
        buffer.rewind()
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        return data
    }

    val yArr = byteBufferToByteArray(planes[0].buffer)
    val yPixelStride = planes[0].pixelStride
    val yRowStride = planes[0].rowStride

    val uArr = byteBufferToByteArray(planes[1].buffer)
    val uPixelStride = planes[1].pixelStride
    val uRowStride = planes[1].rowStride

    val vArr = byteBufferToByteArray(planes[2].buffer)
    val vPixelStride = planes[2].pixelStride
    val vRowStride = planes[2].rowStride

    val yIndex = y * yRowStride + x * yPixelStride
    val yValue = yArr[yIndex].toInt() and 0xFF

    val uvIndex = (y / 2) * uRowStride + (x / 2) * uPixelStride
    val uValue = (uArr[uvIndex].toInt() and 0xFF) - 128
    val vValue = (vArr[uvIndex].toInt() and 0xFF) - 128

    val r = yValue + 1.370705 * vValue
    val g = yValue - 0.698001 * vValue - 0.337633 * uValue
    val b = yValue + 1.732446 * uValue

    val rClamped = r.coerceIn(0.0, 255.0).toInt()
    val gClamped = g.coerceIn(0.0, 255.0).toInt()
    val bClamped = b.coerceIn(0.0, 255.0).toInt()

    return intArrayOf(rClamped, gClamped, bClamped)
}

//Function to smooth color detection. The lower the alpha the smoother the detection
fun updateEmaColor(newColor: IntArray, alpha: Double = 0.2): IntArray {
    val (newRed, newGreen, newBlue) = newColor

    if (!initialized) {
        // Initialize EMA with the first color
        emaRed = newRed.toDouble()
        emaGreen = newGreen.toDouble()
        emaBlue = newBlue.toDouble()
        initialized = true
    } else {
        // Update EMA
        emaRed = alpha * newRed + (1 - alpha) * emaRed
        emaGreen = alpha * newGreen + (1 - alpha) * emaGreen
        emaBlue = alpha * newBlue + (1 - alpha) * emaBlue
    }

    // Return the smoothed color
    return intArrayOf(emaRed.toInt(), emaGreen.toInt(), emaBlue.toInt())
}

fun yuvToRgb5(image: ImageProxy): IntArray {
    val planes = image.planes

    val height = image.height
    val width = image.width

    // Helper function to convert ByteBuffer to byte array
    fun byteBufferToByteArray(buffer: ByteBuffer): ByteArray {
        buffer.rewind()
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        return data
    }

    // Y
    val yArr = byteBufferToByteArray(planes[0].buffer)
    val yPixelStride = planes[0].pixelStride
    val yRowStride = planes[0].rowStride

    // U
    val uArr = byteBufferToByteArray(planes[1].buffer)
    val uPixelStride = planes[1].pixelStride
    val uRowStride = planes[1].rowStride

    // V
    val vArr = byteBufferToByteArray(planes[2].buffer)
    val vPixelStride = planes[2].pixelStride
    val vRowStride = planes[2].rowStride

    val halfHeight = height / 2
    val halfWidth = width / 2

    // Create an array to hold the RGB values
    val rgb = IntArray(width * height)

    for (j in 0 until height) {
        for (i in 0 until width) {
            val yIndex = j * yRowStride + i * yPixelStride
            val y = yArr[yIndex].toInt() and 0xFF

            // Compute the UV index for the current pixel
            val uvIndex = (j / 2) * uRowStride + (i / 2) * uPixelStride
            val u = (uArr[uvIndex].toInt() and 0xFF) - 128
            val v = (vArr[uvIndex].toInt() and 0xFF) - 128

            val r = y + 1.370705 * v
            val g = y - 0.698001 * v - 0.337633 * u
            val b = y + 1.732446 * u

            // Clamp the RGB values to [0, 255]
            val rClamped = r.coerceIn(0.0, 255.0).toInt()
            val gClamped = g.coerceIn(0.0, 255.0).toInt()
            val bClamped = b.coerceIn(0.0, 255.0).toInt()

            // Combine the RGB values into a single int
            rgb[j * width + i] = (0xFF shl 24) or (rClamped shl 16) or (gClamped shl 8) or bClamped
        }
    }

    return rgb
}


fun getPixelColorAtOffset(image: ImageProxy, offset: Offset): IntArray {
    val planes = image.planes

    val height = image.height
    val width = image.width

    // Ensure the offset is within the image bounds
    val x = offset.x.coerceIn(0f, (width - 1).toFloat()).toInt()
    val y = offset.y.coerceIn(0f, (height - 1).toFloat()).toInt()

    // Helper function to convert ByteBuffer to byte array
    fun byteBufferToByteArray(buffer: ByteBuffer): ByteArray {
        buffer.rewind()
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        return data
    }

    // Y
    val yArr = byteBufferToByteArray(planes[0].buffer)
    val yPixelStride = planes[0].pixelStride
    val yRowStride = planes[0].rowStride

    // U
    val uArr = byteBufferToByteArray(planes[1].buffer)
    val uPixelStride = planes[1].pixelStride
    val uRowStride = planes[1].rowStride

    // V
    val vArr = byteBufferToByteArray(planes[2].buffer)
    val vPixelStride = planes[2].pixelStride
    val vRowStride = planes[2].rowStride

    // Calculate indices for the specified offset
    val yIndex = y * yRowStride + x * yPixelStride
    val yValue = yArr[yIndex].toInt() and 0xFF

    // Compute the UV index for the specified offset
    val uvIndex = (y / 2) * uRowStride + (x / 2) * uPixelStride
    val uValue = (uArr[uvIndex].toInt() and 0xFF) - 128
    val vValue = (vArr[uvIndex].toInt() and 0xFF) - 128

    val r = yValue + 1.370705 * vValue
    val g = yValue - 0.698001 * vValue - 0.337633 * uValue
    val b = yValue + 1.732446 * uValue

    // Clamp the RGB values to [0, 255]
    val rClamped = r.coerceIn(0.0, 255.0).toInt()
    val gClamped = g.coerceIn(0.0, 255.0).toInt()
    val bClamped = b.coerceIn(0.0, 255.0).toInt()

    // Return the RGB values as an IntArray
    return intArrayOf(rClamped, gClamped, bClamped)
}

fun getAverageColorAtOffset(image: ImageProxy, offset: Offset, areaSize: Int = 3): IntArray {
    val planes = image.planes

    val height = image.height
    val width = image.width

    // Helper function to convert ByteBuffer to byte array
    fun byteBufferToByteArray(buffer: ByteBuffer): ByteArray {
        buffer.rewind()
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        return data
    }

    // Y
    val yArr = byteBufferToByteArray(planes[0].buffer)
    val yPixelStride = planes[0].pixelStride
    val yRowStride = planes[0].rowStride

    // U
    val uArr = byteBufferToByteArray(planes[1].buffer)
    val uPixelStride = planes[1].pixelStride
    val uRowStride = planes[1].rowStride

    // V
    val vArr = byteBufferToByteArray(planes[2].buffer)
    val vPixelStride = planes[2].pixelStride
    val vRowStride = planes[2].rowStride

    var rSum = 0.0
    var gSum = 0.0
    var bSum = 0.0
    var count = 0

    // Calculate the range to average around the target pixel
    for (dy in -areaSize / 2..areaSize / 2) {
        for (dx in -areaSize / 2..areaSize / 2) {
            // Ensure the offset is within the image bounds
            val x = (offset.x + dx).coerceIn(0f, (width - 1).toFloat()).toInt()
            val y = (offset.y + dy).coerceIn(0f, (height - 1).toFloat()).toInt()

            // Calculate indices for the specified offset
            val yIndex = y * yRowStride + x * yPixelStride
            val yValue = yArr[yIndex].toInt() and 0xFF

            // Compute the UV index for the specified offset
            val uvIndex = (y / 2) * uRowStride + (x / 2) * uPixelStride
            val uValue = (uArr[uvIndex].toInt() and 0xFF) - 128
            val vValue = (vArr[uvIndex].toInt() and 0xFF) - 128

            val r = yValue + 1.370705 * vValue
            val g = yValue - 0.698001 * vValue - 0.337633 * uValue
            val b = yValue + 1.732446 * uValue

            // Accumulate the RGB values
            rSum += r.coerceIn(0.0, 255.0)
            gSum += g.coerceIn(0.0, 255.0)
            bSum += b.coerceIn(0.0, 255.0)
            count++
        }
    }

    // Calculate the average RGB values
    val rAverage = (rSum / count).toInt()
    val gAverage = (gSum / count).toInt()
    val bAverage = (bSum / count).toInt()

    // Return the averaged RGB values as an IntArray
    return intArrayOf(rAverage, gAverage, bAverage)
}

// Smooth color using a simple moving average
//SmoothColor function takes the new color value and the color queue, computes the average of the colors in the queue, and returns the smoothed color.
//If the queue reaches a predefined maximum size (maxQueueSize), the oldest color is removed before adding the new color.
private fun smoothColor(newColor: Color, colorQueue: ArrayDeque<Color>, maxQueueSize: Int = 20) : Color {
    if (colorQueue.size >= maxQueueSize) {
        colorQueue.removeFirst()
    }

    colorQueue.addLast(newColor)

    var r = 0f
    var g  = 0f
    var b = 0f

    for ((index,color) in colorQueue.withIndex()) {
        val weight = (index + 1) / colorQueue.size.toFloat()
        r += color.red * weight
        g += color.green * weight
        b += color.blue * weight
    }

    val size = colorQueue.size
    return Color(r / size, g / size, b / size)
}

// Function to pick a color from the bitmap at the given position
fun pickColorFromBitmap(bitmap: Bitmap, position: Offset): Color {
    val x = position.x.toInt().coerceIn(0, bitmap.width - 1)
    val y = position.y.toInt().coerceIn(0, bitmap.height - 1)

    val pixel = bitmap.getPixel(x, y)
    return Color(pixel)
}



private fun transformCoordinates(pickerPosition: Offset, imageBitmap: ImageProxy, size: IntSize): Offset {
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


// Function to convert YUV to RGB
private fun yuvToRgb(image: ImageProxy): Bitmap {
    val yBuffer = image.planes[0].buffer // Y
    val uBuffer = image.planes[1].buffer // U
    val vBuffer = image.planes[2].buffer // V

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    // Y channel
    yBuffer.get(nv21, 0, ySize)

    // VU channel
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

private fun updateEMAColor(imageProxy: ImageProxy, position: Offset, alpha: Float = 0.5f,selectedColor: Color = Color.Transparent) : Color {
    val yBuffer = imageProxy.planes[0].buffer // Y plane
    val uBuffer = imageProxy.planes[1].buffer // U plane
    val vBuffer = imageProxy.planes[2].buffer // V plane

    val width = imageProxy.width
    val height = imageProxy.height

    val x = position.x.toInt().coerceIn(0, width - 1)
    val y = position.y.toInt().coerceIn(0, height - 1)

    val yRowStride = imageProxy.planes[0].rowStride
    val uvRowStride = imageProxy.planes[1].rowStride
    val uvPixelStride = imageProxy.planes[1].pixelStride

    val yIndex = y * yRowStride + x
    val uvIndex = (y / 2) * uvRowStride + (x / 2) * uvPixelStride

    val yValue = yBuffer[yIndex].toInt() and 0xFF
    val uValue = uBuffer[uvIndex].toInt() and 0xFF
    val vValue = vBuffer[uvIndex].toInt() and 0xFF

    val rgb = yuvToRgb(yValue, uValue, vValue)

    val r = (rgb shr 16) and 0xFF
    val g = (rgb shr 8) and 0xFF
    val b = rgb and 0xFF

    val newColor = Color(r, g, b)

    // Apply EMA filter
     val color = Color(
        red = alpha * newColor.red + (1 - alpha) * selectedColor.red,
        green = alpha * newColor.green + (1 - alpha) * selectedColor.green,
        blue = alpha * newColor.blue + (1 - alpha) * selectedColor.blue
    )
    return color
}

private fun yuvToRgb(y: Int, u: Int, v: Int): Int {
    val c = y - 16
    val d = u - 128
    val e = v - 128

    val r = (298 * c + 409 * e + 128) shr 8
    val g = (298 * c - 100 * d - 208 * e + 128) shr 8
    val b = (298 * c + 516 * d + 128) shr 8

    return (0xFF shl 24) or (r.coerceIn(0, 255) shl 16) or (g.coerceIn(0, 255) shl 8) or b.coerceIn(0, 255)
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


