package com.zoner.android

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.zoner.android.navigation.AppNavGraph
import com.zoner.android.navigation.SignInRoute
import com.zoner.android.ui.theme.ZonerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    // Get ViewModel before calling installSplashScreen()
    private val viewModel: MainViewModel by viewModel()
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            viewModel.startDestination.value == null
        }

        // Optional: Animate splash exit
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // Create exit animation set
            AnimatorSet().apply {
                playTogether(
                    // Scale down with fade
                    ObjectAnimator.ofFloat(splashScreenView.iconView, View.SCALE_X, 1f, 0f),
                    ObjectAnimator.ofFloat(splashScreenView.iconView, View.SCALE_Y, 1f, 0f),
                    ObjectAnimator.ofFloat(splashScreenView.iconView, View.ALPHA, 1f, 0f),

                    // Optional: Add a slight rotation for dynamism
                    ObjectAnimator.ofFloat(splashScreenView.iconView, View.ROTATION, 0f, 15f)
                )

                duration = 600
                interpolator = AccelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        splashScreenView.remove()
                    }
                })
                start()
            }
        }

    super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZonerTheme {

                val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()

                if (startDestination != null) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets.safeDrawing
                    ) {
                        AppNavGraph(
                            navController = rememberNavController(),
                            startDestination = startDestination ?: SignInRoute,
                            windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
                        )
                    }
                }
            }
        }

    }
}

