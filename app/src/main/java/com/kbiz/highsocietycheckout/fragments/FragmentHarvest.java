package com.kbiz.highsocietycheckout.fragments;

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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.kbiz.highsocietycheckout.MainActivity;
import com.kbiz.highsocietycheckout.R;
import com.kbiz.highsocietycheckout.data.StatusViewModel;
import com.kbiz.highsocietycheckout.data.TagContent;
import com.kbiz.highsocietycheckout.databinding.FragmentHarvestBinding;
import com.kbiz.highsocietycheckout.databinding.FragmentRegisterBinding;
import com.kbiz.highsocietycheckout.nfc.NFCHandler;
import com.kbiz.highsocietycheckout.nfc.NFCReactor;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHarvest extends Fragment implements NFCReactor {
    private int amount;
    private static final int maxAmount = 50;

    private NFCHandler nfcHandler;
    private FragmentHarvestBinding binding;

    private StatusViewModel statusViewModel;
    private NFCHandler.NfcIntentHandler nfcIntentHandler;

    public FragmentHarvest() {
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
                statusViewModel.setStatusText("Harvst:Empty Tag discovered");
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
            ndef.close();

            TagContent tagContent = new ViewModelProvider(requireActivity()).get(TagContent.class);
            tagContent.setnDefRecords(nfcHandler.extractTextRecordsFromNdefMessage(ndefMessage));

            if (ndefMessage == null || tagContent.isEmpty()) {
                statusViewModel.setStatusText("Error processing tag. Please re-init tag" );
                Log.d("LOK", "Error processing tag. Please re-init tag");

                ((MainActivity) getContext()).runOnMainThread(() -> {
                    NavHostFragment.findNavController(FragmentHarvest.this).navigate(R.id.action_fragmentHarvest_to_fragmentScan);
                });
            }
        } catch (IOException | FormatException e) {
            Log.e("FragmentScan", "Error processing tag: " + e.getMessage(), e);
            statusViewModel.setStatusText("Error processing tag: " + e.getMessage());
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using view binding
        binding = FragmentHarvestBinding.inflate(inflater, container, false);
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the click listener for the OK button
        binding.button1g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAmountBtn(1);
            }
        });

    }

    private void handleAmountBtn(int amountToAdd) {
        int newAmount=this.amount+amountToAdd;
        newAmount = checkNewAmount(newAmount);

    }

    private int checkNewAmount(int newAmount){

        return newAmount;
    }
    public void handleNFCIntent(Intent intent) {
        nfcHandler.handleIntent(intent, nfcIntentHandler);
    }

    @Override
    public NFCHandler.NfcIntentHandler getNFCIntentHandler() {
        return nfcIntentHandler;
    }
}