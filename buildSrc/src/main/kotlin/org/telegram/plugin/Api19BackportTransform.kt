package org.telegram.plugin

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Litegram: makes bytecode compiled against modern Android APIs safe to run on Android 4.4 (API 19).
 *
 * Calls to framework methods that don't exist on old Android versions are redirected to static
 * helpers in `org.telegram.messenger.compat.Api19`. The helpers call the original method on new
 * Android versions and either skip the call (purely cosmetic APIs like elevation, outlines,
 * ripple hotspots) or emulate it with older APIs (e.g. `Canvas.drawRoundRect(float...)` ->
 * `drawRoundRect(RectF, ...)`) on old ones.
 *
 * Only plain invokevirtual/invokestatic call sites are rewritten; the stack effect of every
 * replacement is identical to the original call, so stack map frames stay valid.
 */
object Api19BackportRules {
    const val HELPER = "org/telegram/messenger/compat/Api19"

    /** A rewrite rule: [declaringClass] declares `name+desc`; [helperName] is the static helper. */
    class Rule(
        val declaringClass: String,
        val name: String,
        val desc: String,
        val helperName: String,
        val isStatic: Boolean = false,
    ) {
        /** Descriptor of the static helper: the receiver becomes the first parameter. */
        val helperDesc: String =
            if (isStatic) desc else "(L$declaringClass;" + desc.substring(1)
    }

    private fun virtual(owner: String, name: String, desc: String, helper: String = name) =
        Rule(owner, name, desc, helper)

    private const val VIEW = "android/view/View"
    private const val TEXT_VIEW = "android/widget/TextView"
    private const val DRAWABLE = "android/graphics/drawable/Drawable"
    private const val CANVAS = "android/graphics/Canvas"
    private const val PATH = "android/graphics/Path"
    private const val PAINT = "android/graphics/Paint"
    private const val WINDOW = "android/view/Window"
    private const val CONTEXT = "android/content/Context"
    private const val RESOURCES = "android/content/res/Resources"

