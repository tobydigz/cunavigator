package xyz.digzdigital.cunavigator.graphhopper;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

import xyz.digzdigital.cunavigator.R;

public class MapDownloadActivity extends AppCompatActivity implements OnSuccessListener<FileDownloadTask.TaskSnapshot>, OnFailureListener {

    private StorageReference storageReference;
    private File mapsFolder;
    private File localFile;

    private FirebaseAuth.AuthStateListener listener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            if (firebaseAuth.getCurrentUser() != null) doStuff();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_download);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(listener);
        auth.signInAnonymously();

        boolean greaterOrEqKitkat = Build.VERSION.SDK_INT >= 19;
        if (greaterOrEqKitkat) {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                logUser("CU campus navigator is not usable without an external storage!");
                return;
            }
            mapsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "/graphhopper/maps/");
        } else
            mapsFolder = new File(Environment.getExternalStorageDirectory(), "/graphhopper/maps/");

        if (!mapsFolder.exists())
            mapsFolder.mkdirs();

    }

    private void doStuff() {
        storageReference = FirebaseStorage.getInstance().getReference().child("maps/CUMAP.ghz");

        try {
            localFile = File.createTempFile("CUMAP", "ghz");
            storageReference.getFile(localFile)
                    .addOnSuccessListener(this)
                    .addOnFailureListener(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If there's a download in progress, save the reference so you can query it later
        if (storageReference != null) {
            outState.putString("reference", storageReference.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // If there was a download in progress, get its reference and create a new StorageReference
        final String stringRef = savedInstanceState.getString("reference");
        if (stringRef == null) {
            return;
        }
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);

        // Find all DownloadTasks under this StorageReference (in this example, there should be one)
        List<FileDownloadTask> tasks = storageReference.getActiveDownloadTasks();
        if (tasks.size() > 0) {
            // Get the task monitoring the download
            FileDownloadTask task = tasks.get(0);

            // Add new listeners to the task using an Activity scope
            task.addOnSuccessListener(this, new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot state) {
                    // handleSuccess(state); //call a user defined function to handle the event.
                }
            });
        }
    }

    @Override
    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
        Toast.makeText(this, "Map Data Downloaded", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onFailure(@NonNull Exception e) {

    }
    private void logUser(String str) {
        log(str);
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    private void log(String str) {
        Log.i("GH", str);
    }

    private void log(String str, Throwable t) {

        Log.i("GH", str, t);
    }

    private void transferFileToStorage(){
if (!(isExternalStorageReadable() && isExternalStorageWritable()))return;

    }
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}
