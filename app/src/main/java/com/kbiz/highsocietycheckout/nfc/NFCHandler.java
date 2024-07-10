package com.kbiz.highsocietycheckout.nfc;

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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.kbiz.highsocietycheckout.MainActivity;
import com.kbiz.highsocietycheckout.R;
import com.kbiz.highsocietycheckout.data.StatusViewModel;
import com.lambdapioneer.argon2kt.Argon2Kt;
import com.lambdapioneer.argon2kt.Argon2KtResult;
import com.lambdapioneer.argon2kt.Argon2Mode;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class NFCHandler implements NfcAdapter.ReaderCallback, NfcAdapter.OnTagRemovedListener {
    public static final int REQUEST_CODE_NFC = 1;
    public static final int NFC_PERMISSION_CODE = 1;
    private static final String LOK = "LOK_NFC";
    private static NFCHandler instance;

    private final NfcAdapter nfcAdapter;
    private final WeakReference<Context> context;
    private NfcIntentHandler intentHandler;
    private final StatusViewModel statusViewModel;

    private NFCHandler(Context context, StatusViewModel statusViewModel) {
        this.context = new WeakReference<>(context);
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        this.statusViewModel = statusViewModel;
        if (nfcAdapter == null) {
            Log.e(LOK, "NFC is not supported on this device.");
        }
        setNFCStatusText();
    }

    // Public method to provide access to the singleton instance
    public static synchronized NFCHandler getInstance(Context context, StatusViewModel statusViewModel) {
        if (instance == null) {
            instance = new NFCHandler(context, statusViewModel);
        }
        return instance;
    }

    // Public method to provide access to the singleton instance
    public static synchronized NFCHandler getInstance() {
        if (instance == null) {
            throw new RuntimeException("init statusViewModel first by using getInstance(context, statusviewmodel) at least once at app startup");
        }
        return instance;
    }

    public boolean isNfcSupported() {
        return nfcAdapter != null;
    }

    public boolean isNfcEnabled() {
        return nfcAdapter != null && nfcAdapter.isEnabled();
    }

    private void setNFCStatusText() {
        if (!isNfcSupported()) {
            statusViewModel.setStatusText(context.get().getResources().getString(R.string.nfc_is_not_supported_on_this_device));
        } else if (!isNfcEnabled()) {
            statusViewModel.setStatusText(context.get().getResources().getString(R.string.nfc_is_not_enabled));
        } else {
            statusViewModel.setStatusText(context.get().getResources().getString(R.string.nfc_is_enabled));
        }
    }

    public void enableReaderMode() {
        if (nfcAdapter == null) {
            return;
        }
        Bundle options = new Bundle();
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);
        if (!getMainActivity().isDestroyed()) {
            nfcAdapter.enableReaderMode(getMainActivity(), this,
                    NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_F |
                            NfcAdapter.FLAG_READER_NFC_V |
                            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS, options);
            statusViewModel.setStatusText("Enabled reader mode");
        }
    }


    public void disableReaderMode() {
        MainActivity activity = getMainActivity();
        if (nfcAdapter == null || activity == null || activity.isDestroyed()) {
            return;
        }
        getMainActivity().runOnMainThread(() -> {
            try {
                nfcAdapter.disableReaderMode(activity);
                statusViewModel.setStatusText("Disabled reader mode");
            } catch (Exception e) {
                Log.d(LOK, "got exception.", e);
            }
        });
    }


    public void enableForegroundDispatch() {
        MainActivity activity = getMainActivity();
        Intent intent = new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        if (ContextCompat.checkSelfPermission(this.context.get(), android.Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
            // Request NFC permission
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.NFC}, NFC_PERMISSION_CODE);
        }
        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null);
        statusViewModel.setStatusText("Enabled foreground dispatch");

    }

    public void disableForegroundDispatch() {
        MainActivity activity = getMainActivity();
        if (nfcAdapter == null || activity == null || activity.isDestroyed()) {
            return;
        }

        nfcAdapter.disableForegroundDispatch(getMainActivity());
        statusViewModel.setStatusText("Disabled foreground dispatch");
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        if (!(getMainActivity().getCurrentFragment() instanceof NFCReactor)) {
            return;
        }
        intentHandler = ((NFCReactor) getMainActivity().getCurrentFragment()).getNFCIntentHandler();
        if (intentHandler == null) {
            return;
        }
        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            intentHandler.onNDEFDiscovered(tag);
        } else {
            intentHandler.onNDEFlessDiscovered(tag);
        }
    }

    @Override
    public void onTagRemoved() {
        statusViewModel.setStatusText("Tag removed");
    }

    public ArrayList<String> extractTextRecordsFromNdefMessage(NdefMessage message) {
        if (message == null) {
//            Log.e(TAG, "Error parsing NDEF record. Message == null.");
            return new ArrayList<>();
        }
        ArrayList<String> records = new ArrayList<>();
        for (NdefRecord record : message.getRecords()) {
            if (record.getTnf() != NdefRecord.TNF_WELL_KNOWN || !Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
                continue;
            }

            try {
                records.add(new String(record.getPayload(), StandardCharsets.UTF_8));
            } catch (Exception e) {
                Log.e(LOK, "Error parsing NDEF record. [" + record.getPayload() + "]", e);
                statusViewModel.setStatusText("Error parsing NDEF record: " + e.getMessage());
            }
        }
        return records;
    }

    public void writeNdefMessage(Tag tag, NdefMessage message) throws IOException, FormatException {
        if (tag == null) {
            return;
        }
        Ndef ndef = Ndef.get(tag);
        try {
            if (ndef == null) {
                return;
            }
            if (!ndef.isConnected()) {
                try {
                    ndef.connect();
                } catch (Exception e) { /*NOOP*/ }
            }
            if (ndef.isWritable()) {
                Log.d(LOK, "Gonna write Hash to NFC tag ...");

                ndef.writeNdefMessage(message);
                Log.d(LOK, "Hash written to the NFC tag successfully.");
            } else {
                Log.d(LOK, "NFC tag is read-only.");
            }
        } catch (Exception e) {
            Log.e(LOK, "Error writing NFC tag", e);
            throw e;
        } finally {
            try {
                if (ndef != null) {
                    ndef.close();
                }
            } catch (Exception e) {
                Log.e(LOK, "Error closing NDEF", e);
                ndef = null;
            }
        }
    }

    public String formatTagNDEF(Tag tag, String payload) throws IOException, FormatException {
        NdefFormatable ndefFormatable = NdefFormatable.get(tag);
        String result = "OK";
        if (ndefFormatable == null) {
            return result;
        }

        try {
            if (!ndefFormatable.isConnected()) {
                ndefFormatable.connect();
            }
            NdefRecord hash = createHashRecord(payload);
            NdefMessage ndefMessage = new NdefMessage(hash);
            statusViewModel.setStatusText("formatting tag with message:" + ndefMessage);
            ndefFormatable.format(ndefMessage);
            statusViewModel.setStatusText("Tag formatted and message written.");
        } finally {
            try {
                if (ndefFormatable != null) {
                    statusViewModel.setStatusText("Formatting done.");
                    ndefFormatable.close();
                }
            } catch (Exception e) {
                Log.e(LOK, "Error closing NDEFFormatable", e);
                result = "ERR:" + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace());
                statusViewModel.setStatusText(result);
                ndefFormatable = null;
            }
        }
        return result;
    }

    private boolean makeReadOnly(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            try {
                ndef.connect();
                if (ndef.canMakeReadOnly()) {
                    ndef.makeReadOnly();
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false; // Failed to make the tag read-only
    }

    public static NdefRecord createHashRecord(String payload) {
        String hash = createHash(payload);
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], hash.getBytes());
    }

    public static String createHash(String payload) {
        Argon2Kt argon2Java = new Argon2Kt();

        byte[] passwordByteArray = payload.getBytes(StandardCharsets.UTF_8);

        // Hash the password
        Argon2KtResult hashResult = argon2Java.hash(
                Argon2Mode.ARGON2_I,
                passwordByteArray,
                "saltqoawidehaWOIUHD".getBytes(),
                5,
                65536
        );

        Log.d(LOK, "Raw hash: " + hashResult.rawHashAsByteArray());
        Log.d(LOK, "Encoded string: " + hashResult.encodedOutputAsString());

        // Verify the password
        boolean verificationResult = argon2Java.verify(
                Argon2Mode.ARGON2_I,
                hashResult.encodedOutputAsString(),
                passwordByteArray
        );

        Log.d(LOK, "payload verified: " + verificationResult);
        return hashResult.encodedOutputAsString();
    }

    public interface NfcIntentHandler {
        void onNDEFDiscovered(Tag tag);

        void onNDEFlessDiscovered(Tag tag);

        void onTagRemoved(Tag tag);

        void onTagError(String errorMessage);
    }

    // New methods to handle reading and formatting tags
    public NdefMessage createNdefMessage(String text) {
        NdefRecord[] records = {createTextRecord(text)};
        return new NdefMessage(records);
    }

    private NdefRecord createTextRecord(String text) {
        byte[] language = Locale.getDefault().getLanguage().getBytes(StandardCharsets.US_ASCII);
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        int languageSize = language.length;
        int textLength = textBytes.length;
        byte[] payload = new byte[1 + languageSize + textLength];

        payload[0] = (byte) languageSize;
        System.arraycopy(language, 0, payload, 1, languageSize);
        System.arraycopy(textBytes, 0, payload, 1 + languageSize, textLength);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
    }

    public String readFirstRecordContent(Tag tag) throws IOException, FormatException {
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            throw new FormatException("Tag is not NDEF formatted");
        }

        if (!ndef.isConnected()) {
            ndef.connect();
        }
        NdefMessage ndefMessage = ndef.getNdefMessage();
        if (ndefMessage == null) {
            return "";
        }
        NdefRecord[] records = ndefMessage.getRecords();
        ndef.close();
        if (records.length == 0) {
            throw new FormatException("No records found");
        }

        NdefRecord record = records[0];
        byte[] payload = record.getPayload();
        if (payload == null || payload.length == 0) {
            return "";
        }
        return new String(payload, 1, payload.length - 1, StandardCharsets.UTF_8);
    }

    public void handleIntent(Intent intent, NfcIntentHandler nfcIntentHandler) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag.class);
            if (tag != null) {
                onTagDiscovered(tag);
            } else {
                intentHandler.onTagError("No tag found in intent.");
            }
        } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag.class);
            if (tag != null) {
                onTagDiscovered(tag);
            } else {
                intentHandler.onTagError("No tag found in intent.");
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag.class);
            if (tag != null) {
                onTagDiscovered(tag);
            } else {
                intentHandler.onTagError("No tag found in intent.");
            }
        } else {
            intentHandler.onTagError("Unsupported NFC action: " + action);
        }
    }

    public void showNFCEnablementStatusTexts() {
        if (nfcAdapter != null && !nfcAdapter.isEnabled()) {
            statusViewModel.setStatusText("NFC is DISABLED! Please activate");
        } else {
            statusViewModel.setStatusText("NFC is active. We're good to go.");
        }
    }

    // Method to get MainActivity safely
    public MainActivity getMainActivity() {
        Context context = this.context.get();
        if (context instanceof MainActivity) {
            return (MainActivity) context;
        }
        return null;
    }
}
