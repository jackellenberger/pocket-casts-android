package au.com.shiftyjelly.pocketcasts.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.com.shiftyjelly.pocketcasts.preferences.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChapterBlocklistViewModel @Inject constructor(
    private val settings: Settings
) : ViewModel() {

    private val _blockList = MutableStateFlow<Set<String>>(emptySet())
    val blockList: StateFlow<Set<String>> = _blockList.asStateFlow()

    init {
        viewModelScope.launch {
            settings.getChapterBlocklist()
                .catch { e ->
                    Timber.e(e, "Error collecting chapter blocklist")
                    emit(emptySet()) // Emit empty set on error to recover gracefully
                 }
                .collectLatest { updatedBlocklist ->
                    _blockList.value = updatedBlocklist
                }
        }
    }

    /**
     * Adds a new term to the blocklist, ignoring empty or blank terms.
     * Persists the updated list.
     */
    fun addTerm(term: String) {
        val trimmedTerm = term.trim()
        if (trimmedTerm.isBlank()) {
            // Optionally handle feedback to the user about blank input
            Timber.w("Attempted to add a blank term to the blocklist.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) { // Use IO dispatcher for SharedPreferences write
            val currentList = _blockList.value // Read the latest value
            if (!currentList.any { it.equals(trimmedTerm, ignoreCase = true) }) { // Prevent adding duplicates (case-insensitive)
                val newList = currentList.toMutableSet().apply {
                    add(trimmedTerm)
                }
                settings.setChapterBlocklist(newList)
            } else {
                 Timber.d("Term '$trimmedTerm' already exists in the blocklist.")
                 // Optionally notify user
            }
        }
    }

    /**
     * Removes a specific term from the blocklist.
     * Persists the updated list.
     */
    fun removeTerm(term: String) {
         viewModelScope.launch(Dispatchers.IO) { // Use IO dispatcher for SharedPreferences write
            val currentList = _blockList.value // Read the latest value
            if (currentList.contains(term)) {
                val newList = currentList.toMutableSet().apply {
                    remove(term)
                }
                settings.setChapterBlocklist(newList)
            } else {
                 Timber.w("Attempted to remove term '$term' which is not in the blocklist.")
            }
         }
    }
}
