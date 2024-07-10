package com.kbiz.highsocietycheckout.database;
import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class FileHelper {
    private final Context context;

    public FileHelper(Context context){
        this.context=context;
    }

    @SuppressLint("Range")
    public static void exportData(Context context, String fileName, DatabaseManager dbManager) throws IOException, JSONException {
        JSONObject data = new JSONObject();
        JSONArray users = new JSONArray();
        JSONArray harvests = new JSONArray();

        // Export users
        Cursor userCursor = dbManager.getUsersCursor();
        if (userCursor.moveToFirst()) {
            do {
                JSONObject user = new JSONObject();
                user.put(DatabaseHelper.COLUMN_USER_HASH, userCursor.getString(userCursor.getColumnIndex(DatabaseHelper.COLUMN_USER_HASH)));
                users.put(user);
            } while (userCursor.moveToNext());
        }
        userCursor.close();

        // Export harvests
        Cursor harvestCursor = dbManager.getHarvestsCursor();
        if (harvestCursor.moveToFirst()) {
            do {
                JSONObject harvest = new JSONObject();
                harvest.put(DatabaseHelper.COLUMN_HARVEST_ID, harvestCursor.getString(harvestCursor.getColumnIndex(DatabaseHelper.COLUMN_HARVEST_ID)));
                harvest.put(DatabaseHelper.COLUMN_USER_HASH, harvestCursor.getString(harvestCursor.getColumnIndex(DatabaseHelper.COLUMN_USER_HASH)));
                harvest.put(DatabaseHelper.COLUMN_TIME, harvestCursor.getLong(harvestCursor.getColumnIndex(DatabaseHelper.COLUMN_TIME)));
                harvest.put(DatabaseHelper.COLUMN_AMOUNT, harvestCursor.getDouble(harvestCursor.getColumnIndex(DatabaseHelper.COLUMN_AMOUNT)));
                harvests.put(harvest);
            } while (harvestCursor.moveToNext());
        }
        harvestCursor.close();

        data.put("users", users);
        data.put("harvests", harvests);

        FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        fos.write(data.toString().getBytes());
        fos.close();
    }

    private void sendEmailWithAttachment(String fileName) {
        File file;
        file = new File(getFilesDir(), fileName);
        if (file.exists()) {
            Uri path = Uri.fromFile(file);
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("application/json");
            String[] to = {"recipient@example.com"};
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_STREAM, path);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Backup Data");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Please find the backup data attached.");
            startActivity(context,Intent.createChooser(emailIntent, "Send email..."),null);
        } else {
            Log.e("LOK", "File not found: " + fileName);
        }
    }

    private File getFilesDir() {
        return context.getFilesDir();
    }
}

