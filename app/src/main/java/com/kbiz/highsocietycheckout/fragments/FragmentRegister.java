package com.kbiz.highsocietycheckout.fragments;

import android.app.DatePickerDialog;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.fragment.NavHostFragment;

import com.kbiz.highsocietycheckout.MainActivity;
import com.kbiz.highsocietycheckout.R;
import com.kbiz.highsocietycheckout.databinding.FragmentRegisterBinding;
import com.kbiz.highsocietycheckout.nfc.NFCHandler;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

public class FragmentRegister extends Fragment {

    private static final Pattern PERSONAL_ID_PATTERN = Pattern.compile("^[A-Z0-9]{9}$");

    public static final String LOK = "LOK_REG";
    public static final String HASH_PREFIX = "HIGH_SOCIETY_";
    private FragmentRegisterBinding binding;
    private View view;
    private Calendar calendar;
    private EditText editTextDate;
    private EditText editTextPersonalID;
    private EditText editTextPlaceOfBirth;
    private Button btnOK;
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        // Inflate the layout for this fragment using view binding
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
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
                    activity.runOnMainThread(
                            () -> NavHostFragment.findNavController(FragmentRegister.this).navigate(R.id.action_fragmentRegister_to_fragmentClearTag));
                    return true;
                } else if (itemId == R.id.action_db_manager) {// Handle about action
                    activity.runOnMainThread(
                            () -> NavHostFragment.findNavController(FragmentRegister.this).navigate(R.id.action_fragmentRegister_to_fragmentDBManager));
                    return true;
                } else if (itemId == R.id.action_show_logs) {// Handle about action
                    activity.runOnMainThread(
                            () -> NavHostFragment.findNavController(FragmentRegister.this).navigate(R.id.action_fragmentRegister_to_fragmentShowLogs));
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        view = inflater.inflate(R.layout.fragment_register, container, false);

        editTextDate = view.findViewById(R.id.editTextDate);
        editTextPersonalID = view.findViewById(R.id.editTextPersonalID);
        editTextPlaceOfBirth = view.findViewById(R.id.editTextPlaceOfBirth);

        calendar = Calendar.getInstance();

        editTextDate.setInputType(InputType.TYPE_NULL);
        editTextDate.setFocusable(false);
        editTextDate.setOnClickListener(v -> {
            showDatePicker();
        });

        // Set up the click listener for the OK button
        btnOK= view.findViewById(R.id.buttonRegOk);

        btnOK.setOnClickListener(v -> {
            // Call a method when the OK button is pressed
            Log.d("LOK", "btn clicked");
            onRegisterNowPressed();
        });

        return view;
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        editTextDate.setText(sdf.format(calendar.getTime()));
        Log.d(LOK, "Date updated: " + sdf.format(calendar.getTime()));
    }

    private void showDatePicker() {
        Log.d(LOK, "showDatePicker called");
        DatePickerFragment newFragment = new DatePickerFragment(new DatePickerFragment.DatePickerListener() {
            @Override
            public void onDateSet(int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                updateLabel();
            }
        });
        newFragment.show(getParentFragmentManager(), "datePicker");
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    // Method to handle the logic when the OK button is pressed
    private void onRegisterNowPressed() {
        String personalID = editTextPersonalID.getText().toString();
        String placeOfBirth = editTextPlaceOfBirth.getText().toString();
        String dateOfBirth = editTextDate.getText().toString();

        if (personalID.isEmpty()) {
            Toast.makeText(getContext(), R.string.please_enter_your_id_personalausweisnummer, Toast.LENGTH_SHORT).show();
            return;
        }

        if (    personalID.length()>15 ||
                ! isValidPersonalAusweisID(personalID)) {
            Toast.makeText(getContext(), R.string.please_enter_a_valid_id, Toast.LENGTH_SHORT).show();
            return;
        }

        if (placeOfBirth.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your place of birth name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dateOfBirth.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your date of birth", Toast.LENGTH_SHORT).show();
            return;
        }

        //make registration string and hash it
        final String regData = dateOfBirth + "##" + placeOfBirth + "##" + personalID;

        final String hash = HASH_PREFIX + NFCHandler.createHash(regData);
        Toast.makeText(getContext(), "Let's init the Tag.", Toast.LENGTH_SHORT).show();

        ((MainActivity) getContext()).runOnMainThread(() -> {
            Bundle bundle = new Bundle();
            bundle.putString("regData", hash);
            NavHostFragment.findNavController(FragmentRegister.this).navigate(R.id.action_fragmentRegister_to_fragmentInitializeTag, bundle);
        });
    }

    public static boolean isValidPersonalAusweisID(String personalID) {
        if (personalID == null || personalID.isEmpty()) {
            return false;
        }

        return PERSONAL_ID_PATTERN.matcher(personalID).matches();
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

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        public interface DatePickerListener {
            void onDateSet(int year, int month, int day);
        }

        private final DatePickerListener listener;

        public DatePickerFragment(DatePickerListener listener) {
            this.listener = listener;
        }
        @Override
        public DatePickerDialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR) - 18; // Default to 18 years ago
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar, this, year, month, day);
            dialog.getDatePicker().setCalendarViewShown(false);
            dialog.getDatePicker().setSpinnersShown(true);

            // Set maximum and minimum date
            dialog.getDatePicker().setMaxDate(c.getTimeInMillis()); // Max date is today
            c.set(Calendar.YEAR, year - 100); // Min date is 100 years ago
            dialog.getDatePicker().setMinDate(c.getTimeInMillis());

            return dialog;
        }


        public void onDateSet(DatePicker view, int year, int month, int day) {
            listener.onDateSet(year, month, day);
        }
    }

}
