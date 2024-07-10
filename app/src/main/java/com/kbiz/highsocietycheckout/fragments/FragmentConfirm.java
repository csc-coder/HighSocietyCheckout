package com.kbiz.highsocietycheckout.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kbiz.highsocietycheckout.MainActivity;
import com.kbiz.highsocietycheckout.R;
import com.kbiz.highsocietycheckout.data.StatusViewModel;
import com.kbiz.highsocietycheckout.databinding.FragmentConfirmBinding;
import com.kbiz.highsocietycheckout.databinding.FragmentInitializeTagBinding;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentConfirm extends Fragment {
    private String message;
    private String target;
    private FragmentConfirmBinding binding;
    private StatusViewModel statusViewModel;

    public FragmentConfirm() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);

        if (getArguments() != null) {
            message = getArguments().getString("MSG");
            target = getArguments().getString("TARGET");
            Log.d("LOK_CONFIRM", "got arg TARGET/MSG: " + target + "/" + message);
        }

        Log.d("LOK_CONFIRM", "setted confirm msg: " + message);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentConfirmBinding.inflate(inflater, container, false);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_confirm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.buttonOk).setOnClickListener(v -> {
                    ((MainActivity) getContext()).runOnMainThread(() -> {
                        if (this.target.equals("harvest")) {
                            Log.d("LOK_CONFIRM", "nav to harvest/with msg: " + target + "/'" + message + "'");
                            NavHostFragment.findNavController(FragmentConfirm.this).navigate(R.id.action_fragmentConfirm_to_fragmentHarvest);
                        } else {
                            statusViewModel.setStatusText("unknown nav target in FragConfirm:" + R.id.action_fragmentConfirm_to_fragmentHarvest);
                        }
                    });
                }
        );

        // Find the TextView by ID and set the text
        TextView txtMsg = view.findViewById(R.id.txtMsg);
        txtMsg.setText(this.message);
    }
}