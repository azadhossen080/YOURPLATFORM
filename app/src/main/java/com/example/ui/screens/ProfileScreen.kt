package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.PostModel
import com.example.model.UserProfile
import com.example.ui.components.PostCardItem
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    posts: List<PostModel>,
    onLikePost: (String) -> Unit,
    onCommentPost: (PostModel) -> Unit,
    onSharePost: (PostModel) -> Unit,
    onDeletePost: (String) -> Unit,
    onUpdateCoverColor: (Long) -> Unit,
    onUpdateCoverUri: (String?) -> Unit,
    onUpdateAvatarColor: (Long) -> Unit,
    onUpdateAvatarUri: (String?) -> Unit,
    onUpdateBio: (String) -> Unit,
    aiRepository: com.example.data.AiRepository,
    friendsCount: Int,
    pendingFriendRequestCount: Int,
    onOpenFriends: () -> Unit,
    onOpenQrCode: () -> Unit,
    onOpenActivityCenter: () -> Unit,
    onOpenSettings: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var menuExpanded by remember { mutableStateOf(false) }
    var isEditingCover by remember { mutableStateOf(false) }
    var showPhotoPickerSheet by remember { mutableStateOf(false) }
    var showAiBioDialog by remember { mutableStateOf(false) }
    var isGeneratingBio by remember { mutableStateOf(false) }
    var generatedBioResult by remember { mutableStateOf("") }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    fun saveBitmapToCache(bitmap: Bitmap, prefix: String): String? {
        return try {
            val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")
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
            if (isEditingCover) {
                onUpdateCoverUri(uri.toString())
                onShowSnackbar("কভার ফটো আপডেট করা হয়েছে!")
            } else {
                onUpdateAvatarUri(uri.toString())
                onShowSnackbar("প্রোফাইল পিকচার আপডেট করা হয়েছে!")
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val uri = saveBitmapToCache(bitmap, if (isEditingCover) "cover" else "avatar")
            if (isEditingCover) {
                onUpdateCoverUri(uri)
                onShowSnackbar("কভার ফটো আপডেট করা হয়েছে!")
            } else {
                onUpdateAvatarUri(uri)
                onShowSnackbar("প্রোফাইল পিকচার আপডেট করা হয়েছে!")
            }
        }
    }

    val userPosts = posts.filter { it.isUserPost }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen")
    ) {
        // Top App Bar for Profile
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "মাই প্রোফাইল",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("profile_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("ইউর প্রোফাইল") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandPrimary) },
                        onClick = { menuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (pendingFriendRequestCount > 0)
                                    "বন্ধু ও রিকোয়েস্ট ($pendingFriendRequestCount নতুন)"
                                else
                                    "বন্ধু ও ফ্রেন্ড রিকোয়েস্ট"
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, tint = BrandPrimary) },
                        onClick = {
                            menuExpanded = false
                            onOpenFriends()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("প্রোফাইল QR কোড") },
                        leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null, tint = BrandPrimary) },
                        onClick = {
                            menuExpanded = false
                            onOpenQrCode()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("অ্যাক্টিভিটি সেন্টার") },
                        leadingIcon = { Icon(Icons.Default.LocalActivity, contentDescription = null, tint = BrandPrimary) },
                        onClick = {
                            menuExpanded = false
                            onOpenActivityCenter()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("সেটিং এন্ড প্রাইভেসি") },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = BrandPrimary) },
                        onClick = {
                            menuExpanded = false
                            onOpenSettings()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("সাইন ইন / লগআউট", color = BrandAccent) },
                        leadingIcon = { Icon(Icons.Default.Login, contentDescription = null, tint = BrandAccent) },
                        onClick = {
                            menuExpanded = false
                            onShowSnackbar("বর্তমানে স্থানীয় মোডে সক্রিয় আছেন")
                        }
                    )
                }
            }
        }

        // Cover and Avatar Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            // Cover Image / Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(userProfile.coverColorHex))
            ) {
                if (userProfile.coverImageUri != null) {
                    AsyncImage(
                        model = userProfile.coverImageUri,
                        contentDescription = "Cover Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Landscape,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }

                // Edit cover button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .clickable {
                            isEditingCover = true
                            showPhotoPickerSheet = true
                        }
                        .testTag("edit_cover_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Cover",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Avatar (overlapping bottom-left)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 18.dp, y = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                        .background(Color(userProfile.avatarColorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    if (userProfile.avatarImageUri != null) {
                        AsyncImage(
                            model = userProfile.avatarImageUri,
                            contentDescription = "Profile Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = userProfile.name.take(1),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                    }
                }

                // Camera button for Avatar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(BrandPrimary)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                        .clickable {
                            isEditingCover = false
                            showPhotoPickerSheet = true
                        }
                        .testTag("edit_avatar_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Edit Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Profile Info & Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = userProfile.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = userProfile.handle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = userProfile.bio,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.widthIn(max = 180.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandPrimary.copy(alpha = 0.12f))
                            .clickable { showAiBioDialog = true }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("ai_bio_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "AI বায়ো",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${userPosts.size}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary
                        )
                    )
                    Text(
                        text = "পোস্ট",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenFriends() }
                        .padding(horizontal = 4.dp)
                        .testTag("profile_friends_stat")
                ) {
                    Text(
                        text = "$friendsCount",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary
                        )
                    )
                    Text(
                        text = "বন্ধু",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = userProfile.followersCount,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary
                        )
                    )
                    Text(
                        text = "ফলোয়ার্স",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }

        // Quick Friends & Friend Requests Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onOpenFriends,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .testTag("profile_open_friends_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (pendingFriendRequestCount > 0)
                        "বন্ধু ও রিকোয়েস্ট ($pendingFriendRequestCount নতুন)"
                    else
                        "বন্ধু তালিকা ($friendsCount)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("আমার পোস্টসমূহ", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("মিডিয়া গ্যালারি", fontWeight = FontWeight.SemiBold) }
            )
        }

        // Tab Content
        if (selectedTab == 0) {
            if (userPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "আপনার কোনো পোস্ট নেই।",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    items(userPosts, key = { it.id }) { post ->
                        PostCardItem(
                            post = post,
                            onLikeClick = { onLikePost(post.id) },
                            onCommentClick = { onCommentPost(post) },
                            onShareClick = { onSharePost(post) },
                            onDeleteClick = { onDeletePost(post.id) },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }
        } else {
            // Media Gallery Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(9) { index ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(BrandPrimary.copy(alpha = 0.15f + (index % 3) * 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Media item",
                            tint = BrandPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }

    // Photo Edit Modal Bottom Sheet
    if (showPhotoPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoPickerSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = if (isEditingCover) "কভার ফটো পরিবর্তন করুন" else "প্রোফাইল পিকচার পরিবর্তন করুন",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Gallery option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showPhotoPickerSheet = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("গ্যালারি থেকে ছবি পছন্দ করুন", fontSize = 14.sp)
                }

                // Camera option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showPhotoPickerSheet = false
                            cameraLauncher.launch(null)
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("ক্যামেরা দিয়ে নতুন ছবি তুলুন", fontSize = 14.sp)
                }

                // Preset Color Theme Options
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "বা কালার থিম নির্ধারণ করুন:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                val presetColors = listOf(
                    0xFF5352ED, 0xFF009688, 0xFFFF5722, 0xFF9C27B0, 0xFF3F51B5, 0xFFE91E63
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    presetColors.forEach { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .clickable {
                                    if (isEditingCover) {
                                        onUpdateCoverColor(colorHex)
                                        onShowSnackbar("কভার ফটো কালার আপডেট করা হয়েছে!")
                                    } else {
                                        onUpdateAvatarColor(colorHex)
                                        onShowSnackbar("প্রোফাইল পিকচার কালার আপডেট করা হয়েছে!")
                                    }
                                    showPhotoPickerSheet = false
                                }
                        )
                    }
                }
            }
        }
    }

    if (showAiBioDialog) {
        var selectedStyle by remember { mutableStateOf("স্মার্ট ও ক্রিয়েটিভ") }
        var customBioText by remember { mutableStateOf(userProfile.bio) }
        var bioError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isGeneratingBio) showAiBioDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI প্রোফাইল বায়ো জেনারেটর", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Gemini AI আপনার জন্য আকর্ষণীয় প্রোফাইল বায়ো লিখে দেবে:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Text(
                        text = "বায়োর ধরন নির্বাচন করুন:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    val styles = listOf("স্মার্ট ও ক্রিয়েটিভ", "প্রফেশনাল", "সংক্ষিপ্ত ও ফানি", "অনুপ্রেরণাদায়ক")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        styles.forEach { style ->
                            val isSelected = selectedStyle == style
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BrandPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedStyle = style }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = style,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isGeneratingBio = true
                                bioError = null
                                try {
                                    val generated = aiRepository.generateProfileBio(userProfile.name, selectedStyle)
                                    customBioText = generated
                                } catch (e: Exception) {
                                    bioError = "বায়ো তৈরিতে সমস্যা হয়েছে: ${e.localizedMessage ?: "চেক ইন্টারনেট"}"
                                } finally {
                                    isGeneratingBio = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_bio_button"),
                        enabled = !isGeneratingBio,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        if (isGeneratingBio) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("তৈরি হচ্ছে...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("✨ AI দিয়ে বায়ো তৈরি করুন")
                        }
                    }

                    if (bioError != null) {
                        Text(text = bioError!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }

                    OutlinedTextField(
                        value = customBioText,
                        onValueChange = { customBioText = it },
                        label = { Text("বায়ো টেক্সট (সম্পাদনাযোগ্য)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customBioText.isNotBlank()) {
                            onUpdateBio(customBioText.trim())
                            showAiBioDialog = false
                            onShowSnackbar("প্রোফাইল বায়ো সফলভাবে আপডেট হয়েছে! ✨")
                        }
                    },
                    enabled = customBioText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiBioDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
