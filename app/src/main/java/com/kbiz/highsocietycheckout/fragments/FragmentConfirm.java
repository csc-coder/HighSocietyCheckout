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

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentConfirm extends Fragment {    
    public static final String LOK = "LOK_CONFIRM";

    private String message;
    private String target;
    private String data;
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
            data = getArguments().getString("DATA");
            Log.d(LOK, "got arg TARGET/MSG: " + target + "/" + message+"/"+data);
        }

        Log.d(LOK, "setted confirm msg: " + message);


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

        view.findViewById(R.id.buttonRegOk).setOnClickListener(v -> {
                    ((MainActivity) getContext()).runOnMainThread(() -> {
                        if (this.target.equals("harvest")) {
                            Log.d(LOK, "nav to '"+target+"' with msg: " + message + "'");
                            Bundle bundle = new Bundle();
                            bundle.putString("USER_HASH", data);
                            NavHostFragment.findNavController(FragmentConfirm.this).navigate(R.id.action_fragmentConfirm_to_fragmentHarvest, bundle);
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