package com.hoshisato.eva.data.datastore

import com.hoshisato.eva.data.model.ApiType
import com.hoshisato.eva.data.model.DynamicTheme
import com.hoshisato.eva.data.model.ThemeMode

interface SettingDataSource {
    suspend fun updateDynamicTheme(theme: DynamicTheme)
    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun updateStatus(apiType: ApiType, status: Boolean)
    suspend fun updateAPIUrl(apiType: ApiType, url: String)
    suspend fun updateToken(apiType: ApiType, token: String)
    suspend fun updateModel(apiType: ApiType, model: String)
    suspend fun updateTemperature(apiType: ApiType, temperature: Float)
    suspend fun updateTopP(apiType: ApiType, topP: Float)
    suspend fun updateSystemPrompt(apiType: ApiType, prompt: String)
    suspend fun getDynamicTheme(): DynamicTheme?
    suspend fun getThemeMode(): ThemeMode?
    suspend fun getStatus(apiType: ApiType): Boolean?
    suspend fun getAPIUrl(apiType: ApiType): String?
    suspend fun getToken(apiType: ApiType): String?
    suspend fun getModel(apiType: ApiType): String?
    suspend fun getTemperature(apiType: ApiType): Float?
    suspend fun getTopP(apiType: ApiType): Float?
    suspend fun getSystemPrompt(apiType: ApiType): String?
}
