package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AiRepository
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun CreatePostScreen(
    aiRepository: AiRepository,
    initialText: String = "",
    onPostCreated: (text: String, type: String, mediaUri: String?) -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var postText by remember { mutableStateOf(initialText) }
    var selectedType by remember { mutableStateOf("text") } // "text", "camera", "gallery", "reels"
    var selectedMediaUri by remember { mutableStateOf<String?>(null) }

    var showAiCaptionDialog by remember { mutableStateOf(false) }
    var aiTopicInput by remember { mutableStateOf("") }
    var aiSelectedTone by remember { mutableStateOf("আকর্ষণীয়") }
    var isGeneratingCaption by remember { mutableStateOf(false) }
    var generatedCaptionResult by remember { mutableStateOf("") }

    var isEnhancingText by remember { mutableStateOf(false) }

    LaunchedEffect(initialText) {
        if (initialText.isNotBlank()) {
            postText = initialText
        }
    }

    fun saveBitmapToCache(bitmap: Bitmap): String? {
        return try {
            val file = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUri = uri.toString()
            selectedType = "gallery"
            onShowSnackbar("গ্যালারি থেকে ছবি সিলেক্ট করা হয়েছে")
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedUri = saveBitmapToCache(bitmap)
            selectedMediaUri = savedUri
            selectedType = "camera"
            onShowSnackbar("ক্যামেরা দিয়ে ছবি তোলা হয়েছে")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("create_post_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "নতুন কন্টেন্ট পোস্ট করুন",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )

            if (selectedMediaUri != null || selectedType != "text") {
                TextButton(
                    onClick = {
                        selectedMediaUri = null
                        selectedType = "text"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "রিমুভ",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // AI Assistant Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // AI Caption Generator Button
            Button(
                onClick = { showAiCaptionDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("open_ai_caption_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("AI ক্যাপশন", color = BrandPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // AI Text Enhancer Button
            Button(
                onClick = {
                    if (postText.isBlank()) {
                        onShowSnackbar("আগে কিছু টেক্সট লিখুন, AI তা পরিমার্জন করবে!")
                    } else {
                        isEnhancingText = true
                        coroutineScope.launch {
                            try {
                                val improved = aiRepository.enhancePostText(postText)
                                postText = improved
                                onShowSnackbar("AI দ্বারা পোস্ট পরিমার্জন সম্পন্ন হয়েছে! ✨")
                            } catch (e: Exception) {
                                onShowSnackbar("পরিমার্জনে সাময়িক সমস্যা হয়েছে")
                            } finally {
                                isEnhancingText = false
                            }
                        }
                    }
                },
                enabled = !isEnhancingText,
                colors = ButtonDefaults.buttonColors(containerColor = BrandAccent.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_enhance_post_button")
            ) {
                if (isEnhancingText) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = BrandAccent)
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = BrandAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("AI পরিমার্জন", color = BrandAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Caption Input Field
        OutlinedTextField(
            value = postText,
            onValueChange = { postText = it },
            placeholder = {
                Text(
                    text = if (selectedType == "reels") "ভিডিও বা রিলস ক্যাপশন লিখুন..." else "আপনার মন যা চায় লিখুন...",
                    fontSize = 14.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .testTag("create_post_text_field"),
            shape = RoundedCornerShape(12.dp)
        )

        // Selected Media Preview Box
        if (selectedMediaUri != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, BrandPrimary, RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = selectedMediaUri,
                    contentDescription = "Selected Media Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable {
                            selectedMediaUri = null
                            selectedType = "text"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else if (selectedType == "reels") {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandPrimary.copy(alpha = 0.1f))
                    .border(1.5.dp, BrandPrimary, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VideoCall,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Waves রিলস ভিডিও মোড সিলেক্ট করা হয়েছে",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "মিডিয়া নির্বাচন করুন:",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Media Selection Options: Camera, Gallery, Reels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Camera Button
            OutlinedButton(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("pick_camera_button"),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (selectedType == "camera") Color(0xFFE91E63) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("ক্যামেরা", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Gallery Button
            OutlinedButton(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("pick_gallery_button"),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (selectedType == "gallery") Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("গ্যালারি", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Reels Button
            OutlinedButton(
                onClick = {
                    selectedType = "reels"
                    selectedMediaUri = null
                    onShowSnackbar("Waves রিলস মোড সিলেক্ট করা হয়েছে")
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("pick_reels_button"),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (selectedType == "reels") BrandPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VideoCall,
                        contentDescription = "Reels",
                        tint = BrandPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("রিলস", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Publish Button
        Button(
            onClick = {
                if (postText.isNotBlank() || selectedMediaUri != null || selectedType != "text") {
                    val finalCaption = if (postText.isBlank()) "নতুন আপডেট পোস্ট" else postText.trim()
                    onPostCreated(finalCaption, selectedType, selectedMediaUri)
                    postText = ""
                    selectedMediaUri = null
                    selectedType = "text"
                    onShowSnackbar("পোস্ট সফলভাবে পাবলিশ হয়েছে!")
                } else {
                    onShowSnackbar("কিছু লিখুন অথবা মিডিয়া যুক্ত করুন!")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("publish_post_button"),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "পাবলিশ করুন",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // AI Caption Generator Dialog
    if (showAiCaptionDialog) {
        val tones = listOf("আকর্ষণীয়", "কাব্যিক", "মজার", "প্রফেশনাল", "অনুপ্রেরণামূলক")

        AlertDialog(
            onDismissRequest = {
                if (!isGeneratingCaption) showAiCaptionDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BrandPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI ক্যাপশন জেনারেটর", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column {
                    Text("বিষয় বা অনুভূতি লিখুন (যেমন: সমুদ্র ভ্রমণ, নতুন কাজ, বৃষ্টি):", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = aiTopicInput,
                        onValueChange = { aiTopicInput = it },
                        placeholder = { Text("কী নিয়ে পোস্ট করতে চান?", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("ক্যাপশনের ধরন (Tone):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        tones.take(3).forEach { tone ->
                            AssistChip(
                                onClick = { aiSelectedTone = tone },
                                label = { Text(tone, fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (aiSelectedTone == tone) BrandPrimary.copy(alpha = 0.15f) else Color.Transparent,
                                    labelColor = if (aiSelectedTone == tone) BrandPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    if (generatedCaptionResult.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("তৈরিকৃত ক্যাপশন:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp)
                        ) {
                            Text(generatedCaptionResult, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            },
            confirmButton = {
                if (generatedCaptionResult.isNotBlank()) {
                    Button(
                        onClick = {
                            postText = generatedCaptionResult
                            showAiCaptionDialog = false
                            generatedCaptionResult = ""
                            aiTopicInput = ""
                            onShowSnackbar("ক্যাপশন পোস্টে যুক্ত হয়েছে!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Text("ক্যাপশন ব্যবহার করুন")
                    }
                } else {
                    Button(
                        onClick = {
                            if (aiTopicInput.isBlank()) {
                                onShowSnackbar("দয়া করে বিষয় উল্লেখ করুন")
                            } else {
                                isGeneratingCaption = true
                                coroutineScope.launch {
                                    try {
                                        val result = aiRepository.generateCaption(aiTopicInput, aiSelectedTone)
                                        generatedCaptionResult = result
                                    } catch (e: Exception) {
                                        onShowSnackbar("ক্যাপশন তৈরিতে সাময়িক সমস্যা হয়েছে")
                                    } finally {
                                        isGeneratingCaption = false
                                    }
                                }
                            }
                        },
                        enabled = !isGeneratingCaption && aiTopicInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        if (isGeneratingCaption) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("তৈরি করুন")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAiCaptionDialog = false
                        generatedCaptionResult = ""
                    }
                ) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}
