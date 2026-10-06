package org.telegram.messenger.compat;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.PowerManager;
import android.text.SpannableStringBuilder;
import android.util.Property;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityNodeInfo;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EdgeEffect;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.lang.reflect.Method;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Litegram: static replacements for Android 5.0+ framework methods.
 *
 * Calls are redirected here at build time by the Api19BackportTransform (buildSrc). On Android 5.0+
 * every helper simply calls the original method; on Android 4.4 cosmetic features (elevation,
 * outlines, ripple hotspots, status bar colors, ...) are skipped and drawing helpers use the
 * older RectF based overloads. Do not call these methods directly from app code.
 */
@SuppressLint({"NewApi", "ObsoleteSdkInt"})
@SuppressWarnings({"deprecation", "unused"})
public final class Api19 {

    private static final int SDK = Build.VERSION.SDK_INT;

    private static final ThreadLocal<RectF> RECT = new ThreadLocal<RectF>() {
        @Override
        protected RectF initialValue() {
            return new RectF();
        }
    };

    private static RectF rect(float left, float top, float right, float bottom) {
        RectF rect = RECT.get();
        rect.set(left, top, right, bottom);
        return rect;
    }

    private Api19() {
    }

    // region View

    public static void viewSetClipToOutline(View view, boolean clipToOutline) {
        if (SDK >= 21) view.setClipToOutline(clipToOutline);
    }

    public static boolean viewGetClipToOutline(View view) {
        return SDK >= 21 && view.getClipToOutline();
    }

    public static void viewInvalidateOutline(View view) {
        if (SDK >= 21) view.invalidateOutline();
    }

    public static void viewSetOutlineProvider(View view, ViewOutlineProvider provider) {
        if (SDK >= 21) view.setOutlineProvider(provider);
    }

    public static ViewOutlineProvider viewGetOutlineProvider(View view) {
        return SDK >= 21 ? view.getOutlineProvider() : null;
    }

    public static void viewSetElevation(View view, float elevation) {
        if (SDK >= 21) view.setElevation(elevation);
    }

    public static float viewGetElevation(View view) {
        return SDK >= 21 ? view.getElevation() : 0f;
    }

    public static void viewSetTranslationZ(View view, float translationZ) {
        if (SDK >= 21) view.setTranslationZ(translationZ);
    }

    public static float viewGetTranslationZ(View view) {
        return SDK >= 21 ? view.getTranslationZ() : 0f;
    }

    public static void viewSetZ(View view, float z) {
        if (SDK >= 21) view.setZ(z);
    }

    public static float viewGetZ(View view) {
        return SDK >= 21 ? view.getZ() : 0f;
    }

    public static void viewSetStateListAnimator(View view, StateListAnimator animator) {
        if (SDK >= 21) view.setStateListAnimator(animator);
    }

    public static void viewSetBackgroundTintList(View view, ColorStateList tint) {
        if (SDK >= 21) view.setBackgroundTintList(tint);
    }

    public static void viewSetNestedScrollingEnabled(View view, boolean enabled) {
        if (SDK >= 21) view.setNestedScrollingEnabled(enabled);
    }

    public static void viewSetTransitionName(View view, String name) {
        if (SDK >= 21) view.setTransitionName(name);
    }

    public static void viewDrawableHotspotChanged(View view, float x, float y) {
        if (SDK >= 21) view.drawableHotspotChanged(x, y);
    }

    public static void viewRequestApplyInsets(View view) {
        if (SDK >= 20) {
            view.requestApplyInsets();
        } else {
            view.requestFitSystemWindows();
        }
    }

    public static void viewSetOnApplyWindowInsetsListener(View view, View.OnApplyWindowInsetsListener listener) {
        if (SDK >= 20) view.setOnApplyWindowInsetsListener(listener);
    }

    public static WindowInsets viewDispatchApplyWindowInsets(View view, WindowInsets insets) {
        return SDK >= 20 ? view.dispatchApplyWindowInsets(insets) : insets;
    }

    public static WindowInsets viewGetRootWindowInsets(View view) {
        return SDK >= 23 ? view.getRootWindowInsets() : null;
    }

