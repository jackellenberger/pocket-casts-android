package au.com.shiftyjelly.pocketcasts.feature.settings.playback

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import au.com.shiftyjelly.pocketcasts.feature.settings.R

import androidx.preference.Preference
import au.com.shiftyjelly.pocketcasts.feature.settings.chapterblocklist.ChapterBlocklistManagerFragment

class PlaybackSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_playback, rootKey)

        val chapterBlocklistPref = findPreference<Preference>("pref_chapter_blocklist_settings")
        chapterBlocklistPref?.setOnPreferenceClickListener {
            parentFragmentManager.beginTransaction()
                .replace(android.R.id.content, ChapterBlocklistManagerFragment()) // Replace with your actual container ID if different
                .addToBackStack(null) // Allows user to navigate back
                .commit()
            true
        }
    }
}
