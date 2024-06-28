package com.drew.and.colorpicker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.drew.and.colorpicker.R
import java.nio.ByteBuffer
import java.util.concurrent.Executor

@Composable
fun CameraPreviewWithCapture(modifier: Modifier, onImageCaptured: (Bitmap) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    // Obtain the camera provider
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    //val cameraProvider = cameraProviderFuture.get()
    val executor: Executor = ContextCompat.getMainExecutor(context)

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

//            val orientationEventListener = object : OrientationEventListener(context) {
//                override fun onOrientationChanged(orientation: Int) {
//                    val rotation: Int = when (orientation) {
//                        in 45..134 -> Surface.ROTATION_270
//                        in 135..224 -> Surface.ROTATION_180
//                        in 225..314 -> Surface.ROTATION_90
//                        else -> Surface.ROTATION_0
//                    }
//                    imageCapture?.targetRotation = rotation
//                }
//            }
//            orientationEventListener.enable()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
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

    Box(modifier = Modifier.fillMaxSize()) {
        // Show the CameraX preview
        AndroidView(
            { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Capture button
        Button(
            onClick = {
                val imageCaptureInstance = imageCapture ?: return@Button
               // val photoFile = createTempFile(context)

               // val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                imageCaptureInstance.takePicture(
                    executor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val bitmap = imageProxyToBitmap(image)
                            onImageCaptured(bitmap)
                            image.close()
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Toast.makeText(context, "Image capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Image(
                modifier = Modifier.size(48.dp),
                painter = painterResource(R.drawable.sharp_camera_24),
                contentDescription = "Capture Color")
            Text("Capture Color")
        }
    }
}

private fun createTempFile(context: android.content.Context): java.io.File {
    val storageDir = context.getExternalFilesDir(null)
    return java.io.File.createTempFile("JPEG_${System.currentTimeMillis()}_", ".jpg", storageDir)
}

//Helper function to convert ImageProxy to Bitmap
private fun imageProxyToBitmap(image: ImageProxy) : Bitmap {
    val buffer: ByteBuffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return rotateBitmap(bitmap, image.imageInfo.rotationDegrees)
}

// Helper function to rotate a Bitmap
private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(bitmap,0,0,bitmap.width, bitmap.height, matrix, true)
}
