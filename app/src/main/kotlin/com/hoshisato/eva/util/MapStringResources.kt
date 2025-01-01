package com.hoshisato.eva.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hoshisato.eva.R
import com.hoshisato.eva.data.dto.APIModel
import com.hoshisato.eva.data.model.ApiType
import com.hoshisato.eva.data.model.DynamicTheme
import com.hoshisato.eva.data.model.ThemeMode

@Composable
fun getPlatformTitleResources(): Map<ApiType, String> = mapOf(
    ApiType.OPENAI to stringResource(R.string.openai),
    ApiType.OLLAMA to stringResource(R.string.ollama)
)

@Composable
fun getPlatformDescriptionResources(): Map<ApiType, String> = mapOf(
    ApiType.OPENAI to stringResource(R.string.openai_description),
    ApiType.OLLAMA to stringResource(R.string.ollama_description)
)

@Composable
fun getPlatformAPILabelResources(): Map<ApiType, String> = mapOf(
    ApiType.OPENAI to stringResource(R.string.openai_api_key),
    ApiType.OLLAMA to stringResource(R.string.ollama_api_key)
)

@Composable
fun getPlatformHelpLinkResources(): Map<ApiType, String> = mapOf(
    ApiType.OPENAI to stringResource(R.string.openai_api_help),
    ApiType.OLLAMA to stringResource(R.string.ollama_api_help)
)

@Composable
fun generateOpenAIModelList(models: LinkedHashSet<String>) = models.mapIndexed { index, model ->
    val (name, description) = when (index) {
        0 -> stringResource(R.string.gpt_4o) to stringResource(R.string.gpt_4o_description)
        1 -> stringResource(R.string.gpt_4o_mini) to stringResource(R.string.gpt_4o_mini_description)
        2 -> stringResource(R.string.gpt_4_turbo) to stringResource(R.string.gpt_4_turbo_description)
        3 -> stringResource(R.string.gpt_4) to stringResource(R.string.gpt_4_description)
        else -> "" to ""
    }
    APIModel(name, description, model)
}

@Composable
fun getAPIModelSelectTitle(apiType: ApiType) = when (apiType) {
    ApiType.OPENAI -> stringResource(R.string.select_openai_model)
    ApiType.OLLAMA -> stringResource(R.string.select_ollama_model)
}

@Composable
fun getAPIModelSelectDescription(apiType: ApiType) = when (apiType) {
    ApiType.OPENAI -> stringResource(R.string.select_openai_model_description)
    ApiType.OLLAMA -> stringResource(id = R.string.select_ollama_model_description)
}

@Composable
fun getDynamicThemeTitle(theme: DynamicTheme) = when (theme) {
    DynamicTheme.ON -> stringResource(R.string.on)
    DynamicTheme.OFF -> stringResource(R.string.off)
}

@Composable
fun getThemeModeTitle(theme: ThemeMode) = when (theme) {
    ThemeMode.SYSTEM -> stringResource(R.string.system_default)
    ThemeMode.DARK -> stringResource(R.string.on)
    ThemeMode.LIGHT -> stringResource(R.string.off)
}

@Composable
fun getPlatformSettingTitle(apiType: ApiType) = when (apiType) {
    ApiType.OPENAI -> stringResource(R.string.openai_setting)
    ApiType.OLLAMA -> stringResource(R.string.ollama_setting)
}

@Composable
fun getPlatformSettingDescription(apiType: ApiType) = when (apiType) {
    ApiType.OPENAI -> stringResource(R.string.platform_setting_description)
    ApiType.OLLAMA -> stringResource(R.string.platform_setting_description)
}

@Composable
fun getPlatformAPIBrandText(apiType: ApiType) = when (apiType) {
    ApiType.OPENAI -> stringResource(R.string.openai_brand_text)
    ApiType.OLLAMA -> stringResource(R.string.ollama_brand_text)
}
