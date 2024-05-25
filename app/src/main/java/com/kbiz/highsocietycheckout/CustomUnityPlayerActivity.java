package com.kbiz.highsocietycheckout;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;

import org.json.JSONStringer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
// Import your NFC library package

public class CustomUnityPlayerActivity {
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
//        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);
    }

    protected void onResume() {
//        super.onResume();
        if (nfcAdapter != null) {
//            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    protected void onPause() {
//        super.onPause();
        if (nfcAdapter != null) {
//            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if (tag == null) {
            sendUnityMessage("OnTagError", "Tag not found");
            return;
        }

        try {
            processTag(tag, intent.getAction());
        } catch (IOException | FormatException e) {
            throw new RuntimeException(e);
        }
    }

    private void processTag(Tag tag, String action) throws IOException, FormatException {
        Ndef ndef = Ndef.get(tag);
        Log.d("LOK", "action:"+action);
        Log.d("LOK", "techs:"+Arrays.toString(tag.getTechList()));
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            handleNdefDiscovered(ndef, tag);
        } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            handleTagDiscovered(ndef, tag);
        }
    }

    private void handleNdefDiscovered(Ndef ndef, Tag tag) throws IOException, FormatException {
        if (ndef == null) {
            sendUnityMessage("OnTagError", "Could not initialize NDEF manager.");
            return;
        }

        ArrayList<String> records = listNdefRecords(ndef);
        if (records.isEmpty()) {
            writeNdefMessage(tag, new NdefMessage(createHashRecord()));
        }
        sendUnityMessage("OnTagScanned", "records (" + records.size() + "): " + records.toString());
    }

    private void handleTagDiscovered(Ndef ndef, Tag tag) throws IOException, FormatException {
        if (ndef != null) {
            ArrayList<String> records = listNdefRecords(ndef);
            if (records.isEmpty()) {
                writeNdefMessage(tag, new NdefMessage(createHashRecord()));
                records = listNdefRecords(ndef);
            }
            sendUnityMessage("OnTagScanned", "got records after scan (" + records.size() + "): " + records.toString());
        } else {
            formatTagNDEF(tag);
            ArrayList<String> records = listNdefRecords(ndef);
            sendUnityMessage("OnTagScanned", "freshly formatted tag. records (" + records.size() + "): " + records.toString());
        }
    }

    private void sendUnityMessage(String method, String message) {
//        UnityPlayer.UnitySendMessage("UIRoot", method, message);
    }


    private void writeNdefMessage(Tag tag, NdefMessage message) {
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
                Log.d("NFC", "Hash written to the NFC tag successfully.");
            } else {
                Log.d("NFC", "NFC tag is read-only.");
            }
        } catch (Exception e) {
            Log.e("NFC", "Error writing NFC tag", e);
        } finally {
            try {
                if (ndef != null) {
                    ndef.close();
                }
            } catch (Exception e) {
                Log.e("LOK", "error closing ndef", e);
                ndef=null;
            }
        }
    }

    private static String formatTagNDEF(Tag tag) throws IOException, FormatException {
        NdefFormatable ndefFormatable = NdefFormatable.get(tag);
        String result = "OK";
        if (ndefFormatable == null) {
            return result;
        }

        // initialize tag with new NDEF message
        try {
            ndefFormatable.connect();
            NdefMessage ndefMessage = new NdefMessage(createHashRecord());

            //do format the tag
            Log.d("LOK", "starting formatting...");
            ndefFormatable.format(ndefMessage);
            Log.d("LOK", "finished formatting.");
        } catch (Exception e) {
            Log.e("LOK", "error fetching ndef messages", e);
            result = "ERR:" + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace());
        } finally {
            try {
                if (ndefFormatable != null) {
                    ndefFormatable.close();
                }
            } catch (Exception e) {
                Log.e("LOK", "error closing ndef", e);
                result = "ERR:" + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace());
                ndefFormatable=null;
            }
        }
        return result;
    }

    private ArrayList<String> listNdefRecords(Ndef ndef) throws IOException {
        ArrayList<String> records = new ArrayList<>();
        if (ndef == null) {
            return records;
        }

        try {
            ndef.connect();

            NdefMessage ndefMessage = ndef.getNdefMessage();
            if (ndefMessage == null) {
                Log.d("LOK", "no NDEF message found");
                return records;
            }
            for (NdefRecord ndefRecord : ndefMessage.getRecords()) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN) {
                    String text = new String(ndefRecord.getPayload());
                    Log.d("LOK", "got ndef record: " + text);
                    records.add(text);
                }
            }
            return records;
        } catch (IOException e) {
            Log.e("LOK", "error fetching ndef messages:" + e.getMessage(), e);
        } catch (FormatException e) {
            Log.e("LOK", "format error fetching ndef messages", e);
        } finally {
            try {
                if (ndef != null) {
                    ndef.close();
                }
            } catch (Exception e) {
                Log.e("LOK", "error closing ndef", e);
                ndef=null;
            }
        }
        return new ArrayList<String>();
    }

    private static NdefRecord createHashRecord() {
        try {
            String payload = createHash();
            byte[] languageCode = new byte[0];
            languageCode = Locale.getDefault().getLanguage().getBytes("US-ASCII");
            byte[] text = payload.getBytes(Charset.forName("UTF-8"));
            byte[] data = new byte[1 + languageCode.length + text.length]; // +1 for the status byte
            data[0] = (byte) languageCode.length; // status byte
            System.arraycopy(languageCode, 0, data, 1, languageCode.length);
            System.arraycopy(text, 0, data, 1 + languageCode.length, text.length);
            NdefRecord hashRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
            return hashRecord;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String createHash() {
        return "fancyHashh189rh183rhj";
    }

}
