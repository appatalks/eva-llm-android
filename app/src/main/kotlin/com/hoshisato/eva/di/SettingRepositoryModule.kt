package com.hoshisato.eva.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.hoshisato.eva.data.datastore.SettingDataSource
import com.hoshisato.eva.data.repository.SettingRepository
import com.hoshisato.eva.data.repository.SettingRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingRepositoryModule {

    @Provides
    @Singleton
    fun provideSettingRepository(
        settingDataSource: SettingDataSource
    ): SettingRepository = SettingRepositoryImpl(settingDataSource)
}
