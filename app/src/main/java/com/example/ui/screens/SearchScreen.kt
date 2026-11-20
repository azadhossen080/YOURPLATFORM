package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FriendStatus
import com.example.model.FriendUser
import com.example.model.PostModel
import com.example.ui.components.PostCardItem
import com.example.ui.theme.BrandPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    posts: List<PostModel>,
    friends: List<FriendUser>,
    onAcceptFriendRequest: (FriendUser) -> Unit,
    onSendFriendRequest: (FriendUser) -> Unit,
    onCancelFriendRequest: (FriendUser) -> Unit,
    onOpenFriendsScreen: () -> Unit = {},
    onLikePost: (String) -> Unit,
    onCommentPost: (PostModel) -> Unit,
    onSharePost: (PostModel) -> Unit,
    onDeletePost: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val trendingTags = listOf("#YOURPLATFORM", "#Waves", "#Tech", "#Bangladesh", "#Reels", "#স্বাগতম")

    val filteredPosts = if (searchQuery.isBlank()) {
        posts
    } else {
        posts.filter {
            it.postText.contains(searchQuery, ignoreCase = true) ||
                it.userName.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredPeople = if (searchQuery.isBlank()) {
        friends.filter { it.status == FriendStatus.SUGGESTION || it.status == FriendStatus.INCOMING_REQUEST }
    } else {
        friends.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.handle.contains(searchQuery, ignoreCase = true) ||
                it.bio.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("search_screen")
    ) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("বন্ধু, ইউজার বা বিষয় খুঁজুন...", fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = BrandPrimary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_text_field"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Trending Tags
        Text(
            text = "জনপ্রিয় বিষয়:",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            trendingTags.forEach { tag ->
                AssistChip(
                    onClick = { onQueryChange(tag.removePrefix("#")) },
                    label = { Text(tag, fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = BrandPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // People & Posts Unified Scrollable Results
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (filteredPeople.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "মানুষজন ও ফ্রেন্ড সাজেশন" else "খুঁজে পাওয়া ব্যবহারকারী (${filteredPeople.size})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary
                            )
                        )
                        Text(
                            text = "সব দেখুন",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = BrandPrimary
                            ),
                            modifier = Modifier
                                .clickable { onOpenFriendsScreen() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(filteredPeople, key = { it.id }) { person ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                                modifier = Modifier.width(168.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(person.avatarColorHex)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = person.name.take(1),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = person.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (person.isVerified) {
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = null,
                                                tint = BrandPrimary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = person.handle,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    when (person.status) {
                                        FriendStatus.INCOMING_REQUEST -> {
                                            Button(
                                                onClick = { onAcceptFriendRequest(person) },
                                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.fillMaxWidth().height(36.dp)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("গ্রহণ করুন", fontSize = 11.sp)
                                            }
                                        }
                                        FriendStatus.OUTGOING_REQUEST -> {
                                            OutlinedButton(
                                                onClick = { onCancelFriendRequest(person) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.fillMaxWidth().height(36.dp)
                                            ) {
                                                Text("পাঠানো হয়েছে", fontSize = 11.sp)
                                            }
                                        }
                                        FriendStatus.FRIEND -> {
                                            OutlinedButton(
                                                onClick = { onOpenFriendsScreen() },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.fillMaxWidth().height(36.dp)
                                            ) {
                                                Text("বন্ধু ✓", fontSize = 11.sp, color = BrandPrimary)
                                            }
                                        }
                                        FriendStatus.SUGGESTION -> {
                                            Button(
                                                onClick = { onSendFriendRequest(person) },
                                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.fillMaxWidth().height(36.dp)
                                            ) {
                                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("অ্যাড ফ্রেন্ড", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (filteredPosts.isEmpty() && filteredPeople.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোনো ফলাফল পাওয়া যায়নি!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            } else {
                items(filteredPosts, key = { it.id }) { post ->
                    PostCardItem(
                        post = post,
                        onLikeClick = { onLikePost(post.id) },
                        onCommentClick = { onCommentPost(post) },
                        onShareClick = { onSharePost(post) },
                        onDeleteClick = if (post.isUserPost) { { onDeletePost(post.id) } } else null,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
        }
    }
}
