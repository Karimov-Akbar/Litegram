# Files changed from upstream

Upstream: Telegram for Android 12.10.6 (see [UPSTREAM.md](UPSTREAM.md)). Changes in the code are
marked with a `Litegram` comment. How it fits together: [PORTING.md](PORTING.md).

## Build

| File | Change |
|---|---|
| `settings.gradle` | Only the needed modules: app, the API 19 stub modules, jlatexmath and 11 media3 modules |
| `build.gradle` | AGP + Kotlin only; forces `play-services-base/basement 18.3.0`, `tasks 18.1.0`, `androidx.core 1.13.1` (last versions with minSdk 19); `-PlitegramLint` mode (`NewApi` only); Windows path fix |
| `gradle.properties` | `APP_PACKAGE=org.litegram.messenger`; heap sizes for an 8 GB build machine; R8 horizontal class merging off (`-Dcom.android.tools.r8.disableHorizontalClassMerging=true`, see PORTING.md) |
| `gradle/wrapper/gradle-wrapper.properties`, `gradlew.bat` | Gradle 8.13 from the official GitHub mirror with checksum; Windows wrapper script |
| `buildSrc/.../TelegramBuildAppPlugin.kt` | Registers `Api19BackportFactory` for every variant |
| `buildSrc/.../Api19BackportTransform.kt` | **New.** Rule table: API 20+ calls -> `org.telegram.messenger.compat.Api19` |
| `TMessagesProj/build.gradle` | minSdk 19, NDK r25c, CMake `android-19`; MultiDex; no Firebase/Android Auto/credentials; API ID from `local.properties`; `litegramAbis`, `litegramSkipNative`, `litegramNativeLibsDir` properties |
| `TMessagesProj_App/build.gradle` | minSdk 19; signing key from `local.properties` (v1 + v2); R8 with `litegram-rules.pro`; APK name; no flavors |
| `TMessagesProj_App/litegram-rules.pro` | **New.** Keeps the placeholder framework classes; `-dontwarn` for removed libraries |
| `TMessagesProj/config/{debug,release}/AndroidManifest.xml` | No FCM/analytics, app name "Litegram" |
| `TMessagesProj/src/main/AndroidManifest.xml` | `tools:overrideLibrary` for ML Kit subject segmentation and Play Billing |
| Removed | `TMessagesProj_AppHockeyApp`, `TMessagesProj_AppHuawei`, `TMessagesProj_AppStandalone`, `TMessagesProj_AppTests`, the SDK23/standalone manifests, `Dockerfile`, `apkdiff.py`, `apkfrombundle.py` |

## Placeholder framework classes (new modules)

| Module | Classes |
|---|---|
| `TMessagesProj_Modules/api19stubs` | `ViewOutlineProvider`, `Outline`, `RippleDrawable`, `VectorDrawable`, `AnimatedVectorDrawable` |
| `TMessagesProj_Modules/api19stubs_nosdk` | `View$OnApplyWindowInsetsListener`, `AccessibilityNodeInfo$AccessibilityAction` (compiled without android.jar; `fakesdk/` is compile-only) |

## Native code

| File | Change |
|---|---|
| `TMessagesProj/jni/CMakeLists.txt` | ThinLTO, `-g0`, no 16 KB alignment, adds the `litegram/` sources |
| `TMessagesProj/jni/litegram/compat_api19.c` | **New.** libc functions missing on Android 4.4 (`mmap64`, `posix_fallocate`, `sigemptyset`, `rand`, `strtof`, ...) |
| `TMessagesProj/jni/litegram/compat_libcxx.cpp` | **New.** `__libcpp_verbose_abort` and `basic_stringstream` instantiations needed by the NDK r27 prebuilt libraries |

## Java: Android 4.4 compatibility

