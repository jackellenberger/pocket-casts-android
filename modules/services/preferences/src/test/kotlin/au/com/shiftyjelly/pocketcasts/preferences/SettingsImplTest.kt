package au.com.shiftyjelly.pocketcasts.preferences

import android.content.Context
import android.content.SharedPreferences
import au.com.shiftyjelly.pocketcasts.test_utils.TestBookmarkFeatureControl
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.squareup.moshi.Moshi
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SettingsImplTest {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var privateSharedPreferences: SharedPreferences
    private lateinit var settings: SettingsImpl
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        sharedPreferences = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
        privateSharedPreferences = context.getSharedPreferences("private_test_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().commit()
        privateSharedPreferences.edit().clear().commit()

        val mockFirebaseRemoteConfig: FirebaseRemoteConfig = mockk(relaxed = true)
        val mockMoshi: Moshi = Moshi.Builder().build() // Real Moshi or mock as needed

        settings = SettingsImpl(
            sharedPreferences = sharedPreferences,
            privatePreferences = privateSharedPreferences,
            context = context,
            firebaseRemoteConfig = mockFirebaseRemoteConfig,
            moshi = mockMoshi,
            bookmarkFeature = TestBookmarkFeatureControl(isEnabled = true)
        )
    }

    @Test
    fun `chapterBlocklist default value is empty set`() {
        assertTrue(settings.chapterBlocklist.value.isEmpty())
    }

    @Test
    fun `chapterBlocklist can save and retrieve a set of strings`() {
        val testSet = setOf("Sponsor Read", "Intro Music")
        settings.chapterBlocklist.value = testSet
        assertEquals(testSet, settings.chapterBlocklist.value)
    }

    @Test
    fun `chapterBlocklist can update the set correctly`() {
        val initialSet = setOf("Sponsor Read")
        settings.chapterBlocklist.value = initialSet
        assertEquals(initialSet, settings.chapterBlocklist.value)

        val updatedSet = initialSet + "Outro Music"
        settings.chapterBlocklist.value = updatedSet
        assertEquals(updatedSet, settings.chapterBlocklist.value)

        val finalSet = updatedSet - "Sponsor Read"
        settings.chapterBlocklist.value = finalSet
        assertEquals(finalSet, settings.chapterBlocklist.value)
        assertEquals(setOf("Outro Music"), settings.chapterBlocklist.value)
    }

    @Test
    fun `chapterBlocklist persists across different instances`() {
        val testSet = setOf("Persistent Chapter")
        settings.chapterBlocklist.value = testSet

        // Create a new SettingsImpl instance with the same SharedPreferences
        val newSettings = SettingsImpl(
            sharedPreferences = sharedPreferences,
            privatePreferences = privateSharedPreferences,
            context = context,
            firebaseRemoteConfig = mockk(relaxed = true),
            moshi = Moshi.Builder().build(),
            bookmarkFeature = TestBookmarkFeatureControl(isEnabled = true)
        )
        assertEquals(testSet, newSettings.chapterBlocklist.value)
    }
}
