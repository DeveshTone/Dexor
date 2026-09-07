package com.wrick.dexor.shizuku

object DexManager {
    data class DexoptState(val status: String, val reason: String)

    /**
     * Parse all package dexopt states from bulk command:
     *   `dumpsys package dexopt`
     * Uses streaming lineSequence and zero regexes to prevent GC pauses.
     */
    suspend fun getAllDexoptStates(): Map<String, DexoptState> {
        val output = ShizukuHelper.execOrEmpty("dumpsys package dexopt")
        if (output.isBlank()) return emptyMap()

        val map = HashMap<String, DexoptState>(400)
        var currentPackage: String? = null

        output.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.startsWith("[") && line.endsWith("]")) {
                val pkgCandidate = line.substring(1, line.length - 1).trim()
                if (pkgCandidate.contains(".")) {
                    currentPackage = pkgCandidate
                }
            } else if (currentPackage != null) {
                val statusIdx = line.indexOf("[status=")
                if (statusIdx != -1) {
                    val statusEnd = line.indexOf(']', statusIdx)
                    if (statusEnd != -1) {
                        val status = line.substring(statusIdx + 8, statusEnd).trim()
                        val reasonIdx = line.indexOf("[reason=")
                        val reason = if (reasonIdx != -1) {
                            val reasonEnd = line.indexOf(']', reasonIdx)
                            if (reasonEnd != -1) line.substring(reasonIdx + 8, reasonEnd).trim() else "unknown"
                        } else "unknown"
                        map[currentPackage!!] = DexoptState(status, reason)
                    }
                }
            }
        }
        return map
    }

    /**
     * Pull Status and Reason for a single package using targeted pipe.
     * Extremely fast and avoids processing megabytes of irrelevant dumpsys text.
     */
    suspend fun getDexoptState(packageName: String): DexoptState? {
        val command = "dumpsys package dexopt | grep -F -A 2 '[$packageName]'"
        val output = ShizukuHelper.execOrEmpty(command)

        if (output.isNotBlank()) {
            val statusIdx = output.indexOf("[status=")
            if (statusIdx != -1) {
                val statusEnd = output.indexOf(']', statusIdx)
                if (statusEnd != -1) {
                    val status = output.substring(statusIdx + 8, statusEnd).trim()
                    val reasonIdx = output.indexOf("[reason=")
                    val reason = if (reasonIdx != -1) {
                        val reasonEnd = output.indexOf(']', reasonIdx)
                        if (reasonEnd != -1) output.substring(reasonIdx + 8, reasonEnd).trim() else "unknown"
                    } else "unknown"
                    return DexoptState(status, reason)
                }
            }
        }

        return null
    }

    /**
     * Execute compilation via `cmd package compile -m <mode> -f <packageName>`.
     * Supported modes: verify, space, speed, everything.
     */
    suspend fun compilePackage(packageName: String, mode: String): Result<String> {
        val command = "cmd package compile -m $mode -f $packageName"
        return ShizukuHelper.exec(command)
    }
}

