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
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.kbiz.highsocietycheckout.MainActivity;
import com.kbiz.highsocietycheckout.R;
import com.kbiz.highsocietycheckout.data.HarvestViewModel;
import com.kbiz.highsocietycheckout.data.StatusViewModel;
import com.kbiz.highsocietycheckout.data.dao.HarvestDAO_Impl;
import com.kbiz.highsocietycheckout.database.DatabaseManager;
import com.kbiz.highsocietycheckout.databinding.FragmentHarvestBinding;
import com.kbiz.highsocietycheckout.nfc.NFCHandler;
import com.kbiz.highsocietycheckout.nfc.NFCReactor;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHarvest extends Fragment implements NFCReactor {
    public static final String LOK = "LOK_HARVEST";

    private HarvestViewModel amount;
    private static final int maxAmount = 50;

    private NFCHandler nfcHandler;
    private FragmentHarvestBinding binding;

    private StatusViewModel statusViewModel;
    private NFCHandler.NfcIntentHandler nfcIntentHandler;
    private HarvestViewModel harvestViewModel;
    private DatabaseManager database;

    public FragmentHarvest() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcHandler = NFCHandler.getInstance();
        amount = new ViewModelProvider(requireActivity()).get(HarvestViewModel.class);
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);
        database= DatabaseManager.getInstance();

        //dont read now. activate again when harvest btn was clicked
        nfcHandler.disableReaderMode();

        nfcIntentHandler=new NFCHandler.NfcIntentHandler() {
            @Override
            public void onNDEFDiscovered(Tag tag) {
                handleNdefDiscovered(tag);
            }

            @Override
            public void onNDEFlessDiscovered(Tag tag) {
//                statusViewModel.setStatusText("Harvst:Empty Tag discovered");
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

            ArrayList<String> recs = nfcHandler.extractTextRecordsFromNdefMessage(ndefMessage);

            if (ndefMessage == null || recs.isEmpty()) {
                statusViewModel.setStatusText("Error processing tag. Please re-init." );

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
        harvestViewModel = new ViewModelProvider(this).get(HarvestViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_harvest, container, false);
        binding.setHarvestModel(harvestViewModel);
        binding.setLifecycleOwner(this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the click listener for the OK button
        binding.button1g.setOnClickListener(v -> handleAmountBtn(1));
        binding.button5g.setOnClickListener(v -> handleAmountBtn(5));
        binding.button10g.setOnClickListener(v -> handleAmountBtn(10));
        binding.button25g.setOnClickListener(v -> handleAmountBtn(25));
        binding.buttonHarvest.setOnClickListener(v -> handleHarvestBtn());
        binding.buttonReset.setOnClickListener(v -> handleResetBtn());
    }

    private void handleResetBtn() {
        this.amount.setHarvestAmount(0);
        this.amount.setAvailAmount(0);
    }

    private void handleHarvestBtn() {
        //Write harvest to db
        nfcHandler.enableReaderMode();
    }

    private void handleAmountBtn(int amountToAdd) {
        int newAmount=this.amount.getHarvestAmount().getValue()+amountToAdd;
        this.amount.setHarvestAmount( fixNewAmount(newAmount));
    }

    private int fixNewAmount(int newAmount){
        if(newAmount > maxAmount){
            return maxAmount;
        }
        if(newAmount<0){
            return 0;
        }
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