package com.hoshisato.eva.presentation.ui.chat

import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoshisato.eva.R
import com.hoshisato.eva.data.database.entity.Message
import com.hoshisato.eva.data.model.ApiType
import com.hoshisato.eva.util.DefaultHashMap
import com.hoshisato.eva.util.multiScrollStateSaver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = hiltViewModel(),
    onBackAction: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val focusManager = LocalFocusManager.current
    /*val clipboardManager = LocalClipboardManager.current*/
    val packageManager = LocalContext.current.packageManager
    val systemChatMargin = 32.dp
    val maximumChatBubbleWidth = screenWidth - 48.dp - systemChatMargin
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val aiCorePackageInfo = try {
        packageManager.getPackageInfo("com.google.android.aicore", 0)
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }
    val privateComputePackageInfo = try {
        packageManager.getPackageInfo("com.google.android.as.oss", 0)
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

    val chatRoom by chatViewModel.chatRoom.collectAsStateWithLifecycle()
    val isChatTitleDialogOpen by chatViewModel.isChatTitleDialogOpen.collectAsStateWithLifecycle()
    val isEditQuestionDialogOpen by chatViewModel.isEditQuestionDialogOpen.collectAsStateWithLifecycle()
    val isIdle by chatViewModel.isIdle.collectAsStateWithLifecycle()
    val isLoaded by chatViewModel.isLoaded.collectAsStateWithLifecycle()
    val messages by chatViewModel.messages.collectAsStateWithLifecycle()
    val question by chatViewModel.question.collectAsStateWithLifecycle()
    val appEnabledPlatforms by chatViewModel.enabledPlatformsInApp.collectAsStateWithLifecycle()
    val editedQuestion by chatViewModel.editedQuestion.collectAsStateWithLifecycle()
    val openaiLoadingState by chatViewModel.openaiLoadingState.collectAsStateWithLifecycle()
    val ollamaLoadingState by chatViewModel.ollamaLoadingState.collectAsStateWithLifecycle()
    val geminiNanoLoadingState by chatViewModel.geminiNanoLoadingState.collectAsStateWithLifecycle()
    val userMessage by chatViewModel.userMessage.collectAsStateWithLifecycle()
    val openAIMessage by chatViewModel.openAIMessage.collectAsStateWithLifecycle()
    val ollamaMessage by chatViewModel.ollamaMessage.collectAsStateWithLifecycle()
    val geminiNano by chatViewModel.geminiNanoMessage.collectAsStateWithLifecycle()
    val canUseChat = (chatViewModel.enabledPlatformsInChat.toSet() - appEnabledPlatforms.toSet()).isEmpty()
    val groupedMessages = remember(messages) { groupMessages(messages) }
    val latestMessageIndex = groupedMessages.keys.maxOrNull() ?: 0
    val chatBubbleScrollStates = rememberSaveable(saver = multiScrollStateSaver) { DefaultHashMap<Int, ScrollState> { ScrollState(0) } }
/*  val canEnableAICoreMode = rememberSaveable { checkAICoreAvailability(aiCorePackageInfo, privateComputePackageInfo) } */

    val context = LocalContext.current
    var base64String by remember { mutableStateOf("") }
/*    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // you would then pass the uri to the viewmodel
                chatViewModel.handleImageSelection(uri)
            }
        }
    }*/
/*    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                base64String = bitmapToBase64(bitmap)
                Toast.makeText(context, "Image Selected", Toast.LENGTH_SHORT).show()
                // You can now send the base64String to the chat or pass it to the chatViewModel
            }
        }else {
            Toast.makeText(context, "Image Selection Cancelled", Toast.LENGTH_SHORT).show()
        }
    }*/

    val scope = rememberCoroutineScope()

    LaunchedEffect(isIdle) {
        listState.animateScrollToItem(groupedMessages.keys.size)
    }

    LaunchedEffect(isLoaded) {
        delay(300)
        listState.animateScrollToItem(groupedMessages.keys.size)
    }

    Log.d("AIPackage", "AICore: ${aiCorePackageInfo?.versionName ?: "Not installed"}, Private Compute Services: ${privateComputePackageInfo?.versionName ?: "Not installed"}")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { focusManager.clearFocus() },
        topBar = { ChatTopBar(chatRoom.title, chatRoom.id > 0, onBackAction, scrollBehavior, chatViewModel::openChatTitleDialog, onExportChatItemClick = chatViewModel::exportChat) },
        bottomBar = {
            ChatInputBox(
                value = question,
                onValueChange = { s -> chatViewModel.updateQuestion(s) },
                chatEnabled = canUseChat,
                sendButtonEnabled = question.trim().isNotBlank() && isIdle,
                onSendButtonClick = {
                    chatViewModel.askQuestion()
                    focusManager.clearFocus()
                })
        },
        floatingActionButton = {
            if (listState.canScrollForward) {
                ScrollToBottomButton {
                    scope.launch {
                        listState.animateScrollToItem(groupedMessages.keys.size)
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        groupedMessages.forEach { (i, k) -> Log.d("grouped", "idx: $i, data: $k") }
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            state = listState
        ) {
            groupedMessages.keys.sorted().forEach { key ->
                if (key % 2 == 0) {
                    // User
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            UserChatBubble(
                                modifier = Modifier.widthIn(max = maximumChatBubbleWidth),
                                text = groupedMessages[key]!![0].content,
                                isLoading = !isIdle
                            )
                        }
                    }
                } else {
                    // Assistant
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(chatBubbleScrollStates[(key - 1) / 2])
                        ) {
                            Spacer(modifier = Modifier.width(8.dp))
                            groupedMessages[key]!!.sortedBy { it.platformType }.forEach { m ->
                                m.platformType?.let { apiType ->
                                    OpponentChatBubble(
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 12.dp)
                                            .widthIn(max = maximumChatBubbleWidth),
                                        canRetry = canUseChat && isIdle && key >= latestMessageIndex,
                                        isLoading = false,
                                        apiType = apiType,
                                        text = m.content,
                                        /*onCopyClick = { clipboardManager.setText(AnnotatedString(m.content.trim())) },*/
                                        onRetryClick = { chatViewModel.retryQuestion(m) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(systemChatMargin))
                        }
                    }
                }
            }

            if (!isIdle) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        UserChatBubble(
                            modifier = Modifier.widthIn(max = maximumChatBubbleWidth),
                            text = userMessage.content,
                            isLoading = true
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(chatBubbleScrollStates[(latestMessageIndex + 1) / 2])
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        chatViewModel.enabledPlatformsInChat.sorted().forEach { apiType ->
                            val message = when (apiType) {
                                ApiType.OPENAI -> openAIMessage
                                ApiType.OLLAMA -> ollamaMessage
                            }

                            val loadingState = when (apiType) {
                                ApiType.OPENAI -> openaiLoadingState
                                ApiType.OLLAMA -> ollamaLoadingState
                            }

                            OpponentChatBubble(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 12.dp)
                                    .widthIn(max = maximumChatBubbleWidth),
                                canRetry = canUseChat,
                                isLoading = loadingState == ChatViewModel.LoadingState.Loading,
                                apiType = apiType,
                                text = message.content,
                                /*onCopyClick = { clipboardManager.setText(AnnotatedString(message.content.trim())) },*/
                                onRetryClick = { chatViewModel.retryQuestion(message) }
                            )
                        }
                        Spacer(modifier = Modifier.width(systemChatMargin))
                    }
                }
            }
        }

        if (isChatTitleDialogOpen) {
            ChatTitleDialog(
                initialTitle = chatRoom.title,
                aiCoreModeEnabled = false,
                aiGeneratedResult = geminiNano.content,
                isAICoreLoading = geminiNanoLoadingState == ChatViewModel.LoadingState.Loading,
                onDefaultTitleMode = chatViewModel::generateDefaultChatTitle,
                onAICoreTitleMode = chatViewModel::generateAIChatTitle,
                onRetryRequest = chatViewModel::generateAIChatTitle,
                onConfirmRequest = { title -> chatViewModel.updateChatTitle(title) },
                onDismissRequest = chatViewModel::closeChatTitleDialog
            )
        }

        if (isEditQuestionDialogOpen) {
            ChatQuestionEditDialog(
                initialQuestion = editedQuestion,
                onDismissRequest = chatViewModel::closeEditQuestionDialog,
                onConfirmRequest = { question ->
                    chatViewModel.editQuestion(question)
                    chatViewModel.closeEditQuestionDialog()
                }
            )
        }
    }
}

