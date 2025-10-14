# =========================================================
#         通用 Android 和 Kotlin 保留规则
# =========================================================

# 优化和构建设置
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-dump class_files.txt
-printseeds seeds.txt
-printusage unused.txt
-printmapping mapping.txt

# 保留调试信息和元数据
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes InnerClasses, EnclosingMethod
-keepattributes *Annotation*

# 保留 Android 核心组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends androidx.fragment.app.Fragment

# 保留 View 的构造函数和 set/get 方法
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    public void get*(...);
}

# 保留 R 类及其所有成员
-keep class **.R$* {
    public static <fields>;
}

# 保留原生方法和被注解的成员
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers class * {
    @com.box.other.blankj.utilcode.util.BusUtils$Bus <methods>;
    @android.webkit.JavascriptInterface <methods>;
    @androidx.annotation.Keep <methods>;
}

# 保留 Activity 中被 XML 引用的点击事件方法
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# 保留枚举类型
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留 Parcelable 和 Serializable 实现
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 保留 Kotlin 协程和反射相关
-keep class kotlin.coroutines.** { *; }
-keepnames class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }
-keep class kotlin.jvm.functions.** { *; }
-keep interface kotlin.jvm.functions.** { *; }


# =========================================================
#         解决 LiveData、ViewModel 和 XPopup 问题的核心规则
# =========================================================

# --- 1. 项目基础库 (base, im.data, im.sdk 等) ---
-keep class com.box.base.** { *; }
-keepclassmembers class com.box.base.** { *; }
-keep class com.box.common.data.** { *; }
-keepclassmembers class com.box.common.data.** { *; }
-keep class com.box.common.sdk.ImSDK { *; }
-keepclassmembers class com.box.common.sdk.ImSDK { *; }
-keep class com.box.common.sdk.ImSDK$Companion { *; }

# --- 2. ViewModel 和 LiveData 核心规则 ---
-keepnames class * implements com.box.base.base.viewmodel.BaseViewModel
-keep class com.box.mod.ui.activity.ModSplashModel { *; }
-keep class androidx.lifecycle.** { *; }
-keep interface androidx.lifecycle.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.LiveData {
    <init>(...);
    void setValue(java.lang.Object);
    void postValue(java.lang.Object);
    java.lang.Object getValue();
}
-keep public class * implements androidx.lifecycle.Observer {
    <init>(...);
    void onChanged(java.lang.Object);
}
# 关键: 保留 Activity 中由 Lambda 生成的匿名 Observer 类
-keep class com.box.mod.ui.activity.ModSplashActivity$createObserver$* { *; }

# --- 3. 数据模型和扩展函数 ---
-keep class com.box.base.state.ResultState { *; }
-keepclassmembers class com.box.base.state.ResultState { *; }
-keep class com.box.common.data.model.AppletsData { *; }
-keepclassmembers class com.box.common.data.model.AppletsData { *; }

# 保留扩展函数所在的Kt文件编译后的类
-keepnames class * implements com.box.base.ext.BaseViewModelExtKt
-keep class com.box.base.ext.BaseViewModelExtKt { *; }
-keep class com.box.base.ext.AnyExtKt { *; }

# --- 4. XPopup 弹窗库及自定义弹窗 ---
-keep class com.box.other.xpopup.** { *; }
-keep interface com.box.other.xpopup.** { *; }
-keepnames class * implements com.box.other.xpopup.core.CenterPopupView
-keep class * implements com.box.other.xpopup.core.BasePopupView { *; }
-keep class com.box.mod.ui.xpop.** { *; }
-keepclassmembers class com.box.mod.ui.xpop.** { *; }
-keep class com.box.mod.ui.xpop.ModXPopupCenterProtocol { *; }
-keepclassmembers class com.box.mod.ui.xpop.ModXPopupCenterProtocol {
    <init>(...);
    *;
}

# --- 5. 全局 ViewModel ---
-keep class com.box.common.event.AppViewModel { *; }
-keepclassmembers class com.box.common.event.AppViewModel { *; }
-keep class com.box.common.event.EventViewModel { *; }
-keepclassmembers class com.box.common.event.EventViewModel { *; }

# --- 6. 项目依赖的工具库 ---
# Chaoji Other & Blankj Utils & Toaster & MMKV
-keep class com.box.other.** { *; }
-keepclassmembers class com.box.other.** { *; }
-dontwarn com.box.other.**
-dontwarn com.box.other.blankj.utilcode.**
-keep class com.box.other.hjq.toast.Toaster { *; }
-keep class com.box.common.utils.MMKVUtil { *; }
-keepclassmembers class com.box.common.utils.MMKVUtil { *; }

# Retrofit & OkHttp & Gson
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz
-dontwarn okhttp3.internal.platform.**
-keep class com.google.gson.** {*; }
-keep class com.google.gson.stream.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# GSYVideoPlayer
-keep class com.shuyu.gsyvideoplayer.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.**
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**

