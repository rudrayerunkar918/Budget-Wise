package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.ChatMessage
import com.example.data.ai.ExecutedChatAction
import com.example.data.ai.MessageSender
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiChatbotView(
  viewModel: ExpenseViewModel,
  currencySymbol: String,
  modifier: Modifier = Modifier
) {
  val chatMessages by viewModel.chatMessages.collectAsState()
  val isChatLoading by viewModel.isChatLoading.collectAsState()
  val listState = rememberLazyListState()
  var inputText by remember { mutableStateOf("") }

  // Auto-scroll to bottom when new messages arrive or loading state changes
  LaunchedEffect(chatMessages.size, isChatLoading) {
    if (chatMessages.isNotEmpty()) {
      listState.animateScrollToItem(chatMessages.size - 1)
    }
  }

  val quickPrompts = listOf(
    "Sync with real time data",
    "What is current NIFTY 50 and SENSEX today?",
    "Add 10 shares of Tata Motors at ₹441.50",
    "What is my current net worth?",
    "Log ₹450 for lunch under Food",
    "How is my stock portfolio performing?",
    "Set Food budget to ₹8,000"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("gemini_chatbot_view")
  ) {
    // Top Sub-header with Clear action
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 2.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
          modifier = Modifier.padding(end = 6.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Live Actions & Editing Enabled",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      IconButton(
        onClick = { viewModel.clearChatHistory() },
        modifier = Modifier.size(32.dp).testTag("clear_chat_button")
      ) {
        Icon(
          imageVector = Icons.Default.DeleteOutline,
          contentDescription = "Clear Chat",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp)
        )
      }
    }

    // Quick Prompts Horizontal Row
    val scrollState = rememberScrollState()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(scrollState)
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Spacer(modifier = Modifier.width(2.dp))
      for (prompt in quickPrompts) {
        FilterChip(
          selected = false,
          onClick = {
            inputText = prompt
            viewModel.sendChatMessage(prompt)
            inputText = ""
          },
          label = {
            Text(
              text = prompt,
              style = MaterialTheme.typography.labelSmall,
              fontSize = 11.sp
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
          ),
          border = null,
          modifier = Modifier.height(28.dp)
        )
      }
      Spacer(modifier = Modifier.width(2.dp))
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Chat Messages List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 2.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(vertical = 8.dp)
    ) {
      items(chatMessages, key = { it.id }) { message ->
        ChatMessageItem(
          message = message,
          currencySymbol = currencySymbol
        )
      }

      if (isChatLoading) {
        item {
          GeminiThinkingIndicator()
        }
      }
    }

    // Bottom Input Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = inputText,
        onValueChange = { inputText = it },
        placeholder = {
          Text(
            text = "Ask questions or add stocks/expenses...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
          )
        },
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("gemini_chat_input_field"),
        maxLines = 3,
        singleLine = false,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(
          onSend = {
            if (inputText.isNotBlank() && !isChatLoading) {
              val msg = inputText
              inputText = ""
              viewModel.sendChatMessage(msg)
            }
          }
        )
      )

      val canSend = inputText.isNotBlank() && !isChatLoading
      IconButton(
        onClick = {
          if (canSend) {
            val msg = inputText
            inputText = ""
            viewModel.sendChatMessage(msg)
          }
        },
        enabled = canSend,
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(
            if (canSend) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant
          )
          .testTag("send_chat_message_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "Send",
          tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
private fun ChatMessageItem(
  message: ChatMessage,
  currencySymbol: String
) {
  val isUser = message.sender == MessageSender.USER
  val timeStr = remember(message.timestamp) {
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    verticalAlignment = Alignment.Top
  ) {
    if (!isUser) {
      // Gemini Avatar
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              listOf(Color(0xFF8E44AD), Color(0xFF3498DB))
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = "Gemini",
          tint = Color.White,
          modifier = Modifier.size(16.dp)
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Column(
      horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
      modifier = Modifier.widthIn(max = 310.dp)
    ) {
      Card(
        shape = if (isUser) {
          RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        } else {
          RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        },
        colors = CardDefaults.cardColors(
          containerColor = if (isUser) {
            MaterialTheme.colorScheme.primary
          } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
          }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
          // Message Body
          Text(
            text = formatMarkdownBold(message.text),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
            lineHeight = 20.sp
          )

          // If an action was executed by Gemini, show confirmation card
          message.executedAction?.let { action ->
            Spacer(modifier = Modifier.height(8.dp))
            ExecutedActionCard(action = action)
          }

          // If message is grounded with Google Search, show grounding metadata
          if (!isUser && (message.isGroundedWithGoogleSearch || message.sources.isNotEmpty())) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF4285F4).copy(alpha = 0.1f),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4285F4).copy(alpha = 0.25f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = Color(0xFF1967D2),
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Grounded with Google Search",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1967D2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                  )
                }

                if (message.searchQueries.isNotEmpty()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Searched: " + message.searchQueries.joinToString(", "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                  )
                }

                if (message.sources.isNotEmpty()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Sources: " + message.sources.take(3).joinToString(" • ") { it.title },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(3.dp))
      Text(
        text = timeStr,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 4.dp)
      )
    }

    if (isUser) {
      Spacer(modifier = Modifier.width(8.dp))
      // User Avatar
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = "User",
          tint = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@Composable
private fun ExecutedActionCard(action: ExecutedChatAction) {
  val isGreen = action.isSuccess
  val badgeBg = if (isGreen) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
  val badgeBorder = if (isGreen) Color(0xFF81C784) else MaterialTheme.colorScheme.error
  val iconColor = if (isGreen) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
  val titleColor = if (isGreen) Color(0xFF1B5E20) else MaterialTheme.colorScheme.error

  Surface(
    shape = RoundedCornerShape(10.dp),
    color = badgeBg,
    border = androidx.compose.foundation.BorderStroke(1.dp, badgeBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        tint = iconColor,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(
          text = action.title,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = titleColor
        )
        Text(
          text = action.description,
          style = MaterialTheme.typography.bodySmall,
          fontSize = 11.sp,
          color = Color(0xFF212121)
        )
        Text(
          text = "✓ Live database updated",
          style = MaterialTheme.typography.labelSmall,
          fontSize = 9.sp,
          color = Color(0xFF388E3C),
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}

@Composable
private fun GeminiThinkingIndicator() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 40.dp, top = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Start
  ) {
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      )
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(14.dp),
          strokeWidth = 2.dp,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Gemini is thinking & updating...",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

/**
 * Basic markdown bold parser (**text**) to clean annotated text
 */
private fun formatMarkdownBold(text: String): String {
  // Simple clean-up of markdown asterisks for display
  return text.replace("**", "").replace("### ", "").replace("## ", "")
}