    public static void textViewSetLetterSpacing(TextView view, float spacing) {
        if (SDK >= 21) view.setLetterSpacing(spacing);
    }

    public static float textViewGetLetterSpacing(TextView view) {
        return SDK >= 21 ? view.getLetterSpacing() : 0f;
    }

    private static Method setSoftInputShownOnFocus;
    private static boolean setSoftInputShownOnFocusResolved;

    public static void textViewSetShowSoftInputOnFocus(TextView view, boolean show) {
        if (SDK >= 21) {
            view.setShowSoftInputOnFocus(show);
            return;
        }
        // hidden API with the same meaning on Android 4.x
        try {
            if (!setSoftInputShownOnFocusResolved) {
                setSoftInputShownOnFocusResolved = true;
                setSoftInputShownOnFocus = TextView.class.getMethod("setSoftInputShownOnFocus", boolean.class);
            }
            if (setSoftInputShownOnFocus != null) {
                setSoftInputShownOnFocus.invoke(view, show);
            }
        } catch (Throwable ignore) {
        }
    }

    // endregion

    // region Drawable

    public static void drawableSetHotspot(Drawable drawable, float x, float y) {
        if (SDK >= 21) drawable.setHotspot(x, y);
    }

    public static void drawableSetHotspotBounds(Drawable drawable, int left, int top, int right, int bottom) {
        if (SDK >= 21) drawable.setHotspotBounds(left, top, right, bottom);
    }

