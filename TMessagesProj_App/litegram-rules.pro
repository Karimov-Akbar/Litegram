# Litegram: extra R8 rules.

# Android 4.4 placeholders for framework classes (project :Api19Stubs). They must keep their
# names and members so that the real framework classes are used on Android 5.0+.
-keep class android.view.ViewOutlineProvider { *; }
-keep class android.view.View$OnApplyWindowInsetsListener { *; }
-keep class android.view.accessibility.AccessibilityNodeInfo$AccessibilityAction { *; }
-keep class android.graphics.drawable.RippleDrawable { *; }
-keep class android.graphics.drawable.VectorDrawable { *; }
-keep class android.graphics.drawable.AnimatedVectorDrawable { *; }
-keep class android.graphics.Outline { *; }

# Firebase (FCM, App Indexing) and the Credential Manager Play Services provider are not part of
# this build; some remaining libraries still reference them reflectively.
-dontwarn com.google.firebase.**
-dontwarn androidx.credentials.playservices.**
-dontwarn com.google.android.gms.identitycredentials.**
-dontwarn com.google.android.gms.fido.**
