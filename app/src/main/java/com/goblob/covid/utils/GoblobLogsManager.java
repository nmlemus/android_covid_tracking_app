package com.goblob.covid.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.goblob.covid.app.CovidApp;
import com.instacart.library.truetime.TrueTime;
import com.parse.ParseUser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class GoblobLogsManager {

    private static final String TAG = GoblobLogsManager.class.getCanonicalName();
    private static GoblobLogsManager instance;
    private final ExecutorService backgroundExecutor;
    private Process process1;
    private Process process2;

    public GoblobLogsManager() {
        backgroundExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "Background executor service");
                thread.setPriority(Thread.MAX_PRIORITY);
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    public static GoblobLogsManager get() {
        if (instance == null) {
            instance = new GoblobLogsManager();
        }
        return instance;
    }

    public void startLogsStore() {
        if (GoblobFileManager.get().isExternalStorageWritable()) {
            File appDirectory = new File(Environment.getExternalStorageDirectory() + "/CovidLogs");
            File logDirectory = new File(appDirectory + "/log");

            Date date;
            if (TrueTime.isInitialized()) {
                date = TrueTime.now();
            } else {
                CovidApp.getInstance().initializeTrueTime();
                date = Calendar.getInstance().getTime();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String utcString = sdf.format(date);

            File logFile = new File(logDirectory, utcString + ".txt");

            // create app folder
            if (!appDirectory.exists()) {
                appDirectory.mkdir();
            }

            // create log folder
            if (!logDirectory.exists()) {
                logDirectory.mkdir();
            } else {
                if (logDirectory.list() != null && logDirectory.list().length > 10) {
                    GoblobFileManager.get().deleteDirectory(logDirectory);
                    if (!logDirectory.exists()) {
                        logDirectory.mkdir();
                    }
                }
            }

            // clear the previous logcat and then write the new one to the file
            try {
                if (process1 != null){
                    process1.destroy();
                }
                if (process2 != null){
                    process2.destroy();
                }
                process1 = Runtime.getRuntime().exec("logcat -c");
                process2 = Runtime.getRuntime().exec("logcat -f " + logFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (GoblobFileManager.get().isExternalStorageReadable()) {
            // only readable
        } else {
            // not accessible
        }
    }

    public void sendLogsByEmail(final Activity activity) {
        GoblobDialogManager.get().showProgressDialog(activity, "Compressing logs to send. This may take a few minutes.");
        runInBackground(new Runnable() {
            @Override
            public void run() {
                File appDirectory = new File(Environment.getExternalStorageDirectory() + "/CovidLogs");
                File logDirectory = new File(appDirectory + "/log");
                File logZip = new File(appDirectory + "/log.zip");

                if (logZip.exists()){
                    logZip.delete();
                }

                boolean success = GoblobFileManager.get().zipFileAtPath(logDirectory.toString(), logZip.toString());

                GoblobDialogManager.get().hideProgressDialog();

                if (success) {
                    try {
                        sendFile(logZip, activity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void contactUsByEmail(final Activity activity) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"edel.moreno@gmail.com"});

        String name = ParseUser.getCurrentUser().getUsername();

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Tell something to us");

        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        activity.startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));
    }

    private void sendFile(File logZip, Activity activity) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"edel.moreno@gmail.com"});

        String name = ParseUser.getCurrentUser().getUsername();

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Logs from " + name);

        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        Uri uri = FileProvider.getUriForFile(activity, "com.goblob.fileProvider", logZip);
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        activity.startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));
    }

    /**
     * Submits request to be executed in background.
     */
    public void runInBackground(final Runnable runnable) {
        backgroundExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }
}