    public static void drawableSetTint(Drawable drawable, int color) {
        if (SDK >= 21) {
            drawable.setTint(color);
        } else {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    public static void drawableSetTintList(Drawable drawable, ColorStateList tint) {
        if (SDK >= 21) {
            drawable.setTintList(tint);
        } else if (tint != null) {
            drawable.setColorFilter(tint.getColorForState(drawable.getState(), tint.getDefaultColor()), PorterDuff.Mode.SRC_IN);
        } else {
            drawable.clearColorFilter();
        }
    }

    public static void drawableSetTintMode(Drawable drawable, PorterDuff.Mode mode) {
        if (SDK >= 21) drawable.setTintMode(mode);
    }

    public static ColorFilter drawableGetColorFilter(Drawable drawable) {
        return SDK >= 21 ? drawable.getColorFilter() : null;
    }

    // endregion

    // region Accessibility

    public static void nodeInfoAddAction(AccessibilityNodeInfo info, AccessibilityNodeInfo.AccessibilityAction action) {
        if (SDK >= 21) info.addAction(action);
    }

    public static boolean nodeInfoRemoveAction(AccessibilityNodeInfo info, AccessibilityNodeInfo.AccessibilityAction action) {
        return SDK >= 21 && info.removeAction(action);
    }

    public static List<AccessibilityNodeInfo.AccessibilityAction> nodeInfoGetActionList(AccessibilityNodeInfo info) {
        return SDK >= 21 ? info.getActionList() : Collections.<AccessibilityNodeInfo.AccessibilityAction>emptyList();
    }

    // endregion

    // region Canvas

    public static void canvasDrawRoundRect(Canvas canvas, float left, float top, float right, float bottom, float rx, float ry, Paint paint) {
        if (SDK >= 21) {
            canvas.drawRoundRect(left, top, right, bottom, rx, ry, paint);
        } else {
            canvas.drawRoundRect(rect(left, top, right, bottom), rx, ry, paint);
        }
    }

    public static void canvasDrawArc(Canvas canvas, float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
        if (SDK >= 21) {
            canvas.drawArc(left, top, right, bottom, startAngle, sweepAngle, useCenter, paint);
        } else {
            canvas.drawArc(rect(left, top, right, bottom), startAngle, sweepAngle, useCenter, paint);
        }
    }

    public static void canvasDrawOval(Canvas canvas, float left, float top, float right, float bottom, Paint paint) {
        if (SDK >= 21) {
            canvas.drawOval(left, top, right, bottom, paint);
        } else {
            canvas.drawOval(rect(left, top, right, bottom), paint);
        }
    }

    public static int canvasSaveLayer(Canvas canvas, float left, float top, float right, float bottom, Paint paint) {
        if (SDK >= 21) {
            return canvas.saveLayer(left, top, right, bottom, paint);
        }
        return canvas.saveLayer(left, top, right, bottom, paint, Canvas.ALL_SAVE_FLAG);
    }

    public static int canvasSaveLayer(Canvas canvas, RectF bounds, Paint paint) {
        if (SDK >= 21) {
            return canvas.saveLayer(bounds, paint);
        }
        return canvas.saveLayer(bounds, paint, Canvas.ALL_SAVE_FLAG);
    }

    public static int canvasSaveLayerAlpha(Canvas canvas, float left, float top, float right, float bottom, int alpha) {
        if (SDK >= 21) {
            return canvas.saveLayerAlpha(left, top, right, bottom, alpha);
        }
        return canvas.saveLayerAlpha(left, top, right, bottom, alpha, Canvas.ALL_SAVE_FLAG);
    }

    public static int canvasSaveLayerAlpha(Canvas canvas, RectF bounds, int alpha) {
        if (SDK >= 21) {
            return canvas.saveLayerAlpha(bounds, alpha);
        }
        return canvas.saveLayerAlpha(bounds, alpha, Canvas.ALL_SAVE_FLAG);
    }

    // endregion

    // region Path

    public static void pathAddRoundRect(Path path, float left, float top, float right, float bottom, float rx, float ry, Path.Direction dir) {
        if (SDK >= 21) {
            path.addRoundRect(left, top, right, bottom, rx, ry, dir);
        } else {
            path.addRoundRect(rect(left, top, right, bottom), rx, ry, dir);
        }
    }

    public static void pathAddRoundRect(Path path, float left, float top, float right, float bottom, float[] radii, Path.Direction dir) {
        if (SDK >= 21) {
            path.addRoundRect(left, top, right, bottom, radii, dir);
        } else {
            path.addRoundRect(rect(left, top, right, bottom), radii, dir);
        }
    }

    public static void pathAddOval(Path path, float left, float top, float right, float bottom, Path.Direction dir) {
        if (SDK >= 21) {
            path.addOval(left, top, right, bottom, dir);
        } else {
            path.addOval(rect(left, top, right, bottom), dir);
        }
    }

    public static void pathAddArc(Path path, float left, float top, float right, float bottom, float startAngle, float sweepAngle) {
        if (SDK >= 21) {
            path.addArc(left, top, right, bottom, startAngle, sweepAngle);
        } else {
            path.addArc(rect(left, top, right, bottom), startAngle, sweepAngle);
        }
    }

    public static void pathArcTo(Path path, float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean forceMoveTo) {
        if (SDK >= 21) {
            path.arcTo(left, top, right, bottom, startAngle, sweepAngle, forceMoveTo);
        } else {
            path.arcTo(rect(left, top, right, bottom), startAngle, sweepAngle, forceMoveTo);
        }
    }

    // endregion

    // region Paint

    public static void paintSetLetterSpacing(Paint paint, float spacing) {
        if (SDK >= 21) paint.setLetterSpacing(spacing);
    }

    public static float paintGetLetterSpacing(Paint paint) {
        return SDK >= 21 ? paint.getLetterSpacing() : 0f;
    }

    public static void paintSetFontFeatureSettings(Paint paint, String settings) {
        if (SDK >= 21) paint.setFontFeatureSettings(settings);
    }

    // endregion

    // region Window

    public static void windowSetStatusBarColor(Window window, int color) {
        if (SDK >= 21) window.setStatusBarColor(color);
    }

    public static void windowSetNavigationBarColor(Window window, int color) {
        if (SDK >= 21) window.setNavigationBarColor(color);
    }

    public static int windowGetStatusBarColor(Window window) {
        return SDK >= 21 ? window.getStatusBarColor() : 0xff000000;
    }

    public static int windowGetNavigationBarColor(Window window) {
        return SDK >= 21 ? window.getNavigationBarColor() : 0xff000000;
    }

    // endregion

    // region Resources

    public static Drawable contextGetDrawable(Context context, int id) {
        if (SDK >= 21) {
            return context.getDrawable(id);
        }
        return ContextCompat.getDrawable(context, id);
    }

    public static int contextGetColor(Context context, int id) {
        if (SDK >= 23) {
            return context.getColor(id);
        }
        return ContextCompat.getColor(context, id);
    }

    public static ColorStateList contextGetColorStateList(Context context, int id) {
        if (SDK >= 23) {
            return context.getColorStateList(id);
        }
        return ContextCompat.getColorStateList(context, id);
    }

    public static Drawable resourcesGetDrawable(Resources resources, int id, Resources.Theme theme) {
        if (SDK >= 21) {
            return resources.getDrawable(id, theme);
        }
        return ResourcesCompat.getDrawable(resources, id, theme);
    }

    public static int resourcesGetColor(Resources resources, int id, Resources.Theme theme) {
        if (SDK >= 23) {
            return resources.getColor(id, theme);
        }
        return ResourcesCompat.getColor(resources, id, theme);
    }

    // endregion

    // region MediaCodec

    /** Before API 21 the buffers come from {@link MediaCodec#getInputBuffers()}. */
    public static ByteBuffer mediaCodecGetInputBuffer(MediaCodec codec, int index) {
        if (SDK >= 21) {
            return codec.getInputBuffer(index);
        }
        ByteBuffer buffer = codec.getInputBuffers()[index];
        buffer.clear(); // same state as returned by getInputBuffer()
        return buffer;
    }

    /** Before API 21 the buffers come from {@link MediaCodec#getOutputBuffers()}; callers use BufferInfo offsets. */
    public static ByteBuffer mediaCodecGetOutputBuffer(MediaCodec codec, int index) {
        if (SDK >= 21) {
            return codec.getOutputBuffer(index);
        }
        return codec.getOutputBuffers()[index];
    }

    public static MediaFormat mediaCodecGetInputFormat(MediaCodec codec) {
        return SDK >= 21 ? codec.getInputFormat() : null;
    }

    public static MediaFormat mediaCodecGetOutputFormat(MediaCodec codec, int index) {
        return SDK >= 21 ? codec.getOutputFormat(index) : null;
    }

    // endregion

    // region Misc

    public static void edgeEffectSetColor(EdgeEffect edgeEffect, int color) {
        if (SDK >= 21) edgeEffect.setColor(color);
    }

    public static boolean audioManagerIsVolumeFixed(AudioManager audioManager) {
        return SDK >= 21 && audioManager.isVolumeFixed();
    }

    public static SpannableStringBuilder spannableStringBuilderAppend(SpannableStringBuilder builder, CharSequence text, Object what, int flags) {
        if (SDK >= 21) {
            return builder.append(text, what, flags);
        }
        int start = builder.length();
        builder.append(text);
        builder.setSpan(what, start, builder.length(), flags);
        return builder;
    }

    public static boolean powerManagerIsInteractive(PowerManager powerManager) {
        return SDK >= 20 ? powerManager.isInteractive() : powerManager.isScreenOn();
    }

    public static void cookieManagerFlush(CookieManager cookieManager) {
        if (SDK >= 21) cookieManager.flush();
    }

    public static void cookieManagerSetAcceptThirdPartyCookies(CookieManager cookieManager, WebView webView, boolean accept) {
        if (SDK >= 21) cookieManager.setAcceptThirdPartyCookies(webView, accept);
    }

    public static void cookieManagerRemoveAllCookies(CookieManager cookieManager, ValueCallback<Boolean> callback) {
        if (SDK >= 21) {
            cookieManager.removeAllCookies(callback);
            return;
        }
        cookieManager.removeAllCookie();
        if (callback != null) {
            callback.onReceiveValue(true);
        }
    }

    public static void cookieManagerRemoveSessionCookies(CookieManager cookieManager, ValueCallback<Boolean> callback) {
        if (SDK >= 21) {
            cookieManager.removeSessionCookies(callback);
            return;
        }
        cookieManager.removeSessionCookie();
        if (callback != null) {
            callback.onReceiveValue(true);
        }
    }

    public static void webSettingsSetMixedContentMode(WebSettings settings, int mode) {
        if (SDK >= 21) settings.setMixedContentMode(mode);
    }

    public static ValueAnimator valueAnimatorOfArgb(int... values) {
        if (SDK >= 21) {
            return ValueAnimator.ofArgb(values);
        }
        ValueAnimator animator = ValueAnimator.ofInt(values);
        animator.setEvaluator(new ArgbEvaluator());
        return animator;
    }

    public static ObjectAnimator objectAnimatorOfArgb(Object target, String propertyName, int... values) {
        if (SDK >= 21) {
            return ObjectAnimator.ofArgb(target, propertyName, values);
        }
        ObjectAnimator animator = ObjectAnimator.ofInt(target, propertyName, values);
        animator.setEvaluator(new ArgbEvaluator());
        return animator;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ObjectAnimator objectAnimatorOfArgb(Object target, Property property, int... values) {
        if (SDK >= 21) {
            return ObjectAnimator.ofArgb(target, property, values);
        }
        ObjectAnimator animator = ObjectAnimator.ofInt(target, (Property<Object, Integer>) property, values);
        animator.setEvaluator(new ArgbEvaluator());
        return animator;
    }

    // endregion

    // region java.* (libcore of Android 4.4 is Java 6/7 without these methods)

    /** {@link Locale#toLanguageTag()} (API 21): language[-REGION][-variant] for the common cases. */
    public static String localeToLanguageTag(Locale locale) {
        if (SDK >= 21) {
            return locale.toLanguageTag();
        }
        String language = locale.getLanguage();
        switch (language) {
            case "iw": language = "he"; break;
            case "in": language = "id"; break;
            case "ji": language = "yi"; break;
            case "": language = "und"; break;
        }
        StringBuilder tag = new StringBuilder(language);
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            tag.append('-').append(country);
        }
        String variant = locale.getVariant();
        if (!variant.isEmpty()) {
            tag.append('-').append(variant.replace('_', '-'));
        }
        return tag.toString();
    }

    /** {@link Locale#forLanguageTag(String)} (API 21): language, script (dropped), region, variant. */
    public static Locale localeForLanguageTag(String languageTag) {
        if (SDK >= 21) {
            return Locale.forLanguageTag(languageTag);
        }
        String[] parts = languageTag.split("[-_]");
        String language = parts.length > 0 && isAlpha(parts[0], 2, 8) ? parts[0].toLowerCase(Locale.US) : "";
        if ("und".equals(language)) {
            language = "";
        }
        String country = "";
        String variant = "";
        int i = 1;
        if (i < parts.length && isAlpha(parts[i], 4, 4)) {
            i++; // script, can't be represented in a Locale before Android 5.0
        }
        if (i < parts.length && (isAlpha(parts[i], 2, 2) || isDigits(parts[i], 3))) {
            country = parts[i++].toUpperCase(Locale.US);
        }
        if (i < parts.length && parts[i].length() >= 4) {
            variant = parts[i];
        }
        return new Locale(language, country, variant);
    }

    public static String localeGetScript(Locale locale) {
        return SDK >= 21 ? locale.getScript() : "";
    }

    public static String localeGetUnicodeLocaleType(Locale locale, String key) {
        return SDK >= 21 ? locale.getUnicodeLocaleType(key) : null;
    }

    /** {@link URLConnection#getContentLengthLong()} (API 24). */
    public static long urlConnectionGetContentLengthLong(URLConnection connection) {
        if (SDK >= 24) {
            return connection.getContentLengthLong();
        }
        String value = connection.getHeaderField("content-length");
        if (value != null) {
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException ignore) {
            }
        }
        return -1;
    }

    private static boolean isAlpha(String s, int minLength, int maxLength) {
        if (s.length() < minLength || s.length() > maxLength) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')) {
                return false;
            }
        }
        return true;
    }

    private static boolean isDigits(String s, int length) {
        if (s.length() != length) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) < '0' || s.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }

    // endregion
}