    val rules: List<Rule> = listOf(
        // View: elevation / outlines / insets (API 20-21) - cosmetic, skipped on old Android.
        virtual(VIEW, "setClipToOutline", "(Z)V", "viewSetClipToOutline"),
        virtual(VIEW, "getClipToOutline", "()Z", "viewGetClipToOutline"),
        virtual(VIEW, "invalidateOutline", "()V", "viewInvalidateOutline"),
        virtual(VIEW, "setOutlineProvider", "(Landroid/view/ViewOutlineProvider;)V", "viewSetOutlineProvider"),
        virtual(VIEW, "getOutlineProvider", "()Landroid/view/ViewOutlineProvider;", "viewGetOutlineProvider"),
        virtual(VIEW, "setElevation", "(F)V", "viewSetElevation"),
        virtual(VIEW, "getElevation", "()F", "viewGetElevation"),
        virtual(VIEW, "setTranslationZ", "(F)V", "viewSetTranslationZ"),
        virtual(VIEW, "getTranslationZ", "()F", "viewGetTranslationZ"),
        virtual(VIEW, "setZ", "(F)V", "viewSetZ"),
        virtual(VIEW, "getZ", "()F", "viewGetZ"),
        virtual(VIEW, "setStateListAnimator", "(Landroid/animation/StateListAnimator;)V", "viewSetStateListAnimator"),
        virtual(VIEW, "setBackgroundTintList", "(Landroid/content/res/ColorStateList;)V", "viewSetBackgroundTintList"),
        virtual(VIEW, "setNestedScrollingEnabled", "(Z)V", "viewSetNestedScrollingEnabled"),
        virtual(VIEW, "setTransitionName", "(Ljava/lang/String;)V", "viewSetTransitionName"),
        virtual(VIEW, "drawableHotspotChanged", "(FF)V", "viewDrawableHotspotChanged"),
        virtual(VIEW, "requestApplyInsets", "()V", "viewRequestApplyInsets"),
        virtual(VIEW, "setOnApplyWindowInsetsListener", "(Landroid/view/View\$OnApplyWindowInsetsListener;)V", "viewSetOnApplyWindowInsetsListener"),
        virtual(VIEW, "dispatchApplyWindowInsets", "(Landroid/view/WindowInsets;)Landroid/view/WindowInsets;", "viewDispatchApplyWindowInsets"),
        virtual(VIEW, "getRootWindowInsets", "()Landroid/view/WindowInsets;", "viewGetRootWindowInsets"),
        virtual(TEXT_VIEW, "setLetterSpacing", "(F)V", "textViewSetLetterSpacing"),
        virtual(TEXT_VIEW, "getLetterSpacing", "()F", "textViewGetLetterSpacing"),
        virtual(TEXT_VIEW, "setShowSoftInputOnFocus", "(Z)V", "textViewSetShowSoftInputOnFocus"),

        // Drawable (API 21)
        virtual(DRAWABLE, "setHotspot", "(FF)V", "drawableSetHotspot"),
        virtual(DRAWABLE, "setHotspotBounds", "(IIII)V", "drawableSetHotspotBounds"),
        virtual(DRAWABLE, "setTint", "(I)V", "drawableSetTint"),
        virtual(DRAWABLE, "setTintList", "(Landroid/content/res/ColorStateList;)V", "drawableSetTintList"),
        virtual(DRAWABLE, "setTintMode", "(Landroid/graphics/PorterDuff\$Mode;)V", "drawableSetTintMode"),
        virtual(DRAWABLE, "getColorFilter", "()Landroid/graphics/ColorFilter;", "drawableGetColorFilter"),

        // Accessibility actions (API 21); AccessibilityAction itself is provided by :Api19Stubs
        virtual("android/view/accessibility/AccessibilityNodeInfo", "addAction", "(Landroid/view/accessibility/AccessibilityNodeInfo\$AccessibilityAction;)V", "nodeInfoAddAction"),
        virtual("android/view/accessibility/AccessibilityNodeInfo", "removeAction", "(Landroid/view/accessibility/AccessibilityNodeInfo\$AccessibilityAction;)Z", "nodeInfoRemoveAction"),
        virtual("android/view/accessibility/AccessibilityNodeInfo", "getActionList", "()Ljava/util/List;", "nodeInfoGetActionList"),

        // Canvas (API 21 float overloads) - emulated with the RectF overloads.
        virtual(CANVAS, "drawRoundRect", "(FFFFFFLandroid/graphics/Paint;)V", "canvasDrawRoundRect"),
        virtual(CANVAS, "drawArc", "(FFFFFFZLandroid/graphics/Paint;)V", "canvasDrawArc"),
        virtual(CANVAS, "drawOval", "(FFFFLandroid/graphics/Paint;)V", "canvasDrawOval"),
        virtual(CANVAS, "saveLayer", "(FFFFLandroid/graphics/Paint;)I", "canvasSaveLayer"),
        virtual(CANVAS, "saveLayer", "(Landroid/graphics/RectF;Landroid/graphics/Paint;)I", "canvasSaveLayer"),
        virtual(CANVAS, "saveLayerAlpha", "(FFFFI)I", "canvasSaveLayerAlpha"),
        virtual(CANVAS, "saveLayerAlpha", "(Landroid/graphics/RectF;I)I", "canvasSaveLayerAlpha"),

        // Path (API 21 float overloads)
        virtual(PATH, "addRoundRect", "(FFFFFFLandroid/graphics/Path\$Direction;)V", "pathAddRoundRect"),
        virtual(PATH, "addRoundRect", "(FFFF[FLandroid/graphics/Path\$Direction;)V", "pathAddRoundRect"),
        virtual(PATH, "addOval", "(FFFFLandroid/graphics/Path\$Direction;)V", "pathAddOval"),
        virtual(PATH, "addArc", "(FFFFFF)V", "pathAddArc"),
        virtual(PATH, "arcTo", "(FFFFFFZ)V", "pathArcTo"),

        // Paint (API 21)
        virtual(PAINT, "setLetterSpacing", "(F)V", "paintSetLetterSpacing"),
        virtual(PAINT, "getLetterSpacing", "()F", "paintGetLetterSpacing"),
        virtual(PAINT, "setFontFeatureSettings", "(Ljava/lang/String;)V", "paintSetFontFeatureSettings"),

        // Window (API 21)
        virtual(WINDOW, "setStatusBarColor", "(I)V", "windowSetStatusBarColor"),
        virtual(WINDOW, "setNavigationBarColor", "(I)V", "windowSetNavigationBarColor"),
        virtual(WINDOW, "getStatusBarColor", "()I", "windowGetStatusBarColor"),
        virtual(WINDOW, "getNavigationBarColor", "()I", "windowGetNavigationBarColor"),

        // Resources / Context (API 21-23)
        virtual(CONTEXT, "getDrawable", "(I)Landroid/graphics/drawable/Drawable;", "contextGetDrawable"),
        virtual(CONTEXT, "getColor", "(I)I", "contextGetColor"),
        virtual(CONTEXT, "getColorStateList", "(I)Landroid/content/res/ColorStateList;", "contextGetColorStateList"),
        virtual(RESOURCES, "getDrawable", "(ILandroid/content/res/Resources\$Theme;)Landroid/graphics/drawable/Drawable;", "resourcesGetDrawable"),
        virtual(RESOURCES, "getColor", "(ILandroid/content/res/Resources\$Theme;)I", "resourcesGetColor"),

        // MediaCodec (API 21) - buffer arrays before Android 5.0
        virtual("android/media/MediaCodec", "getInputBuffer", "(I)Ljava/nio/ByteBuffer;", "mediaCodecGetInputBuffer"),
        virtual("android/media/MediaCodec", "getOutputBuffer", "(I)Ljava/nio/ByteBuffer;", "mediaCodecGetOutputBuffer"),
        virtual("android/media/MediaCodec", "getInputFormat", "()Landroid/media/MediaFormat;", "mediaCodecGetInputFormat"),
        virtual("android/media/MediaCodec", "getOutputFormat", "(I)Landroid/media/MediaFormat;", "mediaCodecGetOutputFormat"),

        // Misc
        virtual("android/widget/EdgeEffect", "setColor", "(I)V", "edgeEffectSetColor"),
        virtual("android/media/AudioManager", "isVolumeFixed", "()Z", "audioManagerIsVolumeFixed"),
        virtual("android/text/SpannableStringBuilder", "append", "(Ljava/lang/CharSequence;Ljava/lang/Object;I)Landroid/text/SpannableStringBuilder;", "spannableStringBuilderAppend"),
        virtual("android/os/PowerManager", "isInteractive", "()Z", "powerManagerIsInteractive"),
        virtual("android/webkit/CookieManager", "flush", "()V", "cookieManagerFlush"),
        virtual("android/webkit/CookieManager", "setAcceptThirdPartyCookies", "(Landroid/webkit/WebView;Z)V", "cookieManagerSetAcceptThirdPartyCookies"),
        virtual("android/webkit/CookieManager", "removeAllCookies", "(Landroid/webkit/ValueCallback;)V", "cookieManagerRemoveAllCookies"),
        virtual("android/webkit/CookieManager", "removeSessionCookies", "(Landroid/webkit/ValueCallback;)V", "cookieManagerRemoveSessionCookies"),
        virtual("android/webkit/WebSettings", "setMixedContentMode", "(I)V", "webSettingsSetMixedContentMode"),

        // Animators (API 21)
        Rule("android/animation/ValueAnimator", "ofArgb", "([I)Landroid/animation/ValueAnimator;", "valueAnimatorOfArgb", isStatic = true),
        Rule("android/animation/ObjectAnimator", "ofArgb", "(Ljava/lang/Object;Ljava/lang/String;[I)Landroid/animation/ObjectAnimator;", "objectAnimatorOfArgb", isStatic = true),
        Rule("android/animation/ObjectAnimator", "ofArgb", "(Ljava/lang/Object;Landroid/util/Property;[I)Landroid/animation/ObjectAnimator;", "objectAnimatorOfArgb", isStatic = true),

        // java.* methods missing from the Java 7 libcore of Android 4.4 (also used by generated code
        // and libraries that lint doesn't see)
        virtual("java/util/Locale", "toLanguageTag", "()Ljava/lang/String;", "localeToLanguageTag"),
        virtual("java/util/Locale", "getScript", "()Ljava/lang/String;", "localeGetScript"),
        virtual("java/util/Locale", "getUnicodeLocaleType", "(Ljava/lang/String;)Ljava/lang/String;", "localeGetUnicodeLocaleType"),
        Rule("java/util/Locale", "forLanguageTag", "(Ljava/lang/String;)Ljava/util/Locale;", "localeForLanguageTag", isStatic = true),
        virtual("java/net/URLConnection", "getContentLengthLong", "()J", "urlConnectionGetContentLengthLong"),
    )

