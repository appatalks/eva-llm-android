package com.hoshisato.eva.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hoshisato.eva.data.database.dao.ChatRoomDao
import com.hoshisato.eva.data.database.dao.MessageDao
import com.hoshisato.eva.data.database.entity.APITypeConverter
import com.hoshisato.eva.data.database.entity.ChatRoom
import com.hoshisato.eva.data.database.entity.Message

@Database(entities = [ChatRoom::class, Message::class], version = 1)
@TypeConverters(APITypeConverter::class)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun chatRoomDao(): ChatRoomDao
    abstract fun messageDao(): MessageDao
}
