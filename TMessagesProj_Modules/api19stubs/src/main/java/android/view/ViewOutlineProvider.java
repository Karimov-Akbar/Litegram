package android.view;

import android.graphics.Outline;

/**
 * Litegram: Android 4.4 placeholder for {@code android.view.ViewOutlineProvider} (API 21).
 * Never used on Android 5.0+, where the framework class is loaded instead. Outlines don't exist
 * on Android 4.4, so {@link #getOutline} is never called there.
 */
public abstract class ViewOutlineProvider {

    public static final ViewOutlineProvider BACKGROUND = new Empty();
    public static final ViewOutlineProvider BOUNDS = new Empty();
    public static final ViewOutlineProvider PADDED_BOUNDS = new Empty();

    public ViewOutlineProvider() {
    }

    public abstract void getOutline(View view, Outline outline);

    private static final class Empty extends ViewOutlineProvider {
        @Override
        public void getOutline(View view, Outline outline) {
        }
    }
}
