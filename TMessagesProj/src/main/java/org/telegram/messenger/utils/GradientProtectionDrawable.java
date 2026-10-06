package org.telegram.messenger.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.animation.Interpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.annotation.IntDef;
import androidx.core.view.animation.PathInterpolatorCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.telegram.ui.ActionBar.Theme;

public class GradientProtectionDrawable extends Drawable {

    /** Litegram: same values as androidx.core.view.WindowInsetsCompat.Side (androidx.core 1.15+, which needs API 21). */
    public static final class Side {
        public static final int LEFT = 1;
        public static final int TOP = 1 << 1;
        public static final int RIGHT = 1 << 2;
        public static final int BOTTOM = 1 << 3;

        @Retention(RetentionPolicy.SOURCE)
        @IntDef(flag = true, value = {LEFT, TOP, RIGHT, BOTTOM})
        public @interface InsetsSide {}

        private Side() {}
    }
    private final Interpolator mInterpolator;

    private final GradientDrawable mDrawable;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect mInsets = new Rect();
    private @Side.InsetsSide int mSide;

    private final int[] mColors;
    private int mColor;
    private int mAlpha = 255;

    public GradientProtectionDrawable(
        @Side.InsetsSide int side) {
        this(side, 0, DEFAULT_INTERPOLATOR, 8);
    }

    public GradientProtectionDrawable(
            @Side.InsetsSide int side,
            @ColorInt int color) {
        this(side, color, DEFAULT_INTERPOLATOR, 8);
    }

    public GradientProtectionDrawable(
        @Side.InsetsSide int side,
        @ColorInt int color,
        Interpolator interpolator,
        int n
    ) {
        super();
        mDrawable = new GradientDrawable();
        mInterpolator = interpolator;
        mColors = new int[n];
        setSide(side);
        setColor(color);
    }

    public void setSide(@Side.InsetsSide int side) {
        this.mSide = side;
        switch (side) {
            case Side.LEFT:
                mDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                break;
            case Side.TOP:
                mDrawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
                break;
            case Side.RIGHT:
                mDrawable.setOrientation(GradientDrawable.Orientation.RIGHT_LEFT);
                break;
            case Side.BOTTOM:
                mDrawable.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
                break;
        }
    }

    public void setColor(@ColorInt int color) {
        if (mColor == color) {
            return;
        }

        mColor = color;
        fillColors(mInterpolator, mColor, mColors);
        mDrawable.setColors(mColors);
        mPaint.setColor(Theme.multAlpha(mColor, mAlpha / 255f));
    }


    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);
        mDrawable.setBounds(
            bounds.left + mInsets.left,
            bounds.top + mInsets.top,
            bounds.right - mInsets.right,
            bounds.bottom - mInsets.bottom
        );
    }

    public void setInsets(int left, int top, int right, int bottom) {
        if (mInsets.left != left || mInsets.top != top || mInsets.right != right || mInsets.bottom != bottom) {
            mInsets.set(left, top, right, bottom);
            onBoundsChange(getBounds());
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();

        if (!bounds.isEmpty()) {
            if (mSide == Side.LEFT && mInsets.left > 0) {
                canvas.drawRect(bounds.left, bounds.top, Math.min(bounds.right, bounds.left + mInsets.left), bounds.bottom, mPaint);
            } else if (mSide == Side.TOP && mInsets.top > 0) {
                canvas.drawRect(bounds.left, bounds.top, bounds.right, Math.min(bounds.bottom, bounds.top + mInsets.top), mPaint);
            } else if (mSide == Side.RIGHT && mInsets.right > 0) {
                canvas.drawRect(Math.max(bounds.left, bounds.right - mInsets.right), bounds.top, bounds.right, bounds.bottom, mPaint);
            } else if (mSide == Side.BOTTOM && mInsets.bottom > 0) {
                canvas.drawRect(bounds.left, Math.max(bounds.top, bounds.bottom - mInsets.bottom), bounds.right, bounds.bottom, mPaint);
            }
        }

        if (!mDrawable.getBounds().isEmpty()) {
            mDrawable.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
        mDrawable.setAlpha(alpha);
        mPaint.setColor(Theme.multAlpha(mColor, mAlpha / 255f));
    }

    @Override
    public int getAlpha() {
        return mAlpha;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mDrawable.setColorFilter(colorFilter);
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    /* source: androidx.core.view.insets.GradientProtection */

    // Litegram: android.view.animation.PathInterpolator is API 21
    public static final Interpolator DEFAULT_INTERPOLATOR = PathInterpolatorCompat.create(0.42f, 0f, 0.58f, 1f);

    public static void fillColors(Interpolator interpolator, int color, int[] colors) {
        final int steps = colors.length - 1;
        final int a = Color.alpha(color);
        for (int i = steps; i >= 0; i--) {
            final float alpha = interpolator.getInterpolation((steps - i)  / (float) steps);
            colors[i] = ColorUtils.setAlphaComponent(color, (int) (alpha * a));
        }
    }
}
