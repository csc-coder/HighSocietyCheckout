package com.kbiz.highsocietycheckout.fragments;

import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.kbiz.highsocietycheckout.MainActivity;
import com.kbiz.highsocietycheckout.R;
import com.kbiz.highsocietycheckout.data.StatusViewModel;
import com.kbiz.highsocietycheckout.database.DatabaseManager;
import com.kbiz.highsocietycheckout.databinding.FragmentScanBinding;
import com.kbiz.highsocietycheckout.nfc.NFCHandler;
import com.kbiz.highsocietycheckout.nfc.NFCReactor;

import java.io.IOException;
import java.util.ArrayList;

public class FragmentScan extends Fragment implements NFCReactor {

    public static final String LOK = "LOK_SCAN";
    private FragmentScanBinding binding;
    private StatusViewModel statusViewModel;
    private NFCHandler nfcHandler;
    private NFCHandler.NfcIntentHandler nfcIntentHandler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScanBinding.inflate(inflater, container, false);
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);
        nfcHandler = NFCHandler.getInstance();

        nfcIntentHandler = new NFCHandler.NfcIntentHandler() {
            @Override
            public void onNDEFDiscovered(Tag tag) {
                determineNavRouteByTagStatus(tag);
            }

            @Override
            public void onNDEFlessDiscovered(Tag tag) {
                statusViewModel.setStatusText("Scan:Empty Tag discovered");
                NavController ctrl = NavHostFragment.findNavController(FragmentScan.this);
                ((MainActivity) getContext()).runOnMainThread(
                        () -> ctrl.navigate(R.id.action_fragmentScan_to_fragmentRegister));
            }

            @Override
            public void onTagRemoved(Tag tag) {
                // Handle tag removal if necessary
                statusViewModel.setStatusText("Tag removed");
            }

            @Override
            public void onTagError(String errorMessage) {
                statusViewModel.setStatusText("Error: " + errorMessage);
            }
        };


        // Register the MenuProvider
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                MainActivity activity = (MainActivity) getContext();
                if (itemId == R.id.action_clear_tag) {// Handle settings action
                    activity.runOnMainThread(
                            () -> NavHostFragment.findNavController(FragmentScan.this).navigate(R.id.action_fragmentScan_to_fragmentClearTag));
                    return true;
                } else if (itemId == R.id.action_db_manager) {// Handle about action
                    activity.runOnMainThread(
                            () -> NavHostFragment.findNavController(FragmentScan.this).navigate(R.id.action_fragmentScan_to_fragmentDBManager));
                    return true;
                } else if (itemId == R.id.action_show_logs) {// Handle about action
                    activity.runOnMainThread(
                            () -> NavHostFragment.findNavController(FragmentScan.this).navigate(R.id.action_fragmentScan_to_fragmentShowLogs));
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    private void determineNavRouteByTagStatus(Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                throw new RuntimeException("Cannot initialize NDEF on this tag");
            }
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            ndef.close();

            ArrayList<String> records = nfcHandler.extractTextRecordsFromNdefMessage(ndefMessage);

            //check if db contains the hash

            String trimmedRecord = records.get(0).substring(1);
            if (ndefMessage == null || records.isEmpty() || !nfcHandler.isValidRecord(trimmedRecord)) {
                Log.d(LOK, "invalid tag found, switching to registration to fix this.");
                ((MainActivity) getContext()).runOnMainThread(
                        () -> NavHostFragment.findNavController(this).navigate(R.id.action_fragmentScan_to_fragmentRegister));
            } else {
                //check if tag has hash and if its in the db
                if (!DatabaseManager.getInstance().userHashExists(trimmedRecord)) {
                    statusViewModel.setStatusText("hash cant be found in user table. please clear tag and register again." + trimmedRecord);
                    return;
                }

                Log.d(LOK, "initialized tag found, switching to harvest");
                ((MainActivity) getContext()).runOnMainThread(
                        () -> NavHostFragment.findNavController(this).navigate(R.id.action_fragmentScan_to_fragmentHarvest));
            }

        } catch (IOException | FormatException e) {
            Log.e(LOK, "Error processing tag: " + e.getMessage(), e);
            statusViewModel.setStatusText("Error processing tag: " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        nfcHandler.showNFCEnablementStatusTexts();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void handleNFCIntent(Intent intent) {
        nfcHandler.handleIntent(intent, nfcIntentHandler);
    }

    @Override
    public NFCHandler.NfcIntentHandler getNFCIntentHandler() {
        return nfcIntentHandler;
    }
}
