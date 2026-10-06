package org.telegram.messenger;

import android.content.Context;

import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;

/**
 * Litegram: passkeys need Android's Credential Manager (API 28+) and only work with the official
 * app ids, so this build ships without them (no androidx.credentials / Kotlin coroutines).
 * The methods behave like upstream with {@link BuildVars#SUPPORTS_PASSKEYS} == false.
 */
public class PasskeysController {

    public static void create(Context context, int currentAccount, Utilities.Callback2<TL_account.Passkey, String> done) {
    }

    public static Runnable login(Context context, int currentAccount, boolean clickedButton, Utilities.Callback3<Long, TLRPC.auth_Authorization, String> done) {
        return null;
    }
}
