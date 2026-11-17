# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\L21Y07W20\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.quizduel.android.data.model.** { *; }
