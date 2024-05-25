package com.kbiz.highsocietycheckout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.kbiz.highsocietycheckout.databinding.FragmentRegisterBinding;

public class FragmentRegister extends Fragment {

    private FragmentRegisterBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment using view binding
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
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
                onOkPressed();
            }
        });
    }

    // Method to handle the logic when the OK button is pressed
    private void onOkPressed() {
        // You can add your registration logic here
        // For demonstration, showing a toast message
        Toast.makeText(getContext(), "OK button pressed. Implement registration logic here.", Toast.LENGTH_SHORT).show();

        // Example of navigating to another fragment if needed
        // NavHostFragment.findNavController(FragmentRegister.this)
        //        .navigate(R.id.action_fragmentRegister_to_nextFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear references to avoid memory leaks
        binding = null;
    }
}
