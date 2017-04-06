package xyz.digzdigital.cunavigator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import xyz.digzdigital.cunavigator.google.OnlineMapsActivity;
import xyz.digzdigital.cunavigator.graphhopper.NavigationActivity;

public class MainActivity extends AppCompatActivity {
    private static ProgressDialog progressDialog;
    private static File mapsFolder;
    private static SharedPreferences preferences;
    private static AssetManager assetManager;
    private static Context context;
    private Thread unzipThread;

    private static void dismissProgressDialog() {
        progressDialog.dismiss();
    }

    public static void setRunned() {
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean("firstRun", false);
        edit.commit();
    }

    private static void copyAssets() {
        String[] files = null;
        try {
            files = assetManager.list("Files");
        } catch (IOException e) {
            Log.e("Esther", e.getMessage());
            return;
        }

        for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open("Files/" + filename);
                out = new FileOutputStream(mapsFolder + "/" + filename);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (IOException e) {
                Log.e("Esther", e.getMessage());
            }
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
    }

    private static void unzipFile() {
        String targetLocation = mapsFolder + "/CUMAP-gh.zip";
        try {
            FileInputStream fileInputStream = new FileInputStream(targetLocation);
            ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
            ZipEntry zipEntry = null;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                showProgressDialog("Initialising map data");
                if (zipEntry.isDirectory()) {
                    dirChecker();
                } else {
                    FileOutputStream fileOutputStream = new FileOutputStream(mapsFolder + "/" + zipEntry.getName());
                    for (int c = zipInputStream.read(); c != -1; c = zipInputStream.read()) {
                        fileOutputStream.write(c);
                    }
                    zipInputStream.closeEntry();
                    fileOutputStream.close();
                }
            }
            zipInputStream.close();
            setRunned();
            dismissProgressDialog();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void dirChecker() {
        File f = new File(mapsFolder + "/CUMAP-gh");
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        assetManager = getAssets();
        context = this;
        boolean greaterOrEqKitkat = Build.VERSION.SDK_INT >= 19;
        if (greaterOrEqKitkat) {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                logUser("CUMAP navigator is not usable without an external storage!");
                return;
            }
            mapsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "/graphhopper/maps/CUMAP-gh/");
        } else
            mapsFolder = new File(Environment.getExternalStorageDirectory(), "/graphhopper/maps/CUMAP-gh/");

        if (!mapsFolder.exists())
            mapsFolder.mkdirs();


        Button offlineMap = (Button) findViewById(R.id.offlineMap);
        Button onlineMap = (Button) findViewById(R.id.onlineMap);
        offlineMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NavigationActivity.class));
            }
        });

        onlineMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, OnlineMapsActivity.class));
            }
        });

        unzipThread = (Thread) getLastNonConfigurationInstance();
        if (unzipThread != null && unzipThread.isAlive()) {
            showProgressDialog("Initialising map data");
        } else {
            preferences = getSharedPreferences("cunavigator", Context.MODE_PRIVATE);
            if (getFirstRun()) {
                showProgressDialog("Initialising map data");
                copyAssetsAndUnzip();
            }
        }
    }

    private static void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setTitle("Please wait");
        progressDialog.show();
    }

    private void copyAssetsAndUnzip() {
unzipThread = new MyThread();
        unzipThread.start();
    }

    public boolean getFirstRun() {
        return preferences.getBoolean("firstRun", true);
    }

    private void logUser(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return unzipThread;
    }

    static public class MyThread extends Thread {

        @Override
        public void run() {
            copyAssets();
            // unzipFile();
            setRunned();
            dismissProgressDialog();
        }
    }
}
