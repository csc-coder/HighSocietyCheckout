package com.kbiz.highsocietycheckout;

import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
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

import com.kbiz.highsocietycheckout.data.TagContent;
import com.kbiz.highsocietycheckout.lookup.Lookup;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentInitializeTag extends Fragment implements NFCReactor {


    // TODO: Rename and change types of parameters
    private NFCHandler nfcHandler;
    private StatusViewModel statusViewModel;
    private NFCHandler.NfcIntentHandler nfcIntentHandler;


    public FragmentInitializeTag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcHandler = Lookup.get(NFCHandler.class);
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);

        nfcIntentHandler=new NFCHandler.NfcIntentHandler() {
            @Override
            public void onNDEFDiscovered(Tag tag) {
                handleNdefDiscovered(tag);
            }

            @Override
            public void onNDEFlessDiscovered(Tag tag) {
                statusViewModel.setStatusText("InitTag:Empty Tag discovered");
//                NavHostFragment.findNavController(FragmentInitializeTag.this).navigate(R.id.action_fragmentInitializeTag_to_fragmentConfirm);
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
        if (nfcHandler.isNfcSupported() && nfcHandler.isNfcEnabled()) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
//            nfcHandler.enableForegroundDispatch(activity);
        } else {
            this.statusViewModel.setStatusText((String) getContext().getResources().getText(R.string.nfc_is_not_supported_on_this_device));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_initialize_tag, container, false);
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