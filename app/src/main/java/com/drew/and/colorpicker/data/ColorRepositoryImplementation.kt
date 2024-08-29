package com.drew.and.colorpicker.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ColorRepositoryImplementation @Inject constructor(val colorDao: ColorDao) : ColorRepository {
    override suspend fun saveColor(color: ColorEntity) = colorDao.insertColor(color)

    override suspend fun deleteColor(color: ColorEntity) = colorDao.deleteColor(color)

    override fun getAllColors(): Flow<List<ColorEntity>> = colorDao.getAllColors()
}