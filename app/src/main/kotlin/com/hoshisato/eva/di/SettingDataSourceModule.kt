package com.hoshisato.eva.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.hoshisato.eva.data.datastore.SettingDataSource
import com.hoshisato.eva.data.datastore.SettingDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingDataSourceModule {
    @Provides
    @Singleton
    fun provideSettingDataStore(dataStore: DataStore<Preferences>): SettingDataSource = SettingDataSourceImpl(dataStore)
}
