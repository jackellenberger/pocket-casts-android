package au.com.shiftyjelly.pocketcasts.feature.settings.chapterblocklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import au.com.shiftyjelly.pocketcasts.feature.settings.databinding.FragmentChapterBlocklistManagerBinding // Assuming ViewBinding
import au.com.shiftyjelly.pocketcasts.preferences.Settings // Required for ViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChapterBlocklistManagerFragment : Fragment() {

    @Inject
    lateinit var settings: Settings // Or inject ViewModel factory if ViewModel needs it

    private var _binding: FragmentChapterBlocklistManagerBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChapterBlocklistViewModel
    private lateinit var adapter: ChapterBlocklistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChapterBlocklistManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel setup
        // Assuming a ViewModelFactory if Settings is a constructor dependency
        viewModel = ViewModelProvider(this, ChapterBlocklistViewModelFactory(settings)).get(ChapterBlocklistViewModel::class.java)

        setupRecyclerView()

        binding.buttonAddChapterTitle.setOnClickListener {
            val titleToAdd = binding.edittextAddChapterTitle.text.toString()
            if (titleToAdd.isNotBlank()) {
                viewModel.addChapterTitle(titleToAdd)
                binding.edittextAddChapterTitle.text.clear()
            }
        }

        viewModel.blocklistedChapters.observe(viewLifecycleOwner) { titles ->
            adapter.submitList(titles.toList())
        }
    }

    private fun setupRecyclerView() {
        adapter = ChapterBlocklistAdapter { titleToRemove ->
            viewModel.removeChapterTitle(titleToRemove)
        }
        binding.recyclerviewChapterBlocklist.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewChapterBlocklist.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

import androidx.lifecycle.asLiveData // Required for UserSetting.flow.asLiveData()
import au.com.shiftyjelly.pocketcasts.feature.settings.databinding.ListItemChapterBlocklistBinding // Assuming ViewBinding for list item

// Basic ViewModel (can be in its own file: ChapterBlocklistViewModel.kt)
// For simplicity, keeping it here for now.
class ChapterBlocklistViewModel(private val settings: Settings) : androidx.lifecycle.ViewModel() {

    // Exposes the blocklist as LiveData, automatically updating when the UserSetting changes.
    val blocklistedChapters: androidx.lifecycle.LiveData<Set<String>> = settings.chapterBlocklist.flow.asLiveData()

    fun addChapterTitle(title: String) {
        val currentSet = settings.chapterBlocklist.value // .value gets the current Set<String>
        val newSet = currentSet.toMutableSet()
        if (newSet.add(title)) { // Add returns true if the element was added, false if it already existed
            settings.chapterBlocklist.set(newSet) // .set() persists the new set
        }
    }

    fun removeChapterTitle(title: String) {
        val currentSet = settings.chapterBlocklist.value
        val newSet = currentSet.toMutableSet()
        if (newSet.remove(title)) { // Remove returns true if the element was removed
            settings.chapterBlocklist.set(newSet)
        }
    }
}

// ViewModelFactory if Settings is a constructor dependency for ViewModel
// (can be in its own file: ChapterBlocklistViewModelFactory.kt)
@Suppress("UNCHECKED_CAST")
class ChapterBlocklistViewModelFactory(private val settings: Settings) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChapterBlocklistViewModel::class.java)) {
            return ChapterBlocklistViewModel(settings) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


// RecyclerView Adapter (can be in its own file: ChapterBlocklistAdapter.kt)
class ChapterBlocklistAdapter(private val onDeleteClick: (String) -> Unit) :
    androidx.recyclerview.widget.ListAdapter<String, ChapterBlocklistAdapter.ViewHolder>(
        object : androidx.recyclerview.widget.DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        }
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemChapterBlocklistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ListItemChapterBlocklistBinding,
        private val onDeleteClick: (String) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(title: String) {
            binding.textviewChapterTitle.text = title
            binding.buttonDeleteChapterTitle.setOnClickListener {
                onDeleteClick(title)
            }
        }
    }
}
