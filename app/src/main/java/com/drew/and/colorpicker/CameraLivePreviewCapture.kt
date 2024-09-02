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


          val imageAnalysis =   imageAnalysisBuilder.build().also {
                it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    val bitmap = imageProxy.toBitmap() //.toBitmap()

                    // Transform picker position to image coordinates
                    val transformedPosition =  transformCoordinates(
                        pickerPosition = pickerPosition,
                        imageProxy = imageProxy,
                        size = screenSize
                    )

                    if (bitmap != null) {
                        coroutineScope.launch {
                            val colorArray =
                                getAverageColorAtOffset(image = imageProxy, offset =  transformedPosition, areaSize = areaSize)
                            val clr = updateEmaColor(colorArray, threshold = 10)
                            colorViewModel.setSelectedColor(Color(clr[0], clr[1], clr[2]))
                            colorViewModel.setFindClosestColor(findClosestColor(clr))
                            imageProxy.close()
                        }
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
                    adjustableAreaSize = adjustableAreaSize.coerceIn(3, 20)
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



