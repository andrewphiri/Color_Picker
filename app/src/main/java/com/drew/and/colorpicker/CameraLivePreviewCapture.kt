package com.drew.and.colorpicker


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import pickColorFromBitmap
import toHex
import toRgb
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executor

@Composable
fun CameraLivePreviewWithCapture(modifier: Modifier, onImageCaptured: (Bitmap) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var imageAnalysis: ImageAnalysis? by remember { mutableStateOf(null) }
   //var capturedLiveImage by remember { mutableStateOf<Bitmap?>(null) }

    var selectedColor by remember { mutableStateOf(Color.Transparent) }
    var pickerPosition by remember { mutableStateOf(Offset.Zero) }
    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }
    width = LocalContext.current.resources.displayMetrics.widthPixels
    height = LocalContext.current.resources.displayMetrics.heightPixels
    var screenSize = IntSize(width = width, height = height)

    // Obtain the camera provider
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    //val cameraProvider = cameraProviderFuture.get()
    val executor: Executor = ContextCompat.getMainExecutor(context)

    LaunchedEffect(Unit) {
        // Initialize picker position when the app starts
        pickerPosition = Offset(width / 2f, height / 2f) // Initial position anywhere visible
    }

    DisposableEffect(Unit) {
        cameraProviderFuture.addListener({
            val preview = Preview.Builder()
                .setTargetRotation(previewView.display.rotation)
                .build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(previewView.display.rotation)
                .build()

            imageAnalysis = ImageAnalysis.Builder()
                .setTargetRotation(previewView.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build().also {
                    it.setAnalyzer(executor) { imageProxy ->
                        val bitmap = imageProxyToBitmapLiveRGBA(imageProxy)
                       // capturedLiveImage = bitmap

                       // Log.d("CameraPreview", "Captured live image: $bitmap")
                        //onImageCaptured(bitmap)
                        if (bitmap != null) {
                            selectedColor = pickColorFromBitmap(
                                bitmap,
                                pickerPosition,
                                screenSize
                            )
                        }
                        imageProxy.close()
                    }

                }


            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                val cameraProvider = cameraProviderFuture.get()
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
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    screenSize = size
                    pickerPosition = Offset(
                        x = (pickerPosition.x + dragAmount.x).coerceIn(
                            0f,
                            size.width.toFloat()
                        ),
                        y = (pickerPosition.y + dragAmount.y).coerceIn(
                            0f,
                            size.height.toFloat()))

//                    // Update selected color based on picker position
//                    if (capturedLiveImage != null) {
//                        selectedColor =
//                            pickColorFromBitmap(capturedLiveImage!!, pickerPosition, size)
//                    }
                }
            }
    ) {
        // Show the CameraX preview
        AndroidView(
            { previewView },
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


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .background(shape = RectangleShape,
                    brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)), alpha = 0.8f),
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
                                        onImageCaptured(bitmap)
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

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement =  Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(50.dp)
                        .background(color = selectedColor, shape = CircleShape)

                )

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

private fun rotateBitmapLive(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
    val matrix = Matrix().apply {
        preRotate(rotationDegrees.toFloat())
    }
    return Bitmap.createBitmap(bitmap,0,0,bitmap.width, bitmap.height, matrix, true)
}
