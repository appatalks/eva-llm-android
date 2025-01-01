package com.hoshisato.eva.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.hoshisato.eva.data.database.dao.ChatRoomDao
import com.hoshisato.eva.data.database.dao.MessageDao
import com.hoshisato.eva.data.repository.ChatRepository
import com.hoshisato.eva.data.repository.ChatRepositoryImpl
import com.hoshisato.eva.data.repository.SettingRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatRepositoryModule {

    @Provides
    @Singleton
    fun provideChatRepository(
        @ApplicationContext appContext: Context,
        chatRoomDao: ChatRoomDao,
        messageDao: MessageDao,
        settingRepository: SettingRepository
    ): ChatRepository = ChatRepositoryImpl(appContext, chatRoomDao, messageDao, settingRepository)
}
