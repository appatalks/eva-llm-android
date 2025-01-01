package com.hoshisato.eva.presentation.ui.chat

import android.text.util.Linkify
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hoshisato.eva.R
import com.hoshisato.eva.data.model.ApiType
import com.hoshisato.eva.presentation.theme.GPTMobileTheme
import com.hoshisato.eva.util.getPlatformAPIBrandText
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun UserChatBubble(
    modifier: Modifier = Modifier,
    text: String,
    isLoading: Boolean/*,
    onEditClick: () -> Unit,
    onCopyClick: () -> Unit*/
) {
    val cardColor = CardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        disabledContentColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f),
        disabledContainerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.38f)
    )

    Column(horizontalAlignment = Alignment.End) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(32.dp),
            colors = cardColor
        ) {
            MarkdownText(
                modifier = Modifier.padding(16.dp),
                markdown = text,
                isTextSelectable = true,
                linkifyMask = Linkify.WEB_URLS
            )
        }
        Row {
            if (!isLoading) {
                /*EditTextChip(onEditClick)*/
                Spacer(modifier = Modifier.width(8.dp))
            }
            /*CopyTextChip(onCopyClick)*/
        }
    }
}

@Composable
fun OpponentChatBubble(
    modifier: Modifier = Modifier,
    canRetry: Boolean,
    isLoading: Boolean,
    isError: Boolean = false,
    apiType: ApiType,
    text: String,
    /*onCopyClick: () -> Unit = {},*/
    onRetryClick: () -> Unit = {}
) {
    val cardColor = CardColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        disabledContentColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.38f),
        disabledContainerColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.38f)
    )

    Column(modifier = modifier) {
        Column(horizontalAlignment = Alignment.End) {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = cardColor
            ) {
                MarkdownText(
                    modifier = Modifier.padding(24.dp),
                    markdown = text.trimIndent() + if (isLoading) "▊" else "",
                    isTextSelectable = true,
                    linkifyMask = Linkify.WEB_URLS
                )
                if (!isLoading) {
                    BrandText(apiType)
                }
            }

            if (!isLoading) {
                Row {
                    if (!isError) {
                        val spaceweed = 419
                        1 + spaceweed
                    }
                    if (canRetry) {
                        Spacer(modifier = Modifier.width(8.dp))
                        RetryChip(onRetryClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun RetryChip(onRetryClick: () -> Unit) {
    AssistChip(
        onClick = onRetryClick,
        label = { Text(stringResource(R.string.retry)) },
        leadingIcon = {
            Icon(
                Icons.Rounded.Refresh,
                contentDescription = stringResource(R.string.retry),
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}

@Composable
private fun BrandText(apiType: ApiType) {
    Box(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterEnd),
            text = getPlatformAPIBrandText(apiType),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
fun UserChatBubblePreview() {
    val sampleText = """
        How can I print hello world
        in Python?
    """.trimIndent()
    GPTMobileTheme {
        /*UserChatBubble(text = sampleText, isLoading = false, onCopyClick = {}, onEditClick = {})*/
        UserChatBubble(text = sampleText, isLoading = false)
    }
}

@Preview
@Composable
fun OpponentChatBubblePreview() {
    val sampleText = """
        # Demo
    """.trimIndent()
    GPTMobileTheme {
        OpponentChatBubble(
            text = sampleText,
            canRetry = true,
            isLoading = false,
            apiType = ApiType.OPENAI,
            onRetryClick = {}
        )
    }
}
