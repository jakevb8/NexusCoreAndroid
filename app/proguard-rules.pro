# ── Moshi ─────────────────────────────────────────────────────────────────────
# Keep all Moshi-generated JsonAdapter classes
-keep class **JsonAdapter { *; }
-keepclassmembers class **JsonAdapter { *; }

# Keep all @JsonClass-annotated data classes and their fields
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers @com.squareup.moshi.JsonClass class * { *; }

# ── Enums used in JSON (de)serialization ──────────────────────────────────────
# R8 strips/renames enum fields by default; Moshi looks them up by name at runtime
-keepclassmembers enum me.jakev.nexuscore.data.model.Role { *; }
-keepclassmembers enum me.jakev.nexuscore.data.model.AssetStatus { *; }
-keepclassmembers enum me.jakev.nexuscore.data.model.OrgStatus { *; }

# ── Retrofit / OkHttp ─────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep interface retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ── Hilt ──────────────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# ── Firebase ──────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
