package android.graphics.drawable;

import android.content.res.ColorStateList;

/**
 * Litegram: Android 4.4 placeholder for {@code android.graphics.drawable.RippleDrawable}
 * (API 21). There are no ripples on Android 4.4: the placeholder just draws its content layer.
 * Never used on Android 5.0+, where the framework class is loaded instead.
 */
public class RippleDrawable extends LayerDrawable {

    public static final int RADIUS_AUTO = -1;

    public RippleDrawable(ColorStateList color, Drawable content, Drawable mask) {
        super(content != null ? new Drawable[]{content} : new Drawable[0]);
    }

    public void setColor(ColorStateList color) {
    }

    public void setEffectColor(ColorStateList color) {
    }

    public void setRadius(int radius) {
    }

    public int getRadius() {
        return RADIUS_AUTO;
    }
}
