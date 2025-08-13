package com.zoner.android.ui.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.window.core.layout.WindowSizeClass
import com.zoner.android.R
import com.zoner.android.ui.navigation.OnboardingRoute
import com.zoner.android.ui.navigation.SignInRoute
import com.zoner.android.ui.navigation.SignUpRoute
import com.zoner.android.ui.designSystem.ZonerButton
import com.zoner.android.util.DeviceConfiguration
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val rootModifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(32.dp)

    val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

    val onFinished = {
        viewModel.setOnboardingCompleted()
        navController.navigate(SignInRoute) {
            popUpTo(OnboardingRoute) {
                inclusive = true
            }
        }
    }

    when (deviceConfiguration) {
        DeviceConfiguration.MOBILE_PORTRAIT -> {
            OnboardingScreenContent(
                modifier = rootModifier,
                onFinished = onFinished,
                deviceConfiguration = deviceConfiguration
            )
        }

        DeviceConfiguration.MOBILE_LANDSCAPE -> {
            OnboardingScreenContent(
                modifier = rootModifier,
                onFinished = onFinished,
                deviceConfiguration = deviceConfiguration
            )
        }

        DeviceConfiguration.TABLET_PORTRAIT,
        DeviceConfiguration.TABLET_LANDSCAPE,
        DeviceConfiguration.DESKTOP -> {
            OnboardingScreenLarge(rootModifier, deviceConfiguration, onFinished)
        }
    }
}


@Composable
fun OnboardingScreenLarge(
    modifier: Modifier = Modifier,
    deviceConfiguration: DeviceConfiguration,
    onFinished: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(48.dp)
        ) {
            // Left illustration
            Image(
                painter = painterResource(R.drawable.onboarding_1), // Optional: dynamic image
                contentDescription = null,
                modifier = Modifier
                    .weight(1f)
                    .padding(32.dp)
                    .clip(RoundedCornerShape(28.dp)),
                contentScale = ContentScale.Crop
            )

            // Right side: pager + text
            OnboardingScreenContent(
                modifier = Modifier
                    .weight(1f)
                    .padding(32.dp),
                onFinished = onFinished,
                deviceConfiguration = deviceConfiguration
            )
        }
    }
}


@Composable
fun OnboardingScreenContent(
    modifier: Modifier = Modifier,
    deviceConfiguration: DeviceConfiguration,
    onFinished: () -> Unit,
) {
    val pages = listOf(
        OnboardingPage(
            title = "Discover businesses near you instantly.",
            description = "Find salons, shops, cafes, and services in your area without the hassle." +
                    " Quickly explore what’s nearby and never miss what your neighborhood has to offer.",
            imageRes = R.drawable.onboarding_1
        ),
        OnboardingPage(
            title = "See real-time offers and updates.",
            description = "Stay updated with the latest deals, discounts, and new arrivals from local businesses." +
                    " Everything is posted in real-time so you’re always in the know.",
            imageRes = R.drawable.onboarding_2
        ),
        OnboardingPage(
            title = "Connect directly via WhatsApp or phone.",
            description = "Reach out to businesses instantly." +
                    " Book a service, ask a question, or place an order through WhatsApp or a quick phone call — no middlemen, no delays.",
            imageRes = R.drawable.onboarding_3
        )
    )

    // Save the current page index
    val savedPage = rememberSaveable { mutableIntStateOf(0) }

    // Create the pager state with the saved page as initial page
    val pagerState = rememberPagerState(initialPage = savedPage.intValue) { pages.size }
    val scope = rememberCoroutineScope()

    // Update the saved page when the pager state changes
    LaunchedEffect(pagerState.currentPage) {
        savedPage.intValue = pagerState.currentPage
    }

    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPageItem(pages[page], deviceConfiguration)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DotsIndicator(
                    totalDots = pages.size,
                    selectedIndex = pagerState.currentPage,
                    modifier = Modifier.padding(16.dp)
                )

                if (pagerState.currentPage < pages.lastIndex) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Next",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    ZonerButton(
                        onClick = {
                            scope.launch { onFinished() }
                        },
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                        text = "GET STARTED"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (pagerState.currentPage < pages.lastIndex) {
            TextButton(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pages.lastIndex)
                    }
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(horizontal = 8.dp)
            ) { Text(text = "SKIP", style = MaterialTheme.typography.titleLarge) }
        }
    }
}


@Composable
fun OnboardingPageItem(
    page: OnboardingPage,
    deviceConfiguration: DeviceConfiguration
) {
    when (deviceConfiguration) {
        DeviceConfiguration.MOBILE_LANDSCAPE -> {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Image(
                    painter = painterResource(id = page.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = page.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .padding(bottom = 24.dp),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = page.description,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unSelectedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(totalDots) { index ->
            val animatedSize by animateDpAsState(
                targetValue = if (index == selectedIndex) 12.dp else 8.dp,
                label = "dotSize"
            )
            val color by animateColorAsState(
                targetValue = if (index == selectedIndex) selectedColor else unSelectedColor,
                label = "dotColor"
            )

            Box(
                modifier = Modifier
                    .size(animatedSize)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
