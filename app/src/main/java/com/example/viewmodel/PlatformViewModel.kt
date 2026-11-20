package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.LocalRepository
import com.example.model.AppSettings
import com.example.model.FriendStatus
import com.example.model.FriendUser
import com.example.model.NotificationModel
import com.example.model.PostModel
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class PlatformViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LocalRepository(application.applicationContext)
    val aiRepository = com.example.data.AiRepository()

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

    init {
        _posts.value = repository.loadPosts()
        _userProfile.value = repository.loadProfile()
        _settings.value = repository.loadSettings()
        _notifications.value = repository.loadNotifications()
        _friends.value = repository.loadFriends()
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addNewPost(text: String, type: String, mediaUri: String?) {
        val newPost = PostModel(
            id = UUID.randomUUID().toString(),
            userName = _userProfile.value.name,
            timeAgo = "মাত্র এখন",
            postText = text,
            mediaUri = mediaUri,
            mediaType = type,
            likeCount = 0,
            isLiked = false,
            comments = emptyList(),
            isUserPost = true
        )
        val updated = listOf(newPost) + _posts.value
        _posts.value = updated
        repository.savePosts(updated)
        _selectedTab.value = 0 // Return to home feed
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
        val updated = _userProfile.value.copy(coverImageUri = uri)
        _userProfile.value = updated
        repository.saveProfile(updated)
    }

    fun updateAvatarColor(colorHex: Long) {
        val updated = _userProfile.value.copy(avatarColorHex = colorHex, avatarImageUri = null)
        _userProfile.value = updated
        repository.saveProfile(updated)
    }

    fun updateAvatarUri(uri: String?) {
        val updated = _userProfile.value.copy(avatarImageUri = uri)
        _userProfile.value = updated
        repository.saveProfile(updated)
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
            val newNotif = NotificationModel(
                id = UUID.randomUUID().toString(),
                title = "আপনি ও $acceptedName এখন বন্ধু! 👋",
                timeAgo = "মাত্র এখন",
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
        val updated = _friends.value.map { user ->
            if (user.id == userId) {
                user.copy(status = FriendStatus.OUTGOING_REQUEST, requestTimeAgo = "মাত্র এখন")
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
}
