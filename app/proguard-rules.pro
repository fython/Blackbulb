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
-keep class com.github.** {*;}
-keep class com.wdullaer.** {*;}