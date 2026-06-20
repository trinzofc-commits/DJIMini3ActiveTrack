# DJI SDK V5 ProGuard Rules
-keep class dji.v5.** { *; }
-keep interface dji.v5.** { *; }
-keep enum dji.v5.** { *; }

-keep class dji.sdk.keyvalue.** { *; }
-keep class dji.v5.manager.** { *; }
-keep class dji.v5.common.** { *; }

# Keep ML Kit
-keep class com.google.mlkit.** { *; }

# General Android
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
