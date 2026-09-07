package com.wrick.dexor.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val packageName: String,
    val lastKnownDexStatus: String?,
    val lastKnownDexReason: String?,
    val lastUpdateTime: Long,
    val lastCompilationTimestamp: Long?,
    val statusChangeTimestamp: Long?,
    val installSource: String? = null,
    val appName: String? = null
)

