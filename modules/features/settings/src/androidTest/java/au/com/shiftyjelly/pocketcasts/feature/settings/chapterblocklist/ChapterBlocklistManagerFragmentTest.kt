package au.com.shiftyjelly.pocketcasts.feature.settings.chapterblocklist

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import au.com.shiftyjelly.pocketcasts.feature.settings.R // Test R class
import au.com.shiftyjelly.pocketcasts.preferences.Settings
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.not // For isNotDisplayed
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import org.junit.After


// Helper RecyclerView action to click a child view within a ViewHolder
fun clickChildViewWithId(id: Int) = object : androidx.test.espresso.ViewAction {
    override fun getConstraints() = null
    override fun getDescription() = "Click on a child view with specified id."
    override fun perform(uiController: androidx.test.espresso.UiController, view: android.view.View) {
        val v = view.findViewById<android.view.View>(id)
        v.performClick()
    }
}

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ChapterBlocklistManagerFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var settings: Settings

    @Before
    fun setUp() {
        hiltRule.inject()
        // Clear the blocklist before each test to ensure a clean state
        settings.chapterBlocklist.set(emptySet())
    }

    @After
    fun tearDown() {
        // Clear the blocklist after each test
        settings.chapterBlocklist.set(emptySet())
    }

    @Test
    fun testDisplayInitialBlocklist() {
        val initialItems = setOf("Intro", "Sponsor Spot")
        settings.chapterBlocklist.set(initialItems)

        launchFragmentInContainer<ChapterBlocklistManagerFragment>(
            themeResId = R.style.Theme_PocketCasts // Make sure this theme exists or use a suitable app theme
        ).moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.recyclerview_chapter_blocklist))
            .check(matches(isDisplayed()))
        onView(withText("Intro")).check(matches(isDisplayed()))
        onView(withText("Sponsor Spot")).check(matches(isDisplayed()))
    }

    @Test
    fun testAddNewItem() {
        launchFragmentInContainer<ChapterBlocklistManagerFragment>(
            themeResId = R.style.Theme_PocketCasts
        ).moveToState(Lifecycle.State.RESUMED)

        val newItem = "Advert"
        onView(withId(R.id.edittext_add_chapter_title))
            .perform(typeText(newItem), closeSoftKeyboard())
        onView(withId(R.id.button_add_chapter_title)).perform(click())

        // Verify in RecyclerView
        onView(withText(newItem)).check(matches(isDisplayed()))

        // Verify EditText is cleared
        onView(withId(R.id.edittext_add_chapter_title)).check(matches(withText("")))

        // Verify in Settings
        assert(settings.chapterBlocklist.value.contains(newItem))
    }

    @Test
    fun testDeleteItem() {
        val itemToDelete = "Sponsor Read"
        val initialItems = setOf("Intro", itemToDelete, "Outro")
        settings.chapterBlocklist.set(initialItems)

        launchFragmentInContainer<ChapterBlocklistManagerFragment>(
            themeResId = R.style.Theme_PocketCasts
        ).moveToState(Lifecycle.State.RESUMED)

        // Assuming "Sponsor Read" is at position 1 (0-indexed) if sorted or added in that order
        // This might be fragile if order changes. A more robust way is to find item by text then click delete.
        // For now, using position based on typical Set to List conversion order.
        // A custom matcher to find the item and then click its delete button would be better.
        onView(withId(R.id.recyclerview_chapter_blocklist))
            .perform(actionOnItemAtPosition<ChapterBlocklistAdapter.ViewHolder>(1, clickChildViewWithId(R.id.button_delete_chapter_title)))

        // Verify item is removed from RecyclerView
        onView(withText(itemToDelete)).check(matches(not(isDisplayed()))) // Check it's not displayed

        // Verify item is removed from Settings
        assert(!settings.chapterBlocklist.value.contains(itemToDelete))
        assert(settings.chapterBlocklist.value.contains("Intro")) // Ensure others remain
    }


    @Test
    fun testAttemptToAddEmptyItem() {
        val initialItemCount = settings.chapterBlocklist.value.size

        launchFragmentInContainer<ChapterBlocklistManagerFragment>(
            themeResId = R.style.Theme_PocketCasts
        ).moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.edittext_add_chapter_title))
            .perform(typeText(""), closeSoftKeyboard()) // Ensure it's empty
        onView(withId(R.id.button_add_chapter_title)).perform(click())

        // Verify RecyclerView count remains unchanged (tricky without direct count access, check if a non-existent item appears)
        // Alternative: ensure no new item (like an empty string) is displayed if it were to be added
        // For this test, we primarily check settings.
        onView(withId(R.id.recyclerview_chapter_blocklist))
             .check(matches(hasChildCount(initialItemCount)))


        // Verify Settings remains unchanged / no empty string added
        assert(settings.chapterBlocklist.value.size == initialItemCount)
        assert(!settings.chapterBlocklist.value.contains(""))
    }
}
