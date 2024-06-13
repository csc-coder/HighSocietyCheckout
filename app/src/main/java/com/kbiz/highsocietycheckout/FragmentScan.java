package com.kbiz.highsocietycheckout;

import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.kbiz.highsocietycheckout.data.TagContent;
import com.kbiz.highsocietycheckout.databinding.FragmentScanBinding;
import com.kbiz.highsocietycheckout.lookup.Lookup;

import java.io.IOException;
import java.util.ArrayList;

public class FragmentScan extends Fragment {

    private FragmentScanBinding binding;
    private NFCHandler nfcHandler;
    private StatusViewModel statusViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScanBinding.inflate(inflater, container, false);
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupNfcHandling();
    }

    private void setupNfcHandling() {
        // Set the intent handler
        nfcHandler = new NFCHandler(getContext(), statusViewModel);
        nfcHandler.setIntentHandler(new NFCHandler.NfcIntentHandler() {
            @Override
            public void onNDefNotNull(Tag tag) {
                try {
                    Ndef ndef = Ndef.get(tag);
                    if (ndef == null) {
                        onTagError("cant init ndef on this tag");
                        throw new RuntimeException("cant init ndef on this tag");
                    }
                    ndef.connect();
                    NdefMessage ndefMessage = ndef.getNdefMessage();

                    //store tag data in global storage
                    TagContent tagContent = Lookup.get(TagContent.class);
                    tagContent.setnDefRecords(nfcHandler.extractTextRecordsFromNdefMessage(ndefMessage));

                    // Handle tag scan result and navigate accordingly
                    if (ndefMessage == null || tagContent.isEmpty()) {
                        // Navigate to registration
                        NavHostFragment.findNavController(FragmentScan.this).navigate(R.id.action_fragmentScan_to_fragmentRegister);
                    } else {
                        // Navigate to harvest
                        NavHostFragment.findNavController(FragmentScan.this).navigate(R.id.action_fragmentScan_to_fragmentHarvest);
                    }
                    ndef.close();
                } catch (IOException | FormatException e) {
                    Log.e("LOK", e.getMessage(), e);
                    onTagError("Error processing tag: " + e.getMessage());
                }
            }

            @Override
            public void onTagDiscovered(Tag tag) {
                statusViewModel.setStatusText("Tag discovered");
                if (Lookup.get(TagContent.class).isEmpty()) {
                    //register
//                    Lookup.get(MainActivity.class).runOnMainThread(() -> {
                        NavHostFragment.findNavController(FragmentScan.this).navigate(R.id.action_fragmentScan_to_fragmentRegister);
//                    });
                } else {
                    processTagData();
                }
            }

            @Override
            public void onTagError(String errorMessage) {
                statusViewModel.setStatusText("Error: " + errorMessage);
            }
        });

        // Check if NFC is supported and enabled
        if (!nfcHandler.isNfcSupported()) {
            statusViewModel.setStatusText((String) getContext().getResources().getText(R.string.nfc_is_not_supported_on_this_device));
        } else if (!nfcHandler.isNfcEnabled()) {
            statusViewModel.setStatusText((String) getContext().getResources().getText(R.string.nfc_is_not_enabled));
        } else {
            statusViewModel.setStatusText((String) getContext().getResources().getText(R.string.nfc_is_enabled));
        }
    }

    private void processTagData() {
        ArrayList<String> records = Lookup.get(TagContent.class).getnDefRecords();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcHandler.isNfcSupported() && nfcHandler.isNfcEnabled()) {
            nfcHandler.enableReaderMode((AppCompatActivity) getActivity());
        } else {
            statusViewModel.setStatusText((String) getContext().getResources().getText(R.string.nfc_is_not_supported_on_this_device));
        }
        checkNfcEnabled();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Disable foreground dispatch when the fragment is not visible
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            nfcHandler.disableReaderMode(activity);
        }
    }

    public void checkNfcEnabled() {
        NfcAdapter nfcAdapter = nfcHandler.getNfcAdapter();
        if (nfcAdapter != null && !nfcAdapter.isEnabled()) {
            // NFC is not enabled. Show a dialog to the user to enable NFC
            new AlertDialog.Builder(this.getContext())
                    .setTitle("NFC is disabled")
                    .setMessage("Please enable NFC to use this feature.")
                    .setPositiveButton("Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            statusViewModel.setStatusText("NFC is active");
        }
    }

    public void handleNfcIntent(Intent intent) {
        // Handle new intent with NFC data
        nfcHandler.handleIntent(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
