package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AiRepository
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String,
    val sender: String, // "user" or "ai"
    var text: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiStudioScreen(
    aiRepository: AiRepository,
    onBack: () -> Unit,
    onUseTextInPost: (String) -> Unit,
    onCopyText: (String) -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "welcome",
                sender = "ai",
                text = "স্বাগতম! আমি YOURPLATFORM AI Assistant 🤖✨\n\nপোস্টের ক্যাপশন, রিলস ভিডিওর স্ক্রিপ্ট, হ্যাশট্যাগ আইডিয়া কিংবা যেকোনো প্রশ্ন—আমাকে জানাতে পারেন!"
            )
        )
    }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val quickPrompts = listOf(
        "🎬 Waves রিলসের জন্য ৩টি ট্রেন্ডিং ভিডিও আইডিয়া",
        "✍️ ফেসবুক/সোশ্যাল মিডিয়ায় পোস্ট করার জন্য সুন্দর ক্যাপশন",
        "💡 কীভাবে দ্রুত ফলোয়ার্স বাড়ানো যায়?",
        "🏷️ জনপ্রিয় বাংলা হ্যাশট্যাগের তালিকা"
    )

    fun sendMessage(userText: String) {
        if (userText.isBlank() || isGenerating) return
        val trimmed = userText.trim()
        inputText = ""
        val userMsg = ChatMessage(id = System.currentTimeMillis().toString(), sender = "user", text = trimmed)
        messages.add(userMsg)

        val aiMsgId = (System.currentTimeMillis() + 1).toString()
        val aiMsg = ChatMessage(id = aiMsgId, sender = "ai", text = "")
        messages.add(aiMsg)

        isGenerating = true
        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
            try {
                aiRepository.chatStream(trimmed).collect { chunk ->
                    val index = messages.indexOfFirst { it.id == aiMsgId }
                    if (index != -1) {
                        val current = messages[index]
                        messages[index] = current.copy(text = current.text + chunk)
                    }
                }
            } catch (e: Exception) {
                val index = messages.indexOfFirst { it.id == aiMsgId }
                if (index != -1) {
                    messages[index] = messages[index].copy(
                        text = "দুঃখিত, উত্তর তৈরিতে সাময়িক সমস্যা হয়েছে। দয়া করে আবার চেষ্টা করুন।"
                    )
                }
            } finally {
                isGenerating = false
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    LaunchedEffect(messages.size) {
        listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(BrandPrimary, BrandAccent))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("YOURPLATFORM AI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Gemini 3.8 Flash দ্বারা চালিত", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("ai_studio_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Chat messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isUser = msg.sender == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        if (!isUser) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(BrandPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Column(
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isUser) 16.dp else 4.dp,
                                            bottomEnd = if (isUser) 4.dp else 16.dp
                                        )
                                    )
                                    .background(
                                        if (isUser) BrandPrimary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                if (msg.text.isEmpty() && isGenerating) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = BrandPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "চিন্তা করছে...",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 20.sp
                                        )
                                    )
                                }
                            }

                            // AI Action Buttons: Copy & Use in Post
                            if (!isUser && msg.text.isNotBlank() && msg.id != "welcome") {
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextButton(
                                        onClick = {
                                            onCopyText(msg.text)
                                            onShowSnackbar("কপি করা হয়েছে!")
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            modifier = Modifier.size(14.dp),
                                            tint = BrandPrimary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("কপি", fontSize = 11.sp, color = BrandPrimary)
                                    }

                                    TextButton(
                                        onClick = {
                                            onUseTextInPost(msg.text)
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PostAdd,
                                            contentDescription = "Use in post",
                                            modifier = Modifier.size(14.dp),
                                            tint = BrandAccent
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("পোস্টে ব্যবহার করুন", fontSize = 11.sp, color = BrandAccent)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick prompts chips
            if (messages.size <= 3 && !isGenerating) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    quickPrompts.forEach { prompt ->
                        AssistChip(
                            onClick = { sendMessage(prompt) },
                            label = { Text(prompt, fontSize = 11.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("AI-কে যেকোনো প্রশ্ন বা নির্দেশনা দিন...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_input"),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { sendMessage(inputText) },
                    enabled = inputText.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank() && !isGenerating) BrandPrimary else Color.Gray)
                        .testTag("ai_chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
