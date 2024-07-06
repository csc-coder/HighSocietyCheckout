package com.kbiz.highsocietycheckout;

import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.kbiz.highsocietycheckout.data.TagContent;
import com.kbiz.highsocietycheckout.databinding.FragmentRegisterBinding;
import com.kbiz.highsocietycheckout.lookup.Lookup;

import java.io.IOException;

public class FragmentRegister extends Fragment implements NFCReactor{

    private FragmentRegisterBinding binding;
    private NFCHandler nfcHandler;
    private StatusViewModel statusViewModel;
    private NFCHandler.NfcIntentHandler nfcIntentHandler;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment using view binding
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        nfcHandler = Lookup.get(NFCHandler.class);
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);

        Lookup.get(MainActivity.class).activeFragment=this;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Lookup.get(MainActivity.class).activeFragment=this;
        // Set up the click listener for the OK button
        binding.buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call a method when the OK button is pressed
                onRegisterNowPressed();
            }
        });


        nfcIntentHandler=new NFCHandler.NfcIntentHandler() {
            @Override
            public void onNDEFDiscovered(Tag tag) {
                handleNdefDiscovered(tag);
            }

            @Override
            public void onNDEFlessDiscovered(Tag tag) {
                statusViewModel.setStatusText("Empty Tag discovered");
                NavHostFragment.findNavController(FragmentRegister.this).navigate(R.id.action_fragmentScan_to_fragmentRegister);
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
    // Method to handle the logic when the OK button is pressed
    private void onRegisterNowPressed() {
        String id=binding.editTextPersonalID.getText().toString();
        if(id.isEmpty()){
            id="SHNDJBN929";
        }

        String dob=binding.editTextDateOfBirth.getText().toString();
        if(dob.isEmpty()){
            dob="1970-01-01";
        }

        String pob=binding.editTextPlaceOfBirth.getText().toString();
        if(pob.isEmpty()){
            pob="bad-saarow-pieskow";
        }

        Toast.makeText(getContext(), "Thx! ("+dob+","+pob+","+id+") please attach tag again.", Toast.LENGTH_SHORT).show();

        Lookup.get(MainActivity.class).runOnMainThread(() -> {
            NavHostFragment.findNavController(FragmentRegister.this).navigate(R.id.action_fragmentRegister_to_fragmentInitializeTag);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear references to avoid memory leaks
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Lookup.get(MainActivity.class).activeFragment=this;
        nfcHandler.showNFCEnablementStatusTexts();
        if (nfcHandler.isNfcSupported() && nfcHandler.isNfcEnabled()) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            nfcHandler.enableReaderMode(activity);
        } else {
            statusViewModel.setStatusText(getString(R.string.nfc_is_not_supported_on_this_device));
        }
    }

    public void handleNFCIntent(Intent intent) {
        nfcHandler.handleIntent(intent, nfcIntentHandler);
    }

    @Override
    public NFCHandler.NfcIntentHandler getNFCIntentHandler() {
        return nfcIntentHandler;
    }

}
