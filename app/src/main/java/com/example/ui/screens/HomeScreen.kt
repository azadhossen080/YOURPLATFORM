package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.rounded.VideoCall
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.PostModel
import com.example.model.UserProfile
import com.example.ui.components.PostCardItem
import com.example.ui.components.UploadProgressCard
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.util.LocalAppStrings
import com.example.viewmodel.PostUploadState

@Composable
fun HomeScreen(
    posts: List<PostModel>,
    userProfile: UserProfile,
    uploadState: PostUploadState,
    friendsCount: Int,
    pendingRequestCount: Int,
    onRetryUpload: () -> Unit,
    onDismissUpload: () -> Unit,
    onOpenFriends: () -> Unit,
    onOpenCreatePost: () -> Unit,
    onLikePost: (String) -> Unit,
    onCommentPost: (PostModel) -> Unit,
    onSharePost: (PostModel) -> Unit,
    onDeletePost: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_feed_list"),
        contentPadding = PaddingValues(12.dp)
    ) {
        // Live post processing & upload status banner
        if (uploadState !is PostUploadState.Idle) {
            item {
                UploadProgressCard(
                    uploadState = uploadState,
                    onRetry = onRetryUpload,
                    onDismiss = onDismissUpload,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }

        // Quick create post card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable { onOpenCreatePost() }
                    .testTag("quick_create_post_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(userProfile.avatarColorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userProfile.avatarImageUri != null) {
                            AsyncImage(
                                model = userProfile.avatarImageUri,
                                contentDescription = strings.navProfile,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = userProfile.displayInitial,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = strings.quickCreatePrompt,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Rounded.VideoCall,
                        contentDescription = strings.navCreatePost,
                        tint = BrandPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Quick Friends & Friend Requests Banner
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BrandPrimary.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { onOpenFriends() }
                    .testTag("home_friends_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BrandPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.friendsAndRequestsBanner,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPrimary,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = if (pendingRequestCount > 0)
                                    strings.newRequestsCount(pendingRequestCount, friendsCount)
                                else
                                    strings.friendsCountFindNew(friendsCount),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (pendingRequestCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BrandAccent)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = strings.newBadgeCount(pendingRequestCount),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = strings.friendsRequestsDesc,
                            tint = BrandPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Post list items
        items(posts, key = { it.id }) { post ->
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
