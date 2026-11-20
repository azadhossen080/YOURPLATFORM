package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.PostModel
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary

@Composable
fun PostCardItem(
    post: PostModel,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BrandPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.userName.take(1),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.userName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = post.timeAgo,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    )
                }

                if (onDeleteClick != null) {
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("পোস্ট ডিলিট করুন", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Post Text
            Text(
                text = post.postText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp
                )
            )

            // Post Media
            if (post.mediaType == "gradient") {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(BrandPrimary, BrandAccent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "YOURPLATFORM",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "Make your any choose....",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else if (!post.mediaUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = post.mediaUri,
                    contentDescription = "Post Media",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else if (post.mediaType == "reels") {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF232338)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.PlayCircleFilled,
                            contentDescription = "Reels",
                            tint = BrandPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Waves Reel কন্টেন্ট",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )

            // Actions row: Like, Comment, Share
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                val likeColor by animateColorAsState(
                    targetValue = if (post.isLiked) BrandPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    animationSpec = spring(),
                    label = "likeColor"
                )

                TextButton(
                    onClick = onLikeClick,
                    modifier = Modifier.testTag("post_like_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Like",
                        tint = likeColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.likeCount} লাইক",
                        color = likeColor,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                    )
                }

                // Comment Button
                TextButton(
                    onClick = onCommentClick,
                    modifier = Modifier.testTag("post_comment_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.comments.size} কমেন্ট",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                    )
                }

                // Share Button
                TextButton(
                    onClick = onShareClick,
                    modifier = Modifier.testTag("post_share_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "শেয়ার",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                    )
                }
            }
        }
    }
}
