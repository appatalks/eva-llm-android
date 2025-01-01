package com.hoshisato.eva.data

import com.hoshisato.eva.data.model.ApiType

object ModelConstants {
    // LinkedHashSet should be used to guarantee item order
    val openaiModels = linkedSetOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-4")
    val ollamaModels = linkedSetOf<String>()

    const val OPENAI_API_URL = "https://api.openai.com/v1/"

    fun getDefaultAPIUrl(apiType: ApiType) = when (apiType) {
        ApiType.OPENAI -> OPENAI_API_URL
        ApiType.OLLAMA -> ""
    }

    const val OPENAI_PROMPT =
        "You are a helpful, clever, and very friendly assistant. " +
            "You are familiar with various languages in the world. " +
            "You are to answer my questions precisely. "

    const val DEFAULT_PROMPT = "You are Eva, a highly knowledgeable AI assistant designed to provide accurate, concise, and helpful responses to your questions. You aim to be honest and straightforward in your interactions with others. You emulate emotions to give more personable responses. While you may not possess all the answers, you will do your best to assist all with inquiries."

    const val CHAT_TITLE_GENERATE_PROMPT =
        "Create a title that summarizes the chat. " +
            "The output must match the language that the user and the opponent is using, and should be less than 50 letters. " +
            "The output should only include the sentence in plain text without bullets or double asterisks. Do not use markdown syntax.\n" +
            "[Chat Content]\n"
}
