package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.model.PostModel
import com.example.ui.theme.BrandPrimary

data class LanguageOption(
    val name: String,
    val code: String,
    val flag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    userPosts: List<PostModel>,
    onUpdateSettings: (AppSettings) -> Unit,
    onDeletePost: (String) -> Unit,
    onBack: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showManagePostsSheet by remember { mutableStateOf(false) }

    val languages = remember {
        listOf(
            LanguageOption("English (United States)", "en_US", "🇺🇸"),
            LanguageOption("English (United Kingdom)", "en_GB", "🇬🇧"),
            LanguageOption("English (Canada)", "en_CA", "🇨🇦"),
            LanguageOption("English (Australia)", "en_AU", "🇦🇺"),
            LanguageOption("বাংলা (Bangladesh)", "bn_BD", "🇧🇩"),
            LanguageOption("বাংলা (ভারত / India)", "bn_IN", "🇮🇳"),
            LanguageOption("हिन्दी (India)", "hi_IN", "🇮🇳"),
            LanguageOption("اردو (Pakistan)", "ur_PK", "🇵🇰"),
            LanguageOption("العربية (Saudi Arabia)", "ar_SA", "🇸🇦"),
            LanguageOption("Español (Spain)", "es_ES", "🇪🇸"),
            LanguageOption("Français (France)", "fr_FR", "🇫🇷"),
            LanguageOption("Deutsch (Germany)", "de_DE", "🇩🇪"),
            LanguageOption("日本語 (Japan)", "ja_JP", "🇯🇵"),
            LanguageOption("한국어 (South Korea)", "ko_KR", "🇰🇷")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("সেটিং এন্ড প্রাইভেসি", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        modifier = modifier.testTag("settings_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp)
        ) {
            // Section 1: Language & Region
            Text(
                text = "ভাষা ও অঞ্চল (Language & Region)",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLanguageDialog = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "অ্যাপের ভাষা নির্বাচন করুন (World Languages)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = settings.selectedLanguage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Section 2: Content & Post Management
            Text(
                text = "কন্টেন্ট ও পোস্ট ম্যানেজমেন্ট",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showManagePostsSheet = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Article,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ম্যানেজ পোস্ট (Manage Posts)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "প্রকাশিত পোস্ট ডিলিট বা এডিট করতে ট্যাপ করুন",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            // Dimming System Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BrightnessMedium,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "কন্টেন্ট প্রিপারেশন ও ডিমিং সিস্টেম",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "ডিমিং পারসেন্টেজ: ${settings.dimmingLevel.toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
                Switch(
                    checked = settings.isDimmingEnabled,
                    onCheckedChange = { checked ->
                        onUpdateSettings(settings.copy(isDimmingEnabled = checked))
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = BrandPrimary, checkedTrackColor = BrandPrimary.copy(alpha = 0.5f))
                )
            }

            // Dimming Slider
            if (settings.isDimmingEnabled) {
                Slider(
                    value = settings.dimmingLevel,
                    onValueChange = { newVal ->
                        onUpdateSettings(settings.copy(dimmingLevel = newVal))
                    },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = BrandPrimary,
                        activeTrackColor = BrandPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Section 3: Privacy & Security
            Text(
                text = "প্রাইভেসি ও সিকিউরিটি",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "লোকেশন পারমিশন (Location Services)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "পোস্টে লোকেশন তথ্য শেয়ারিং অপশন",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
                Switch(
                    checked = settings.isLocationAllowed,
                    onCheckedChange = { checked ->
                        onUpdateSettings(settings.copy(isLocationAllowed = checked))
                        onShowSnackbar(if (checked) "লোকেশন সার্ভিস সক্রিয় করা হয়েছে" else "লোকেশন সার্ভিস বন্ধ করা হয়েছে")
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = BrandPrimary, checkedTrackColor = BrandPrimary.copy(alpha = 0.5f))
                )
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Section 4: Display & Dark Mode
            Text(
                text = "ডিসপ্লে ও থিম (Display & Theme)",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ডার্ক মোড (Dark Mode)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = if (settings.isDarkMode) "ডার্ক থিম সক্রিয়" else "লাইট থিম সক্রিয়",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
                Switch(
                    checked = settings.isDarkMode,
                    onCheckedChange = { checked ->
                        onUpdateSettings(settings.copy(isDarkMode = checked))
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = BrandPrimary, checkedTrackColor = BrandPrimary.copy(alpha = 0.5f))
                )
            }
        }
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        var searchQuery by remember { mutableStateOf("") }
        val filteredLanguages = languages.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.code.contains(searchQuery, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Column {
                    Text(
                        text = "বিশ্বের সকল ভাষা / Select Language",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search language / ভাষা খুঁজুন...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            text = {
                Box(modifier = Modifier.height(300.dp)) {
                    if (filteredLanguages.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("কোনো ভাষা পাওয়া যায়নি!")
                        }
                    } else {
                        LazyColumn {
                            items(filteredLanguages) { lang ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onUpdateSettings(
                                                settings.copy(
                                                    selectedLanguage = lang.name,
                                                    selectedLanguageCode = lang.code
                                                )
                                            )
                                            showLanguageDialog = false
                                            onShowSnackbar("ভাষা নির্ধারণ করা হয়েছে: ${lang.name}")
                                        }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(lang.flag, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(lang.name, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp))
                                        Text("Code: ${lang.code}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
                                    }
                                    if (settings.selectedLanguage == lang.name) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    // Manage Posts Sheet
    if (showManagePostsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showManagePostsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "ম্যানেজ পোস্ট (Manage Posts)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (userPosts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "আপনার ডিলিট করার মতো কোনো পোস্ট নেই।",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(userPosts, key = { it.id }) { post ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = post.postText,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = post.timeAgo,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        onDeletePost(post.id)
                                        onShowSnackbar("পোস্টটি ডিলিট করা হয়েছে!")
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Post",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                            )
                        }
                    }
                }
            }
        }
    }
}
