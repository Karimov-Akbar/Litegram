# How the Android 4.4 port works

Litegram is Telegram for Android 12.10.6 ([DrKLO/Telegram](https://github.com/DrKLO/Telegram),
commit in [UPSTREAM.md](UPSTREAM.md)) running on Android 4.4 KitKat (API 19).

Upstream supported API 19 up to version 11.13.4 (August 2025). Since 12.0 its `minSdk` is 21, so
newer code calls Android 5.0+ APIs without version checks. Instead of patching hundreds of call
sites by hand, the port works in four layers.

## 1. Build configuration

* `minSdk 19` in every module, including the media3 fork
  (`TMessagesProj_Modules/media/constants.gradle`) and jlatexmath.
* Legacy MultiDex for Dalvik: `androidx.multidex` + `MultiDex.install()` in
  `ApplicationLoader.attachBaseContext()`.
* Library versions that still support API 19, forced in the root `build.gradle`:
  `androidx.core 1.13.1`, `play-services-base/basement 18.3.0`, `play-services-tasks 18.1.0`.
* Libraries whose code never runs on 4.4 but declare a higher `minSdk` (Play Billing, ML Kit
  subject segmentation) are allowed with `tools:overrideLibrary` in
  `TMessagesProj/src/main/AndroidManifest.xml`.
* Core library desugaring (`desugar_jdk_libs`) provides `java.time`, streams etc.

## 2. Bytecode rewriting

`buildSrc/src/main/kotlin/org/telegram/plugin/Api19BackportTransform.kt` is an AGP
instrumentation (`AsmClassVisitorFactory`, scope `ALL`), so it also rewrites library classes and
generated code. Every call to a method listed in `Api19BackportRules.rules` is replaced with a
static call to `org.telegram.messenger.compat.Api19` (the receiver becomes the first argument).
The helper calls the real method on new Android versions and on old ones:

* skips purely cosmetic calls - elevation, outlines, ripple hotspots, system bar colors, tints;
* emulates the call with older APIs - `Canvas.drawRoundRect/drawArc/saveLayer(float...)`,
  `Path.addRoundRect(float...)`, `MediaCodec.getInputBuffer/getOutputBuffer`,
  `SpannableStringBuilder.append(text, what, flags)`, `ValueAnimator.ofArgb`,
  `Locale.toLanguageTag/forLanguageTag`, `URLConnection.getContentLengthLong`, ...

One rule fixes a behaviour change instead of a missing method: before Android 7.0 a `LinearLayout`
keeps only the size of `MarginLayoutParams` passed to `addView` (e.g. `LayoutHelper.createFrame`)
and drops the margins. Upstream code relies on the 7.0+ behaviour in dozens of places, so
`ViewGroup.addView(View, [int,] LayoutParams)` goes through `Api19.viewGroupAddView`, which
converts such parameters to `LinearLayout.LayoutParams` with the margins.

To support another method: add a rule to `Api19BackportRules.rules` and a static method with the
same name to `Api19`. Static methods use `Rule(..., isStatic = true)`.

## 3. Placeholder framework classes

`TMessagesProj_Modules/api19stubs` and `api19stubs_nosdk` (Gradle projects `:Api19Stubs`,
`:Api19StubsNoSdk`) add `ViewOutlineProvider`, `Outline`, `RippleDrawable`,
`VectorDrawable`, `AnimatedVectorDrawable`, `View$OnApplyWindowInsetsListener` and
`AccessibilityNodeInfo$AccessibilityAction` to the APK under their framework names. On Android 5.0+
the boot class loader always wins, so the real classes are used; on 4.4 the placeholders are, and
code that creates, subclasses or `instanceof`-checks these types keeps working.
`TMessagesProj_App/litegram-rules.pro` stops R8 from renaming them. Stubs of nested framework
classes are compiled without android.jar (`api19stubs_nosdk/fakesdk` is compile-only) because javac
would otherwise resolve the name to the real nested class.

R8 adds its own stubs for exception classes that only appear in `catch` clauses
(`android.system.ErrnoException`, `CameraAccessException`, ...). Without them Dalvik rejects the
whole class with a `VerifyError`, which is why debug (D8) builds can fail where release builds
don't.

## The Dalvik verifier

ART (Android 5.0+) tolerates references to missing classes; Dalvik (Android 4.4) verifies a whole
class when it is first used and rejects it with `VerifyError` if any method is not provably
type-safe. A missing class is treated as `java.lang.Object`, so code like

```java
RecordingCanvas c = source.beginRecording(w, h);   // app method declared to return RecordingCanvas (API 29)
c.drawColor(color);                               // Canvas method on an "Object" -> class rejected
```

kills its class on 4.4 even when it sits behind `if (Build.VERSION.SDK_INT >= 29)`. Calls to
missing *framework* methods and `new` of missing classes are fine (Dalvik replaces just that
instruction); the problem is values of missing types that come from **app** methods, fields or
parameters and are then used as their existing superclass (`Canvas`, `Shader`, `View`, ...), and
**arrays of missing classes** (`RenderNode[]`, `Network[]`, `Range[]`): Dalvik types such an array
as `Object`, so `array[i]` or `array.length` rejects the class. Litegram therefore declares such
members with the existing superclass or as `Object[]` with casts at the use sites
(`BlurredBackgroundSourceRenderNode`, `DownscaleScrollableNoiseSuppressor`, `RichMediaCell`,
`MotionBackgroundPaint`, `SizeNotifierFrameLayout`, WebRTC's `NetworkMonitorAutoDetect` and
`Camera2Enumerator`).

R8's horizontal class merging makes this much worse: it merges unrelated classes (lambdas,
anonymous classes) into one, and one bad method then takes all of them down - including classes
used at startup. `gradle.properties` disables it with
`-Dcom.android.tools.r8.disableHorizontalClassMerging=true` (R8 runs inside the Gradle daemon).
R8's API modeling stays on: it keeps code for newer APIs out of methods that run on old devices
and generates the exception stubs mentioned above.

