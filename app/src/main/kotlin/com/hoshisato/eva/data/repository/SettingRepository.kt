package com.hoshisato.eva.data.repository

import com.hoshisato.eva.data.dto.Platform
import com.hoshisato.eva.data.dto.ThemeSetting

interface SettingRepository {
    suspend fun fetchPlatforms(): List<Platform>
    suspend fun fetchThemes(): ThemeSetting
    suspend fun updatePlatforms(platforms: List<Platform>)
    suspend fun updateThemes(themeSetting: ThemeSetting)
}
