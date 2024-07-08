package com.kbiz.highsocietycheckout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.kbiz.highsocietycheckout.data.StatusViewModel;
import com.kbiz.highsocietycheckout.databinding.ActivityMainBinding;
import com.kbiz.highsocietycheckout.fragments.FragmentStatusBar;
import com.kbiz.highsocietycheckout.nfc.NFCHandler;
import com.kbiz.highsocietycheckout.nfc.NFCReactor;

public class MainActivity extends AppCompatActivity {
    private StatusViewModel statusViewModel;

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private NFCHandler nfcHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        statusViewModel = new ViewModelProvider(this).get(StatusViewModel.class);
        nfcHandler = NFCHandler.getInstance(this, statusViewModel);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (savedInstanceState == null) {

            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
            if (navHostFragment == null) {
                navHostFragment = NavHostFragment.create(R.navigation.nav_graph);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment_content_main, navHostFragment)
                        .setPrimaryNavigationFragment(navHostFragment)
                        .commit();
            }
        }
        // Load the status bar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.status_bar_container, new FragmentStatusBar())
                .commit();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.NFC}, NFCHandler.REQUEST_CODE_NFC);
        }
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Enable foreground dispatch
        nfcHandler.enableForegroundDispatch();
        // Optionally enable reader mode
        nfcHandler.enableReaderMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Disable foreground dispatch
        nfcHandler.disableForegroundDispatch();
        // Optionally disable reader mode
        nfcHandler.disableReaderMode();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Log the intent action and extras for debugging
        Log.d("LOK", "Received intent with action: " + intent.getAction());
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                Log.d("LOK", String.format("intent details: Extra: %s - Value: %s", key, value));
            }
        }

        Log.d("LOK", "Intent details: " + intent);
        // Check if the intent is an NFC intent
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {

            Fragment fragment;
            // Pass the intent to the fragment
            if (getCurrentFragment() == null) {
                fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
            } else {
                fragment = getCurrentFragment();
            }
            Log.d("LOK", "Intent details: " + fragment.toString());

            if (fragment instanceof NFCReactor) {
                ((NFCReactor) fragment).handleNFCIntent(intent);
            }
        }
    }

    public Fragment getCurrentFragment() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);

        if (navHostFragment != null) {
            return navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
        }
        return null;
    }

    public void runOnMainThread(Runnable action) {
        runOnUiThread(action);
    }

    public void updateStatus(String status) {
        statusViewModel.setStatusText(status);
    }
}
