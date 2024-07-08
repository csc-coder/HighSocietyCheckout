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
import com.kbiz.highsocietycheckout.data.TagContent;
import com.kbiz.highsocietycheckout.databinding.FragmentInitializeTagBinding;
import com.kbiz.highsocietycheckout.nfc.NFCHandler;
import com.kbiz.highsocietycheckout.nfc.NFCReactor;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentInitializeTag extends Fragment implements NFCReactor {


    // TODO: Rename and change types of parameters
    private NFCHandler nfcHandler;
    private FragmentInitializeTagBinding binding;

    private StatusViewModel statusViewModel;
    private NFCHandler.NfcIntentHandler nfcIntentHandler;
    private String userData;


    public FragmentInitializeTag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcHandler = NFCHandler.getInstance();
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);
        nfcIntentHandler=new NFCHandler.NfcIntentHandler() {
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
        // Retrieve arguments
        if (getArguments() != null) {
            userData = getArguments().getString("regData");
            // Use regData as needed
        }
        // Inflate the layout for this fragment using view binding
        binding = FragmentInitializeTagBinding.inflate(inflater, container, false);
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);

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
            nfcHandler.formatTagNDEF(tag, userData);
        } catch (IOException e) {
            Log.e("LOK_ERR", e.getMessage()+"\n\n");
            e.printStackTrace();
            statusViewModel.setStatusText("Error: "+e.getMessage());
        } catch (FormatException e) {
            Log.e("LOK_ERR", e.getMessage()+"\n\n");
            e.printStackTrace();
            statusViewModel.setStatusText("Error: "+e.getMessage());
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

            TagContent tagContent = new ViewModelProvider(requireActivity()).get(TagContent.class);
            tagContent.setnDefRecords(nfcHandler.extractTextRecordsFromNdefMessage(ndefMessage));

            if (ndefMessage == null || tagContent.isEmpty()) {
                NdefRecord hash=NFCHandler.createHashRecord(this.userData);
                ndefMessage = new NdefMessage(hash);
                nfcHandler.writeNdefMessage(tag,ndefMessage);

                Log.d("LOK", "hash was written to tag: "+hash);

                ((MainActivity) getContext()).runOnMainThread(() -> {
                    NavHostFragment.findNavController(FragmentInitializeTag.this).navigate(R.id.action_fragmentInitializeTag_to_fragmentHarvest);
                });
            } else{
                Log.d("LOK", "tag initialized. moving to harvest");

                ((MainActivity) getContext()).runOnMainThread(() -> {
                    NavHostFragment.findNavController(FragmentInitializeTag.this).navigate(R.id.action_fragmentInitializeTag_to_fragmentHarvest);
                });
            }
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