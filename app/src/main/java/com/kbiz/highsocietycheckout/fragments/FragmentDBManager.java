package com.kbiz.highsocietycheckout.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kbiz.highsocietycheckout.R;
import com.kbiz.highsocietycheckout.data.DataViewModel;
import com.kbiz.highsocietycheckout.data.adapters.HarvestAdapter;
import com.kbiz.highsocietycheckout.data.adapters.UserAdapter;
import com.kbiz.highsocietycheckout.data.entities.Harvest;
import com.kbiz.highsocietycheckout.data.entities.User;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

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

        usersRecyclerView = view.findViewById(R.id.users_list);
        harvestsRecyclerView = view.findViewById(R.id.harvests_list);

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        harvestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userAdapter = new UserAdapter(new ArrayList<>());
        harvestAdapter = new HarvestAdapter(new ArrayList<>());

        usersRecyclerView.setAdapter(userAdapter);
        harvestsRecyclerView.setAdapter(harvestAdapter);

        mainViewModel = new ViewModelProvider(this).get(DataViewModel.class);

        mainViewModel.getAllUsers().observe(getViewLifecycleOwner(), users -> userAdapter.setUsers(users));

        mainViewModel.getAllHarvests().observe(getViewLifecycleOwner(), harvests -> harvestAdapter.setHarvests(harvests));

        return view;
    }

    private void backupDatabase() {
        Context context = getContext();
        if (context == null) {
            return;
        }

        File dbFile = context.getDatabasePath("harvest.db");
        File backupDir = new File(context.getExternalFilesDir(null), "backup");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        File backupFile = new File(backupDir, "harvest_backup.db");

        try (FileChannel src = new FileInputStream(dbFile).getChannel();
             FileChannel dst = new FileOutputStream(backupFile).getChannel()) {
            dst.transferFrom(src, 0, src.size());
            sendBackupEmail(backupFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendBackupEmail(File backupFile) {
        Context context = getContext();
        if (context == null) {
            return;
        }

        Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", backupFile);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("vnd.android.cursor.dir/email");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Database Backup");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Attached is the backup of the database.");
        emailIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }
}