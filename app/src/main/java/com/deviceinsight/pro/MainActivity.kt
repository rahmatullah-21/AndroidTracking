package com.deviceinsight.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deviceinsight.pro.presentation.AppEntryViewModel
import com.deviceinsight.pro.presentation.navigation.DeviceInsightRoot
import com.deviceinsight.pro.presentation.theme.DeviceInsightTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val entryViewModel: AppEntryViewModel = hiltViewModel()
            val settings by entryViewModel.settings.collectAsStateWithLifecycle()
            DeviceInsightTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                DeviceInsightRoot()
            }
        }
    }
}
