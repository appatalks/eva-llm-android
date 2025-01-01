package com.hoshisato.eva.data.dto

import com.hoshisato.eva.data.model.DynamicTheme
import com.hoshisato.eva.data.model.ThemeMode

data class ThemeSetting(
    val dynamicTheme: DynamicTheme = DynamicTheme.OFF,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)
