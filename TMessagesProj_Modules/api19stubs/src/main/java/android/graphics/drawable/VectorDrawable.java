package android.graphics.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;

/**
 * Litegram: Android 4.4 placeholder for {@code android.graphics.drawable.VectorDrawable} (API 21),
 * so that {@code instanceof VectorDrawable} checks work (and are false) on Android 4.4, where
 * vector resources are pre-rendered to PNG by the build. Never instantiated by the framework there.
 */
public class VectorDrawable extends Drawable {

    public VectorDrawable() {
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
