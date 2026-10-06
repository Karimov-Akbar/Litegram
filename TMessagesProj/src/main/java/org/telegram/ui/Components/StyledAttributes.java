package org.telegram.ui.Components;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;

import org.telegram.messenger.FileLog;
import org.xmlpull.v1.XmlPullParser;

/**
 * Litegram: provides an {@link AttributeSet} read from an XML resource whose root element has a
 * {@code style="@style/..."} attribute. Passing it to a 3-argument View constructor applies that
 * style exactly like the 4-argument (defStyleRes) constructor, which only exists since API 21.
 */
public final class StyledAttributes {

    private static final ThreadLocal<XmlResourceParser> current = new ThreadLocal<>();

    private StyledAttributes() {
    }

    /** Must be followed by {@link #release()} once the view constructor returned. */
    public static AttributeSet obtain(Context context, int xmlRes) {
        try {
            XmlResourceParser parser = context.getResources().getXml(xmlRes);
            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
                // skip to the root element
            }
            current.set(parser);
            return Xml.asAttributeSet(parser);
        } catch (Exception e) {
            FileLog.e(e);
            return null;
        }
    }

    public static void release() {
        XmlResourceParser parser = current.get();
        if (parser != null) {
            current.set(null);
            parser.close();
        }
    }
}