| File | Change |
|---|---|
| `messenger/compat/Api19.java` | **New.** Static replacements for API 20+ framework methods and for Java APIs missing in 4.4's libcore (`Locale.toLanguageTag/forLanguageTag`, `URLConnection.getContentLengthLong`, ...), used by the bytecode rules |
| `messenger/AndroidUtilities.java` | Refresh rate: `getSupportedRefreshRates()` only on API 21+ |
| `messenger/ApplicationLoader.java` | `MultiDex.install` below API 21; TLS 1.2 (`LegacyTlsSocketFactory`); no FCM |
| `messenger/LegacyTlsSocketFactory.java` | **New.** Enables TLS 1.1/1.2 for `HttpsURLConnection` on 4.4 |
| `messenger/pip/PipActivityController.java` | `MediaSession` only on API 21+ |
| `messenger/utils/GradientProtectionDrawable.java` | Own `Side` enum and `PathInterpolatorCompat` instead of API 21 classes |
| `ui/ActionBar/ActionBarMenuItem.java`, `ui/Components/ChatAttachAlert.java`, `ui/Components/PollVotesAlert.java`, `ui/Components/emojiview/FoundStickerPackButtonContainer.java`, `ui/Components/poll/sheets/CountrySelectBottomSheet.java`, `ui/community/CommunityEditActivity.java`, `ui/community/CommunitySheet.java` | `WindowInsetsCompat.Side` -> `GradientProtectionDrawable.Side` |
| `ui/Components/CubicBezierInterpolator.java` | `PathInterpolatorCompat` instead of `PathInterpolator` |
| `ui/Components/EditTextEffects.java`, `ui/Components/StyledAttributes.java` (new), `res/xml/litegram_edit_text_no_background.xml` (new) | The 4-argument `EditText` constructor (API 21) is replaced by an `AttributeSet` carrying the style |
| `ui/Components/InstantCameraViewBase.java` | Camera2 pipeline for round videos only on API 21+ |
| `ui/Components/ScaleStateListAnimator.java` | `StateListAnimator` only on API 21+ |
| `ui/DialogsActivity.java` | Animated speed icon only on API 21+; passkey hint off |
| `ui/GroupCallActivity.java` | Screen sharing only on API 21+ |
| `ui/IntroActivity.java` | Refresh rate below API 21 |
| `ui/LaunchActivity.java` | `TaskDescription` and the circular theme-switch animation only on API 21+; no App Indexing |
| `ui/PhotoViewer.java` | Video size alignment without `VideoCapabilities` below API 21; Chromecast check |
| `org/webrtc/Camera2Enumerator.java` | Camera2 not supported below API 21 |
| `res/drawable/avd_speed.xml`, `res/drawable/avd_flip.xml` | Static icons for 4.4; the animated vectors moved to `res/drawable-v21/` |
| `ui/Components/blur3/source/BlurredBackgroundSourceRenderNode.java`, `ui/Components/blur3/DownscaleScrollableNoiseSuppressor.java`, `ui/iv/RichMediaCell.java` | `RecordingCanvas` (API 29) declared as `Canvas`, so that Dalvik does not reject the callers |
| `ui/Components/MotionBackgroundPaint.java` | `RuntimeShader` (API 33) field declared as `Shader` for the same reason |
| `ui/Components/SizeNotifierFrameLayout.java`, `ui/Components/blur3/DownscaleScrollableNoiseSuppressor.java` | `RenderNode[]` (API 29) arrays declared as `Object[]`: Dalvik rejects any class that indexes an array of a missing class |
| `org/webrtc/NetworkMonitorAutoDetect.java`, `org/webrtc/Camera2Enumerator.java` | `Network[]`, `Range[]`, `Size[]` (API 21) declared as `Object[]` for the same reason (voice and video calls) |

## Java: lighter, unofficial build

