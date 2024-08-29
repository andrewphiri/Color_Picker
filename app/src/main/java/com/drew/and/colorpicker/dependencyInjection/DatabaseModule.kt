package com.drew.and.colorpicker.dependencyInjection

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.drew.and.colorpicker.data.ColorAppDatabase
import com.drew.and.colorpicker.data.ColorDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideColorDao(database: ColorAppDatabase): ColorDao {
        return database.colorDao()
    }

    @Provides
    fun provideColorAppDatabase(@ApplicationContext context: Context): ColorAppDatabase {
        return return Room.databaseBuilder(
            context = context,
            ColorAppDatabase::class.java,
        "COLOR_DATABASE"
        )
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .fallbackToDestructiveMigration()
            .build()
    }
}