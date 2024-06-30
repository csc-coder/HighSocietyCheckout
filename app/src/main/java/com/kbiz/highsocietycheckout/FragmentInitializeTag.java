package com.kbiz.highsocietycheckout;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kbiz.highsocietycheckout.lookup.Lookup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentInitializeTag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentInitializeTag extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private NFCHandler nfcHandler;
    private StatusViewModel statusViewModel;

    public FragmentInitializeTag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.nfcHandler = Lookup.get(NFCHandler.class);
        statusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcHandler.isNfcSupported() && nfcHandler.isNfcEnabled()) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            nfcHandler.enableForegroundDispatch(activity);
        } else {
            this.statusViewModel.setStatusText((String) getContext().getResources().getText(R.string.nfc_is_not_supported_on_this_device));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_initialize_tag, container, false);
    }
}