# Upstream

Litegram is based on the official Telegram for Android sources.

* Repository: <https://github.com/DrKLO/Telegram>
* Version: 12.10.6 (7112)
* Commit: `f2908b14133bbffbf7ab04f641ecb5bfaf533242`

## Submodules

Unmodified upstream submodules stay git submodules at the same commits:

| Path | Commit |
|---|---|
| `TMessagesProj/jni/td` | `022d60202e446ad1287b9fb68e687c8a0760788b` |
| `TMessagesProj/jni/tlottie` | `92df98dc209bc39b1e567ec74a8c86a0af5239de` (v1.0.6) |
| `TMessagesProj/jni/third_party/absl` | `54fac219c4ef0bc379dfffb0b8098725d77ac81b` |
| `TMessagesProj/jni/third_party/boringssl` | `2b44a3701a4788e1ef866ddc7f143060a3d196c9` |
| `TMessagesProj/jni/third_party/dav1d` | `54706fc6bc0cdecab7e9593974a4039cc038fca7` |
| `TMessagesProj/jni/third_party/ffmpeg` | `45f1910444f34b02621f9f0426ea1a538a613c41` |
| `TMessagesProj/jni/third_party/libvpx` | `1024874c5919305883187e2953de8fcb4c3d7fa6` |
| `TMessagesProj/jni/third_party/libyuv` | `28ce69c2744a6aafdb58564e7b884aec3f66be5f` |
| `TMessagesProj/jni/third_party/openh264` | `652bdb7719f30b52b08e506645a7322ff1b2cc6f` |
| `TMessagesProj/jni/third_party/wamr` | `25bd7eb63e828e4bd242cc9b38d260b4b31c6605` |
| `TMessagesProj/jni/third_party/xiph/ogg` | `be05b13e98b048f0b5a0f5fa8ce514d56db5f822` |
| `TMessagesProj/jni/third_party/xiph/opus` | `22244de5a79bd1d6d623c32e72bf1954b56235be` |
| `TMessagesProj/jni/third_party/xiph/opusfile` | `a55c164e9891a9326188b7d4d216ec9a88373739` |

Modified submodules are vendored as normal directories:

| Path | Origin |
|---|---|
| `TMessagesProj_Modules/media` | <https://github.com/Arseny271/media> at `c430d207677071b1873f9f18266d55ec45722180`, only the 11 modules the app uses |
| `TMessagesProj/lib/jlatexmath` | <https://github.com/dkaraush/jlatexmath-android> at `919e50b2f6f64b04b712cdb13d558ff9ecf9c8ed` (branch `android`), without the sample app |
