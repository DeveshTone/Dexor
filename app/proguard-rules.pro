# Shizuku reflection and binder preservation
-keep class rikka.shizuku.** { *; }
-dontwarn rikka.shizuku.**

# Dexor Data Models & Database Entities
-keep class com.wrick.dexor.model.** { *; }
-keep class com.wrick.dexor.db.** { *; }

# Room SQLite
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Coil Image Loading
-keep class coil.** { *; }
-dontwarn coil.**

# Coroutines
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}