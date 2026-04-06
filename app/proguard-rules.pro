# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep jcifs classes
-keep class jcifs.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}