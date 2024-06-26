package com.kbiz.highsocietycheckout;

import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
        nfcHandler.setIntentHandler(new NFCHandler.NfcIntentHandler() {
            @Override
            public void onNDEFDiscovered(Tag tag) {
                handleNdefDiscovered(tag);
            }

            @Override
            public void onTagDiscovered(Tag tag) {
                statusViewModel.setStatusText("Empty Tag discovered");
                NavHostFragment.findNavController(FragmentScan.this).navigate(R.id.action_fragmentScan_to_fragmentRegister);
            }

            @Override
            public void onTagRemoved(Tag tag) {
                // Handle tag removal if necessary
            }

            @Override
            public void onTagError(String errorMessage) {
                statusViewModel.setStatusText("Error: " + errorMessage);
            }
        });
    }

    private void setupNfcHandling() {
        nfcHandler = new NFCHandler(getContext(), statusViewModel);
        Lookup.add(nfcHandler);

        if (!nfcHandler.isNfcSupported()) {
            statusViewModel.setStatusText(getString(R.string.nfc_is_not_supported_on_this_device));
        } else if (!nfcHandler.isNfcEnabled()) {
            statusViewModel.setStatusText(getString(R.string.nfc_is_not_enabled));
        } else {
            statusViewModel.setStatusText(getString(R.string.nfc_is_enabled));
        }
    }

    private void handleNdefDiscovered(Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                throw new RuntimeException("Cannot initialize NDEF on this tag");
            }
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();

            TagContent tagContent = Lookup.get(TagContent.class);
            tagContent.setnDefRecords(nfcHandler.extractTextRecordsFromNdefMessage(ndefMessage));

            AppCompatActivity activity = (AppCompatActivity) getActivity();
            nfcHandler.disableReaderMode(activity);

            if (ndefMessage == null || tagContent.isEmpty()) {
                NavHostFragment.findNavController(this).navigate(R.id.action_fragmentScan_to_fragmentRegister);
            } else {
                NavHostFragment.findNavController(this).navigate(R.id.action_fragmentScan_to_fragmentHarvest);
            }

            ndef.close();
        } catch (IOException | FormatException e) {
            Log.e("FragmentScan", "Error processing tag: " + e.getMessage(), e);
            statusViewModel.setStatusText("Error processing tag: " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        nfcHandler.showNFCEnablementStatusTexts();
        if (nfcHandler.isNfcSupported() && nfcHandler.isNfcEnabled()) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            nfcHandler.enableReaderMode(activity);
        } else {
            statusViewModel.setStatusText(getString(R.string.nfc_is_not_supported_on_this_device));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcHandler.disableReaderMode((AppCompatActivity) getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void handleNfcIntent(Intent intent) {
        nfcHandler.handleIntent(intent);
    }
}
