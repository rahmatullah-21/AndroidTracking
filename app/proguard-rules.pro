# Keep Room entities/DAOs metadata
-keep class com.deviceinsight.pro.database.entity.** { *; }

# Hilt generated components are handled by the Hilt Gradle plugin.

# Keep enum values used in Room TypeConverters / serialization
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# WorkManager workers instantiated via reflection by Hilt's WorkerFactory
-keep class * extends androidx.work.ListenableWorker
