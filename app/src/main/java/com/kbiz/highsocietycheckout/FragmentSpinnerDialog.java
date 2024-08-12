package com.kbiz.highsocietycheckout;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentSpinnerDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSpinnerDialog extends DialogFragment {

    public static FragmentSpinnerDialog newInstance() {
        return new FragmentSpinnerDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_spinner_dialog, container, false);
    }
}