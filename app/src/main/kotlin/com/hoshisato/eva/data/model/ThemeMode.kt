package com.hoshisato.eva.data.model

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT;

    companion object {
        fun getByValue(value: Int) = entries.firstOrNull { it.ordinal == value }
    }
}
