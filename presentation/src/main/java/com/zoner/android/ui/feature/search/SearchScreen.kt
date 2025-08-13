package com.zoner.android.ui.feature.search

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.zoner.android.ui.designSystem.DropdownMenuButton
import com.zoner.android.ui.designSystem.ZonerDropdownSelector
import com.zoner.android.ui.designSystem.ZonerSpacer
import com.zoner.android.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = koinViewModel()
) {

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is SearchEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "Search",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            SearchScreenHeader(
                locations = listOf("Nairobi", "Wangige", "GValley", "Ruiru", "Imenti"),
                selectedLocation = selectedLocation,
                searchQuery = searchQuery,
                onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                onOptionSelected = { viewModel.updateSelectedLocation(it) },
            )
            SearchScreenContent(
                modifier = Modifier.fillMaxSize()
            )
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreenHeader(
    modifier: Modifier = Modifier,
    locations: List<String>,
    selectedLocation: String,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onOptionSelected: (String) -> Unit,
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val caegories = listOf("All", "Salon", "Electronics", "Food", "Fashion", "Services")

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Dial code selector
//            ZonerDropdownSelector(
//                hint = "location",
//                selectedOption = selectedLocation,
//                onOptionSelected = onOptionSelected,
//                options = locations,
//                modifier = Modifier.weight(1f)
//            )
            DropdownMenuButton(
                selectedOption = selectedLocation,
                onOptionSelected = { index -> onOptionSelected(locations[index]) },
                options = locations,
                modifier = Modifier.weight(1f)
            )
            ZonerSpacer(8.dp)
            // Search input only (no expanding SearchBar wrapper)
            Surface(
                color = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant).containerColor, // ✅ default container color
                tonalElevation = SearchBarDefaults.TonalElevation,
                shape = SearchBarDefaults.inputFieldShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
            ) {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChanged,
                    onSearch = { keyboardController?.hide() },
                    expanded = false,
                    onExpandedChange = {},
                    modifier = Modifier
                        .fillMaxWidth(),
                    placeholder = { Text("Search businesses around you", maxLines = 1) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = if (searchQuery.isNotBlank()) {
                        {
                            IconButton(
                                onClick = { onSearchQueryChanged("") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Cancel search input"
                                )
                            }
                        }
                    } else null,
                    colors = SearchBarDefaults.inputFieldColors()
                )
            }
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            item {
                ElevatedFilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text("Filters") },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                )
            }
            items(caegories) { category ->
                ElevatedFilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text(category) },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                )
            }
        }
    }
}



@Composable
private fun SearchScreenContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No recent Searches"
        )
    }
}