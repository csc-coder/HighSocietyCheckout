package com.kbiz.highsocietycheckout.fragments;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.kbiz.highsocietycheckout.R;
import com.kbiz.highsocietycheckout.data.DataViewModel;
import com.kbiz.highsocietycheckout.data.adapters.HarvestAdapter;
import com.kbiz.highsocietycheckout.data.adapters.UserAdapter;
import com.kbiz.highsocietycheckout.data.dao.HarvestDAO;
import com.kbiz.highsocietycheckout.data.dao.UserDAO;
import com.kbiz.highsocietycheckout.data.entities.Harvest;
import com.kbiz.highsocietycheckout.data.entities.User;
import com.kbiz.highsocietycheckout.database.AppDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class FragmentDBManager extends Fragment {

    private RecyclerView usersRecyclerView;
    private RecyclerView harvestsRecyclerView;
    private UserAdapter userAdapter;
    private HarvestAdapter harvestAdapter;
    private DataViewModel mainViewModel;
    private UserDAO userDao;
    private HarvestDAO harvestDao;

    public FragmentDBManager() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_db_manager, container, false);
        FloatingActionButton fabBackup = view.findViewById(R.id.fab_backup);
        Button btnClearUserTable = view.findViewById(R.id.btnClearUserTable);
        Button btnClearHarvestsTable = view.findViewById(R.id.btnClearHarvestsTable);

        usersRecyclerView = view.findViewById(R.id.users_list);
        harvestsRecyclerView = view.findViewById(R.id.harvests_list);

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        harvestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userAdapter = new UserAdapter(new ArrayList<>());
        harvestAdapter = new HarvestAdapter(new ArrayList<>());

        usersRecyclerView.setAdapter(userAdapter);
        harvestsRecyclerView.setAdapter(harvestAdapter);

        AppDatabase db = AppDatabase.getDatabase(getContext());
        userDao = db.userDAO();
        harvestDao = db.harvestDAO();
        mainViewModel = new ViewModelProvider(this).get(DataViewModel.class);

        mainViewModel.getAllUsers().observe(getViewLifecycleOwner(), users -> userAdapter.setUsers(users));

        mainViewModel.getAllHarvests().observe(getViewLifecycleOwner(), harvests -> harvestAdapter.setHarvests(harvests));

        fabBackup.setOnClickListener(view12 -> backupDatabase());

        btnClearUserTable.setOnClickListener(view1 -> {
            showConfirmDialog((dialog, which) -> mainViewModel.clearAllUsers(),"Really? Are you sure? To ERASE the user table?");
        });
        btnClearHarvestsTable.setOnClickListener(view1 -> {
            showConfirmDialog((dialog, which) -> mainViewModel.clearAllHarvests(),"Really? Are you sure? To ERASE the user table?");
        });
        return view;
    }
    private void backupDatabase() {
        new Thread(() -> {
            Context context = getContext();
            if (context == null) {
                return;
            }

            // Fetch data

            List<User> users = userAdapter.getUsers();
            List<Harvest> harvests = harvestAdapter.getHarvests();

            // Convert to JSON
            String jsonString = convertDataToJson(users, harvests);

            // Write JSON to file
            try {
                File backupFile = writeJsonToFile(jsonString);
                if (backupFile != null) {
                    // Send the JSON file via email
                    sendBackupEmail(backupFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void showConfirmDialog(DialogInterface.OnClickListener taskOnYesClick, String message) {
        new AlertDialog.Builder(requireContext()).setTitle("Confirmation").setMessage(message).setPositiveButton("Yes", taskOnYesClick).setNegativeButton("No", null).show();
    }

    private String convertDataToJson(List<User> users, List<Harvest> harvests) {
        Gson gson = new Gson();
        Map<String, Object> data = new HashMap<>();
        data.put("users", users);
        data.put("harvests", harvests);
        return gson.toJson(data);
    }
    private File writeJsonToFile(String jsonString) throws IOException {
        Context context = getContext();
        if (context == null) {
            return null;
        }

        File backupDir = new File(context.getExternalFilesDir(null), "backup");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        File backupFile = new File(backupDir, "harvest_backup.json");

        try (FileWriter writer = new FileWriter(backupFile)) {
            writer.write(jsonString);
        }

        return backupFile;
    }

    private void sendBackupEmail(File backupFile) {
        Context context = getContext();
        if (context == null) {
            return;
        }
        Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", backupFile);
        String mimeType = "application/json";
        String[] mimeTypeArray = new String[] { mimeType };
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setType(mimeType);

        // Add the uri as a ClipData
        emailIntent.setClipData(new ClipData(
                "A label describing your file to the user",
                mimeTypeArray,
                new ClipData.Item(contentUri)
        ));

        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"csc.codemaker@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Database Backup");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Attached is the backup of the database.");
        emailIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

        // Grant URI permissions to the email client
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Grant URI permission to all apps that can handle the email intent
        List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(emailIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resolveInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

}