package android.graphics.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;

/**
 * Litegram: Android 4.4 placeholder for {@code android.graphics.drawable.AnimatedVectorDrawable}
 * (API 21), so that {@code instanceof AnimatedVectorDrawable} checks work (and are false) on
 * Android 4.4. Animated vector resources have static fallbacks there.
 */
public class AnimatedVectorDrawable extends Drawable implements Animatable {

    public AnimatedVectorDrawable() {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    public void reset() {
    }

    @Override
    public void draw(Canvas canvas) {
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
