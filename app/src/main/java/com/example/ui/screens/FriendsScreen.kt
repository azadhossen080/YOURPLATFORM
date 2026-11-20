package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FriendStatus
import com.example.model.FriendUser
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    friends: List<FriendUser>,
    initialTab: Int = 0,
    onBack: () -> Unit,
    onAcceptRequest: (FriendUser) -> Unit,
    onDeclineRequest: (FriendUser) -> Unit,
    onSendRequest: (FriendUser) -> Unit,
    onCancelRequest: (FriendUser) -> Unit,
    onUnfriend: (FriendUser) -> Unit,
    onRemoveSuggestion: (FriendUser) -> Unit,
    onShowSnackbar: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf("") }
    var userToUnfriend by remember { mutableStateOf<FriendUser?>(null) }

    val filteredAll = remember(friends, searchQuery) {
        if (searchQuery.isBlank()) friends
        else friends.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.handle.contains(searchQuery, ignoreCase = true) ||
                it.bio.contains(searchQuery, ignoreCase = true)
        }
    }

    val incomingRequests = filteredAll.filter { it.status == FriendStatus.INCOMING_REQUEST }
    val outgoingRequests = filteredAll.filter { it.status == FriendStatus.OUTGOING_REQUEST }
    val acceptedFriends = filteredAll.filter { it.status == FriendStatus.FRIEND }
    val suggestions = filteredAll.filter { it.status == FriendStatus.SUGGESTION }

    val totalIncoming = friends.count { it.status == FriendStatus.INCOMING_REQUEST }
    val totalFriends = friends.count { it.status == FriendStatus.FRIEND }
    val totalSuggestions = friends.count { it.status == FriendStatus.SUGGESTION }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("বন্ধু ও ফ্রেন্ড রিকোয়েস্ট", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            text = "$totalFriends জন বন্ধু • $totalIncoming টি নতুন রিকোয়েস্ট",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("friends_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("friends_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("বন্ধু বা নতুন মানুষ খুঁজুন...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Friends",
                        tint = BrandPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("friends_search_field")
            )

            // Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.testTag("tab_friend_requests"),
                    text = {
                        Text(
                            text = if (totalIncoming > 0) "রিকোয়েস্ট ($totalIncoming)" else "রিকোয়েস্ট",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.testTag("tab_friends_list"),
                    text = {
                        Text(
                            text = "বন্ধু ($totalFriends)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.testTag("tab_friend_suggestions"),
                    text = {
                        Text(
                            text = "সাজেশন ($totalSuggestions)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                )
            }

            // Tab Contents
            when (selectedTab) {
                0 -> {
                    if (incomingRequests.isEmpty() && outgoingRequests.isEmpty()) {
                        EmptyFriendsState(
                            icon = Icons.Default.PersonAdd,
                            title = "কোনো ফ্রেন্ড রিকোয়েস্ট নেই",
                            subtitle = "সাজেশন ট্যাব থেকে নতুন বন্ধুদের রিকোয়েস্ট পাঠাতে পারেন।"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (incomingRequests.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "আগত ফ্রেন্ড রিকোয়েস্ট (${incomingRequests.size})",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = BrandPrimary
                                        ),
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                                items(incomingRequests, key = { it.id }) { user ->
                                    FriendUserCard(
                                        user = user,
                                        subtitleExtra = if (user.requestTimeAgo.isNotBlank()) " • ${user.requestTimeAgo}" else "",
                                        actions = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        onAcceptRequest(user)
                                                        onShowSnackbar("${user.name}-এর ফ্রেন্ড রিকোয়েস্ট গ্রহণ করা হয়েছে! 🎉")
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp)
                                                        .testTag("accept_request_${user.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("নিশ্চিত করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        onDeclineRequest(user)
                                                        onShowSnackbar("রিকোয়েস্ট মুছে ফেলা হয়েছে")
                                                    },
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp)
                                                        .testTag("decline_request_${user.id}")
                                                ) {
                                                    Text("মুছে ফেলুন", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    )
                                }
                            }

                            if (outgoingRequests.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "আপনার পাঠানো রিকোয়েস্ট (${outgoingRequests.size})",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        ),
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                                items(outgoingRequests, key = { it.id }) { user ->
                                    FriendUserCard(
                                        user = user,
                                        subtitleExtra = " • রিকোয়েস্ট পাঠানো হয়েছে",
                                        actions = {
                                            OutlinedButton(
                                                onClick = {
                                                    onCancelRequest(user)
                                                    onShowSnackbar("ফ্রেন্ড রিকোয়েস্ট বাতিল করা হয়েছে")
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp)
                                                    .testTag("cancel_request_${user.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("রিকোয়েস্ট বাতিল করুন", fontSize = 12.sp)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    if (acceptedFriends.isEmpty()) {
                        EmptyFriendsState(
                            icon = Icons.Default.Group,
                            title = "আপনার বন্ধু তালিকা খালি",
                            subtitle = "রিকোয়েস্ট গ্রহণ করুন অথবা সাজেশন থেকে নতুন বন্ধু যুক্ত করুন।"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(acceptedFriends, key = { it.id }) { friend ->
                                FriendUserCard(
                                    user = friend,
                                    subtitleExtra = if (friend.isOnline) " • অনলাইন" else "",
                                    actions = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(BrandPrimary.copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.HowToReg,
                                                        contentDescription = null,
                                                        tint = BrandPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "বন্ধু আছেন",
                                                        color = BrandPrimary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                            OutlinedButton(
                                                onClick = { userToUnfriend = friend },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .height(40.dp)
                                                    .testTag("unfriend_button_${friend.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PersonRemove,
                                                    contentDescription = "Unfriend",
                                                    tint = BrandAccent,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("আনফ্রেন্ড", fontSize = 12.sp, color = BrandAccent)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                else -> {
                    if (suggestions.isEmpty()) {
                        EmptyFriendsState(
                            icon = Icons.Default.Group,
                            title = "কোনো সাজেশন বাকি নেই",
                            subtitle = "সার্চ করে অথবা রিকোয়েস্ট ট্যাবে আপনার বন্ধুদের দেখুন।"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    text = "আপনি চিনতে পারেন",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPrimary
                                    ),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                            items(suggestions, key = { it.id }) { person ->
                                FriendUserCard(
                                    user = person,
                                    actions = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    onSendRequest(person)
                                                    onShowSnackbar("${person.name}-কে ফ্রেন্ড রিকোয়েস্ট পাঠানো হয়েছে! ✨")
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                                    .testTag("add_friend_${person.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PersonAdd,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("অ্যাড ফ্রেন্ড", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    onRemoveSuggestion(person)
                                                    onShowSnackbar("সাজেশন থেকে সরানো হয়েছে")
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .weight(0.75f)
                                                    .height(48.dp)
                                                    .testTag("remove_suggestion_${person.id}")
                                            ) {
                                                Text("সরান", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (userToUnfriend != null) {
        val target = userToUnfriend!!
        AlertDialog(
            onDismissRequest = { userToUnfriend = null },
            title = { Text("বন্ধু তালিকা থেকে সরান?", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Text(
                    "আপনি কি নিশ্চিত যে আপনি ${target.name}-কে আপনার বন্ধু তালিকা থেকে সরাতে চান?",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUnfriend(target)
                        userToUnfriend = null
                        onShowSnackbar("${target.name}-কে বন্ধু তালিকা থেকে সরানো হয়েছে")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandAccent)
                ) {
                    Text("আনফ্রেন্ড করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToUnfriend = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun FriendUserCard(
    user: FriendUser,
    subtitleExtra: String = "",
    actions: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar with online indicator
                Box(modifier = Modifier.size(52.dp)) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(user.avatarColorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.take(1),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    if (user.isOnline) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2ECC71))
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (user.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = BrandPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Text(
                        text = "${user.handle}$subtitleExtra",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (user.bio.isNotBlank()) {
                        Text(
                            text = user.bio,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                                fontSize = 12.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if (user.mutualFriendsCount > 0) {
                        Text(
                            text = "${user.mutualFriendsCount} জন মিউচুয়াল ফ্রেন্ড",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BrandPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            actions()
        }
    }
}

@Composable
private fun EmptyFriendsState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(BrandPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    fontSize = 12.sp
                )
            )
        }
    }
}
