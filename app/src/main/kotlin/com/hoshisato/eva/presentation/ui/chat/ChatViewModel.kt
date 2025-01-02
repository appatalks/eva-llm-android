@file:Suppress("SpellCheckingInspection")

package com.hoshisato.eva.presentation.ui.chat

import android.content.Intent
import android.util.Log
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.hoshisato.eva.data.database.entity.ChatRoom
import com.hoshisato.eva.data.database.entity.Message
import com.hoshisato.eva.data.dto.ApiState
import com.hoshisato.eva.data.model.ApiType
import com.hoshisato.eva.data.repository.ChatRepository
import com.hoshisato.eva.data.repository.SettingRepository
import com.hoshisato.eva.util.handleStates
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val application: Application,
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {

    sealed class LoadingState {
        data object Idle : LoadingState()
        data object Loading : LoadingState()
    }

    private val chatRoomId: Int = checkNotNull(savedStateHandle["chatRoomId"])
    private val enabledPlatformString: String = checkNotNull(savedStateHandle["enabledPlatforms"])
    val enabledPlatformsInChat = enabledPlatformString.split(',').map { s -> ApiType.valueOf(s) }
    private val currentTimeStamp: Long
        get() = System.currentTimeMillis() / 1000

    private val _chatRoom = MutableStateFlow<ChatRoom>(ChatRoom(id = -1, title = "", enabledPlatform = enabledPlatformsInChat))
    val chatRoom = _chatRoom.asStateFlow()

    private val _isChatTitleDialogOpen = MutableStateFlow(false)
    val isChatTitleDialogOpen = _isChatTitleDialogOpen.asStateFlow()

    private val _isEditQuestionDialogOpen = MutableStateFlow(false)
    val isEditQuestionDialogOpen = _isEditQuestionDialogOpen.asStateFlow()

    // Enabled platforms list
    private val _enabledPlatformsInApp = MutableStateFlow(listOf<ApiType>())
    val enabledPlatformsInApp = _enabledPlatformsInApp.asStateFlow()

    // List of question & answers (User, Assistant)
    private val _messages = MutableStateFlow(listOf<Message>())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // User input used for TextField
    private val _question = MutableStateFlow("")
    val question: StateFlow<String> = _question.asStateFlow()

    // Used for passing user question to Edit User Message Dialog
    private val _editedQuestion = MutableStateFlow(Message(chatId = chatRoomId, content = "", platformType = null))
    val editedQuestion = _editedQuestion.asStateFlow()

    // Loading state for each platforms
    private val _openaiLoadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val openaiLoadingState = _openaiLoadingState.asStateFlow()

    private val _ollamaLoadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val ollamaLoadingState = _ollamaLoadingState.asStateFlow()

    private val _geminiNanoLoadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val geminiNanoLoadingState = _geminiNanoLoadingState.asStateFlow()

    // Total loading state. It should be updated if one of the loading state has changed.
    // If all loading states are idle, this value should have `true`.
    private val _isIdle = MutableStateFlow(true)
    val isIdle = _isIdle.asStateFlow()

    // State for the message loading state (From the database)
    private val _isLoaded = MutableStateFlow(false)
    val isLoaded = _isLoaded.asStateFlow()

    // Currently active(chat completion) user input. This is used when user input is sent.
    private val _userMessage = MutableStateFlow(Message(chatId = chatRoomId, content = "", platformType = null))
    val userMessage = _userMessage.asStateFlow()

    // Currently active(chat completion) assistant output. This is used when data is received from the API.
    private val _openAIMessage = MutableStateFlow(Message(chatId = chatRoomId, content = "", platformType = ApiType.OPENAI))
    val openAIMessage = _openAIMessage.asStateFlow()

    private val _ollamaMessage = MutableStateFlow(Message(chatId = chatRoomId, content = "", platformType = ApiType.OLLAMA))
    val ollamaMessage = _ollamaMessage.asStateFlow()

    private val _geminiNanoMessage = MutableStateFlow(Message(chatId = chatRoomId, content = "", platformType = null))
    val geminiNanoMessage = _geminiNanoMessage.asStateFlow()

    // Flows for assistant message streams
    private val openAIFlow = MutableSharedFlow<ApiState>()
    private val ollamaFlow = MutableSharedFlow<ApiState>()
    private val geminiNanoFlow = MutableSharedFlow<ApiState>()

    init {
        Log.d("ViewModel", "$chatRoomId")
        Log.d("ViewModel", "$enabledPlatformsInChat")
        fetchChatRoom()
        viewModelScope.launch { fetchMessages() }
        fetchEnabledPlatformsInApp()
        observeFlow()
    }

    fun askQuestion() {
        Log.d("Question: ", _question.value)
        _userMessage.update { it.copy(content = _question.value, createdAt = currentTimeStamp) }
        _question.update { "" }
        completeChat()
    }

    fun closeChatTitleDialog() = _isChatTitleDialogOpen.update { false }

    fun closeEditQuestionDialog() {
        _editedQuestion.update { Message(chatId = chatRoomId, content = "", platformType = null) }
        _isEditQuestionDialogOpen.update { false }
    }

    fun editQuestion(q: Message) {
        _messages.update { it.filter { message -> message.id < q.id && message.createdAt < q.createdAt } }
        _userMessage.update { it.copy(content = q.content, createdAt = currentTimeStamp) }
        completeChat()
    }

    fun openChatTitleDialog() = _isChatTitleDialogOpen.update { true }

/*    fun openEditQuestionDialog(question: Message) {
        _editedQuestion.update { question }
        _isEditQuestionDialogOpen.update { true }
    }*/

    fun generateDefaultChatTitle(): String? = chatRepository.generateDefaultChatTitle(_messages.value)

    fun generateAIChatTitle() {
        viewModelScope.launch {
            _geminiNanoLoadingState.update { LoadingState.Loading }
            _geminiNanoMessage.update { it.copy(content = "") }
        }
    }

    fun retryQuestion(message: Message) {
        val latestQuestionIndex = _messages.value.indexOfLast { it.platformType == null }

        if (latestQuestionIndex != -1 && _isIdle.value) {
            // Update user input to latest question
            _userMessage.update { _messages.value[latestQuestionIndex] }

            // Get previous answers from the assistant
            val previousAnswers = enabledPlatformsInChat.mapNotNull { apiType -> _messages.value.lastOrNull { it.platformType == apiType } }

            // Remove latest question & answers
            _messages.update { it - setOf(_messages.value[latestQuestionIndex]) - previousAnswers.toSet() }

            // Restore messages that are not retrying
            enabledPlatformsInChat.forEach { apiType ->
                when (apiType) {
                    message.platformType -> {}
                    else -> restoreMessageState(apiType, previousAnswers)
                }
            }
        }
        message.platformType?.let { updateLoadingState(it, LoadingState.Loading) }

        when (message.platformType) {
            ApiType.OPENAI -> {
                _openAIMessage.update { it.copy(id = message.id, content = "", createdAt = currentTimeStamp) }
                completeOpenAIChat()
            }

            ApiType.OLLAMA -> {
                _ollamaMessage.update { it.copy(id = message.id, content = "", createdAt = currentTimeStamp) }
                completeOllamaChat()
            }

            else -> {}
        }
    }

    fun updateChatTitle(title: String) {
        // Should be only used for changing chat title after the chatroom is created.
        if (_chatRoom.value.id > 0) {
            _chatRoom.update { it.copy(title = title) }
            viewModelScope.launch {
                chatRepository.updateChatTitle(_chatRoom.value, title)
            }
        }
    }

    fun exportChat() {
        val context = application.applicationContext

        // Build the chat history in Markdown format
        val chatHistoryMarkdown = buildString {
            appendLine("# Chat Export: \"${chatRoom.value.title}\"")
            appendLine()
            appendLine("**Exported on:** ${formatCurrentDateTime()}")
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## Chat History")
            appendLine()
            messages.value.forEach { message ->
                val sender = if (message.platformType == null) "User" else "Eva"
                appendLine("**$sender:**")
                appendLine(message.content)
                appendLine()
            }
        }

        // Save the Markdown file
        val fileName = "export_${chatRoom.value.title}_${System.currentTimeMillis()}.md"
        val file = java.io.File(context.getExternalFilesDir(null), fileName)
        file.writeText(chatHistoryMarkdown)

        // Share the file
        // https://stackoverflow.com/questions/56598480/couldnt-find-meta-data-for-provider-with-authority
        val uri = androidx.core.content.FileProvider.getUriForFile(context, "com.hoshisato.eva" + ".fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/markdown"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Chat Export").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun formatCurrentDateTime(): String {
        val currentDate = java.util.Date()
        val format = java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a", java.util.Locale.getDefault())
        return format.format(currentDate)
    }

    fun updateQuestion(q: String) = _question.update { q }

    private fun addMessage(message: Message) = _messages.update { it + listOf(message) }

    private fun clearQuestionAndAnswers() {
        _userMessage.update { it.copy(id = 0, content = "") }
        _openAIMessage.update { it.copy(id = 0, content = "") }
        _ollamaMessage.update { it.copy(id = 0, content = "") }
    }

    private fun completeChat() {
        enabledPlatformsInChat.forEach { apiType -> updateLoadingState(apiType, LoadingState.Loading) }
        val enabledPlatforms = enabledPlatformsInChat.toSet()

        if (ApiType.OPENAI in enabledPlatforms) {
            completeOpenAIChat()
        }

        if (ApiType.OLLAMA in enabledPlatforms) {
            completeOllamaChat()
        }
    }

    private fun completeOllamaChat() {
        viewModelScope.launch {
            val chatFlow = chatRepository.completeOllamaChat(question = _userMessage.value, history = _messages.value)
            chatFlow.collect { chunk -> ollamaFlow.emit(chunk) }
        }
    }

    private fun completeOpenAIChat() {
        viewModelScope.launch {
            val chatFlow = chatRepository.completeOpenAIChat(question = _userMessage.value, history = _messages.value)
            chatFlow.collect { chunk -> openAIFlow.emit(chunk) }
        }
    }

    private suspend fun fetchMessages() {
        // If the room isn't new
        if (chatRoomId != 0) {
            _messages.update { chatRepository.fetchMessages(chatRoomId) }
            _isLoaded.update { true } // Finish fetching
            return
        }

        // When message id should sync after saving chats
        if (_chatRoom.value.id != 0) {
            _messages.update { chatRepository.fetchMessages(_chatRoom.value.id) }
            return
        }
    }

    private fun fetchChatRoom() {
        viewModelScope.launch {
            _chatRoom.update {
                if (chatRoomId == 0) {
                    ChatRoom(id = 0, title = "Untitled Chat", enabledPlatform = enabledPlatformsInChat)
                } else {
                    chatRepository.fetchChatList().first { it.id == chatRoomId }
                }
            }
            Log.d("ViewModel", "chatroom: $chatRoom")
        }
    }

    private fun fetchEnabledPlatformsInApp() {
        viewModelScope.launch {
            val enabled = settingRepository.fetchPlatforms().filter { it.enabled }.map { it.name }
            _enabledPlatformsInApp.update { enabled }
        }
    }

    private fun observeFlow() {
        viewModelScope.launch {
            openAIFlow.handleStates(
                messageFlow = _openAIMessage,
                onLoadingComplete = { updateLoadingState(ApiType.OPENAI, LoadingState.Idle) }
            )
        }

        viewModelScope.launch {
            ollamaFlow.handleStates(
                messageFlow = _ollamaMessage,
                onLoadingComplete = { updateLoadingState(ApiType.OLLAMA, LoadingState.Idle) }
            )
        }

        viewModelScope.launch {
            geminiNanoFlow.handleStates(
                messageFlow = _geminiNanoMessage,
                onLoadingComplete = { _geminiNanoLoadingState.update { LoadingState.Idle } }
            )
        }

        viewModelScope.launch {
            _isIdle.collect { status ->
                if (status) {
                    Log.d("status", "val: ${_userMessage.value}")
                    if (_chatRoom.value.id != -1 && _userMessage.value.content.isNotBlank()) {
                        syncQuestionAndAnswers()
                        Log.d("message", "${_messages.value}")
                        _chatRoom.update { chatRepository.saveChat(_chatRoom.value, _messages.value) }
                        fetchMessages() // For syncing message ids
                    }
                    clearQuestionAndAnswers()
                }
            }
        }
    }

    private fun restoreMessageState(apiType: ApiType, previousAnswers: List<Message>) {
        val message = previousAnswers.firstOrNull { it.platformType == apiType }
        val retryingState = when (apiType) {
            ApiType.OPENAI -> _openaiLoadingState
            ApiType.OLLAMA -> _ollamaLoadingState
        }

        if (retryingState == LoadingState.Loading) return
        if (message == null) return

        when (apiType) {
            ApiType.OPENAI -> _openAIMessage.update { message }
            ApiType.OLLAMA -> _ollamaMessage.update { message }
        }
    }

    private fun syncQuestionAndAnswers() {
        addMessage(_userMessage.value)
        val enabledPlatforms = enabledPlatformsInChat.toSet()

        if (ApiType.OPENAI in enabledPlatforms) {
            addMessage(_openAIMessage.value)
        }

        if (ApiType.OLLAMA in enabledPlatforms) {
            addMessage(_ollamaMessage.value)
        }
    }

    private fun updateLoadingState(apiType: ApiType, loadingState: LoadingState) {
        when (apiType) {
            ApiType.OPENAI -> _openaiLoadingState.update { loadingState }
            ApiType.OLLAMA -> _ollamaLoadingState.update { loadingState }
        }

        var result = true
        enabledPlatformsInChat.forEach {
            val state = when (it) {
                ApiType.OPENAI -> _openaiLoadingState
                ApiType.OLLAMA -> _ollamaLoadingState
            }

            result = result && (state.value is LoadingState.Idle)
        }

        _isIdle.update { result }
    }
}
