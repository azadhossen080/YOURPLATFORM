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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
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
import com.example.ui.screens.WavesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppStringsRegistry
import com.example.util.LocalAppStrings
import com.example.util.configureAppCheck
import com.example.viewmodel.PlatformViewModel
import kotlinx.coroutines.launch
import java.util.Locale

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
            val localizedStrings = remember(settings.selectedLanguageCode) {
                AppStringsRegistry.forCode(settings.selectedLanguageCode)
            }
            val layoutDir = remember(settings.selectedLanguageCode) {
                if (AppStringsRegistry.isRtl(settings.selectedLanguageCode)) {
                    LayoutDirection.Rtl
                } else {
                    LayoutDirection.Ltr
                }
            }

            LaunchedEffect(settings.selectedLanguageCode) {
                val locale = Locale.forLanguageTag(settings.selectedLanguageCode.replace("_", "-"))
                Locale.setDefault(locale)
            }

            CompositionLocalProvider(
                LocalAppStrings provides localizedStrings,
                LocalLayoutDirection provides layoutDir
            ) {
                MyApplicationTheme(darkTheme = settings.isDarkMode) {
                    val currentStrings = LocalAppStrings.current
                    MainAppContent(
                        viewModel = viewModel,
                        onShareText = { text ->
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, currentStrings.sharePlatformChooser)
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
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    viewModel: PlatformViewModel,
    onShareText: (String) -> Unit,
    onCopyText: (String) -> Unit
) {
    val strings = LocalAppStrings.current
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
    val uploadState by viewModel.uploadState.collectAsState()

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
                        showSnackbar(strings.markAllReadSnackbar)
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
                    selectedLanguageCode = settings.selectedLanguageCode,
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
                        showSnackbar(strings.friendAcceptedSnackbar(user.name))
                    },
                    onDeclineRequest = { user ->
                        viewModel.declineFriendRequest(user.id)
                        showSnackbar(strings.friendDeclinedSnackbar(user.name))
                    },
                    onSendRequest = { user ->
                        viewModel.sendFriendRequest(user.id)
                        showSnackbar(strings.friendRequestSentSnackbar(user.name))
                    },
                    onCancelRequest = { user ->
                        viewModel.cancelFriendRequest(user.id)
                        showSnackbar(strings.friendRequestCancelledSnackbar(user.name))
                    },
                    onUnfriend = { user ->
                        viewModel.unfriend(user.id)
                        showSnackbar(strings.unfriendedSnackbar(user.name))
                    },
                    onRemoveSuggestion = { user ->
                        viewModel.removeSuggestion(user.id)
                    },
                    onBack = { activeSubScreen = null }
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    AppHeader(
                        userProfile = userProfile,
                        unreadNotificationCount = notifications.count { !it.isRead },
                        pendingFriendRequestCount = pendingFriendRequestCount,
                        onAiStudioClick = { activeSubScreen = SubScreen.AI_STUDIO },
                        onFriendsClick = { activeSubScreen = SubScreen.FRIENDS },
                        onNotificationClick = { activeSubScreen = SubScreen.NOTIFICATIONS },
                        onProfileClick = { viewModel.setSelectedTab(4) }
                    )

                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tabContent"
                    ) { tab ->
                        when (tab) {
                            0 -> HomeScreen(
                                posts = posts,
                                userProfile = userProfile,
                                uploadState = uploadState,
                                friendsCount = friendsCount,
                                pendingRequestCount = pendingFriendRequestCount,
                                onRetryUpload = {
                                    viewModel.retryLastUpload {
                                        showSnackbar(strings.postPublishedSnackbar)
                                    }
                                },
                                onDismissUpload = { viewModel.dismissUploadState() },
                                onOpenFriends = { activeSubScreen = SubScreen.FRIENDS },
                                onOpenCreatePost = { viewModel.setSelectedTab(2) },
                                onLikePost = { viewModel.toggleLike(it) },
                                onCommentPost = { post -> activeCommentPost = post },
                                onSharePost = { post ->
                                    onCopyText("https://yourplatform.app/post/${post.id}")
                                    showSnackbar(strings.postLinkCopied)
                                },
                                onDeletePost = { viewModel.deletePost(it) }
                            )

                            1 -> WavesScreen(
                                onShareReel = { text -> onShareText(text) }
                            )

                            2 -> CreatePostScreen(
                                aiRepository = viewModel.aiRepository,
                                uploadState = uploadState,
                                selectedLanguageCode = settings.selectedLanguageCode,
                                initialText = createPostInitialText,
                                onSubmitPost = { text, type, uri, previewUri ->
                                    viewModel.submitPost(
                                        text = text,
                                        type = type,
                                        mediaUri = uri,
                                        previewUri = previewUri,
                                        onPublished = {
                                            createPostInitialText = ""
                                            showSnackbar(strings.postPublishedSnackbar)
                                        }
                                    )
                                },
                                onRetryUpload = {
                                    viewModel.retryLastUpload {
                                        createPostInitialText = ""
                                        showSnackbar(strings.postPublishedSnackbar)
                                    }
                                },
                                onDismissUpload = { viewModel.dismissUploadState() },
                                onShowSnackbar = { showSnackbar(it) }
                            )

                            3 -> SearchScreen(
                                searchQuery = searchQuery,
                                onQueryChange = { viewModel.setSearchQuery(it) },
                                posts = posts,
                                friends = friends,
                                onSendFriendRequest = { user ->
                                    viewModel.sendFriendRequest(user.id)
                                    showSnackbar(strings.friendRequestSentSnackbar(user.name))
                                },
                                onAcceptFriendRequest = { user ->
                                    viewModel.acceptFriendRequest(user.id)
                                    showSnackbar(strings.friendAcceptedSnackbar(user.name))
                                },
                                onCancelFriendRequest = { user ->
                                    viewModel.cancelFriendRequest(user.id)
                                    showSnackbar(strings.friendRequestCancelledSnackbar(user.name))
                                },
                                onOpenFriendsScreen = { activeSubScreen = SubScreen.FRIENDS },
                                onLikePost = { viewModel.toggleLike(it) },
                                onCommentPost = { post -> activeCommentPost = post },
                                onSharePost = { post ->
                                    onCopyText("https://yourplatform.app/post/${post.id}")
                                    showSnackbar(strings.postLinkCopied)
                                },
                                onDeletePost = { viewModel.deletePost(it) }
                            )

                            4 -> ProfileScreen(
                                userProfile = userProfile,
                                posts = posts,
                                selectedLanguageCode = settings.selectedLanguageCode,
                                onLikePost = { viewModel.toggleLike(it) },
                                onCommentPost = { post -> activeCommentPost = post },
                                onSharePost = { post ->
                                    onCopyText("https://yourplatform.app/post/${post.id}")
                                    showSnackbar(strings.postLinkCopied)
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

            if (settings.isDimmingEnabled && settings.dimmingLevel > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = (settings.dimmingLevel / 100f) * 0.45f))
                )
            }
        }
    }

    if (activeCommentPost != null) {
        val currentPost = posts.find { it.id == activeCommentPost?.id } ?: activeCommentPost!!
        var aiSuggestions by remember(currentPost.id, settings.selectedLanguageCode) {
            mutableStateOf(strings.defaultCommentSuggestions)
        }

        LaunchedEffect(currentPost.id, settings.selectedLanguageCode) {
            try {
                val aiLang = AppStringsRegistry.aiLanguageName(settings.selectedLanguageCode)
                val suggestions = viewModel.aiRepository.generateCommentSuggestions(
                    postText = currentPost.postText,
                    languageName = aiLang,
                    fallbackSuggestions = strings.defaultCommentSuggestions
                )
                if (suggestions.isNotEmpty()) {
                    aiSuggestions = suggestions
                }
            } catch (_: Exception) {
            }
        }

        CommentBottomSheet(
            post = currentPost,
            aiSuggestions = aiSuggestions,
            onDismiss = { activeCommentPost = null },
            onAddComment = { commentText ->
                viewModel.addComment(currentPost.id, commentText)
                showSnackbar(strings.commentAddedSnackbar)
            }
        )
    }

    if (showQrDialog) {
        QrCodeDialog(
            userProfile = userProfile,
            onDismiss = { showQrDialog = false },
            onShare = {
                onShareText("https://yourplatform.app/u/${userProfile.handle.removePrefix("@")}")
                showQrDialog = false
            }
        )
    }

    if (showActivityCenter) {
        ActivityCenterSheet(onDismiss = { showActivityCenter = false })
    }
}
