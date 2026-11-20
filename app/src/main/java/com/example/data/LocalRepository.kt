package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppSettings
import com.example.model.FriendStatus
import com.example.model.FriendUser
import com.example.model.NotificationModel
import com.example.model.PostModel
import com.example.model.UserProfile
import org.json.JSONArray
import org.json.JSONObject

class LocalRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("yourplatform_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_POSTS = "posts_json"
        private const val KEY_SETTINGS = "settings_json"
        private const val KEY_PROFILE = "profile_json"
        private const val KEY_NOTIFICATIONS = "notifications_json"
        private const val KEY_FRIENDS = "friends_json"
    }

    fun loadPosts(): List<PostModel> {
        val raw = prefs.getString(KEY_POSTS, null)
        if (raw.isNullOrBlank()) {
            val defaults = getDefaultPosts()
            savePosts(defaults)
            return defaults
        }
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<PostModel>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val commentsArray = obj.optJSONArray("comments") ?: JSONArray()
                val commentsList = mutableListOf<String>()
                for (c in 0 until commentsArray.length()) {
                    commentsList.add(commentsArray.getString(c))
                }
                list.add(
                    PostModel(
                        id = obj.getString("id"),
                        userName = obj.getString("userName"),
                        timeAgo = obj.getString("timeAgo"),
                        postText = obj.getString("postText"),
                        mediaUri = if (obj.has("mediaUri") && !obj.isNull("mediaUri")) obj.getString("mediaUri") else null,
                        mediaType = obj.optString("mediaType", "text"),
                        thumbnailUri = if (obj.has("thumbnailUri") && !obj.isNull("thumbnailUri")) obj.getString("thumbnailUri") else null,
                        isVideo = obj.optBoolean("isVideo", false),
                        likeCount = obj.optInt("likeCount", 0),
                        isLiked = obj.optBoolean("isLiked", false),
                        comments = commentsList,
                        isUserPost = obj.optBoolean("isUserPost", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            getDefaultPosts()
        }
    }

    fun savePosts(posts: List<PostModel>) {
        val array = JSONArray()
        for (p in posts) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("userName", p.userName)
            obj.put("timeAgo", p.timeAgo)
            obj.put("postText", p.postText)
            obj.put("mediaUri", p.mediaUri)
            obj.put("mediaType", p.mediaType)
            obj.put("thumbnailUri", p.thumbnailUri)
            obj.put("isVideo", p.isVideo)
            obj.put("likeCount", p.likeCount)
            obj.put("isLiked", p.isLiked)
            obj.put("isUserPost", p.isUserPost)
            val cArray = JSONArray()
            p.comments.forEach { cArray.put(it) }
            obj.put("comments", cArray)
            array.put(obj)
        }
        prefs.edit().putString(KEY_POSTS, array.toString()).apply()
    }

    fun loadProfile(): UserProfile {
        val raw = prefs.getString(KEY_PROFILE, null) ?: return UserProfile()
        return try {
            val obj = JSONObject(raw)
            UserProfile(
                name = obj.optString("name", "আজাদ হোসেন"),
                handle = obj.optString("handle", "@azadhossen"),
                bio = obj.optString("bio", "YOURPLATFORM কন্টেন্ট ক্রিয়েটর 🚀"),
                followersCount = obj.optString("followersCount", "১.২K"),
                coverColorHex = obj.optLong("coverColorHex", 0xFF3F51B5),
                coverImageUri = if (obj.has("coverImageUri") && !obj.isNull("coverImageUri")) obj.getString("coverImageUri") else null,
                avatarColorHex = obj.optLong("avatarColorHex", 0xFF5352ED),
                avatarImageUri = if (obj.has("avatarImageUri") && !obj.isNull("avatarImageUri")) obj.getString("avatarImageUri") else null
            )
        } catch (e: Exception) {
            UserProfile()
        }
    }

    fun saveProfile(profile: UserProfile) {
        val obj = JSONObject()
        obj.put("name", profile.name)
        obj.put("handle", profile.handle)
        obj.put("bio", profile.bio)
        obj.put("followersCount", profile.followersCount)
        obj.put("coverColorHex", profile.coverColorHex)
        obj.put("coverImageUri", profile.coverImageUri)
        obj.put("avatarColorHex", profile.avatarColorHex)
        obj.put("avatarImageUri", profile.avatarImageUri)
        prefs.edit().putString(KEY_PROFILE, obj.toString()).apply()
    }

    fun loadSettings(): AppSettings {
        val raw = prefs.getString(KEY_SETTINGS, null) ?: return AppSettings()
        return try {
            val obj = JSONObject(raw)
            AppSettings(
                isDarkMode = obj.optBoolean("isDarkMode", false),
                isDimmingEnabled = obj.optBoolean("isDimmingEnabled", true),
                dimmingLevel = obj.optDouble("dimmingLevel", 30.0).toFloat(),
                isLocationAllowed = obj.optBoolean("isLocationAllowed", true),
                selectedLanguage = obj.optString("selectedLanguage", "বাংলা (Bangladesh)"),
                selectedLanguageCode = obj.optString("selectedLanguageCode", "bn_BD")
            )
        } catch (e: Exception) {
            AppSettings()
        }
    }

    fun saveSettings(settings: AppSettings) {
        val obj = JSONObject()
        obj.put("isDarkMode", settings.isDarkMode)
        obj.put("isDimmingEnabled", settings.isDimmingEnabled)
        obj.put("dimmingLevel", settings.dimmingLevel.toDouble())
        obj.put("isLocationAllowed", settings.isLocationAllowed)
        obj.put("selectedLanguage", settings.selectedLanguage)
        obj.put("selectedLanguageCode", settings.selectedLanguageCode)
        prefs.edit().putString(KEY_SETTINGS, obj.toString()).apply()
    }

    fun loadNotifications(): List<NotificationModel> {
        val raw = prefs.getString(KEY_NOTIFICATIONS, null)
        if (raw.isNullOrBlank()) {
            val defaults = getDefaultNotifications()
            saveNotifications(defaults)
            return defaults
        }
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<NotificationModel>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    NotificationModel(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        timeAgo = obj.getString("timeAgo"),
                        iconType = obj.optString("iconType", "like"),
                        isRead = obj.optBoolean("isRead", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            getDefaultNotifications()
        }
    }

    fun saveNotifications(notifications: List<NotificationModel>) {
        val array = JSONArray()
        for (n in notifications) {
            val obj = JSONObject()
            obj.put("id", n.id)
            obj.put("title", n.title)
            obj.put("timeAgo", n.timeAgo)
            obj.put("iconType", n.iconType)
            obj.put("isRead", n.isRead)
            array.put(obj)
        }
        prefs.edit().putString(KEY_NOTIFICATIONS, array.toString()).apply()
    }

    private fun getDefaultPosts(): List<PostModel> {
        return listOf(
            PostModel(
                id = "1",
                userName = "YOURPLATFORM Official",
                timeAgo = "২ মিনিট আগে",
                postText = "YOURPLATFORM-এ আপনাকে স্বাগতম! আপনার কভার ফটো এবং প্রোফাইল পিকচার এডিট করতে প্রোফাইলে যান।",
                mediaType = "gradient",
                likeCount = 18,
                comments = listOf("দারুণ অভিজ্ঞতা!"),
                isLiked = false,
                isUserPost = false
            ),
            PostModel(
                id = "2",
                userName = "আজাদ হোসেন",
                timeAgo = "১ ঘণ্টা আগে",
                postText = "নতুন ক্যামেরা, গ্যালারি ও রিলস আপলোড সাপোর্ট যুক্ত করা হলো।",
                mediaType = "text",
                likeCount = 52,
                comments = listOf("অভিনন্দন ভাই!"),
                isLiked = false,
                isUserPost = true
            )
        )
    }

    private fun getDefaultNotifications(): List<NotificationModel> {
        return listOf(
            NotificationModel(
                id = "1",
                title = "YOURPLATFORM Official আপনার পোস্টে লাইক দিয়েছেন।",
                timeAgo = "১০ মিনিট আগে",
                iconType = "like"
            ),
            NotificationModel(
                id = "2",
                title = "Tech World আপনার পোস্টে কমেন্ট করেছেন।",
                timeAgo = "১ ঘণ্টা আগে",
                iconType = "comment"
            ),
            NotificationModel(
                id = "3",
                title = "YOURPLATFORM এর নতুন Waves রিলস ফিচার ব্যবহার করে দেখুন!",
                timeAgo = "৩ ঘণ্টা আগে",
                iconType = "welcome"
            )
        )
    }

    fun loadFriends(): List<FriendUser> {
        val raw = prefs.getString(KEY_FRIENDS, null)
        if (raw.isNullOrBlank()) {
            val defaults = getDefaultFriends()
            saveFriends(defaults)
            return defaults
        }
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<FriendUser>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val statusName = obj.optString("status", FriendStatus.SUGGESTION.name)
                val status = try {
                    FriendStatus.valueOf(statusName)
                } catch (_: Exception) {
                    FriendStatus.SUGGESTION
                }
                list.add(
                    FriendUser(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        handle = obj.getString("handle"),
                        bio = obj.optString("bio", ""),
                        mutualFriendsCount = obj.optInt("mutualFriendsCount", 0),
                        avatarColorHex = obj.optLong("avatarColorHex", 0xFF5352ED),
                        isVerified = obj.optBoolean("isVerified", false),
                        isOnline = obj.optBoolean("isOnline", false),
                        status = status,
                        requestTimeAgo = obj.optString("requestTimeAgo", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            getDefaultFriends()
        }
    }

    fun saveFriends(friends: List<FriendUser>) {
        val array = JSONArray()
        for (f in friends) {
            val obj = JSONObject()
            obj.put("id", f.id)
            obj.put("name", f.name)
            obj.put("handle", f.handle)
            obj.put("bio", f.bio)
            obj.put("mutualFriendsCount", f.mutualFriendsCount)
            obj.put("avatarColorHex", f.avatarColorHex)
            obj.put("isVerified", f.isVerified)
            obj.put("isOnline", f.isOnline)
            obj.put("status", f.status.name)
            obj.put("requestTimeAgo", f.requestTimeAgo)
            array.put(obj)
        }
        prefs.edit().putString(KEY_FRIENDS, array.toString()).apply()
    }

    private fun getDefaultFriends(): List<FriendUser> {
        return listOf(
            // Incoming Friend Requests
            FriendUser(
                id = "u_req_1",
                name = "তানভীর আহমেদ",
                handle = "@tanvir_ahmed",
                bio = "ফটোগ্রাফি ও ট্রাভেল ভ্লগার 📸",
                mutualFriendsCount = 12,
                avatarColorHex = 0xFF009688,
                isVerified = true,
                isOnline = true,
                status = FriendStatus.INCOMING_REQUEST,
                requestTimeAgo = "১৫ মিনিট আগে"
            ),
            FriendUser(
                id = "u_req_2",
                name = "নুসরাত জাহান",
                handle = "@nusrat_jahan",
                bio = "ডিজিটাল আর্টিস্ট ও ডিজাইনার 🎨",
                mutualFriendsCount = 8,
                avatarColorHex = 0xFFE91E63,
                isOnline = true,
                status = FriendStatus.INCOMING_REQUEST,
                requestTimeAgo = "২ ঘণ্টা আগে"
            ),
            FriendUser(
                id = "u_req_3",
                name = "রাফি ইসলাম",
                handle = "@rafi_islam",
                bio = "টেক রিভিউয়ার ও গেমার 🎮",
                mutualFriendsCount = 5,
                avatarColorHex = 0xFF3F51B5,
                isOnline = false,
                status = FriendStatus.INCOMING_REQUEST,
                requestTimeAgo = "১ দিন আগে"
            ),
            FriendUser(
                id = "u_req_4",
                name = "সাদিয়া আক্তার",
                handle = "@sadia_akter",
                bio = "কবিতা ও গল্প ভালোবাসি ✍️",
                mutualFriendsCount = 3,
                avatarColorHex = 0xFF9C27B0,
                isOnline = false,
                status = FriendStatus.INCOMING_REQUEST,
                requestTimeAgo = "২ দিন আগে"
            ),

            // Accepted Friends
            FriendUser(
                id = "u_fr_1",
                name = "মাহমুদ হাসান",
                handle = "@mahmud_dev",
                bio = "সফটওয়্যার ইঞ্জিনিয়ার 💻",
                mutualFriendsCount = 24,
                avatarColorHex = 0xFF5352ED,
                isVerified = true,
                isOnline = true,
                status = FriendStatus.FRIEND
            ),
            FriendUser(
                id = "u_fr_2",
                name = "ফারহানা ইয়াসমিন",
                handle = "@farhana_y",
                bio = "ফ্যাশন ও লাইফস্টাইল ক্রিয়েটর ✨",
                mutualFriendsCount = 19,
                avatarColorHex = 0xFFFF5722,
                isVerified = true,
                isOnline = true,
                status = FriendStatus.FRIEND
            ),
            FriendUser(
                id = "u_fr_3",
                name = "আরিফুর রহমান",
                handle = "@arifur_bd",
                bio = "মিউজিক লাভার 🎵",
                mutualFriendsCount = 11,
                avatarColorHex = 0xFF2196F3,
                isOnline = false,
                status = FriendStatus.FRIEND
            ),
            FriendUser(
                id = "u_fr_4",
                name = "তাসনিম তাবাসসুম",
                handle = "@tasnim_t",
                bio = "বই ও কফি ☕📚",
                mutualFriendsCount = 7,
                avatarColorHex = 0xFF8E24AA,
                isOnline = true,
                status = FriendStatus.FRIEND
            ),
            FriendUser(
                id = "u_fr_5",
                name = "মেহেদী হাসান",
                handle = "@mehedi_hasan",
                bio = "Waves রিলস মেকার 🎬",
                mutualFriendsCount = 15,
                avatarColorHex = 0xFF00897B,
                isOnline = false,
                status = FriendStatus.FRIEND
            ),

            // Suggestions (People You May Know)
            FriendUser(
                id = "u_sug_1",
                name = "জুবায়ের খান",
                handle = "@zubayer_k",
                bio = "ফিটনেস ও স্পোর্টস 🏋️",
                mutualFriendsCount = 6,
                avatarColorHex = 0xFFD84315,
                isOnline = true,
                status = FriendStatus.SUGGESTION
            ),
            FriendUser(
                id = "u_sug_2",
                name = "মিথিলা রহমান",
                handle = "@mithila_r",
                bio = "ফুড ব্লগার 🍲",
                mutualFriendsCount = 10,
                avatarColorHex = 0xFFC2185B,
                isVerified = true,
                isOnline = false,
                status = FriendStatus.SUGGESTION
            ),
            FriendUser(
                id = "u_sug_3",
                name = "নাফিজ আহমেদ",
                handle = "@nafiz_a",
                bio = "ইউটিউবার ও কনটেন্ট ক্রিয়েটর 🎥",
                mutualFriendsCount = 4,
                avatarColorHex = 0xFF1E88E5,
                isOnline = true,
                status = FriendStatus.SUGGESTION
            ),
            FriendUser(
                id = "u_sug_4",
                name = "সুমাইয়া ইসলাম",
                handle = "@sumaiya_i",
                bio = "ভ্রমণপিপাসু 🌍",
                mutualFriendsCount = 2,
                avatarColorHex = 0xFF43A047,
                isOnline = false,
                status = FriendStatus.SUGGESTION
            )
        )
    }
}
