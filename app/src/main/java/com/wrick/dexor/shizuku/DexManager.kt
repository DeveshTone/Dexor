package com.wrick.dexor.shizuku

object DexManager {
    data class DexoptState(val status: String, val reason: String)

    private val STATUS_REGEX = Regex("""\[status=([^\]]+)\]""")
    private val REASON_REGEX = Regex("""\[reason=([^\]]+)\]""")

    /**
     * Parse all package dexopt states from bulk command:
     *   `dumpsys package dexopt`
     */
    suspend fun getAllDexoptStates(): Map<String, DexoptState> {
        val output = ShizukuHelper.execOrEmpty("dumpsys package dexopt")
        if (output.isBlank()) return emptyMap()

        val map = HashMap<String, DexoptState>(400)
        val lines = output.lines()

        var currentPackage: String? = null

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                val pkgCandidate = trimmed.substring(1, trimmed.length - 1).trim()
                if (pkgCandidate.contains(".")) {
                    currentPackage = pkgCandidate
                }
            } else if (currentPackage != null && trimmed.contains("[status=")) {
                val statusMatch = STATUS_REGEX.find(trimmed)
                val reasonMatch = REASON_REGEX.find(trimmed)
                if (statusMatch != null) {
                    val status = statusMatch.groupValues[1].trim()
                    val reason = reasonMatch?.groupValues?.get(1)?.trim() ?: "unknown"
                    map[currentPackage] = DexoptState(status, reason)
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
            val statusMatch = STATUS_REGEX.find(output)
            val reasonMatch = REASON_REGEX.find(output)

            if (statusMatch != null) {
                return DexoptState(
                    status = statusMatch.groupValues[1].trim(),
                    reason = reasonMatch?.groupValues?.get(1)?.trim() ?: "unknown"
                )
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

