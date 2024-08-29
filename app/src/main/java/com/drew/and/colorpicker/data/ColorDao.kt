package com.drew.and.colorpicker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ColorDao {

    @Insert
    suspend fun insertColor(color: ColorEntity)

    @Delete
    suspend fun deleteColor(color: ColorEntity)

    @Query("SELECT * FROM colors")
    fun getAllColors(): Flow<List<ColorEntity>>
}