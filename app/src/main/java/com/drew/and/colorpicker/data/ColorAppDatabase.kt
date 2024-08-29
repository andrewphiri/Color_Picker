package com.drew.and.colorpicker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(version = 1, entities = [ColorEntity::class], exportSchema = false)
abstract class ColorAppDatabase : RoomDatabase() {
    abstract fun colorDao(): ColorDao

}