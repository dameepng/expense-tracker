# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve line numbers and source file names for production crash reporting and stack traces
-keepattributes SourceFile,LineNumberTable

# Retain Gson serialization models for Anthropic Claude API
-keepclassmembers class com.example.expense_tracker.data.ai.** {
    <fields>;
    <init>(...);
}
-keep class com.example.expense_tracker.data.ai.Claude** { *; }

# Retain Room Database entities & DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# Retain Parcelable / Serializable / Enum types
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}