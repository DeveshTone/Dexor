package com.wrick.dexor

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.wrick.dexor.ui.MainScreen
import com.wrick.dexor.ui.theme.createDexorTypography
import com.wrick.dexor.viewmodel.MainViewModel
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    private val onBinderReceived = Shizuku.OnBinderReceivedListener {
        vm.refreshShizukuState()
        vm.loadApps(forceRefresh = false)
    }

    private val onBinderDead = Shizuku.OnBinderDeadListener {
        vm.refreshShizukuState()
    }

    private val onPermissionResult = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            vm.refreshShizukuState()
            vm.refreshApps()
        } else {
            Toast.makeText(this, "Shizuku permission denied", Toast.LENGTH_SHORT).show()
            vm.refreshShizukuState()
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Strictly lock orientation to portrait only
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // Edge-to-edge transparent system bars
        enableEdgeToEdge()

        Shizuku.addBinderReceivedListenerSticky(onBinderReceived)
        Shizuku.addBinderDeadListener(onBinderDead)
        Shizuku.addRequestPermissionResultListener(onPermissionResult)

        val appColors = darkColorScheme(
            primary = Color(0xFF90CAF9),
            onPrimary = Color.Black,
            surface = Color(0xFF0F1115),
            background = Color(0xFF090A0C),
            onBackground = Color(0xFFECEFF1),
            onSurface = Color(0xFFECEFF1),
            surfaceVariant = Color(0xFF131720),
            onSurfaceVariant = Color(0xFF90A4AE)
        )

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
            val typography = createDexorTypography(isExpanded)

            MaterialTheme(
                colorScheme = appColors,
                typography = typography
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(vm, windowSizeClass)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeBinderReceivedListener(onBinderReceived)
        Shizuku.removeBinderDeadListener(onBinderDead)
        Shizuku.removeRequestPermissionResultListener(onPermissionResult)
    }
}

