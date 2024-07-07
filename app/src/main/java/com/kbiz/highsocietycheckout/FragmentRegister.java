package com.kbiz.highsocietycheckout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.kbiz.highsocietycheckout.data.StatusViewModel;
import com.kbiz.highsocietycheckout.databinding.FragmentRegisterBinding;

public class FragmentRegister extends Fragment  {

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
        binding.buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call a method when the OK button is pressed
                onRegisterNowPressed();
            }
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

        Toast.makeText(getContext(), "Thx! (" + dob + "," + pob + "," + id + ") please attach tag again.", Toast.LENGTH_SHORT).show();

        ((MainActivity) getContext()).runOnMainThread(() -> {
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
    }

}
