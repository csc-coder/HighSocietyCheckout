package com.kbiz.highsocietycheckout;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.kbiz.highsocietycheckout.databinding.FragmentScanBinding;

import java.util.ArrayList;

public class FragmentScan extends Fragment {

    private FragmentScanBinding binding;
    private NFCHandler nfcHandler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScanBinding.inflate(inflater, container, false);
        nfcHandler = new NFCHandler(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupNfcHandling();
    }

    private void setupNfcHandling() {
        // Check if NFC is supported and enabled
        if (!nfcHandler.isNfcSupported()) {
            binding.textViewStatus.setText(R.string.nfc_is_not_supported_on_this_device);
            binding.textViewStatus.setVisibility(View.VISIBLE);
        } else if (!nfcHandler.isNfcEnabled()) {
            binding.textViewStatus.setText(R.string.nfc_is_not_enabled);
            binding.textViewStatus.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Setup foreground dispatch to capture NFC tags when the fragment is visible
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            nfcHandler.enableForegroundDispatch(activity);
            activity.getSupportActionBar().setTitle(R.string.scan_nfc_tag);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Disable foreground dispatch when the fragment is not visible
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            nfcHandler.disableForegroundDispatch(activity);
        }
    }

    public void handleNfcIntent(Intent intent) {
        // Handle new intent with NFC data
        nfcHandler.handleIntent(intent, new NFCHandler.NfcIntentHandler() {
            @Override
            public void onNdefMessageRead(ArrayList<String> records) {
                binding.textViewStatus.setText(getString(R.string.ndef_message) + records.toString());
                binding.textViewStatus.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTagDiscovered(Tag tag) {
                binding.textViewStatus.setText(R.string.nfc_tag_discovered);
                binding.textViewStatus.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTagError(String errorMessage) {
                binding.textViewStatus.setText(getString(R.string.error) + errorMessage);
                binding.textViewStatus.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
