package android.view.accessibility;

/**
 * Litegram: Android 4.4 placeholder for the nested class
 * {@code android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction} (API 21).
 * Instances can be created on Android 4.4; AccessibilityNodeInfo.addAction(AccessibilityAction)
 * is skipped there by the Api19BackportTransform (custom accessibility actions need API 21).
 *
 * Compiled without android.jar (module :Api19StubsNoSdk): javac would otherwise confuse this
 * top-level class with the nested framework class of the same binary name. The action ids are
 * the AccessibilityNodeInfo.ACTION_* constants.
 */
@SuppressWarnings("DollarSignInName")
public final class AccessibilityNodeInfo$AccessibilityAction {

    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_FOCUS = of(0x00000001);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_CLEAR_FOCUS = of(0x00000002);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_SELECT = of(0x00000004);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_CLEAR_SELECTION = of(0x00000008);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_CLICK = of(0x00000010);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_LONG_CLICK = of(0x00000020);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_ACCESSIBILITY_FOCUS = of(0x00000040);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_CLEAR_ACCESSIBILITY_FOCUS = of(0x00000080);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_NEXT_AT_MOVEMENT_GRANULARITY = of(0x00000100);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY = of(0x00000200);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_NEXT_HTML_ELEMENT = of(0x00000400);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_PREVIOUS_HTML_ELEMENT = of(0x00000800);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_SCROLL_FORWARD = of(0x00001000);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_SCROLL_BACKWARD = of(0x00002000);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_COPY = of(0x00004000);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_PASTE = of(0x00008000);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_CUT = of(0x00010000);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_SET_SELECTION = of(0x00020000);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_EXPAND = of(0x00040000);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_COLLAPSE = of(0x00080000);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_DISMISS = of(0x00100000);
    public static final AccessibilityNodeInfo$AccessibilityAction ACTION_SET_TEXT = of(0x00200000);

    private final int actionId;
    private final CharSequence label;

    public AccessibilityNodeInfo$AccessibilityAction(int actionId, CharSequence label) {
        this.actionId = actionId;
        this.label = label;
    }

    private static AccessibilityNodeInfo$AccessibilityAction of(int actionId) {
        return new AccessibilityNodeInfo$AccessibilityAction(actionId, null);
    }

    public int getId() {
        return actionId;
    }

    public CharSequence getLabel() {
        return label;
    }
}
