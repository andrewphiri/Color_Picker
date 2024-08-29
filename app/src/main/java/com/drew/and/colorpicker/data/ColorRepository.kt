package com.drew.and.colorpicker.data

import kotlinx.coroutines.flow.Flow

interface ColorRepository {
    suspend fun saveColor(color: ColorEntity)
    suspend fun deleteColor(color: ColorEntity)
    fun getAllColors(): Flow<List<ColorEntity>>
}