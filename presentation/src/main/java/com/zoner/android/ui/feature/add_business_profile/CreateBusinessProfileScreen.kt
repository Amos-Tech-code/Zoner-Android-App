package com.zoner.android.ui.feature.add_business_profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.window.core.layout.WindowSizeClass
import com.zoner.android.ui.navigation.CountrySelectRoute
import com.zoner.android.ui.designSystem.ZonerButton
import com.zoner.android.ui.designSystem.ZonerDropdownSelector
import com.zoner.android.ui.designSystem.ZonerSelectorTextField
import com.zoner.android.ui.designSystem.ZonerSpacer
import com.zoner.android.ui.designSystem.ZonerTextField
import com.zoner.android.ui.designSystem.ZonerTextLink
import com.zoner.android.util.DeviceConfiguration
import com.zoner.android.util.ObserveAsEvents
import com.zoner.domain.model.CountryModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBusinessProfileScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: CreateBusinessProfileViewModel = koinViewModel()
) {

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is CreateBusinessProfileEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT).show()
            }

            CreateBusinessProfileEvent.NavigateToCountrySelection -> {
                navController.navigate(CountrySelectRoute)
            }
        }
    }

    val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val useThisCountry =
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<CountryModel?>(
            "selected_country",null)?.collectAsStateWithLifecycle()

    LaunchedEffect(useThisCountry?.value) {
        useThisCountry?.value?.let {
            viewModel.onCountrySelected(it)
            // Optional: Clear savedStateHandle to avoid re-triggering on recomposition
            navController.currentBackStackEntry?.savedStateHandle?.remove<CountryModel>("selected_country")
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Create Business Profile",
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
        ResponsiveFormWrapper(
            modifier = Modifier.padding(innerPadding),
            deviceConfig = deviceConfiguration
        ) {
            CreateBusinessForm(
                name = formState.businessName,
                category = formState.category,
                location = formState.location,
                selectedCountry = formState.selectedCountry,
                phoneNumber = formState.phoneNumber,
                description = formState.description,
                isTermsAccepted = formState.isTermsAccepted,
                onNameChange = { viewModel.onBusinessNameChange(it) },
                onCategoryChange = { viewModel.onCategoryChange(it) },
                onLocationChange = { viewModel.onLocationChange(it) },
                onPhoneNumberChange = { viewModel.onPhoneNumberChange(it) },
                onDescriptionChange = { viewModel.onDescriptionChange(it) },
                onTermsChanged = { viewModel.onTermsAcceptedChange() },
                onSelectCountryClicked = viewModel::onCountrySelectionClicked,
                onTermsClicked = {},
                onCreateAccount = viewModel::createBusinessAccount
            )
        }

    }

}


@Composable
fun ResponsiveFormWrapper(
    deviceConfig: DeviceConfiguration,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val formModifier = when (deviceConfig) {
        DeviceConfiguration.MOBILE_LANDSCAPE,
        DeviceConfiguration.TABLET_PORTRAIT,
        DeviceConfiguration.TABLET_LANDSCAPE,
        DeviceConfiguration.DESKTOP -> Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .wrapContentWidth(Alignment.CenterHorizontally)
            .defaultMinSize(minWidth = 600.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(32.dp)

        else -> Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = formModifier,
            content = content
        )
    }
}



@Composable
private fun CreateBusinessForm(
    modifier: Modifier = Modifier,
    name: String,
    category: String,
    location:  String,
    selectedCountry: CountryModel,
    phoneNumber: String,
    description: String,
    isTermsAccepted: Boolean,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTermsChanged: () -> Unit,
    onSelectCountryClicked: () -> Unit,
    onTermsClicked: () -> Unit,
    onCreateAccount: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        ZonerTextField(
            text = "Business Name",
            value = name,
            onValueChange = onNameChange,
            hint = "Enter business name",
        )
        ZonerSpacer(16.dp)
        ZonerDropdownSelector(
            label = "Category",
            selectedOption = category,
            options = listOf("Salon", "Services", "Foods", "Fashion"),
            hint = "Select Category",
            onOptionSelected = onCategoryChange
        )
        ZonerSpacer(16.dp)
        ZonerDropdownSelector(
            label = "Location",
            selectedOption = location,
            options = listOf("Nairobi", "Nyeri", "Muranga", "Kiambu"),
            hint = "Select Location",
            onOptionSelected = onLocationChange
        )
        ZonerSpacer(16.dp)
        Column {
            Text(
                text = "Phone Number/ Whatsapp",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Dial code selector
                ZonerSelectorTextField(
                    value = "${selectedCountry.emoji} ${selectedCountry.dialCode}",
                    hint = "+254",
                    onClick = onSelectCountryClicked,
                    modifier = Modifier.weight(1f)
                )
                ZonerSpacer(8.dp)
                // Phone number input
                ZonerTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    label = "Phone Number",
                    hint = "Enter your phone number",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(2f)
                )
            }
        }
        ZonerSpacer(16.dp)
        ZonerTextField(
            text = "Brief description for your business",
            value = description,
            onValueChange = onDescriptionChange,
            hint = "Write a brief description of your business...",
            singleLine = false,
        )
        ZonerSpacer(16.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Checkbox(
                checked = isTermsAccepted,
                onCheckedChange = { onTermsChanged() },
            )

            Text(
                text = "I Accept",
                fontWeight = FontWeight.SemiBold
            )
            ZonerTextLink(
                text = "Terms & Conditions",
                textColor = MaterialTheme.colorScheme.primary,
                onClick = onTermsClicked
            )
        }
        ZonerButton(
            text = "Create Business Account",
            onClick = onCreateAccount,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

    }
}