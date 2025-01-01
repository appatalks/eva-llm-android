package com.hoshisato.eva.data.repository

import com.hoshisato.eva.data.database.entity.ChatRoom
import com.hoshisato.eva.data.database.entity.Message
import com.hoshisato.eva.data.dto.ApiState
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    suspend fun completeOpenAIChat(question: Message, history: List<Message>): Flow<ApiState>
    suspend fun completeOllamaChat(question: Message, history: List<Message>): Flow<ApiState>
    suspend fun fetchChatList(): List<ChatRoom>
    suspend fun fetchMessages(chatId: Int): List<Message>
    fun generateDefaultChatTitle(messages: List<Message>): String?
    suspend fun updateChatTitle(chatRoom: ChatRoom, title: String)
    suspend fun saveChat(chatRoom: ChatRoom, messages: List<Message>): ChatRoom
    suspend fun deleteChats(chatRooms: List<ChatRoom>)
}
