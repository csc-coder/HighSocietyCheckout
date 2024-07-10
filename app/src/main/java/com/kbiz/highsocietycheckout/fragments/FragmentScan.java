package com.kbiz.highsocietycheckout.fragments;

import static com.kbiz.highsocietycheckout.fragments.FragmentRegister.HASH_PREFIX;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

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
                handleNdefDiscovered(tag);
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


    private void handleNdefDiscovered(Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                throw new RuntimeException("Cannot initialize NDEF on this tag");
            }
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            ndef.close();

            ArrayList<String> records = nfcHandler.extractTextRecordsFromNdefMessage(ndefMessage);

            String firstRecord = records.get(0);
            if (ndefMessage == null || records.isEmpty() || !isValidRecord(firstRecord)) {
                Log.d(LOK, "invalid tag found, switching to registration to fix this.");
                ((MainActivity) getContext()).runOnMainThread(
                        () -> NavHostFragment.findNavController(this).navigate(R.id.action_fragmentScan_to_fragmentRegister));
            } else {
                //check if tag has hash and if its in the db
                Log.d(LOK, "initialized tag found, switching to harvest");
                ((MainActivity) getContext()).runOnMainThread(
                        () -> NavHostFragment.findNavController(this).navigate(R.id.action_fragmentScan_to_fragmentHarvest));
            }

        } catch (IOException | FormatException e) {
            Log.e(LOK, "Error processing tag: " + e.getMessage(), e);
            statusViewModel.setStatusText("Error processing tag: " + e.getMessage());
        }
    }

    private boolean isValidRecord(String firstRecord) {
        if (firstRecord == null || firstRecord.isEmpty() || firstRecord.length() < 10) {//lets assume hash is biggr than 10 chars
            statusViewModel.setStatusText("record from tag is empty or does not exist");
            return false;
        }

        if (!firstRecord.substring(2).startsWith(HASH_PREFIX)) {
            statusViewModel.setStatusText("record from tag  does not start with our prefix:" + firstRecord);
            return false;
        }

        //check if db contains the hash
        if (!DatabaseManager.getInstance().userHashExists(firstRecord)) {
            statusViewModel.setStatusText("hash cant be found in user table: " + firstRecord);
            return false;
        }

        return true;
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