## 4. Source changes

Where neither a rule nor a stub fits (constructors, fields, new classes): `CubicBezierInterpolator`
and `GradientProtectionDrawable` (`PathInterpolatorCompat`), `EditTextEffects` (the 4-argument
`EditText` constructor is replaced by an `AttributeSet` carrying the style, see
`StyledAttributes`), refresh rate handling in `AndroidUtilities`/`IntroActivity`, `LaunchActivity`,
`PhotoViewer`, `DialogsActivity`, `GroupCallActivity`, `ScaleStateListAnimator`,
`InstantCameraViewBase` (Camera2 only on 5.0+), `org.webrtc.Camera2Enumerator`, static icons
instead of animated vectors (`res/drawable/avd_*.xml`, originals in `drawable-v21`).

Upstream draws the main window edge-to-edge and takes the status and navigation bar sizes from
`WindowInsetsCompat`. Android 4.4 has no window insets and cannot make the system bars
transparent, so the content would end up under an opaque status bar with a zero offset.
`LaunchActivity`, `DrawerLayoutContainer` and `AndroidUtilities.enableEdgeToEdge()` keep the
window between the system bars on API < 21, as Telegram did before it dropped Android 4.x. The
keyboard height formulas ("root view height - status bar - visible frame") use
`AndroidUtilities.statusBarHeightForKeyboard()`, which is the top of the visible frame there;
with `statusBarHeight` = 0 the status bar counted as an always open keyboard.

media3 got back the ExoPlayer 2.x code paths for API < 21: `SynchronousMediaCodecAdapter`
(buffer arrays), `MediaCodecUtil` (`MediaCodecList` before 21), `MediaCodecInfo`,
`MediaCodecRenderer`, `MediaCodecVideoRenderer`, `DefaultAudioSink` +
`AudioTrackPositionTracker` + `DefaultAudioTrackProvider` (`AudioTrack.write(byte[])`), `Util`,
`FileDataSource`, `FileDescriptorDataSource`, `DrmUtil`, `VideoFrameReleaseEarlyTimeForecaster`.

The full list of changed files is in [CHANGES.md](CHANGES.md); every change in the code carries a
`Litegram` comment.

## Native code

* NDK r25c (25.2.9519653), the last NDK that targets API 19; `ANDROID_PLATFORM=android-19`.
* armeabi-v7a only (every Android 4.4 phone is 32-bit; add x86 for the emulator), ThinLTO instead
  of full LTO, no debug info (`-g0`), no 16 KB page alignment.