private fun groupMessages(messages: List<Message>): HashMap<Int, MutableList<Message>> {
    val classifiedMessages = hashMapOf<Int, MutableList<Message>>()
    var counter = 0

    messages.sortedBy { it.createdAt }.forEach { message ->
        if (message.platformType == null) {
            if (classifiedMessages.containsKey(counter) || counter % 2 == 1) {
                counter++
            }

            classifiedMessages[counter] = mutableListOf(message)
            counter++
        } else {
            if (counter % 2 == 0) {
                counter++
            }

            if (classifiedMessages.containsKey(counter)) {
                classifiedMessages[counter]?.add(message)
            } else {
                classifiedMessages[counter] = mutableListOf(message)
            }
        }
    }
    return classifiedMessages
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ChatTopBar(
    title: String,
    isMenuItemEnabled: Boolean,
    onBackAction: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    onChatTitleItemClick: () -> Unit,
    onExportChatItemClick: () -> Unit
) {
    var isDropDownMenuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(
                onClick = onBackAction
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.go_back))
            }
        },
        actions = {
            IconButton(
                onClick = { isDropDownMenuExpanded = isDropDownMenuExpanded.not() }
            ) {
                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.options))
            }

            ChatDropdownMenu(
                isDropDownMenuExpanded = isDropDownMenuExpanded,
                isMenuItemEnabled = isMenuItemEnabled,
                onDismissRequest = { isDropDownMenuExpanded = false },
                onChatTitleItemClick = {
                    onChatTitleItemClick.invoke()
                    isDropDownMenuExpanded = false
                },
                onExportChatItemClick = onExportChatItemClick
            )
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun ChatDropdownMenu(
    isDropDownMenuExpanded: Boolean,
    isMenuItemEnabled: Boolean,
    onDismissRequest: () -> Unit,
    onChatTitleItemClick: () -> Unit,
    onExportChatItemClick: () -> Unit
) {
    DropdownMenu(
        modifier = Modifier.wrapContentSize(),
        expanded = isDropDownMenuExpanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            enabled = isMenuItemEnabled,
            text = { Text(text = stringResource(R.string.update_chat_title)) },
            onClick = onChatTitleItemClick
        )
        /* Export Chat */
        DropdownMenuItem(
            enabled = isMenuItemEnabled,
            text = { Text(text = stringResource(R.string.export_chat)) },
            onClick = {
                onExportChatItemClick()
                onDismissRequest()
            }
        )
    }
}

