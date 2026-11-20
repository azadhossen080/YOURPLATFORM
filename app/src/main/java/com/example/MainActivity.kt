package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.FriendStatus
import com.example.model.PostModel
import com.example.ui.components.ActivityCenterSheet
import com.example.ui.components.AppHeader
import com.example.ui.components.BottomNavBar
import com.example.ui.components.CommentBottomSheet
import com.example.ui.components.QrCodeDialog
import com.example.ui.screens.AiStudioScreen
import com.example.ui.screens.CreatePostScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.configureAppCheck
import com.example.viewmodel.PlatformViewModel
import kotlinx.coroutines.launch

enum class SubScreen {
    NOTIFICATIONS,
    SETTINGS,
    AI_STUDIO,
    FRIENDS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure App Check synchronously before Firebase calls or ViewModels
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            val viewModel: PlatformViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()

            MyApplicationTheme(darkTheme = settings.isDarkMode) {
                MainAppContent(
                    viewModel = viewModel,
                    onShareText = { text ->
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, text)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "YOURPLATFORM শেয়ার করুন")
                        startActivity(shareIntent)
                    },
                    onCopyText = { text ->
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Post Link", text)
                        clipboard.setPrimaryClip(clip)
                    }
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    viewModel: PlatformViewModel,
    onShareText: (String) -> Unit,
    onCopyText: (String) -> Unit
) {
    var showSplash by remember { mutableStateOf(true) }
    var activeSubScreen by remember { mutableStateOf<SubScreen?>(null) }
    var activeCommentPost by remember { mutableStateOf<PostModel?>(null) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showActivityCenter by remember { mutableStateOf(false) }
    var createPostInitialText by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val posts by viewModel.posts.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val friendsCount = friends.count { it.status == FriendStatus.FRIEND }
    val pendingFriendRequestCount = friends.count { it.status == FriendStatus.INCOMING_REQUEST }

    fun showSnackbar(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    if (showSplash) {
        SplashScreen(onSplashFinished = { showSplash = false })
        return
    }

    // Handle back button navigation
    BackHandler(enabled = activeSubScreen != null || selectedTab != 0) {
        if (activeSubScreen != null) {
            activeSubScreen = null
        } else if (selectedTab != 0) {
            viewModel.setSelectedTab(0)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (activeSubScreen == null) {
                BottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tabIndex ->
                        viewModel.setSelectedTab(tabIndex)
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (activeSubScreen == SubScreen.NOTIFICATIONS) {
                NotificationsScreen(
                    notifications = notifications,
                    onBack = { activeSubScreen = null },
                    onMarkAllRead = {
                        viewModel.markNotificationsRead()
                        showSnackbar("সকল নোটিফিকেশন পড়া হিসেবে চিহ্নিত হয়েছে")
                    }
                )
            } else if (activeSubScreen == SubScreen.SETTINGS) {
                SettingsScreen(
                    settings = settings,
                    userPosts = posts.filter { it.isUserPost },
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onDeletePost = { viewModel.deletePost(it) },
                    onBack = { activeSubScreen = null },
                    onShowSnackbar = { showSnackbar(it) }
                )
            } else if (activeSubScreen == SubScreen.AI_STUDIO) {
                AiStudioScreen(
                    aiRepository = viewModel.aiRepository,
                    onBack = { activeSubScreen = null },
                    onUseTextInPost = { generatedText ->
                        activeSubScreen = null
                        createPostInitialText = generatedText
                        viewModel.setSelectedTab(2)
                    },
                    onCopyText = { onCopyText(it) },
                    onShowSnackbar = { showSnackbar(it) }
                )
            } else if (activeSubScreen == SubScreen.FRIENDS) {
                FriendsScreen(
                    friends = friends,
                    onAcceptRequest = { user ->
                        viewModel.acceptFriendRequest(user.id)
                        showSnackbar("${user.name}-কে বন্ধু হিসেবে যুক্ত করা হয়েছে! 🤝")
                    },
                    onDeclineRequest = { user ->
                        viewModel.declineFriendRequest(user.id)
                        showSnackbar("${user.name}-এর রিকোয়েস্ট মুছে ফেলা হয়েছে")
                    },
                    onSendRequest = { user ->
                        viewModel.sendFriendRequest(user.id)
                        showSnackbar("${user.name}-কে ফ্রেন্ড রিকোয়েস্ট পাঠানো হয়েছে!")
                    },
                    onCancelRequest = { user ->
                        viewModel.cancelFriendRequest(user.id)
                        showSnackbar("${user.name}-কে পাঠানো রিকোয়েস্ট বাতিল করা হয়েছে")
                    },
                    onUnfriend = { user ->
                        viewModel.unfriend(user.id)
                        showSnackbar("${user.name}-কে বন্ধু তালিকা থেকে সরানো হয়েছে")
                    },
                    onRemoveSuggestion = { user ->
                        viewModel.removeSuggestion(user.id)
                    },
                    onBack = { activeSubScreen = null }
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header (Logo, AI Studio sparkle, Friends, Notifications bell, Profile)
                    AppHeader(
                        userProfile = userProfile,
                        unreadNotificationCount = notifications.count { !it.isRead },
                        pendingFriendRequestCount = pendingFriendRequestCount,
                        onAiStudioClick = {
                            activeSubScreen = SubScreen.AI_STUDIO
                        },
                        onFriendsClick = {
                            activeSubScreen = SubScreen.FRIENDS
                        },
                        onNotificationClick = {
                            activeSubScreen = SubScreen.NOTIFICATIONS
                        },
                        onProfileClick = {
                            viewModel.setSelectedTab(4)
                        }
                    )

                    // Tab Views with Animated Transitions
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tabContent"
                    ) { tab ->
                        when (tab) {
                            0 -> HomeScreen(
                                posts = posts,
                                userProfile = userProfile,
                                friendsCount = friendsCount,
                                pendingRequestCount = pendingFriendRequestCount,
                                onOpenFriends = { activeSubScreen = SubScreen.FRIENDS },
                                onOpenCreatePost = { viewModel.setSelectedTab(2) },
                                onLikePost = { viewModel.toggleLike(it) },
                                onCommentPost = { post -> activeCommentPost = post },
                                onSharePost = { post ->
                                    onCopyText("https://yourplatform.app/post/${post.id}")
                                    showSnackbar("পোস্ট লিংক কপি করা হয়েছে!")
                                },
                                onDeletePost = { viewModel.deletePost(it) }
                            )

                            1 -> com.example.ui.screens.WavesScreen(
                                onShareReel = { text ->
                                    onShareText(text)
                                }
                            )

                            2 -> CreatePostScreen(
                                aiRepository = viewModel.aiRepository,
                                initialText = createPostInitialText,
                                onPostCreated = { text, type, uri ->
                                    viewModel.addNewPost(text, type, uri)
                                    createPostInitialText = ""
                                },
                                onShowSnackbar = { showSnackbar(it) }
                            )

                            3 -> SearchScreen(
                                searchQuery = searchQuery,
                                onQueryChange = { viewModel.setSearchQuery(it) },
                                posts = posts,
                                friends = friends,
                                onSendFriendRequest = { user ->
                                    viewModel.sendFriendRequest(user.id)
                                    showSnackbar("${user.name}-কে ফ্রেন্ড রিকোয়েস্ট পাঠানো হয়েছে!")
                                },
                                onAcceptFriendRequest = { user ->
                                    viewModel.acceptFriendRequest(user.id)
                                    showSnackbar("${user.name}-কে বন্ধু হিসেবে যুক্ত করা হয়েছে! 🤝")
                                },
                                onCancelFriendRequest = { user ->
                                    viewModel.cancelFriendRequest(user.id)
                                    showSnackbar("রিকোয়েস্ট বাতিল করা হয়েছে")
                                },
                                onOpenFriendsScreen = { activeSubScreen = SubScreen.FRIENDS },
                                onLikePost = { viewModel.toggleLike(it) },
                                onCommentPost = { post -> activeCommentPost = post },
                                onSharePost = { post ->
                                    onCopyText("https://yourplatform.app/post/${post.id}")
                                    showSnackbar("পোস্ট লিংক কপি করা হয়েছে!")
                                },
                                onDeletePost = { viewModel.deletePost(it) }
                            )

                            4 -> ProfileScreen(
                                userProfile = userProfile,
                                posts = posts,
                                onLikePost = { viewModel.toggleLike(it) },
                                onCommentPost = { post -> activeCommentPost = post },
                                onSharePost = { post ->
                                    onCopyText("https://yourplatform.app/post/${post.id}")
                                    showSnackbar("পোস্ট লিংক কপি করা হয়েছে!")
                                },
                                onDeletePost = { viewModel.deletePost(it) },
                                onUpdateCoverColor = { viewModel.updateCoverColor(it) },
                                onUpdateCoverUri = { viewModel.updateCoverUri(it) },
                                onUpdateAvatarColor = { viewModel.updateAvatarColor(it) },
                                onUpdateAvatarUri = { viewModel.updateAvatarUri(it) },
                                onUpdateBio = { viewModel.updateBio(it) },
                                aiRepository = viewModel.aiRepository,
                                friendsCount = friendsCount,
                                pendingFriendRequestCount = pendingFriendRequestCount,
                                onOpenFriends = { activeSubScreen = SubScreen.FRIENDS },
                                onOpenQrCode = { showQrDialog = true },
                                onOpenActivityCenter = { showActivityCenter = true },
                                onOpenSettings = { activeSubScreen = SubScreen.SETTINGS },
                                onShowSnackbar = { showSnackbar(it) }
                            )
                        }
                    }
                }
            }

            // Optional Dimming Filter Overlay
            if (settings.isDimmingEnabled && settings.dimmingLevel > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = (settings.dimmingLevel / 100f) * 0.45f))
                )
            }
        }
    }

    // Comment Modal Bottom Sheet
    if (activeCommentPost != null) {
        val currentPost = posts.find { it.id == activeCommentPost?.id } ?: activeCommentPost!!
        var aiSuggestions by remember(currentPost.id) {
            mutableStateOf(listOf("দারুণ পোস্ট! ❤️", "অনেক ভালো লাগলো 👏", "শুভকামনা রইলো 🌟"))
        }

        androidx.compose.runtime.LaunchedEffect(currentPost.id) {
            try {
                val suggestions = viewModel.aiRepository.generateCommentSuggestions(currentPost.postText)
                if (suggestions.isNotEmpty()) {
                    aiSuggestions = suggestions
                }
            } catch (_: Exception) {
                // Keep default suggestions
            }
        }

        CommentBottomSheet(
            post = currentPost,
            aiSuggestions = aiSuggestions,
            onDismiss = { activeCommentPost = null },
            onAddComment = { commentText ->
                viewModel.addComment(currentPost.id, commentText)
                showSnackbar("কমেন্ট সফলভাবে যোগ করা হয়েছে!")
            }
        )
    }

    // QR Code Dialog
    if (showQrDialog) {
        QrCodeDialog(
            userProfile = userProfile,
            onDismiss = { showQrDialog = false },
            onShare = {
                onShareText("আমার YOURPLATFORM প্রোফাইল চেক করুন: https://yourplatform.app/u/${userProfile.handle.removePrefix("@")}")
                showQrDialog = false
            }
        )
    }

    // Activity Center Bottom Sheet
    if (showActivityCenter) {
        ActivityCenterSheet(onDismiss = { showActivityCenter = false })
    }
}
