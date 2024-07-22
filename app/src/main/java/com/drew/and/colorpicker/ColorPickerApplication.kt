package com.drew.and.colorpicker

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader

class ColorPickerApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        if (!OpenCVLoader.initLocal()) {
            Log.e("OpenCV", "Failed to initialize OpenCV")
        } else {
            Log.d("OpenCV", "OpenCV initialized successfully")
        }
    }
}