@Preview
@Composable
fun ChatInputBox(
    value: String = "",
    onValueChange: (String) -> Unit = {},
    chatEnabled: Boolean = true,
    sendButtonEnabled: Boolean = true,
    onSendButtonClick: (String) -> Unit = {},
    onImageClick: () -> Unit = {} // Add the onImageClick parameter
) {
    val localStyle = LocalTextStyle.current
    val mergedStyle = localStyle.merge(TextStyle(color = LocalContentColor.current))
    val context = LocalContext.current
    var base64String by remember { mutableStateOf("") }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                base64String = bitmapToBase64(bitmap)
                Toast.makeText(context, "Image Selected", Toast.LENGTH_SHORT).show()
                Log.d("Base64String", base64String)
                // You can now send the base64String to the chat or pass it to the chatViewModel
            }
        }else {
            Toast.makeText(context, "Image Selection Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(BottomAppBarDefaults.windowInsets)
            .padding(BottomAppBarDefaults.ContentPadding)
            .background(color = MaterialTheme.colorScheme.surface)
    ) {
        BasicTextField(
            modifier = Modifier
                .heightIn(max = 120.dp),
            value = value,
            enabled = chatEnabled,
            textStyle = mergedStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            onValueChange = { if (chatEnabled) onValueChange(it) },
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(size = 24.dp))
                        .padding(all = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            pickImageLauncher.launch(intent)
                        }
                    ) {
                        Text(" 📎 ")
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .padding(start = 16.dp)
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                modifier = Modifier.alpha(0.38f),
                                text = if (chatEnabled) stringResource(R.string.ask_a_question) else stringResource(R.string.some_platforms_disabled)
                            )
                        }
                        innerTextField()
                    }
                    IconButton(
                        enabled = chatEnabled && sendButtonEnabled,
                        onClick = { onSendButtonClick(value) }
                    ) {
                        Icon(imageVector = ImageVector.vectorResource(id = R.drawable.ic_send), contentDescription = stringResource(R.string.send))
                    }
                }
            }
        )
    }
}

private fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

@Composable
fun ScrollToBottomButton(onClick: () -> Unit) {
    SmallFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    ) {
        Icon(Icons.Rounded.KeyboardArrowDown, stringResource(R.string.scroll_to_bottom_icon))
    }
}
