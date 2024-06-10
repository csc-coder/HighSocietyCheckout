package com.kbiz.highsocietycheckout;

import android.Manifest;
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
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class NFCHandler implements NfcAdapter.ReaderCallback {
    public static final int REQUEST_CODE_NFC = 1;
    public static final int NFC_PERMISSION_CODE = 1;

    private static final String TAG = "NFCHandler";
    private final NfcAdapter nfcAdapter;
    private final Context context;
    private NfcIntentHandler intentHandler;

    public NFCHandler(Context context) {
        this.context = context;
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (nfcAdapter == null) {
            Log.e(TAG, "NFC is not supported on this device.");
        }
    }

    public boolean isNfcSupported() {
        return nfcAdapter != null;
    }

    public boolean isNfcEnabled() {
        return nfcAdapter != null && nfcAdapter.isEnabled();
    }

    public void enableReaderMode(AppCompatActivity activity) {
        if (nfcAdapter != null) {
            Bundle options = new Bundle();
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);
            nfcAdapter.enableReaderMode(activity, this,
                    NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_F | NfcAdapter.FLAG_READER_NFC_V |
                            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS, options);
        }
    }

    public void disableReaderMode(AppCompatActivity activity) {
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(activity);
        }
    }


    public void enableForegroundDispatch(AppCompatActivity activity) {
        Intent intent = new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        if (ContextCompat.checkSelfPermission(this.context, android.Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
            // Request NFC permission
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.NFC}, NFC_PERMISSION_CODE);
        }
        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null);
    }

    public void disableForegroundDispatch(AppCompatActivity activity) {
        nfcAdapter.disableForegroundDispatch(activity);
    }
    @Override
    public void onTagDiscovered(Tag tag) {
        if (intentHandler != null) {
            try {
                Ndef ndef = Ndef.get(tag);
                if (ndef != null) {
                    processNdefMessage(ndef, intentHandler);
                } else {
                    intentHandler.onTagDiscovered(tag);
                }
            } catch (IOException | FormatException e) {
                intentHandler.onTagError("Error processing tag: " + e.getMessage());
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
                    records.add(new String(record.getPayload(), Charset.forName("UTF-8")));
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing NDEF record.", e);
                }
            }
        }
        return records;
    }

    public void writeNdefMessage(Tag tag, NdefMessage message) {
        if (tag == null) {
            return;
        }
        Ndef ndef = Ndef.get(tag);
        try {
            if (ndef == null) {
                return;
            }
            ndef.connect();
            if (ndef.isWritable()) {
                ndef.writeNdefMessage(message);
                Log.d(TAG, "Hash written to the NFC tag successfully.");
            } else {
                Log.d(TAG, "NFC tag is read-only.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error writing NFC tag", e);
        } finally {
            try {
                if (ndef != null) {
                    ndef.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error closing NDEF", e);
                ndef = null;
            }
        }
    }

    public static String formatTagNDEF(Tag tag) throws IOException, FormatException {
        NdefFormatable ndefFormatable = NdefFormatable.get(tag);
        String result = "OK";
        if (ndefFormatable == null) {
            return result;
        }

        try {
            ndefFormatable.connect();
            NdefMessage ndefMessage = new NdefMessage(createHashRecord());
            ndefFormatable.format(ndefMessage);
            Log.d(TAG, "Tag formatted and message written.");
        } catch (Exception e) {
            Log.e(TAG, "Error formatting tag", e);
            result = "ERR:" + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace());
        } finally {
            try {
                if (ndefFormatable != null) {
                    ndefFormatable.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error closing NDEFFormatable", e);
                result = "ERR:" + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace());
                ndefFormatable = null;
            }
        }
        return result;
    }

    private static NdefRecord createHashRecord() {
        try {
            String payload = createHash();
            byte[] languageCode = Locale.getDefault().getLanguage().getBytes("US-ASCII");
            byte[] text = payload.getBytes(Charset.forName("UTF-8"));
            byte[] data = new byte[1 + languageCode.length + text.length]; // +1 for the status byte
            data[0] = (byte) languageCode.length; // status byte
            System.arraycopy(languageCode, 0, data, 1, languageCode.length);
            System.arraycopy(text, 0, data, 1 + languageCode.length, text.length);
            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String createHash() {
        return "fancyHashh189rh183rhj";
    }

    public void setIntentHandler(NfcIntentHandler handler) {
        this.intentHandler = handler;
    }

    public NfcAdapter getNfcAdapter() {
        return nfcAdapter;
    }

    public interface NfcIntentHandler {
        void onNdefMessageRead(ArrayList<String> records);
        void onTagDiscovered(Tag tag);
        void onTagError(String errorMessage);
    }

    // New methods to handle reading and formatting tags
    public NdefMessage createNdefMessage(String text) {
        NdefRecord[] records = { createTextRecord(text) };
        return new NdefMessage(records);
    }

    private NdefRecord createTextRecord(String text) {
        try {
            byte[] language = Locale.getDefault().getLanguage().getBytes("US-ASCII");
            byte[] textBytes = text.getBytes(Charset.forName("UTF-8"));
            int languageSize = language.length;
            int textLength = textBytes.length;
            byte[] payload = new byte[1 + languageSize + textLength];

            payload[0] = (byte) languageSize;
            System.arraycopy(language, 0, payload, 1, languageSize);
            System.arraycopy(textBytes, 0, payload, 1 + languageSize, textLength);

            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding", e);
        }
    }

    public String readTextFromTag(Tag tag) throws IOException, FormatException {
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            throw new FormatException("Tag is not NDEF formatted");
        }

        NdefMessage ndefMessage = ndef.getNdefMessage();
        NdefRecord[] records = ndefMessage.getRecords();
        if (records.length == 0) {
            throw new FormatException("No records found");
        }

        NdefRecord record = records[0];
        byte[] payload = record.getPayload();
        return new String(payload, 1, payload.length - 1, Charset.forName("UTF-8"));
    }

    public void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                onTagDiscovered(tag);
            } else {
                intentHandler.onTagError("No tag found in intent.");
            }
        }
    }
}
