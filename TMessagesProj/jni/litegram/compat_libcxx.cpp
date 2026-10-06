/*
 * Litegram: libc++ symbols expected by the prebuilt TDLib archives (libtdutils.a / libtde2e.a).
 *
 * Upstream builds those archives with NDK r27 (LLVM 18 libc++ headers), while Litegram links
 * with NDK r25c (LLVM 14 libc++), the last NDK that supports Android 4.4. Newer libc++ headers
 * declare a few things as provided by the library; they are defined here. libc++ keeps its ABI
 * stable between these versions, so the class layouts are identical.
 */

#include <android/log.h>

#include <cstdarg>
#include <cstdlib>
#include <sstream>

#if __ANDROID_API__ < 21

_LIBCPP_BEGIN_NAMESPACE_STD

// Added in libc++ 15: called by _LIBCPP_ASSERT and friends.
[[noreturn]] __attribute__((used)) void __libcpp_verbose_abort(char const *format, ...);

void __libcpp_verbose_abort(char const *format, ...) {
    va_list args;
    va_start(args, format);
    __android_log_vprint(ANDROID_LOG_FATAL, "libc++", format, args);
    va_end(args);
    abort();
}

// Since libc++ 15 these are declared as extern templates (instantiated inside the library).
template class basic_stringbuf<char>;
template class basic_stringstream<char>;

_LIBCPP_END_NAMESPACE_STD

#endif
