package com.kbiz.highsocietycheckout.fragments;

import static com.kbiz.highsocietycheckout.fragments.FragmentInitializeTag.createTextRecord;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kbiz.highsocietycheckout.MainActivity;
import com.kbiz.highsocietycheckout.R;
import com.kbiz.highsocietycheckout.data.StatusViewModel;
import com.kbiz.highsocietycheckout.nfc.NFCHandler;
import com.kbiz.highsocietycheckout.nfc.NFCReactor;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentClearTag extends Fragment implements NFCReactor {

    private StatusViewModel statusViewModel;
    private NFCHandler.NfcIntentHandler nfcIntentHandler;
    private NFCHandler nfcHandler;


    public FragmentClearTag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        nfcHandler = NFCHandler.getInstance();

        nfcIntentHandler = new NFCHandler.NfcIntentHandler() {
            @Override
            public void onNDEFDiscovered(Tag tag) {
                handleNdefDiscovered(tag);
            }

            @Override
            public void onNDEFlessDiscovered(Tag tag) {
                statusViewModel.setStatusText("Scan:Empty Tag discovered");
                NavController ctrl = NavHostFragment.findNavController(FragmentClearTag.this);
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_clear_tag, container, false);
    }

    private void handleNdefDiscovered(Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                throw new RuntimeException("Cannot initialize NDEF on this tag");
            }

            //clear tag
            nfcHandler.formatTagNDEF(tag, "0");

            //check if record was written to tag
            String firstRecordContent = nfcHandler.readFirstRecordContent(tag);
            if (firstRecordContent == null || firstRecordContent.isEmpty()) {
                statusViewModel.setStatusText("Tag cleared.");
            } else {
                statusViewModel.setStatusText("Tag not cleared. Trying another way.");
                if (!ndef.isConnected()) {
                    ndef.connect();
                }

                nfcHandler.writeNdefMessage(tag, nfcHandler.createNdefMessage("0"));
                firstRecordContent = nfcHandler.readFirstRecordContent(tag);
                if (firstRecordContent == null || firstRecordContent.isEmpty()) {
                    statusViewModel.setStatusText("Tag cleared. Finally.");
                } else {
                    throw new IOException("tag not cleared. please reattach and hold very still");
                }
            }
        } catch (IOException | FormatException e) {
            Log.e("FragmentScan", "Error processing tag: " + e.getMessage(), e);
            statusViewModel.setStatusText("Error processing tag: " + e.getMessage());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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