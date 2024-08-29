package com.drew.and.colorpicker.dependencyInjection

import com.drew.and.colorpicker.data.ColorRepository
import com.drew.and.colorpicker.data.ColorRepositoryImplementation
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    abstract fun bindColorRepository(impl: ColorRepositoryImplementation): ColorRepository

}