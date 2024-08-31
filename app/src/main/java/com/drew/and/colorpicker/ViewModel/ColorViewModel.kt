package com.drew.and.colorpicker.ViewModel

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drew.and.colorpicker.data.ColorEntity
import com.drew.and.colorpicker.data.ColorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ColorViewModel @Inject constructor(private val repository: ColorRepository) : ViewModel() {

    private val _selectedColor = MutableLiveData<Color>()
    val selectedColor: LiveData<Color> get() = _selectedColor

    private val _pickerPosition = MutableLiveData<Offset>()
    val pickerPosition: LiveData<Offset> get() = _pickerPosition

    private val _pickerSize = MutableLiveData<Float>()
    val pickerSize: LiveData<Float> get() = _pickerSize

    private val _areaSize = MutableLiveData<Int>()
    val areaSize: LiveData<Int> get() = _areaSize

    private val _capturedImage = MutableLiveData<Bitmap?>()
    val capturedImage: LiveData<Bitmap?> get() = _capturedImage

    private val _findClosestColor = MutableLiveData<String>()
    val findClosestColor: LiveData<String> get() = _findClosestColor

    val allColors: StateFlow<List<ColorEntity>> = repository
        .getAllColors()
        .map { it }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf()
        )

    fun setSelectedColor(color: Color) {
        _selectedColor.value = color
    }

    fun setPickerPosition(offset: Offset) {
        _pickerPosition.value = offset
    }

    fun setAreaSize(size: Int) {
        _areaSize.value = size
    }

    fun setPickerSize(size: Float) {
        _pickerSize.value = size
    }

    fun setCapturedImage(bitmap: Bitmap?) {
        _capturedImage.value = bitmap
    }

    fun setFindClosestColor(color: String) {
        _findClosestColor.value = color
    }

    fun saveColor(color: ColorEntity) {
        viewModelScope.launch {
            repository.saveColor(color)
        }
        Log.d("ColorViewModel", "Color saved: $color")
    }

    fun deleteColor(color: ColorEntity) {
        viewModelScope.launch {
            repository.deleteColor(color)
        }
    }

}