# BRVAH
-keep class com.chad.library.adapter.base.** { *; }
-keepclassmembers class com.chad.library.adapter.base.** { *; }

# =========================================================
#         第三方 SDK 保留规则
# =========================================================

# VIVO Union SDK
-keep class com.vivo.unionsdk.** { *; }
-dontwarn com.vivo.unionsdk.**

# Umeng
-keep class com.umeng.** {*;}
-dontwarn com.umeng.**

# AMap (高德地图)
-keep class com.amap.** { *; }
-dontwarn com.amap.**
-keep class com.autonavi.**{*;}
-keep class com.amap.api.**{*;}

# Tencent
-keep class com.tencent.** { *; }
-dontwarn com.tencent.**
-keep class com.tencent.mm.opensdk.** { *; }
-dontwarn com.tencent.mm.opensdk.**

# MIIT (移动安全联盟)
-keep class com.bun.miitmdid.core.** {*;}
-keep class XI.CA.XI.**{*;}
-keep class XI.K0.XI.**{*;}
-keep class XI.XI.K0.**{*;}
-keep class XI.xo.XI.XI.**{*;}
-keep class com.asus.msa.SupplementaryDID.**{*;}
-keep class com.asus.msa.sdid.**{*;}
-keep class com.bun.lib.**{*;}
-keep class com.bun.miitmdid.**{*;}
-keep class com.huawei.hms.ads.identifier.**{*;}
-keep class com.samsung.android.deviceidservice.**{*;}
-keep class com.zui.opendeviceidlibrary.**{*;}
-keep public class com.netease.nis.sdkwrapper.Utils {public <methods>;}

# 其他推送或保活相关
-keep class com.huawei.android.hms.agent.**{*;}
-dontwarn com.huawei.android.hms.agent.**
-keep class com.xiaomi.mipush.sdk.**{*;}
-keep class com.xiaomi.push.**{*;}
-dontwarn com.xiaomi.mipush.sdk.**
-keep class com.meizu.cloud.pushsdk.**{*;}
-dontwarn com.meizu.cloud.pushsdk.**
-keep class com.vivo.push.**{*;}
-dontwarn com.vivo.push.**
-keep class com.hihonor.push.**{*;}
-dontwarn com.hihonor.push.**
-keep class com.taobao.accs.**{*;}
-keep class anet.channel.**{*;}
-keep class org.android.agoo.**{*;}
-keep class com.taobao.tlog.**{*;}
-dontwarn com.taobao.accs.**
-dontwarn anet.channel.**
-dontwarn org.android.agoo.**
-dontwarn com.taobao.tlog.**

# 其他
-dontwarn dalvik.**
-dontwarn org.codehaus.mojo.animal_sniffer.*


# =========================================================
#         网络请求核心规则 (修正)
# =========================================================

# 【关键补充】保留 Retrofit 使用的 ApiService 接口及其所有方法，防止反射调用失败
-keep interface com.box.common.network.ApiService { *; }

# 【关键补充】保留所有网络数据模型（请求体和响应体）及其成员，防止序列化/反序列化失败
# 虽然已有 com.chaoji.im.data.** 规则，但这条更通用，可以覆盖其他可能的数据类
-keepclassmembers class com.box.base.network.BaseResponse { *; }
-keep class com.box.common.network.** { *; }
-keepclassmembers class com.box.common.network.** { *; }

# 以下是您已有的网络规则，保持不变
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz
-dontwarn okhttp3.internal.platform.**
-keep class com.google.gson.** {*; }
-keep class com.google.gson.stream.** { *; }

# ================== OPPO Game SDK a.k.a nearme.game.sdk ==================
# 防止混淆 OPPO SDK 的主要包名 com.nearme 和 com.oplus
# -keep: 保留类和类的成员（方法、字段）不被混淆和移除
# { *; }: 表示保留类中的所有成员
-keep class com.nearme.** { *; }
-keep class com.nearme.game.sdk.** { *; }
-keep class com.nearme.game.** { *; }
-keep class com.oplus.** { *; }
-keep class com.oplus.cutpayment.protect.** { *; }
# -dontwarn: 告诉 Proguard/R8 不要警告找不到 com.nearme 和 com.oplus 包下的某些依赖
# SDK 内部可能引用了一些系统或其他不存在的类，这在编译时是正常的，使用此规则可以避免构建过程因这些警告而中断
-dontwarn com.nearme.game.IFrameInitCallback
-dontwarn com.nearme.game.IFrameInitService
-dontwarn com.nearme.game.IFrameInitCallback
-dontwarn com.oplus.**
# 如果你的游戏也集成了 OPPO 的推送服务 (HeyTap Push / MCS)，则需要额外添加以下规则
-keep public class * extends android.app.Service
-keep class com.heytap.msp.** { *; }
-dontwarn com.heytap.msp.**
-keep class com.heytap.mcs.** { *; }
-dontwarn com.heytap.mcs.**