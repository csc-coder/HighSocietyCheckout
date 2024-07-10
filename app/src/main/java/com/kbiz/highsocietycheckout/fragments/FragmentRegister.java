package com.kbiz.highsocietycheckout.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.kbiz.highsocietycheckout.MainActivity;
import com.kbiz.highsocietycheckout.R;
import com.kbiz.highsocietycheckout.data.StatusViewModel;
import com.kbiz.highsocietycheckout.database.DatabaseManager;
import com.kbiz.highsocietycheckout.databinding.FragmentRegisterBinding;
import com.kbiz.highsocietycheckout.nfc.NFCHandler;

public class FragmentRegister extends Fragment {

    public static final String LOK = "LOK_REG";
    public static final String HASH_PREFIX = "HIGH_SOCIETY_";
    private FragmentRegisterBinding binding;
    private StatusViewModel statusViewModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment using view binding
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the click listener for the OK button
        binding.buttonOk.setOnClickListener(v -> {
            // Call a method when the OK button is pressed
            onRegisterNowPressed();
        });

    }

    // Method to handle the logic when the OK button is pressed
    private void onRegisterNowPressed() {
        String id = binding.editTextPersonalID.getText().toString();
        if (id.isEmpty()) {
            id = "SHNDJBN929";
        }

        String dob = binding.editTextDateOfBirth.getText().toString();
        if (dob.isEmpty()) {
            dob = "1970-01-01";
        }

        String pob = binding.editTextPlaceOfBirth.getText().toString();
        if (pob.isEmpty()) {
            pob = "bad-saarow-pieskow";
        }

//        Toast.makeText(getContext(), "Thx! (" + dob + "," + pob + "," + id + ")", Toast.LENGTH_SHORT).show();
        final String regData = dob + "##" + pob + "##" + id;

        final String hash = HASH_PREFIX+NFCHandler.createHash(regData);

        ((MainActivity) getContext()).runOnMainThread(() -> {
            Bundle bundle = new Bundle();
            bundle.putString("regData", hash);
            NavHostFragment.findNavController(FragmentRegister.this).navigate(R.id.action_fragmentRegister_to_fragmentInitializeTag, bundle);
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
    }

}
