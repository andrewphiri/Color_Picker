package com.drew.and.colorpicker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "colors")
data class ColorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hexCode: String,
    val rgbCode: String,
    val name: String
)
