package com.hoshisato.eva.presentation.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.hoshisato.eva.data.ModelConstants.ollamaModels
import com.hoshisato.eva.data.ModelConstants.openaiModels
import com.hoshisato.eva.data.dto.Platform
import com.hoshisato.eva.data.model.ApiType
import com.hoshisato.eva.data.repository.SettingRepository
import com.hoshisato.eva.presentation.common.Route
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SetupViewModel @Inject constructor(private val settingRepository: SettingRepository) : ViewModel() {

    private val _platformState = MutableStateFlow(
        listOf(
        //    Platform(ApiType.OPENAI),
            Platform(ApiType.OLLAMA)
        )
    )
    val platformState: StateFlow<List<Platform>> = _platformState.asStateFlow()

    fun updateAPIAddress(platform: Platform, address: String) {
        val index = _platformState.value.indexOf(platform)

        if (index >= 0) {
            _platformState.update {
                it.mapIndexed { i, p ->
                    if (index == i) {
                        p.copy(apiUrl = address.trim())
                    } else {
                        p
                    }
                }
            }
        }
    }

    fun updateCheckedState(platform: Platform) {
        val index = _platformState.value.indexOf(platform)

        if (index >= 0) {
            _platformState.update {
                it.mapIndexed { i, p ->
                    if (index == i) {
                        p.copy(selected = p.selected.not())
                    } else {
                        p
                    }
                }
            }
        }
    }

    fun updateToken(platform: Platform, token: String) {
        val index = _platformState.value.indexOf(platform)

        if (index >= 0) {
            _platformState.update {
                it.mapIndexed { i, p ->
                    if (index == i) {
                        p.copy(token = token.ifBlank { null })
                    } else {
                        p
                    }
                }
            }
        }
    }

    fun updateModel(apiType: ApiType, model: String) {
        val index = _platformState.value.indexOfFirst { it.name == apiType }

        if (index >= 0) {
            _platformState.update {
                it.mapIndexed { i, p ->
                    if (index == i) {
                        p.copy(model = model)
                    } else {
                        p
                    }
                }
            }
        }
    }

    fun savePlatformState() {
        _platformState.update { platforms ->
            // Update to platform enabled value
            platforms.map { p ->
                p.copy(enabled = p.selected, selected = false)
            }
        }
        viewModelScope.launch {
            settingRepository.updatePlatforms(_platformState.value)
        }
    }

    fun getNextSetupRoute(currentRoute: String?): String {
        val steps = listOf(
            Route.SELECT_PLATFORM,
            Route.TOKEN_INPUT,
            Route.OPENAI_MODEL_SELECT,
            Route.OLLAMA_MODEL_SELECT,
            Route.OLLAMA_API_ADDRESS,
            Route.SETUP_COMPLETE
        )
        val commonSteps = mutableSetOf(Route.SELECT_PLATFORM, Route.TOKEN_INPUT, Route.SETUP_COMPLETE)
        val platformStep = mapOf(
            Route.OPENAI_MODEL_SELECT to ApiType.OPENAI,
            Route.OLLAMA_MODEL_SELECT to ApiType.OLLAMA,
            Route.OLLAMA_API_ADDRESS to ApiType.OLLAMA
        )

        val currentIndex = steps.indexOfFirst { it == currentRoute }
        val enabledPlatform = platformState.value.filter { it.selected }.map { it.name }.toSet()

        if (enabledPlatform.size == 1 && ApiType.OLLAMA in enabledPlatform) {
            // Skip API Token input page
            commonSteps.remove(Route.TOKEN_INPUT)
        }

        val remainingSteps = steps.filterIndexed { index, setupStep ->
            index > currentIndex &&
                (setupStep in commonSteps || platformStep[setupStep] in enabledPlatform)
        }

        if (remainingSteps.isEmpty()) {
            // Setup Complete
            return Route.CHAT_LIST
        }

        return remainingSteps.first()
    }

    fun setDefaultModel(apiType: ApiType, defaultModelIndex: Int): String {
        val modelList = when (apiType) {
            ApiType.OPENAI -> openaiModels
            ApiType.OLLAMA -> ollamaModels
        }.toList()

        if (modelList.size <= defaultModelIndex) {
            return ""
        }

        val model = modelList[defaultModelIndex]
        updateModel(apiType, model)

        return model
    }
}
