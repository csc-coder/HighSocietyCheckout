package com.kbiz.highsocietycheckout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import com.kbiz.highsocietycheckout.data.StatusViewModel;
import com.kbiz.highsocietycheckout.data.dao.HarvestDAO;
import com.kbiz.highsocietycheckout.database.AppDatabase;
import com.kbiz.highsocietycheckout.database.DatabaseHelper;
import com.kbiz.highsocietycheckout.database.DatabaseManager;
import com.kbiz.highsocietycheckout.databinding.ActivityMainBinding;
import com.kbiz.highsocietycheckout.fragments.FragmentConfirm;
import com.kbiz.highsocietycheckout.fragments.FragmentStatusBar;
import com.kbiz.highsocietycheckout.nfc.NFCHandler;
import com.kbiz.highsocietycheckout.nfc.NFCReactor;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "LOK_MAIN_ACTVT";
    private StatusViewModel statusViewModel;

    private ActivityMainBinding binding;

    private NFCHandler nfcHandler;

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private Toolbar toolbar;
    private AppDatabase db;
    private HarvestDAO harvestDAO;
    private DatabaseManager dbManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        statusViewModel = new ViewModelProvider(this).get(StatusViewModel.class);
        nfcHandler = NFCHandler.getInstance(this, statusViewModel);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the home button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home); // Set your home icon
        }

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        dbManager=DatabaseManager.getInstance(this);
        dbManager.open();

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "harvest.db")
                .fallbackToDestructiveMigration()
                .build();
        harvestDAO = db.harvestDAO();

        // Update the check and create table logic if needed
        checkAndCreateTables();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handle home button click here
                navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.action_global_fragmentScan);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void checkAndCreateTables() {
        // Check and create users table
        checkAndCreateTable(DatabaseHelper.TABLE_USERS, DatabaseHelper.TABLE_CREATE_USERS, new String[]{
                DatabaseHelper.COLUMN_USER_HASH
        });

        // Check and create harvests table
        checkAndCreateTable(DatabaseHelper.TABLE_HARVESTS, DatabaseHelper.TABLE_CREATE_HARVESTS, new String[]{
                DatabaseHelper.COLUMN_HARVEST_ID,
                DatabaseHelper.COLUMN_USER_HASH,
                DatabaseHelper.COLUMN_TIME,
                DatabaseHelper.COLUMN_AMOUNT
        });
    }

    private void checkAndCreateTable(String tableName, String createTableQuery, String[] expectedColumns) {
        SQLiteDatabase db = dbManager.getWritableDatabase();
        if (!dbManager.getDatabaseHelper().tableExists(db, tableName)) {
            db.execSQL(createTableQuery);
            Log.d(TAG, tableName + " table created.");
            return;
        }

        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        if (cursor != null) {
            try {
                // Store actual columns in a set for easy comparison
                Set<String> actualColumns = new HashSet<>();
                while (cursor.moveToNext()) {
                    int nameIdx = cursor.getColumnIndex("name");
                    actualColumns.add(cursor.getString(nameIdx));
                }

                // Check if all expected columns are present
                boolean columnsMatch = true;
                for (String expectedColumn : expectedColumns) {
                    if (!actualColumns.contains(expectedColumn)) {
                        columnsMatch = false;
                        break;
                    }
                }

                // If columns do not match, drop and recreate the table
                if (!columnsMatch) {
                    db.execSQL("DROP TABLE IF EXISTS " + tableName);
                    db.execSQL(createTableQuery);
                    Log.d(TAG, tableName + " table recreated due to schema mismatch.");
                }
            } finally {
                cursor.close();
            }
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Enable foreground dispatch and reader mode in onResumeFragments to ensure proper state
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        // Enable foreground dispatch
        try{
            nfcHandler.enableForegroundDispatch();
        } catch (IllegalStateException e){
            /*NOOP*/
            Log.d("LOK_MAINACT","snag with nfc foregrounddispatchDisablement. can be ignored");
        }
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
