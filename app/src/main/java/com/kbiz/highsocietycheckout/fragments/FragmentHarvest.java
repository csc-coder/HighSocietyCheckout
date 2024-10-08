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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.gson.Gson;
import com.kbiz.highsocietycheckout.MainActivity;
import com.kbiz.highsocietycheckout.R;
import com.kbiz.highsocietycheckout.data.HarvestViewModel;
import com.kbiz.highsocietycheckout.data.StatusViewModel;
import com.kbiz.highsocietycheckout.data.dao.HarvestDAO;
import com.kbiz.highsocietycheckout.data.dao.UserDAO;
import com.kbiz.highsocietycheckout.data.entities.Harvest;
import com.kbiz.highsocietycheckout.data.entities.User;
import com.kbiz.highsocietycheckout.database.AppDatabase;
import com.kbiz.highsocietycheckout.database.DatabaseManager;
import com.kbiz.highsocietycheckout.databinding.FragmentHarvestBinding;
import com.kbiz.highsocietycheckout.nfc.NFCHandler;
import com.kbiz.highsocietycheckout.nfc.NFCReactor;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * TODO fix harvest limits check and enforcement
 */
public class FragmentHarvest extends Fragment implements NFCReactor {
    public static final String LOK = "LOK_HARVEST";

    private static final Integer MAX_MONTHLY_HARVEST_AMOUNT = 50;

    private NFCHandler nfcHandler;
    private FragmentHarvestBinding binding;

    private StatusViewModel statusViewModel;
    private NFCHandler.NfcIntentHandler nfcIntentHandler;
    private HarvestViewModel harvestViewModel;
    private DatabaseManager database;
    private UserDAO userDAO;
    private HarvestDAO harvestDAO;
    private String userHash;

    public FragmentHarvest() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcHandler = NFCHandler.getInstance();
        //dont read now. activate again when harvest btn was clicked
        nfcHandler.disableReaderMode();

