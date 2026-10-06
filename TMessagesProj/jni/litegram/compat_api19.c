/*
 * Litegram: Android 4.4 (API 19) compatibility shims.
 *
 * The prebuilt static libraries in jni/prebuild (FFmpeg, BoringSSL, TDLib, WAMR, ...) were
 * compiled by upstream for API 21. They reference a handful of libc symbols that bionic only
 * started to export in Android 5.0 (on 4.4 most of them were inline functions in the headers).
 * libtmessages is linked for API 19, so these symbols are provided here. They have hidden
 * visibility (-fvisibility=hidden), i.e. they only satisfy references inside libtmessages.
 *
 * The C identifiers are prefixed and the real symbol names are set with asm labels, so the
 * definitions never clash with the API 19 inline versions declared by the NDK headers.
 */

#include <errno.h>
#include <fcntl.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <sys/syscall.h>
#include <sys/types.h>
#include <unistd.h>

#if __ANDROID_API__ < 21

#define LITEGRAM_SHIM __attribute__((used))

/* void _Exit(int) -- on API 19 the header renames it to _exit(). */
LITEGRAM_SHIM __attribute__((noreturn)) void litegram__Exit(int status) __asm__("_Exit");
void litegram__Exit(int status) {
    _exit(status);
}

/* Real-time signal range (bionic reserves the first few RT signals for itself). */
LITEGRAM_SHIM int litegram_sigrtmin(void) __asm__("__libc_current_sigrtmin");
int litegram_sigrtmin(void) {
    return 32 + 3;
}

LITEGRAM_SHIM int litegram_sigrtmax(void) __asm__("__libc_current_sigrtmax");
int litegram_sigrtmax(void) {
    return 64;
}

/* FORTIFY helper for read(). */
LITEGRAM_SHIM ssize_t litegram_read_chk(int fd, void *buf, size_t count, size_t buf_size) __asm__("__read_chk");
ssize_t litegram_read_chk(int fd, void *buf, size_t count, size_t buf_size) {
    if (count > buf_size) {
        abort();
    }
    return read(fd, buf, count);
}

LITEGRAM_SHIM int litegram_getpagesize(void) __asm__("getpagesize");
int litegram_getpagesize(void) {
    return (int) sysconf(_SC_PAGESIZE);
}

LITEGRAM_SHIM int litegram_linkat(int olddirfd, const char *oldpath, int newdirfd, const char *newpath, int flags) __asm__("linkat");
int litegram_linkat(int olddirfd, const char *oldpath, int newdirfd, const char *newpath, int flags) {
    return (int) syscall(__NR_linkat, olddirfd, oldpath, newdirfd, newpath, flags);
}

LITEGRAM_SHIM ssize_t litegram_readlinkat(int dirfd, const char *path, char *buf, size_t bufsiz) __asm__("readlinkat");
ssize_t litegram_readlinkat(int dirfd, const char *path, char *buf, size_t bufsiz) {
    return (ssize_t) syscall(__NR_readlinkat, dirfd, path, buf, bufsiz);
}

LITEGRAM_SHIM int litegram_symlinkat(const char *target, int newdirfd, const char *linkpath) __asm__("symlinkat");
int litegram_symlinkat(const char *target, int newdirfd, const char *linkpath) {
    return (int) syscall(__NR_symlinkat, target, newdirfd, linkpath);
}

/* mmap64: 32-bit bionic on 4.4 only has mmap() with a 32-bit offset. */
LITEGRAM_SHIM void *litegram_mmap64(void *addr, size_t size, int prot, int flags, int fd, int64_t offset) __asm__("mmap64");
void *litegram_mmap64(void *addr, size_t size, int prot, int flags, int fd, int64_t offset) {
    if (offset < 0 || offset > INT32_MAX) {
        errno = EOVERFLOW;
        return MAP_FAILED;
    }
    return mmap(addr, size, prot, flags, fd, (off_t) offset);
}

/* posix_fadvise is only a hint. (32-bit off_t: the callers were built without _FILE_OFFSET_BITS=64) */
LITEGRAM_SHIM int litegram_posix_fadvise(int fd, long offset, long len, int advice) __asm__("posix_fadvise");
int litegram_posix_fadvise(int fd, long offset, long len, int advice) {
    (void) fd; (void) offset; (void) len; (void) advice;
    return 0;
}

/* posix_fallocate: make sure the file is at least offset+len bytes long. */
LITEGRAM_SHIM int litegram_posix_fallocate(int fd, long offset, long len) __asm__("posix_fallocate");
int litegram_posix_fallocate(int fd, long offset, long len) {
    struct stat st;
    if (offset < 0 || len <= 0 || offset > INT32_MAX - len) {
        return EINVAL;
    }
    if (fstat(fd, &st) != 0) {
        return errno;
    }
    if (st.st_size < (long long) offset + len && ftruncate(fd, offset + len) != 0) {
        return errno;
    }
    return 0;
}

/* sigset_t is a single unsigned long on 32-bit bionic. */
LITEGRAM_SHIM int litegram_sigemptyset(unsigned long *set) __asm__("sigemptyset");
int litegram_sigemptyset(unsigned long *set) {
    if (set == NULL) {
        errno = EINVAL;
        return -1;
    }
    *set = 0;
    return 0;
}

LITEGRAM_SHIM int litegram_sigaddset(unsigned long *set, int signum) __asm__("sigaddset");
int litegram_sigaddset(unsigned long *set, int signum) {
    int bit = signum - 1;
    if (set == NULL || bit < 0 || bit >= (int) (8 * sizeof(unsigned long))) {
        errno = EINVAL;
        return -1;
    }
    *set |= 1UL << bit;
    return 0;
}

/* rand()/srand() were inline wrappers around lrand48()/srand48() before API 21. */
LITEGRAM_SHIM int litegram_rand(void) __asm__("rand");
int litegram_rand(void) {
    return (int) lrand48();
}

LITEGRAM_SHIM void litegram_srand(unsigned int seed) __asm__("srand");
void litegram_srand(unsigned int seed) {
    srand48(seed);
}

LITEGRAM_SHIM float litegram_strtof(const char *nptr, char **endptr) __asm__("strtof");
float litegram_strtof(const char *nptr, char **endptr) {
    return (float) strtod(nptr, endptr);
}

#endif /* __ANDROID_API__ < 21 */
