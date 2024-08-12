package com.kbiz.highsocietycheckout.fragments;

import android.content.Intent;
import android.nfc.FormatException;
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
import android.view.View;
import android.view.ViewGroup;

import com.kbiz.highsocietycheckout.MainActivity;
import com.kbiz.highsocietycheckout.R;
import com.kbiz.highsocietycheckout.data.StatusViewModel;
import com.kbiz.highsocietycheckout.nfc.NFCHandler;
import com.kbiz.highsocietycheckout.nfc.NFCReactor;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentClearTag extends Fragment implements NFCReactor {
    public static final String LOK = "LOK_CLEAR_TAG";

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
                clearTag(tag);
            }

            @Override
            public void onNDEFlessDiscovered(Tag tag) {
                clearTag(tag);
            }

            @Override
            public void onTagRemoved(Tag tag) {
                // Handle tag removal if necessary
                statusViewModel.setStatusText("Tag removed. Waiting ...");
            }

            @Override
            public void onTagError(String errorMessage) {
                statusViewModel.setStatusText("Error: " + errorMessage);
            }
        };
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_clear_tag, container, false);
    }

    private void clearTag(Tag tag) {
        try {
//            SpinnerUtil.showSpinner(getParentFragmentManager());

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                throw new RuntimeException("Cannot initialize NDEF on this tag");
            }

            //clear tag
            nfcHandler.formatTagWithEmptyMessage(tag);

            //check if record was written to tag
            String firstRecordContent = nfcHandler.readFirstRecordContent(tag);
            if (firstRecordContent == null || firstRecordContent.isEmpty()) {
                statusViewModel.setStatusText("Tag cleared.");
            } else {
                statusViewModel.setStatusText("Tag not cleared. Trying to override first record");
                if (!ndef.isConnected()) {
                    ndef.connect();
                }

                //try overwriting
                nfcHandler.writeNdefMessage(tag, NFCHandler.createEmptyNDEFMessage());

                firstRecordContent = nfcHandler.readFirstRecordContent(tag);

                if (firstRecordContent == null || firstRecordContent.isEmpty()|| firstRecordContent.equals("0")) {
                    statusViewModel.setStatusText("Tag cleared. Finally.");
                } else {
                    statusViewModel.setStatusText(" Record Content: "+firstRecordContent);
                    throw new IOException("tag not cleared. please reattach and hold very still.");
                }
            }
        } catch (IOException | FormatException e) {
            Log.e("FragmentScan", "Error processing tag: " + e.getMessage(), e);
            statusViewModel.setStatusText("Error processing tag: " + e.getMessage());
            return;
        }

        ((MainActivity) getContext()).runOnMainThread(() -> {
            Bundle bundle = new Bundle();
            bundle.putString("MSG", getString(R.string.tag_deleted));
            bundle.putString("TARGET", "initTag");
//            bundle.putString("DATA", "EMPTY");
            NavHostFragment.findNavController(FragmentClearTag.this).navigate(R.id.action_fragmentClearTag_to_fragmentConfirm, bundle);
        });
        statusViewModel.setStatusText("Navigating to Registration");

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