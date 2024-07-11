package com.kbiz.highsocietycheckout.fragments;

import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.kbiz.highsocietycheckout.MainActivity;
import com.kbiz.highsocietycheckout.R;
import com.kbiz.highsocietycheckout.data.StatusViewModel;
import com.kbiz.highsocietycheckout.data.dao.UserDAO;
import com.kbiz.highsocietycheckout.data.entities.User;
import com.kbiz.highsocietycheckout.database.AppDatabase;
import com.kbiz.highsocietycheckout.database.DatabaseManager;
import com.kbiz.highsocietycheckout.databinding.FragmentInitializeTagBinding;
import com.kbiz.highsocietycheckout.nfc.NFCHandler;
import com.kbiz.highsocietycheckout.nfc.NFCReactor;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentInitializeTag extends Fragment implements NFCReactor {
    public static final String LOK = "LOK_INIT_TAG";


    private NFCHandler nfcHandler;
    private FragmentInitializeTagBinding binding;

    private StatusViewModel statusViewModel;
    private NFCHandler.NfcIntentHandler nfcIntentHandler;
    private String userHash;
    private DatabaseManager dbManager;
    private UserDAO userDAO;


    public FragmentInitializeTag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //DB
        dbManager = DatabaseManager.getInstance();
        AppDatabase db = AppDatabase.getDatabase(getContext().getApplicationContext());
        userDAO = db.userDAO();

        //NFC
        nfcHandler = NFCHandler.getInstance();
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);
        nfcIntentHandler = new NFCHandler.NfcIntentHandler() {
            @Override
            public void onNDEFDiscovered(Tag tag) {
                initTag(tag);
            }

            @Override
            public void onNDEFlessDiscovered(Tag tag) {
                initTag(tag);
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
                            () -> NavHostFragment.findNavController(FragmentInitializeTag.this).navigate(R.id.action_fragmentInitializeTag_to_fragmentClearTag));
                    return true;
                } else if (itemId == R.id.action_db_manager) {// Handle about action
                    activity.runOnMainThread(
                            () -> NavHostFragment.findNavController(FragmentInitializeTag.this).navigate(R.id.action_fragmentInitializeTag_to_fragmentDBManager));
                    return true;
                } else if (itemId == R.id.action_show_logs) {// Handle about action
                    activity.runOnMainThread(
                            () -> NavHostFragment.findNavController(FragmentInitializeTag.this).navigate(R.id.action_fragmentInitializeTag_to_fragmentShowLogs));
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_initialize_tag, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initTag(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            throw new RuntimeException("Cannot initialize NDEF on this tag");
        }
        try {
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            ndef.close();

            nfcHandler.formatTagWithPayload(tag, userHash);
            nfcHandler.writeNdefMessage(tag, nfcHandler.createNdefMessage(userHash));

            //check if record was written to tag
            String firstRecordContent = nfcHandler.readFirstRecordContent(tag);
            if (!userHash.equals(firstRecordContent)) {
                statusViewModel.setStatusText("could not read high society record after init. Try again.(" + userHash + "/" + firstRecordContent + ")");
                return;
            }
            Log.d(LOK, "user hash sucessfully written to tag: " + userHash);

            if (!dbManager.userHashExists(userHash)) {
                this.userDAO.insert(new User(userHash));
                statusViewModel.setStatusText("user hash saved to database.");
            } else {
                statusViewModel.setStatusText("user hash already in database.");
            }

            ((MainActivity) getContext()).runOnMainThread(() -> {
                Bundle bundle = new Bundle();
                bundle.putString("MSG", "Formatting successful.");
                bundle.putString("TARGET", "harvest");
                NavHostFragment.findNavController(FragmentInitializeTag.this).navigate(R.id.action_fragmentInitializeTag_to_fragmentConfirm, bundle);
            });
        } catch (IOException | FormatException e) {
            Log.e("FragmentScan", "Error processing tag: " + e.getMessage(), e);
            statusViewModel.setStatusText("Error processing tag: " + e.getMessage());
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