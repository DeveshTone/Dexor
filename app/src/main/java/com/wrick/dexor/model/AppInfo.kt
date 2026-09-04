package com.wrick.dexor.model

import androidx.compose.runtime.Immutable

enum class InstallSource {
    SYSTEM, GOOGLE_PLAY, USER
}

enum class AppFilter {
    USER, SYSTEM
}

enum class SortMode {
    NAME, DEX_STATUS, SOURCE
}

enum class ShizukuState {
    NOT_RUNNING,
    NO_PERMISSION,
    READY
}

data class CompileMode(
    val id: String,
    val label: String,
    val description: String,
    val estimatedSecondsPerApp: Int
)

val COMPILE_MODES = listOf(
    CompileMode(
        id = "verify",
        label = "verify",
        description = "Completely clears all native code binaries. Frees up maximum storage space. Reverts the app to the raw JIT interpreter.",
        estimatedSecondsPerApp = 5
    ),
    CompileMode(
        id = "space",
        label = "space",
        description = "Compiles only core initialization methods. Optimizes for the lowest possible storage footprint while ensuring stable app boot times.",
        estimatedSecondsPerApp = 15
    ),
    CompileMode(
        id = "speed",
        label = "speed",
        description = "Force-compiles all accessible app methods into native machine code. Maximizes runtime smoothness and eliminates micro-stutters (ideal for gaming).",
        estimatedSecondsPerApp = 30
    ),
    CompileMode(
        id = "everything",
        label = "everything",
        description = "Compiles all code and resolves all class architecture references. Provides maximum possible deep optimization but uses the most storage space.",
        estimatedSecondsPerApp = 60
    )
)

@Immutable
data class AppInfo(
    val name: String,
    val packageName: String,
    val source: InstallSource,
    val dexStatus: String,
    val dexReason: String,
    val lastUpdateTime: Long,
    val lastStatusChangeTime: Long? = null,
    val lastCompilationTime: Long? = null,
    val versionName: String,
    val versionCode: Long,
    val targetSdk: Int,
    val minSdk: Int?,
    val codePath: String?,
    val dataDir: String?
)

