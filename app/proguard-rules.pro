# ============================================================================
# TARNEEB GAME - ProGuard Rules
# Version: 1.0.0
# ============================================================================

# ============================================================================
# Tarneeb Engine & Network (مهم جداً!)
# ============================================================================
-keep class com.tarneeb.engine.** { *; }
-keep class com.tarneeb.network.** { *; }
-keep class com.tarneeb.app.** { *; }
-keep class com.tarneeb.ui.** { *; }

# ============================================================================
# Android Components
# ============================================================================
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View

# ============================================================================
# Kotlin & Coroutines (ضروري جداً)
# ============================================================================
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keep class kotlinx.coroutines.** { *; }

# ============================================================================
# Serialization (JSON)
# ============================================================================
-keep class kotlinx.serialization.** { *; }
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ============================================================================
# Room Database
# ============================================================================
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.Dao *;
    @androidx.room.Query *;
    @androidx.room.Insert *;
    @androidx.room.Update *;
    @androidx.room.Delete *;
}

# ============================================================================
# Compose (مهم جداً للـ UI)
# ============================================================================
-keep class androidx.compose.** { *; }
-keep public class * extends androidx.compose.runtime.Composable
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable *;
}

# ============================================================================
# Material Design
# ============================================================================
-keep class com.google.android.material.** { *; }
-keep class androidx.appcompat.** { *; }

# ============================================================================
# Networking (OkHttp)
# ============================================================================
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ============================================================================
# Coil (صور)
# ============================================================================
-keep class coil.** { *; }

# ============================================================================
# MultiDex
# ============================================================================
-keep class androidx.multidex.** { *; }

# ============================================================================
# DataStore
# ============================================================================
-keep class androidx.datastore.** { *; }

# ============================================================================
# WorkManager
# ============================================================================
-keep class androidx.work.** { *; }

# ============================================================================
# Timber (Logging)
# ============================================================================
-keep class timber.log.** { *; }

# ============================================================================
# قواعد عامة مهمة
# ============================================================================
# منع إزالة الـ constructors الافتراضية
-keepclasseswithmembers class * {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# الاحتفاظ بالـ Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# الاحتفاظ بالـ Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ============================================================================
# تجاهل التحذيرات (اختياري)
# ============================================================================
-dontwarn javax.**
-dontwarn java.**
-dontwarn org.slf4j.**
-dontwarn org.apache.**

# ============================================================================
# تحسينات R8
# ============================================================================
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify
-verbose
