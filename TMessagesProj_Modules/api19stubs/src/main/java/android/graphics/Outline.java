package android.graphics;

/**
 * Litegram: Android 4.4 placeholder for {@code android.graphics.Outline} (API 21). App code
 * creates Outline objects in a few places (e.g. to compute shadow paths); on Android 4.4 the
 * placeholder just remembers the shape. Never used on Android 5.0+, where the framework class
 * is loaded instead.
 */
public final class Outline {

    private final Rect rect = new Rect();
    private float radius;
    private float alpha = 1f;
    private boolean empty = true;
    private Path path;

    public Outline() {
    }

    public Outline(Outline src) {
        set(src);
    }

    public void setEmpty() {
        rect.setEmpty();
        radius = 0;
        path = null;
        empty = true;
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean canClip() {
        return false;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getAlpha() {
        return alpha;
    }

    public void set(Outline src) {
        rect.set(src.rect);
        radius = src.radius;
        alpha = src.alpha;
        empty = src.empty;
        path = src.path;
    }

    public void setRect(int left, int top, int right, int bottom) {
        setRoundRect(left, top, right, bottom, 0);
    }

    public void setRect(Rect rect) {
        setRect(rect.left, rect.top, rect.right, rect.bottom);
    }

    public void setRoundRect(int left, int top, int right, int bottom, float radius) {
        rect.set(left, top, right, bottom);
        this.radius = radius;
        path = null;
        empty = left >= right || top >= bottom;
    }

    public void setRoundRect(Rect rect, float radius) {
        setRoundRect(rect.left, rect.top, rect.right, rect.bottom, radius);
    }

    public boolean getRect(Rect outRect) {
        if (path != null || empty) {
            return false;
        }
        outRect.set(rect);
        return true;
    }

    public float getRadius() {
        return radius;
    }

    public void setOval(int left, int top, int right, int bottom) {
        setRoundRect(left, top, right, bottom, Math.min(right - left, bottom - top) / 2f);
    }

    public void setOval(Rect rect) {
        setOval(rect.left, rect.top, rect.right, rect.bottom);
    }

    public void setConvexPath(Path convexPath) {
        setPath(convexPath);
    }

    public void setPath(Path path) {
        this.path = path;
        rect.setEmpty();
        radius = 0;
        empty = path == null || path.isEmpty();
    }

    public void offset(int dx, int dy) {
        rect.offset(dx, dy);
        if (path != null) {
            path.offset(dx, dy);
        }
    }
}
