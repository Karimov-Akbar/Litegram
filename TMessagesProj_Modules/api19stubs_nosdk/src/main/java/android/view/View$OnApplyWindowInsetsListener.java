package android.view;

/**
 * Litegram: Android 4.4 placeholder for the nested interface
 * {@code android.view.View.OnApplyWindowInsetsListener} (API 20). The binary name of this
 * top-level type is exactly {@code android.view.View$OnApplyWindowInsetsListener}, so listeners
 * (and desugared lambdas) implementing it can be created on Android 4.4. They are never invoked
 * there: View.setOnApplyWindowInsetsListener is skipped by the Api19BackportTransform.
 */
@SuppressWarnings("DollarSignInName")
public interface View$OnApplyWindowInsetsListener {
    WindowInsets onApplyWindowInsets(View v, WindowInsets insets);
}
