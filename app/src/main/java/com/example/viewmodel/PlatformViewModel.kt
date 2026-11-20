package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiRepository
import com.example.data.LocalRepository
import com.example.model.AppSettings
import com.example.model.FriendStatus
import com.example.model.FriendUser
import com.example.model.NotificationModel
import com.example.model.PostModel
import com.example.model.ReviewModel
import com.example.model.ReviewStatus
import com.example.model.UserProfile
import com.example.util.AppStringsRegistry
import com.example.util.MediaProcessor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class UploadStage {
    PREPARING,
    PROCESSING_MEDIA,
    UPLOADING,
    FINALIZING
}

sealed interface PostUploadState {
    data object Idle : PostUploadState
    data class Uploading(
        val postText: String,
        val mediaType: String,
        val previewUri: String?,
        val progress: Float,
        val stage: UploadStage
    ) : PostUploadState

    data class Success(val postId: String) : PostUploadState
    data class Failed(val message: String) : PostUploadState
}

private data class PendingUploadRequest(
    val text: String,
    val type: String,
    val mediaUri: String?,
    val previewUri: String?
)

class PlatformViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LocalRepository(application.applicationContext)
    val aiRepository = AiRepository()

    private val _posts = MutableStateFlow<List<PostModel>>(emptyList())
    val posts: StateFlow<List<PostModel>> = _posts.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _friends = MutableStateFlow<List<FriendUser>>(emptyList())
    val friends: StateFlow<List<FriendUser>> = _friends.asStateFlow()

    private val _reviews = MutableStateFlow<List<ReviewModel>>(emptyList())
    val reviews: StateFlow<List<ReviewModel>> = _reviews.asStateFlow()

    private val _lastExportPath = MutableStateFlow<String?>(null)
    val lastExportPath: StateFlow<String?> = _lastExportPath.asStateFlow()

    private val _lastExportTimestamp = MutableStateFlow<Long?>(null)
    val lastExportTimestamp: StateFlow<Long?> = _lastExportTimestamp.asStateFlow()

    private val _uploadState = MutableStateFlow<PostUploadState>(PostUploadState.Idle)
    val uploadState: StateFlow<PostUploadState> = _uploadState.asStateFlow()

    private var lastUploadRequest: PendingUploadRequest? = null

    init {
        _posts.value = repository.loadPosts()
        _userProfile.value = repository.loadProfile()
        _settings.value = repository.loadSettings()
        _notifications.value = repository.loadNotifications()
        _friends.value = repository.loadFriends()
        _reviews.value = repository.loadReviews()
        _lastExportPath.value = repository.getLastExportPath()
        _lastExportTimestamp.value = repository.getLastExportTime()
    }

    private fun currentStrings() = AppStringsRegistry.forCode(_settings.value.selectedLanguageCode)

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun submitPost(
        text: String,
        type: String,
        mediaUri: String?,
        previewUri: String? = mediaUri,
        onPublished: () -> Unit = {}
    ) {
        if (_uploadState.value is PostUploadState.Uploading) return

        val finalCaption = if (text.isBlank()) currentStrings().defaultMediaPostText else text.trim()
        lastUploadRequest = PendingUploadRequest(finalCaption, type, mediaUri, previewUri)
        _uploadState.value = PostUploadState.Uploading(
            postText = finalCaption,
            mediaType = type,
            previewUri = previewUri ?: mediaUri,
            progress = 0.08f,
            stage = UploadStage.PREPARING
        )

        viewModelScope.launch {
            try {
                delay(200)

                var finalMediaUri = mediaUri
                var finalThumbnailUri = previewUri
                var isVideo = type == "reels"

                if (!mediaUri.isNullOrBlank()) {
                    _uploadState.value = PostUploadState.Uploading(
                        postText = finalCaption,
                        mediaType = type,
                        previewUri = previewUri ?: mediaUri,
                        progress = 0.22f,
                        stage = UploadStage.PROCESSING_MEDIA
                    )

                    val processed = MediaProcessor.processAndSaveMedia(
                        context = getApplication(),
                        sourceUriString = mediaUri,
                        requestedType = type
                    ) { mediaFrac ->
                        val mapped = 0.22f + mediaFrac * 0.43f
                        _uploadState.value = PostUploadState.Uploading(
                            postText = finalCaption,
                            mediaType = type,
                            previewUri = previewUri ?: mediaUri,
                            progress = mapped.coerceIn(0.22f, 0.65f),
                            stage = UploadStage.PROCESSING_MEDIA
                        )
                    }
                    finalMediaUri = processed.mediaUri
                    finalThumbnailUri = processed.thumbnailUri ?: previewUri
                    isVideo = processed.isVideo
                } else {
                    _uploadState.value = PostUploadState.Uploading(
                        postText = finalCaption,
                        mediaType = type,
                        previewUri = null,
                        progress = 0.45f,
                        stage = UploadStage.PROCESSING_MEDIA
                    )
                    delay(200)
                }

                // Uploading phase with smooth visible progress
                for (step in listOf(0.72f, 0.85f, 0.94f)) {
                    _uploadState.value = PostUploadState.Uploading(
                        postText = finalCaption,
                        mediaType = type,
                        previewUri = finalThumbnailUri ?: finalMediaUri,
                        progress = step,
                        stage = UploadStage.UPLOADING
                    )
                    delay(220)
                }

                _uploadState.value = PostUploadState.Uploading(
                    postText = finalCaption,
                    mediaType = type,
                    previewUri = finalThumbnailUri ?: finalMediaUri,
                    progress = 0.99f,
                    stage = UploadStage.FINALIZING
                )
                delay(160)

                val newPost = PostModel(
                    id = UUID.randomUUID().toString(),
                    userName = _userProfile.value.name,
                    timeAgo = currentStrings().justNow,
                    postText = finalCaption,
                    mediaUri = finalMediaUri,
                    mediaType = type,
                    thumbnailUri = finalThumbnailUri,
                    isVideo = isVideo,
                    likeCount = 0,
                    isLiked = false,
                    comments = emptyList(),
                    isUserPost = true
                )
                val updated = listOf(newPost) + _posts.value
                _posts.value = updated
                repository.savePosts(updated)

                _uploadState.value = PostUploadState.Success(newPost.id)
                _selectedTab.value = 0
                onPublished()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uploadState.value = PostUploadState.Failed(
                    e.localizedMessage ?: currentStrings().uploadFailedBanner
                )
            }
        }
    }

    fun retryLastUpload(onPublished: () -> Unit = {}) {
        val req = lastUploadRequest ?: return
        submitPost(
            text = req.text,
            type = req.type,
            mediaUri = req.mediaUri,
            previewUri = req.previewUri,
            onPublished = onPublished
        )
    }

    fun dismissUploadState() {
        if (_uploadState.value !is PostUploadState.Uploading) {
            _uploadState.value = PostUploadState.Idle
        }
    }

    fun addNewPost(text: String, type: String, mediaUri: String?) {
        val newPost = PostModel(
            id = UUID.randomUUID().toString(),
            userName = _userProfile.value.name,
            timeAgo = currentStrings().justNow,
            postText = text,
            mediaUri = mediaUri,
            mediaType = type,
            isVideo = type == "reels",
            likeCount = 0,
            isLiked = false,
            comments = emptyList(),
            isUserPost = true
        )
        val updated = listOf(newPost) + _posts.value
        _posts.value = updated
        repository.savePosts(updated)
        _selectedTab.value = 0
    }

    fun deletePost(id: String) {
        val updated = _posts.value.filter { it.id != id }
        _posts.value = updated
        repository.savePosts(updated)
    }

    fun toggleLike(id: String) {
        val updated = _posts.value.map { post ->
            if (post.id == id) {
                val newLiked = !post.isLiked
                val newCount = if (newLiked) post.likeCount + 1 else (post.likeCount - 1).coerceAtLeast(0)
                post.copy(isLiked = newLiked, likeCount = newCount)
            } else post
        }
        _posts.value = updated
        repository.savePosts(updated)
    }

    fun addComment(id: String, comment: String) {
        if (comment.isBlank()) return
        val updated = _posts.value.map { post ->
            if (post.id == id) {
                post.copy(comments = post.comments + comment.trim())
            } else post
        }
        _posts.value = updated
        repository.savePosts(updated)
    }

    fun updateCoverColor(colorHex: Long) {
        val updated = _userProfile.value.copy(coverColorHex = colorHex, coverImageUri = null)
        _userProfile.value = updated
        repository.saveProfile(updated)
    }

    fun updateCoverUri(uri: String?) {
        if (uri == null) {
            val updated = _userProfile.value.copy(coverImageUri = null)
            _userProfile.value = updated
            repository.saveProfile(updated)
            return
        }
        viewModelScope.launch {
            val savedUri = MediaProcessor.persistImageToFilesDir(getApplication(), uri, "cover")
            val updated = _userProfile.value.copy(coverImageUri = savedUri)
            _userProfile.value = updated
            repository.saveProfile(updated)
        }
    }

    fun updateAvatarColor(colorHex: Long) {
        val updated = _userProfile.value.copy(avatarColorHex = colorHex, avatarImageUri = null)
        _userProfile.value = updated
        repository.saveProfile(updated)
    }

    fun updateAvatarUri(uri: String?) {
        if (uri == null) {
            val updated = _userProfile.value.copy(avatarImageUri = null)
            _userProfile.value = updated
            repository.saveProfile(updated)
            return
        }
        viewModelScope.launch {
            val savedUri = MediaProcessor.persistImageToFilesDir(getApplication(), uri, "avatar")
            val updated = _userProfile.value.copy(avatarImageUri = savedUri)
            _userProfile.value = updated
            repository.saveProfile(updated)
        }
    }

    fun updateBio(bio: String) {
        val updated = _userProfile.value.copy(bio = bio)
        _userProfile.value = updated
        repository.saveProfile(updated)
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        repository.saveSettings(newSettings)
    }

    fun markNotificationsRead() {
        val updated = _notifications.value.map { it.copy(isRead = true) }
        _notifications.value = updated
        repository.saveNotifications(updated)
    }

    fun acceptFriendRequest(userId: String) {
        var acceptedName = ""
        val updated = _friends.value.map { user ->
            if (user.id == userId) {
                acceptedName = user.name
                user.copy(status = FriendStatus.FRIEND, requestTimeAgo = "")
            } else user
        }
        _friends.value = updated
        repository.saveFriends(updated)

        if (acceptedName.isNotEmpty()) {
            val strings = currentStrings()
            val newNotif = NotificationModel(
                id = UUID.randomUUID().toString(),
                title = strings.nowFriendsNotification(acceptedName),
                timeAgo = strings.justNow,
                iconType = "friend",
                isRead = false
            )
            val updatedNotifs = listOf(newNotif) + _notifications.value
            _notifications.value = updatedNotifs
            repository.saveNotifications(updatedNotifs)
        }
    }

    fun declineFriendRequest(userId: String) {
        val updated = _friends.value.map { user ->
            if (user.id == userId) {
                user.copy(status = FriendStatus.SUGGESTION, requestTimeAgo = "")
            } else user
        }
        _friends.value = updated
        repository.saveFriends(updated)
    }

    fun sendFriendRequest(userId: String) {
        val now = currentStrings().justNow
        val updated = _friends.value.map { user ->
            if (user.id == userId) {
                user.copy(status = FriendStatus.OUTGOING_REQUEST, requestTimeAgo = now)
            } else user
        }
        _friends.value = updated
        repository.saveFriends(updated)
    }

    fun cancelFriendRequest(userId: String) {
        val updated = _friends.value.map { user ->
            if (user.id == userId) {
                user.copy(status = FriendStatus.SUGGESTION, requestTimeAgo = "")
            } else user
        }
        _friends.value = updated
        repository.saveFriends(updated)
    }

    fun unfriend(userId: String) {
        val updated = _friends.value.map { user ->
            if (user.id == userId) {
                user.copy(status = FriendStatus.SUGGESTION, requestTimeAgo = "")
            } else user
        }
        _friends.value = updated
        repository.saveFriends(updated)
    }

    fun removeSuggestion(userId: String) {
        val updated = _friends.value.filter { it.id != userId }
        _friends.value = updated
        repository.saveFriends(updated)
    }

    fun createOrUpdateIdentity(userName: String, userId: String) {
        val cleanName = userName.trim().ifBlank { "Guest User" }
        val cleanId = userId.trim().removePrefix("@").replace("\\s+".toRegex(), "_")
        val current = _userProfile.value
        val updated = current.copy(
            name = cleanName,
            userId = cleanId,
            handle = if (cleanId.isNotBlank()) "@$cleanId" else "",
            followersCount = current.followersCount.ifBlank { "0" },
            isProfileCreated = true,
            createdAtTimestamp = if (current.createdAtTimestamp > 0L) current.createdAtTimestamp else System.currentTimeMillis()
        )
        _userProfile.value = updated
        repository.saveProfile(updated)
    }

    fun submitReview(rating: Int, comment: String) {
        if (comment.isBlank()) return
        val profile = _userProfile.value
        val newReview = ReviewModel(
            id = UUID.randomUUID().toString(),
            userId = profile.displayUserId,
            userName = profile.name,
            rating = rating.coerceIn(1, 5),
            comment = comment.trim(),
            timestamp = System.currentTimeMillis(),
            timeAgo = currentStrings().justNow,
            status = ReviewStatus.APPROVED,
            isPinned = false
        )
        val updated = listOf(newReview) + _reviews.value
        _reviews.value = updated
        repository.saveReviews(updated)
    }

    fun updateReviewStatus(reviewId: String, status: ReviewStatus) {
        val updated = _reviews.value.map { r ->
            if (r.id == reviewId) r.copy(status = status) else r
        }
        _reviews.value = updated
        repository.saveReviews(updated)
    }

    fun togglePinReview(reviewId: String) {
        val updated = _reviews.value.map { r ->
            if (r.id == reviewId) r.copy(isPinned = !r.isPinned) else r
        }
        _reviews.value = updated
        repository.saveReviews(updated)
    }

    fun deleteReview(reviewId: String) {
        val updated = _reviews.value.filter { it.id != reviewId }
        _reviews.value = updated
        repository.saveReviews(updated)
    }

    fun buildCollectedUserDataJson(): String {
        return repository.buildCollectedUserDataJson(
            profile = _userProfile.value,
            posts = _posts.value,
            friends = _friends.value,
            reviews = _reviews.value,
            settings = _settings.value
        )
    }

    fun exportCollectedUserData(): File? {
        val json = buildCollectedUserDataJson()
        val file = repository.exportCollectedUserDataToFile(json, _userProfile.value.displayUserId)
        if (file != null) {
            _lastExportPath.value = file.absolutePath
            _lastExportTimestamp.value = System.currentTimeMillis()
        }
        return file
    }
}