* Upstream's prebuilt static libraries (`TMessagesProj/jni/prebuild/lib`: FFmpeg, BoringSSL, libvpx,
  dav1d, opus, openh264, TDLib parts, tlottie, WAMR - built with NDK r27 for API 21) are linked
  as they are. `TMessagesProj/jni/litegram/compat_api19.c` and `compat_libcxx.cpp` provide the libc
  and libc++ symbols they need that Android 4.4 lacks (`mmap64`, `posix_fallocate`,
  `sigemptyset`, `__libcpp_verbose_abort`, ...).
* Gradle builds the native code itself (`externalNativeBuild`). `-PlitegramNativeLibsDir=<dir>`
  packages an already built `<dir>/<abi>/libtmessages.49.so` instead, `-PlitegramSkipNative`
  skips native code entirely (Java-only checks), `-PlitegramAbis=armeabi-v7a,x86` selects ABIs.

## Lighter and unofficial-build changes

* No Firebase/FCM, App Indexing, Android Auto, passkeys (`PasskeysController` is a stub) or Google
  Play Billing (`BillingController.PLAY_BILLING_DISABLED`, payments use Telegram invoices).
  Notifications come from Telegram's background connection; it and the keep-alive service are on
  by default (`ApplicationLoader.startPushService`, `ConnectionsManager`,
  `NotificationsSettingsActivity`).
* `LiteMode.PRESET_LITEGRAM_LEGACY`: power saving "everything off" by default on Android < 5.0 and
  low-end devices.
* `SharedConfig.getDefaultCacheLimit()`: smaller default cache on devices with little storage.
* `ChromecastController.isSupported()`: Cast is off on API < 21 and low-end devices.
* `LegacyTlsSocketFactory`: TLS 1.1/1.2 for `HttpsURLConnection` on 4.4.
* Own `applicationId` (`org.litegram.messenger`) and contacts sync account type, so it installs next
  to the official app; API ID and signing key from `local.properties`.

## Checking for new incompatibilities

* `gradlew -PlitegramSkipNative -PlitegramLint :TMessagesProj:lintRelease` (and
  `:TMessagesProj_Modules:media3:media-lib-<name>:lintRelease` for media3) runs only lint's `NewApi`
  check. Calls handled by the bytecode rules are still reported - compare with the rule table. lint
  does not see generated code and libraries.
* `dexdump` of the release APK shows references to framework classes and methods newer than
  API 19 (compare with `platforms/android-36/data/api-versions.xml`), `catch` clauses with such
  exception classes and app members declared with such types (see "The Dalvik verifier").
* Run it on Android 4.4. Dalvik's `VFY:` / `Could not find method` / `Verifier rejected class` log
  lines show every reference that will throw if it is executed.

## Testing on Android 4.4

Android 4.4 ARM system images only have a "goldfish" kernel, which needs the classic QEMU1
emulator engine. It was removed in emulator 29.0.6, so use emulator **28.0.23**
(`https://dl.google.com/android/repository/emulator-windows-5264690.zip`, also available for
Linux/macOS) with the `system-images;android-19;default;armeabi-v7a` image and
`emulator -avd <name> -engine classic`. It does not need hardware virtualization and is close to a
real budget phone (512 MB RAM, 480x854, 240 dpi for a Pixi 3). On a PC with 8 GB of RAM stop
Gradle before (`gradlew --stop`): when the PC swaps, Android's watchdog restarts the emulated
system.

Start it with `-prop dalvik.vm.execution-mode=int:fast` (Dalvik JIT off). When Dalvik clears its
full JIT code cache ("JIT code cache reset" in logcat, a few minutes after Litegram starts), the
classic engine keeps running stale translations of the erased code and the app dies with
`SIGSEGV` in `dalvik-jit-code-cache`. Without the JIT the app ran without crashes; real ARM
phones flush the instruction cache after the reset.

## Updating to a new Telegram version

1. Get the new upstream sources (with submodules) next to this repository.
2. Carry over the Litegram changes ([CHANGES.md](CHANGES.md); search for `Litegram` comments) and
   update the submodule commits and [UPSTREAM.md](UPSTREAM.md).
3. Run the checks above and fix new findings with a rule, a stub or a `Build.VERSION.SDK_INT` check.
4. Test on Android 4.4.
