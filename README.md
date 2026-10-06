<p align="center">
  <img src="TMessagesProj/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="Litegram" width="96">
</p>

<h1 align="center">Litegram</h1>

<p align="center">
  <img src="docs/badges/android.svg" alt="Android 4.4+">
  <img src="docs/badges/telegram.svg" alt="Telegram 12.10.6">
  <img src="docs/badges/abi.svg" alt="ABI armeabi-v7a">
  <img src="docs/badges/license.svg" alt="License GPL v2">
</p>

Litegram is an unofficial, lightweight fork of [Telegram for Android](https://github.com/DrKLO/Telegram)
(version 12.10.6) that runs on **Android 4.4 KitKat** and newer.

The official app requires Android 5.0 since version 12.0. Litegram brings the current Telegram -
stories, reactions, topics, folders, gifts and Stars, round videos, calls, translations and the
rest of 12.10.6 - back to old and low-end phones such as the Alcatel One Touch Pixi 3 (512 MB RAM).

Litegram is not affiliated with Telegram; it uses the Telegram API like any other third-party
client.

## Changes

* **Android 4.4 support** (`minSdk` 19): native code built with NDK r25c, Android 5.0+ API calls
  redirected to compatibility code at build time, code rejected by the Dalvik VM of Android 4.4
  rewritten, the media player brought back to the code paths for Android 4.4, TLS 1.2 enabled.
* **Lighter**: `armeabi-v7a` only (~36 MB APK); no Firebase, Google Play Billing, Android Auto and
  passkeys; Chromecast, animations and blur effects off by default on Android 4.x and low-end
  devices; a smaller default cache limit on phones with little storage.
* **Notifications** come through Telegram's own background connection (there is no FCM).
* Own package name (`org.litegram.messenger`) and icon: installs next to the official app.

How the port works and the full list of changed files: [docs/PORTING.md](docs/PORTING.md),
[docs/CHANGES.md](docs/CHANGES.md).

## Known limitations

* Push notifications rely on the background connection; on some firmwares Litegram has to be
  excluded from battery optimization.
* No Google sign-in, passkeys or Google Play purchases (Premium and Stars can be bought through
  Telegram's payment forms); Google Maps may not load in the location picker.
* Not available on Android 4.4: picture-in-picture, sticker cut-outs, the Camera2 round-video
  camera, blur and "glass" effects. Bot Mini Apps depend on the old system WebView.

## Download

APK files are on the [Releases](../../releases) page. Requirements: Android 4.4 or newer and an ARM
processor.

## Compilation Guide

You will need JDK 17 and the Android SDK with platform 36, build-tools 36.0.0,
**NDK 25.2.9519653 (r25c)** - the last NDK that supports Android 4.4 - and CMake 3.22.1.

1. Clone the source code with its submodules:
   ```bash
   git clone --recursive https://github.com/<your-account>/Litegram.git
   ```
2. Copy `local.properties.example` to `local.properties` and fill it in:
   * `sdk.dir` - path to the Android SDK;
   * `litegram.apiId`, `litegram.apiHash` - your own API credentials from
     [my.telegram.org](https://my.telegram.org) ([how to get them](https://core.telegram.org/api/obtaining_api_id));
   * `litegram.storeFile`, `litegram.storePassword`, `litegram.keyAlias`, `litegram.keyPassword` -
     your release keystore.
3. Build:
   ```bash
   ./gradlew :TMessagesProj_App:assembleRelease
   ```
   The APK is written to `TMessagesProj_App/build/outputs/apk/release/`. The first build compiles
   the native code and takes about an hour.

GitHub Actions can build the APK too: see [.github/workflows/build.yml](.github/workflows/build.yml).

### API, Protocol documentation

Telegram API manuals: https://core.telegram.org/api

MTProto protocol manuals: https://core.telegram.org/mtproto

### Localization

Translations are managed by Telegram: https://translations.telegram.org

## Thanks

* [Telegram for Android](https://github.com/DrKLO/Telegram) - the app Litegram is based on
* [AndroidX Media3](https://github.com/androidx/media), [TDLib](https://github.com/tdlib/td),
  [FFmpeg](https://ffmpeg.org), [BoringSSL](https://boringssl.googlesource.com/boringssl) and the
  other libraries used by Telegram

## License

Litegram is licensed under the GNU General Public License v2, like Telegram for Android - see
[LICENSE](LICENSE). Third-party components keep their own licenses.
