package com.example.model

data class UserProfile(
    val name: String = "আজাদ হোসেন",
    val handle: String = "@azadhossen",
    val bio: String = "YOURPLATFORM কন্টেন্ট ক্রিয়েটর 🚀",
    val followersCount: String = "১.২K",
    val coverColorHex: Long = 0xFF3F51B5,
    val coverImageUri: String? = null,
    val avatarColorHex: Long = 0xFF5352ED,
    val avatarImageUri: String? = null
)
