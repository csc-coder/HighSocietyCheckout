package com.kbiz.highsocietycheckout;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import android.Manifest;

public class NFCHandler {
    private int NFC_PERMISSION_CODE = 1;

    private final NfcAdapter nfcAdapter;
    private final Context context;

    public NFCHandler(Context context) {
        this.context = context;
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (nfcAdapter == null) {
            Log.e("NFC", "NFC is not supported on this device.");
            // Handle lack of NFC support gracefully
        }
    }

    public boolean isNfcSupported() {
        return nfcAdapter != null;
    }

    public boolean isNfcEnabled() {
        return nfcAdapter != null && nfcAdapter.isEnabled();
    }

    public void enableForegroundDispatch(AppCompatActivity activity) {
        Intent intent = new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        if (ContextCompat.checkSelfPermission(this.context, Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
            // Request NFC permission
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.NFC}, NFC_PERMISSION_CODE);
        }
        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null);
    }

    public void disableForegroundDispatch(AppCompatActivity activity) {
        nfcAdapter.disableForegroundDispatch(activity);
    }

    public void handleIntent(Intent intent, NfcIntentHandler handler) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag.class);
            if (tag != null) {
                try {
                    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
                        Ndef ndef = Ndef.get(tag);
                        if (ndef == null) {
                            handler.onTagError("NDEF is not supported by this tag.");
                            return;
                        }
                        processNdefMessage(ndef, handler);
                    } else {
                        handler.onTagDiscovered(tag);
                    }
                } catch (IOException | FormatException e) {
                    handler.onTagError("Error processing tag: " + e.getMessage());
                }
            } else {
                handler.onTagError("NFC tag not found in intent.");
            }
        }
    }

    private void processNdefMessage(Ndef ndef, NfcIntentHandler handler) throws IOException, FormatException {
        ndef.connect();
        NdefMessage ndefMessage = ndef.getNdefMessage();
        if (ndefMessage == null) {
            handler.onTagError("No NDEF message found on tag.");
            return;
        }
        ArrayList<String> records = extractTextRecordsFromNdefMessage(ndefMessage);
        handler.onNdefMessageRead(records);
        ndef.close();
    }

    private ArrayList<String> extractTextRecordsFromNdefMessage(NdefMessage message) {
        ArrayList<String> records = new ArrayList<>();
        for (NdefRecord record : message.getRecords()) {
            if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                    Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
                try {
                    records.add(new String(record.getPayload(), StandardCharsets.UTF_8));
                } catch (Exception e) {
                    Log.e("NFC", "Error parsing NDEF record.", e);
                }
            }
        }
        return records;
    }

    public interface NfcIntentHandler {
        void onNdefMessageRead(ArrayList<String> records);
        void onTagDiscovered(Tag tag);
        void onTagError(String errorMessage);
    }

    public NfcAdapter getNfcAdapter() {
        return nfcAdapter;
    }

}
