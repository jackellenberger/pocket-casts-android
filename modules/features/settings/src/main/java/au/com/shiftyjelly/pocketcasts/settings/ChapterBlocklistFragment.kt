package au.com.shiftyjelly.pocketcasts.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import au.com.shiftyjelly.pocketcasts.compose.components.FormField
import au.com.shiftyjelly.pocketcasts.compose.components.FormFieldDefaults
import au.com.shiftyjelly.pocketcasts.compose.theme.PocketCastsTheme // Assuming a theme exists
import au.com.shiftyjelly.pocketcasts.localization.R // For string resources
import au.com.shiftyjelly.pocketcasts.settings.viewmodel.ChapterBlocklistViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChapterBlocklistFragment : Fragment() {

    private val viewModel: ChapterBlocklistViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                // Assuming PocketCastsTheme is the appropriate theme composable
                PocketCastsTheme {
                    ChapterBlocklistScreen(
                        viewModel = viewModel,
                        onNavigateUp = { findNavController().navigateUp() }
                    )
                }
            }
        }
    }
}

@Composable
fun ChapterBlocklistScreen(
    viewModel: ChapterBlocklistViewModel,
    onNavigateUp: () -> Unit
) {
    val blocklist by viewModel.blockList.collectAsState()
    var newTerm by remember { mutableStateOf("") }
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // For potential snackbar messages

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.chapter_blocklist_title)) }, // Add this string resource
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.abc_action_bar_up_description))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Input field and Add button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FormField(
                    value = newTerm,
                    onValueChange = { newTerm = it },
                    label = { Text(stringResource(id = R.string.blocklist_add_term_hint)) }, // Add this string resource
                    modifier = Modifier.weight(1f),
                    colors = FormFieldDefaults.textFieldColors(), // Use appropriate colors
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        viewModel.addTerm(newTerm)
                        // Optionally show feedback, e.g., a snackbar
                        scope.launch {
                            if (newTerm.isNotBlank() && !blocklist.any { it.equals(newTerm.trim(), ignoreCase = true) }) {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    context.getString(R.string.blocklist_term_added, newTerm.trim()) // Add this string
                                )
                                newTerm = "" // Clear input after adding
                            } else if (newTerm.isNotBlank()){
                                 scaffoldState.snackbarHostState.showSnackbar(
                                    context.getString(R.string.blocklist_term_duplicate, newTerm.trim()) // Add this string
                                )
                            } else {
                                // Handle blank input feedback if needed
                            }
                        }

                    },
                    enabled = newTerm.isNotBlank()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.blocklist_add_term_button)) // Add this string
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of blocked terms
            Text(
                text = stringResource(id = R.string.blocklist_current_terms_header), // Add this string
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (blocklist.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.blocklist_empty_message), // Add this string
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(blocklist.sorted().toList()) { term -> // Display sorted list
                        BlockedTermItem(
                            term = term,
                            onRemoveClick = { viewModel.removeTerm(term) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedTermItem(
    term: String,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = term,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        )
        IconButton(onClick = onRemoveClick) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(id = R.string.blocklist_remove_term_button, term) // Add this string
            )
        }
    }
}

// --- TODO: Add the following String Resources to strings.xml ---
// <string name="chapter_blocklist_title">Chapter Blocklist</string>
// <string name="blocklist_add_term_hint">Term to block</string>
// <string name="blocklist_add_term_button">Add</string>
// <string name="blocklist_current_terms_header">Blocked Terms</string>
// <string name="blocklist_empty_message">Your blocklist is empty.</string>
// <string name="blocklist_remove_term_button">Remove %s</string>
// <string name="blocklist_term_added">Added \'%s\' to blocklist</string>
// <string name="blocklist_term_duplicate">Term \'%s\' already in blocklist</string>