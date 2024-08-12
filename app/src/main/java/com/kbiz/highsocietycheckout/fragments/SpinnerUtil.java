package com.kbiz.highsocietycheckout.fragments;

import androidx.fragment.app.FragmentManager;

import com.kbiz.highsocietycheckout.FragmentSpinnerDialog;

public class SpinnerUtil {
    private static final String TAG = "SpinnerDialogFragment";

    public static void showSpinner(FragmentManager fragmentManager) {
        FragmentSpinnerDialog spinnerDialogFragment = FragmentSpinnerDialog.newInstance();
        spinnerDialogFragment.show(fragmentManager, TAG);
    }

    public static void hideSpinner(FragmentManager fragmentManager) {
        FragmentSpinnerDialog spinnerDialogFragment = (FragmentSpinnerDialog) fragmentManager.findFragmentByTag(TAG);
        if (spinnerDialogFragment != null) {
            spinnerDialogFragment.dismiss();
        }
    }
}
