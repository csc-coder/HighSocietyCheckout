package com.kbiz.highsocietycheckout.fragments;

import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kbiz.highsocietycheckout.MainActivity;
import com.kbiz.highsocietycheckout.R;
import com.kbiz.highsocietycheckout.data.StatusViewModel;
import com.kbiz.highsocietycheckout.database.DatabaseManager;
import com.kbiz.highsocietycheckout.databinding.FragmentInitializeTagBinding;
import com.kbiz.highsocietycheckout.nfc.NFCHandler;
import com.kbiz.highsocietycheckout.nfc.NFCReactor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentInitializeTag extends Fragment implements NFCReactor {
    public static final String LOK = "LOK_INIT_TAG";


    // TODO: Rename and change types of parameters
    private NFCHandler nfcHandler;
    private FragmentInitializeTagBinding binding;

    private StatusViewModel statusViewModel;
    private NFCHandler.NfcIntentHandler nfcIntentHandler;
    private String userHash;
    private DatabaseManager dbManager;


    public FragmentInitializeTag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcHandler = NFCHandler.getInstance();
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);
        nfcIntentHandler = new NFCHandler.NfcIntentHandler() {
            @Override
            public void onNDEFDiscovered(Tag tag) {
                handleNdefDiscovered(tag);
            }

            @Override
            public void onNDEFlessDiscovered(Tag tag) {
                handleNDEFLess(tag);
            }

            @Override
            public void onTagRemoved(Tag tag) {
                // Handle tag removal if necessary
            }

            @Override
            public void onTagError(String errorMessage) {
                statusViewModel.setStatusText("Error: " + errorMessage);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!nfcHandler.isNfcSupported() || !nfcHandler.isNfcEnabled()) {
            this.statusViewModel.setStatusText((String) getContext().getResources().getText(R.string.nfc_is_not_supported_on_this_device));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Retrieve arguments
        if (getArguments() != null) {
            userHash = getArguments().getString("regData");
            // Use regData as needed
        }
        // Inflate the layout for this fragment using view binding
        binding = FragmentInitializeTagBinding.inflate(inflater, container, false);
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);
        dbManager = DatabaseManager.getInstance();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_initialize_tag, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void handleNDEFLess(Tag tag) {
        statusViewModel.setStatusText("InitTag: Tag found! formatting ...");
        try {
            nfcHandler.formatTagNDEF(tag, userHash);

            ((MainActivity) getContext()).runOnMainThread(() -> {
                Bundle bundle = new Bundle();
                bundle.putString("MSG", "Formatting successful.");
                bundle.putString("TARGET", "harvest");
                NavHostFragment.findNavController(FragmentInitializeTag.this).navigate(R.id.action_fragmentInitializeTag_to_fragmentConfirm, bundle);
            });
        } catch (IOException | FormatException e) {
            Log.e("LOK_ERR", e.getMessage() + "\n\n");
            e.printStackTrace();
            statusViewModel.setStatusText("Error: " + e.getMessage());
        }
    }

    private void handleNdefDiscovered(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            throw new RuntimeException("Cannot initialize NDEF on this tag");
        }
        try {
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            ndef.close();


            if (ndefMessage == null ) {

                nfcHandler.formatTagNDEF(tag, userHash);
                nfcHandler.writeNdefMessage(tag, nfcHandler.createNdefMessage(userHash));

                //check if record was written to tag
                String firstRecordContent = nfcHandler.readFirstRecordContent(tag);
                userHash="en"+userHash;
                if ( ! userHash.equals(firstRecordContent)) {
                    statusViewModel.setStatusText("could not read high society record after init. Try again.(" + userHash + "/" + firstRecordContent + ")");
                    return;
                }
                Log.d(LOK, "user hash sucessfully written to tag: " + userHash);

                this.dbManager.addUser(userHash);
                Log.d(LOK, "user hash saved to database. moving to harvest");
            } else {
                NdefRecord rec = ndefMessage.getRecords()[0];
                Log.d(LOK, "tag already initialized:"+rec);
            }

            ((MainActivity) getContext()).runOnMainThread(() -> {
                Bundle bundle = new Bundle();
                bundle.putString("MSG", "Formatting successful.");
                bundle.putString("TARGET", "harvest");

                Log.d(LOK, "navigating to confirm frag");
                NavHostFragment.findNavController(FragmentInitializeTag.this).navigate(R.id.action_fragmentInitializeTag_to_fragmentConfirm, bundle);
            });
        } catch (IOException | FormatException e) {
            Log.e("FragmentScan", "Error processing tag: " + e.getMessage(), e);
            statusViewModel.setStatusText("Error processing tag: " + e.getMessage());
        }
    }

    public static   NdefRecord createTextRecord(String content) {
        try {
            byte[] languageCode = "en".getBytes(StandardCharsets.US_ASCII);
            byte[] textBytes = content.getBytes(StandardCharsets.UTF_8);
            byte[] payload = new byte[1 + languageCode.length + textBytes.length];
            payload[0] = (byte) languageCode.length;
            System.arraycopy(languageCode, 0, payload, 1, languageCode.length);
            System.arraycopy(textBytes, 0, payload, 1 + languageCode.length, textBytes.length);
            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
        } catch (Exception e) {
            throw new RuntimeException("Error creating text record", e);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    public void handleNFCIntent(Intent intent) {
        nfcHandler.handleIntent(intent, nfcIntentHandler);
    }

    @Override
    public NFCHandler.NfcIntentHandler getNFCIntentHandler() {
        return nfcIntentHandler;
    }
}