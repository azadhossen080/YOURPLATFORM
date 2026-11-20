package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.FriendStatus
import com.example.viewmodel.PlatformViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("YOURPLATFORM", appName)
    }

    @Test
    fun `launch MainActivity successfully`() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        assertNotNull(controller.get())
    }

    @Test
    fun `friends list and friend request lifecycle works`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = PlatformViewModel(app)

        val initialFriends = viewModel.friends.value
        assertTrue(initialFriends.any { it.status == FriendStatus.INCOMING_REQUEST })
        assertTrue(initialFriends.any { it.status == FriendStatus.FRIEND })
        assertTrue(initialFriends.any { it.status == FriendStatus.SUGGESTION })

        // Accept incoming request
        val incoming = initialFriends.first { it.status == FriendStatus.INCOMING_REQUEST }
        viewModel.acceptFriendRequest(incoming.id)
        val afterAccept = viewModel.friends.value.first { it.id == incoming.id }
        assertEquals(FriendStatus.FRIEND, afterAccept.status)

        // Send and cancel request
        val suggestion = viewModel.friends.value.first { it.status == FriendStatus.SUGGESTION }
        viewModel.sendFriendRequest(suggestion.id)
        assertEquals(FriendStatus.OUTGOING_REQUEST, viewModel.friends.value.first { it.id == suggestion.id }.status)

        viewModel.cancelFriendRequest(suggestion.id)
        assertEquals(FriendStatus.SUGGESTION, viewModel.friends.value.first { it.id == suggestion.id }.status)

        // Unfriend
        viewModel.unfriend(incoming.id)
        assertEquals(FriendStatus.SUGGESTION, viewModel.friends.value.first { it.id == incoming.id }.status)
    }
}