    /** name+desc -> rules with that signature (one per declaring class). */
    val byNameAndDesc: Map<String, List<Rule>> = rules.groupBy { it.name + it.desc }
}

abstract class Api19BackportFactory : AsmClassVisitorFactory<InstrumentationParameters.None> {

    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
        return Api19BackportClassVisitor(instrumentationContext.apiVersion.get(), nextClassVisitor, classContext)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        // never rewrite the helper itself (it calls the real methods)
        return !classData.className.startsWith("org.telegram.messenger.compat.Api19")
    }
}

private class Api19BackportClassVisitor(
    private val api: Int,
    next: ClassVisitor,
    private val classContext: ClassContext,
) : ClassVisitor(api, next) {

    private val assignableCache = HashMap<String, Boolean>()

    /** Whether [owner] (internal name) is [declaringClass] or one of its subclasses. */
    fun isAssignable(owner: String, declaringClass: String): Boolean {
        if (owner == declaringClass) return true
        if (owner.startsWith("[")) return false
        return assignableCache.getOrPut("$owner>$declaringClass") {
            val data = try {
                classContext.loadClassData(owner.replace('/', '.'))
            } catch (t: Throwable) {
                null
            }
            data != null && data.superClasses.contains(declaringClass.replace('/', '.'))
        }
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?,
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        return object : MethodVisitor(api, mv) {
            override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
                val candidates = Api19BackportRules.byNameAndDesc[name + descriptor]
                if (candidates != null && !isInterface) {
                    for (rule in candidates) {
                        val matches = if (rule.isStatic) {
                            opcode == Opcodes.INVOKESTATIC && owner == rule.declaringClass
                        } else {
                            opcode == Opcodes.INVOKEVIRTUAL && isAssignable(owner, rule.declaringClass)
                        }
                        if (matches) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, Api19BackportRules.HELPER, rule.helperName, rule.helperDesc, false)
                            return
                        }
                    }
                }
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        }
    }
}
