package com.example

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.model.FriendStatus
import com.example.util.AppStringsRegistry
import com.example.viewmodel.PlatformViewModel
import com.example.viewmodel.PostUploadState
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

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

        val incoming = initialFriends.first { it.status == FriendStatus.INCOMING_REQUEST }
        viewModel.acceptFriendRequest(incoming.id)
        val afterAccept = viewModel.friends.value.first { it.id == incoming.id }
        assertEquals(FriendStatus.FRIEND, afterAccept.status)

        val suggestion = viewModel.friends.value.first { it.status == FriendStatus.SUGGESTION }
        viewModel.sendFriendRequest(suggestion.id)
        assertEquals(FriendStatus.OUTGOING_REQUEST, viewModel.friends.value.first { it.id == suggestion.id }.status)

        viewModel.cancelFriendRequest(suggestion.id)
        assertEquals(FriendStatus.SUGGESTION, viewModel.friends.value.first { it.id == suggestion.id }.status)

        viewModel.unfriend(incoming.id)
        assertEquals(FriendStatus.SUGGESTION, viewModel.friends.value.first { it.id == incoming.id }.status)
    }

    @Test
    fun `language change updates settings and localized strings`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = PlatformViewModel(app)

        val enStrings = AppStringsRegistry.forCode("en_US")
        assertEquals("Settings & Privacy", enStrings.settingsTitle)
        assertEquals("Publish", enStrings.publishButton)
        assertFalse(AppStringsRegistry.isRtl("en_US"))
        assertTrue(AppStringsRegistry.isRtl("ar_SA"))

        viewModel.updateSettings(
            viewModel.settings.value.copy(
                selectedLanguage = "English (United States)",
                selectedLanguageCode = "en_US"
            )
        )
        assertEquals("en_US", viewModel.settings.value.selectedLanguageCode)

        // Verify persistence across ViewModel re-creation
        val reloaded = PlatformViewModel(app)
        assertEquals("en_US", reloaded.settings.value.selectedLanguageCode)
        assertEquals("English (United States)", reloaded.settings.value.selectedLanguage)
    }

    @Test
    fun `post processing and upload workflow persists image post and shows upload states`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = PlatformViewModel(app)

        val tempImg = File(app.cacheDir, "test_upload_${System.currentTimeMillis()}.jpg")
        val bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        FileOutputStream(tempImg).use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        bmp.recycle()

        val initialCount = viewModel.posts.value.size
        var publishedCallbackInvoked = false
        viewModel.submitPost(
            text = "Processed photo test post",
            type = "gallery",
            mediaUri = Uri.fromFile(tempImg).toString(),
            onPublished = { publishedCallbackInvoked = true }
        )

        // Should enter Uploading state immediately
        assertTrue(viewModel.uploadState.value is PostUploadState.Uploading)

        // Advance Robolectric main looper & IO coroutine steps to complete
        val mainLooper = org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper())
        var attempts = 0
        while (viewModel.uploadState.value is PostUploadState.Uploading && attempts < 50) {
            mainLooper.idleFor(java.time.Duration.ofMillis(250))
            delay(50)
            attempts++
        }

        assertTrue(
            "Expected Success but was ${viewModel.uploadState.value}",
            viewModel.uploadState.value is PostUploadState.Success
        )
        assertTrue(publishedCallbackInvoked)
        assertEquals(initialCount + 1, viewModel.posts.value.size)

        val createdPost = viewModel.posts.value.first()
        assertEquals("Processed photo test post", createdPost.postText)
        assertNotNull(createdPost.mediaUri)
        assertTrue(createdPost.mediaUri!!.contains("/files/media/"))
        assertEquals(0, viewModel.selectedTab.value)
    }
}
