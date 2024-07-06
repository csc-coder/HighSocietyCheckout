package com.kbiz.highsocietycheckout;

import android.content.Intent;

public interface NFCReactor {
    /**
     * intent caught by activity is routed to this method
     * @param intent the intent to route
     */
    void handleNFCIntent(Intent intent);
    NFCHandler.NfcIntentHandler getNFCIntentHandler();
}