        harvestViewModel = new ViewModelProvider(requireActivity()).get(HarvestViewModel.class);
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);

        database = DatabaseManager.getInstance();
        AppDatabase db = AppDatabase.getDatabase(getContext().getApplicationContext());
        userDAO = db.userDAO();
        harvestDAO = db.harvestDAO();


        //fetch args, check hash validity and existence in db and aggregate this months avail amount 
        if (getArguments() == null) {
            Log.d(LOK + "_ERR", "expected param USER_HASH but no params given");
            throw new RuntimeException("expected param USER_HASH but no params given");
        }
        userHash = getArguments().getString("USER_HASH");
        Log.d(LOK, "got userHash via frag param: " + userHash);

        Log.d(LOK, "checking db for hash and calculating this months available amount: " + userHash);
        if (!database.userHashExists(userHash)) {
            statusViewModel.setStatusText("user hash not found in database. Please try again or clear tag and reinit.");
            Toast.makeText(getContext(), "Unknown user. Please retry or clear tag and reinit", Toast.LENGTH_SHORT).show();
            ((MainActivity) getContext()).runOnMainThread(() -> NavHostFragment.findNavController(this).navigate(R.id.action_fragmentHarvest_to_fragmentScan));
        }

        statusViewModel.setStatusText("user hash found :D we can go harvesting.");

        nfcIntentHandler = new NFCHandler.NfcIntentHandler() {
            @Override
            public void onNDEFDiscovered(Tag tag) {
                confirmHarvestWithTag(tag);
            }

            @Override
            public void onNDEFlessDiscovered(Tag tag) {
                statusViewModel.setStatusText("Harvest:Empty Tag discovered. Please try again.");
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

    private void confirmHarvestWithTag(Tag tag) {
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
                statusViewModel.setStatusText("Error processing tag. Please re-init.");

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment using view binding
        binding = FragmentHarvestBinding.inflate(inflater, container, false);
        harvestViewModel = new ViewModelProvider(this).get(HarvestViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_harvest, container, false);
        binding.setHarvestModel(harvestViewModel);
        binding.setLifecycleOwner(this);
        harvestDAO.getTotalHarvestForCurrentMonth(userHash).observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long totalHarvest) {
                Log.d(LOK, "found total harvested weed this month: " + totalHarvest);

                long remaining = MAX_MONTHLY_HARVEST_AMOUNT - totalHarvest;
                remaining = Math.max(0, Math.min(remaining, 50));//keep remaining amount between 0 and MAX
                harvestViewModel.setAvailAmount(Math.toIntExact(remaining));
            }
        });
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

        //aggregate avail amount by this months harvest records
        ((MainActivity) getContext()).runOnMainThread(() -> {
            harvestDAO.getTotalHarvestForCurrentMonth(userHash).observe(getViewLifecycleOwner(), new Observer<Long>() {
                @Override
                public void onChanged(Long totalHarvest) {
                    long availAmount = 50 - totalHarvest;
                    harvestViewModel.setAvailAmount(Math.toIntExact(availAmount));
                    Log.d(LOK, "aggregated avail  amount this month: " + availAmount);
                }
            });
        });

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
                    activity.runOnMainThread(() -> NavHostFragment.findNavController(FragmentHarvest.this).navigate(R.id.action_fragmentHarvest_to_fragmentClearTag));
                    return true;
                } else if (itemId == R.id.action_db_manager) {// Handle about action
                    activity.runOnMainThread(() -> NavHostFragment.findNavController(FragmentHarvest.this).navigate(R.id.action_fragmentHarvest_to_fragmentDBManager));
                    return true;
                } else if (itemId == R.id.action_show_logs) {// Handle about action
                    activity.runOnMainThread(() -> NavHostFragment.findNavController(FragmentHarvest.this).navigate(R.id.action_fragmentHarvest_to_fragmentShowLogs));
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void handleResetBtn() {
        ((MainActivity) getContext()).runOnMainThread(() -> {
            harvestDAO.getTotalHarvestForCurrentMonth(userHash).observe(getViewLifecycleOwner(), totalHarvest -> {
                long availAmount = 50 - totalHarvest;
                Log.d(LOK, "aggregated avail  amount this month: " + availAmount);
                harvestViewModel.setAvailAmount(Math.toIntExact(availAmount));
                harvestViewModel.setHarvestAmount(0);
            });
        });
    }

    private void handleHarvestBtn() {
//        nfcHandler.enableReaderMode();
        long amountToHarvest = harvestViewModel.getHarvestAmount().getValue();
        long time = System.currentTimeMillis();

        Harvest harvest = new Harvest(userHash, time, amountToHarvest);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            harvestDAO.insert(harvest);
        });
        Log.d(LOK, "inserted new Harvest record: " + (new Gson()).toJson(harvest));
        Toast.makeText(getContext(), "Congrats! you got " + amountToHarvest + "g of finest weed. Enjoy :D", Toast.LENGTH_LONG).show();
        ((MainActivity) getContext()).runOnMainThread(() -> NavHostFragment.findNavController(FragmentHarvest.this).navigate(R.id.action_fragmentHarvest_to_fragmentScan));

    }
    private void handleAmountBtn(int amountToAdd) {
        long currentAvailAmount = harvestViewModel.getAvailAmount().getValue();

        // Check if there is enough available amount left
        if (currentAvailAmount <= 0) {
            // No weed available, stop further processing
            return;
        }

        // Calculate new available amount
        long newAvailAmount = currentAvailAmount - amountToAdd;
        newAvailAmount = Math.max(0, Math.min(newAvailAmount, 50));
        this.harvestViewModel.setAvailAmount(Math.toIntExact(newAvailAmount));

        // Calculate new harvest amount
        int harvestAmount = this.harvestViewModel.getHarvestAmount().getValue() + amountToAdd;
        int newAmount = Math.max(0, Math.min(harvestAmount, 50));
        this.harvestViewModel.setHarvestAmount(newAmount);
    }



    public void handleNFCIntent(Intent intent) {
        nfcHandler.handleIntent(intent, nfcIntentHandler);
    }

    @Override
    public NFCHandler.NfcIntentHandler getNFCIntentHandler() {
        return nfcIntentHandler;
    }
}