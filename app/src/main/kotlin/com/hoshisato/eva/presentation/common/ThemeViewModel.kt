package com.hoshisato.eva.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.hoshisato.eva.data.dto.ThemeSetting
import com.hoshisato.eva.data.model.DynamicTheme
import com.hoshisato.eva.data.model.ThemeMode
import com.hoshisato.eva.data.repository.SettingRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ThemeViewModel @Inject constructor(private val settingRepository: SettingRepository) : ViewModel() {

    private val _themeSetting = MutableStateFlow(ThemeSetting())
    val themeSetting = _themeSetting.asStateFlow()

    init {
        fetchThemes()
    }

    private fun fetchThemes() {
        viewModelScope.launch {
            _themeSetting.update { settingRepository.fetchThemes() }
        }
    }

    fun updateDynamicTheme(theme: DynamicTheme) {
        _themeSetting.update { setting ->
            setting.copy(dynamicTheme = theme)
        }
        viewModelScope.launch {
            settingRepository.updateThemes(_themeSetting.value)
        }
    }

    fun updateThemeMode(theme: ThemeMode) {
        _themeSetting.update { setting ->
            setting.copy(themeMode = theme)
        }
        viewModelScope.launch {
            settingRepository.updateThemes(_themeSetting.value)
        }
    }
}
