# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in G:\adt\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-allowaccessmodification
-optimizations !code/simplification/arithmetic
-keep class cyanogenmod.** { *; }
-dontobfuscate
-dontwarn cyanogenmod.**
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}
-keepclassmembers class **.R$* {
    public static <fields>;
}

-keep class info.papdt.blackblub.receiver.** {*;}
-keep class info.papdt.blackblub.services.** {*;}
-keep class info.papdt.blackblub.ui.** {*;}
-keep class info.papdt.blackblub.ui.LaunchActivity$MessageReceiver {*;}
-keep class info.papdt.blackblub.ui.shortcut.ToggleActivity$MessageReceiver {*;}
-keep class com.wdullaer.** {*;}