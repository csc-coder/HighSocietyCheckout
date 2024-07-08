package com.kbiz.highsocietycheckout;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import com.kbiz.highsocietycheckout.nfc.NFCHandler;

import org.junit.Assert;
import org.junit.Test;

public class NFCHandlerTest {
    @Test
    public void testHashing(){
        String payload="testpayload";
        NdefRecord rec=NFCHandler.createHashRecord(payload);
        Assert.assertTrue(rec.getPayload().length>0);
    }
}
