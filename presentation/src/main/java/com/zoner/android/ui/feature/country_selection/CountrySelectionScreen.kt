package com.zoner.android.ui.feature.country_selection

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.window.core.layout.WindowSizeClass
import com.zoner.android.ui.designSystem.ErrorScreen
import com.zoner.android.ui.designSystem.ZonerSpacer
import com.zoner.android.util.DeviceConfiguration
import com.zoner.domain.model.CountryModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountrySelectionScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: CountrySelectionViewModel = koinViewModel()
) {

    LaunchedEffect(true) {
        viewModel.event.collectLatest {
            when (it) {
                is CountrySelectionEvent.ShowErrorMessage -> {
                    Toast.makeText(navController.context, it.message, Toast.LENGTH_SHORT).show()
                }

                is CountrySelectionEvent.OnCountrySelected -> {
                    // Pass result back to previous screen
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_country", it.country)
                    navController.popBackStack() // Go back to SignIn screen
                }
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val countries by viewModel.countries.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Select a country",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state) {
                is CountrySelectionState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                is CountrySelectionState.Success -> {

                    when (deviceConfiguration) {
                        DeviceConfiguration.MOBILE_PORTRAIT,
                        DeviceConfiguration.MOBILE_LANDSCAPE -> {
                            CountrySelectorScreen(
                                countries = countries,
                                searchQuery = searchQuery,
                                onSearchQueryChanged = viewModel::updateSearchQuery,
                                onCountrySelected = viewModel::selectCountry,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }

                        DeviceConfiguration.TABLET_PORTRAIT,
                        DeviceConfiguration.TABLET_LANDSCAPE,
                        DeviceConfiguration.DESKTOP -> {
                            Row(modifier = Modifier.padding(16.dp)) {

                                // Optional placeholder or detail section (right pane)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(start = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Select a country from the list",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                // Country list & search input (left pane)
                                CountrySelectorScreen(
                                    countries = countries,
                                    searchQuery = searchQuery,
                                    onSearchQueryChanged = viewModel::updateSearchQuery,
                                    onCountrySelected = viewModel::selectCountry,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }
                        }
                    }

                }

                is CountrySelectionState.Error -> {
                    val errorMessage = (state as CountrySelectionState.Error).message
                    ErrorScreen(message = errorMessage)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountrySelectorScreen(
    modifier: Modifier = Modifier,
    countries: List<CountryModel>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onCountrySelected: (CountryModel) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier.fillMaxSize()) {
        // Search input only (no expanding SearchBar wrapper)
        Surface(
            color = SearchBarDefaults.colors().containerColor, // ✅ default container color
            tonalElevation = SearchBarDefaults.TonalElevation,
            shape = SearchBarDefaults.inputFieldShape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            SearchBarDefaults.InputField(
                query = searchQuery,
                onQueryChange = onSearchQueryChanged,
                onSearch = { keyboardController?.hide() },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = { Text("Search countries") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                colors = SearchBarDefaults.inputFieldColors()
            )
        }

        // Always visible filtered list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (countries.isEmpty()) {
                item {
                    Text(
                        text = "No results found",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(countries) { country ->
                    CountryItem(
                        countryModel = country,
                        onCountrySelected = onCountrySelected,
                    )
                }
            }
        }
    }
}


@Composable
private fun CountryItem(
    countryModel: CountryModel,
    onCountrySelected: (CountryModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCountrySelected(countryModel) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Country flag emoji
        Text(
            text = "${ countryModel.emoji }(${countryModel.code})",
            fontSize = 20.sp
        )
        ZonerSpacer(12.dp)
        // Country name
        Text(
            text = countryModel.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f) // This takes all remaining space
        )

        // Dial code (pushed to the end)
        Text(
            text = countryModel.dialCode,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