| File | Change |
|---|---|
| `messenger/PushListenerController.java`, `GcmPushListenerService.java` (removed) | No Firebase; `NoPushServiceProvider` |
| `tgnet/ConnectionsManager.java`, `ui/NotificationsSettingsActivity.java` | Background connection and keep-alive service on by default |
| `messenger/BillingController.java` | Google Play Billing is not initialized (`PLAY_BILLING_DISABLED`) |
| `messenger/BuildVars.java` | API ID from `BuildConfig`; passkeys off |
| `messenger/PasskeysController.java` | Stub (no androidx.credentials) |
| `messenger/car/*` | Removed (Android Auto) |
| `messenger/ContactsController.java`, `res/xml/auth.xml`, `res/xml/sync_contacts.xml`, `res/xml/auth_menu.xml` | Account type `org.litegram.messenger` |
| `messenger/LiteMode.java` | "Everything off" power saving preset for Android < 5.0 and low-end devices |
| `messenger/SharedConfig.java`, `messenger/AutoDeleteMediaTask.java`, `ui/CacheControlActivity.java` | Default cache limit by storage size; 300 MB option |
| `messenger/chromecast/ChromecastController.java`, `ui/Components/AudioPlayerAlert.java` | Chromecast off below API 21 and on low-end devices |
| `ui/LoginActivity.java`, `ui/PassportActivity.java` | No Firebase SMS, SMS Retriever hash or Google sign-in (all bound to the official app signature) |
| `ui/bots/BotStorage.java` | Firebase import removed |
| `ui/LauncherIconController.java`, `res/mipmap-*/ic_launcher*.png`, `res/mipmap-anydpi-v26/ic_launcher*.xml`, `res/drawable/litegram_icon_*.xml` (new) | Own launcher icon instead of Telegram's logo |
| `README.md`, `docs/`, `.github/`, `.gitignore`, `.gitattributes`, `.gitmodules`, `local.properties.example` | Litegram documentation and repository setup |

## media3 (`TMessagesProj_Modules/media`)

Vendored from upstream's submodule ([Arseny271/media](https://github.com/Arseny271/media) at
`c430d20`), reduced to the 11 modules the app uses.

| File | Change |
|---|---|
| `constants.gradle` | minSdk 19 |
| `libraries/*/build.gradle` | Test dependencies disabled (`// [litegram]`) |
| `exoplayer/.../mediacodec/SynchronousMediaCodecAdapter.java` | `getInputBuffers/getOutputBuffers` arrays below API 21 |
| `exoplayer/.../mediacodec/MediaCodecUtil.java` | `MediaCodecListCompatV16` below API 21 |
| `exoplayer/.../mediacodec/MediaCodecInfo.java` | Video size checks without `VideoCapabilities` below API 21 |
| `exoplayer/.../mediacodec/MediaCodecRenderer.java`, `MediaCodecDecoderException.java` | `CodecException` only on API 21+ |
| `exoplayer/.../video/MediaCodecVideoRenderer.java` | Frame release without `releaseOutputBuffer(index, time)` below API 21 |
| `exoplayer/.../video/VideoFrameReleaseEarlyTimeForecaster.java` | No `android.util.Range` |
| `exoplayer/.../audio/DefaultAudioSink.java`, `AudioTrackPositionTracker.java`, `DefaultAudioTrackProvider.java` | `AudioTrack.write(byte[])` and the old `AudioTrack` constructors below API 21 |
| `exoplayer/.../drm/DrmUtil.java` | API 21+ DRM exceptions only on API 21+ |
| `common/.../util/Util.java` | `generateAudioSessionId`, `Locale.toLanguageTag` below API 21 |
| `datasource/.../FileDataSource.java`, `FileDescriptorDataSource.java` | `ErrnoException` and `Os.lseek` only on API 21+ |

## jlatexmath (`TMessagesProj/lib/jlatexmath`)

Vendored from upstream's submodule ([dkaraush/jlatexmath-android](https://github.com/dkaraush/jlatexmath-android)
at `919e50b`) without the sample app; `jlatexmath/build.gradle`: minSdk 19.
