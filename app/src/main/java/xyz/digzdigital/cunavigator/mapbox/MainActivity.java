package xyz.digzdigital.cunavigator.mapbox;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xyz.digzdigital.cunavigator.R;
import xyz.digzdigital.cunavigator.navigator.Place;
import xyz.digzdigital.cunavigator.navigator.Router;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, OfflineManager.CreateOfflineRegionCallback, OfflineRegion.OfflineRegionObserver, AdapterView.OnItemSelectedListener, Router.onRoute, View.OnClickListener {

    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";

    private static final String TAG = MainActivity.class.getSimpleName();
    ArrayList<Place> originPlaces, destinationPlaces;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private MarkerView originMarker, destinationMarker;
    private Polyline polyline;
    private boolean isEndNotified;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    private Router router;
    private Spinner originSelector, destinationSelector;
    private Button routeButton;
    private File mapsFolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkIfDeviceHasSdCard()){
            if (!mapsFolder.exists())mapsFolder.mkdirs();
        }else{
            Toast.makeText(this, "Offline mapping won't work without an sd card installed", Toast.LENGTH_LONG).show();
        }
        MapboxAccountManager.start(this, "pk.eyJ1IjoidG9ieWRpZ3oiLCJhIjoiY2l3MTExM3JqMDA0YTJ5cTkxcXJiNzQ3NSJ9.OL1zvr8Hb6HbKPq01CEwsg");
        mapView = (MapView) findViewById(R.id.mapview);

        originSelector = (Spinner) findViewById(R.id.placeOriginSelector);
        destinationSelector = (Spinner) findViewById(R.id.placeDestinationSelector);
        routeButton = (Button) findViewById(R.id.route);
        routeButton.setOnClickListener(this);

        mapView.setStyle(Style.LIGHT);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        router = new Router(this);
        router.setOnRouteListener(this);
        loadOriginArray();
    }

    private boolean checkIfDeviceHasSdCard() {
        boolean greaterOrEqKitkat = Build.VERSION.SDK_INT >= 19;
        if (greaterOrEqKitkat) {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                return false;
            }
            mapsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/graphhopper/maps/");
        } else
            mapsFolder = new File(Environment.getExternalStorageDirectory(), "/graphhopper/maps/");
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(6.671307, 3.158210)) // Sets the new camera position
                .zoom(16) // Sets the zoom
                // .bearing(180) // Rotate the camera
                // .tilt(30) // Set the camera tilt
                .build(); // Creates a CameraPosition from the builder
        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 1000);
        OfflineManager offlineManager = OfflineManager.getInstance(this);

        LatLngBounds latLngBounds = createBounds();

        OfflineTilePyramidRegionDefinition definition = createOfflineDefinition(latLngBounds);

        byte[] metadata;

        try {
            metadata = createMetadata();
        } catch (Exception e) {
            Log.e(TAG, "Failed to encode metadata: " + e.getMessage());
            metadata = null;
        }

        if (metadata != null) offlineManager.createOfflineRegion(definition, metadata, this);
    }

    private LatLngBounds createBounds() {
        return new LatLngBounds.Builder()
                .include(new LatLng(6.677912, 3.162845))
                .include(new LatLng(6.668023, 3.151976))
                .build();
    }

    private OfflineTilePyramidRegionDefinition createOfflineDefinition(LatLngBounds latLngBounds) {
        return new OfflineTilePyramidRegionDefinition(
                mapView.getStyleUrl(),
                latLngBounds,
                10,
                20,
                this.getResources().getDisplayMetrics().density
        );
    }

    private byte[] createMetadata() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_FIELD_REGION_NAME, "Covenant University");
        String json = jsonObject.toString();
        return json.getBytes(JSON_CHARSET);
    }

    private void addOriginMarker(Place place) {
        if (originMarker != null) {
            updateOriginMarkerPosition(place);
            return;
        }
        MarkerViewOptions markerViewOptions = new MarkerViewOptions()
                .position(new LatLng(place.latitude(), place.longitude()))
                .title("Origin: " + place.placeName());
        originMarker = mapboxMap.addMarker(markerViewOptions);
    }

    private void updateOriginMarkerPosition(Place place) {
        originMarker.setPosition(new LatLng(place.latitude(), place.longitude()));
        originMarker.setTitle("Origin: " + place.placeName());
    }

    private void addDestinationMarker(Place place) {
        if (destinationMarker != null){
            updateDestinationMarkerPosition(place);
            return;
        }
        MarkerViewOptions markerViewOptions = new MarkerViewOptions()
                .position(new LatLng(place.latitude(), place.longitude()))
                .title("Destination: " + place.placeName());
        destinationMarker = mapboxMap.addMarker(markerViewOptions);
    }

    private void updateDestinationMarkerPosition(Place place) {
        destinationMarker.setPosition(new LatLng(place.latitude(), place.longitude()));
        destinationMarker.setTitle("Destination: " + place.placeName());
    }

    private void drawPolyline(List<double[]> points) {
        LatLng[] latLngs = new LatLng[points.size()];
        int i = 0;
        for (double[] point : points) {
            latLngs[i] = (new LatLng(point[0], point[1]));
            i++;
        }
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(latLngs)
                .width(4)
                .color(Color.MAGENTA);

        polyline = mapboxMap.addPolyline(polylineOptions);
    }

    private void removePolyline() {
        assert polyline != null;
        mapboxMap.removePolyline(polyline);
    }

    @Override
    public void onCreate(OfflineRegion offlineRegion) {
        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        startProgress();

        offlineRegion.setObserver(this);
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "onError: " + error);
    }

    private void startProgress() {
        isEndNotified = false;
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setPercentage(final int percentage) {
        progressBar.setIndeterminate(false);
        progressBar.setProgress(percentage);
    }

    private void endProgress(final String message) {
        if (isEndNotified) return;

        isEndNotified = true;
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(OfflineRegionStatus status) {
        double percentage = status.getRequiredResourceCount() >= 0
                ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount())
                : 0;

        if (status.isComplete()) {
            endProgress("CU region downloaded successfully");
        }

        if (status.isRequiredResourceCountPrecise()) {
            setPercentage((int) Math.round(percentage));
        }
    }

    @Override
    public void onError(OfflineRegionError error) {
        Log.e(TAG, "onError reason: " + error.getReason());
        Log.e(TAG, "onError message: " + error.getMessage());
    }

    @Override
    public void mapboxTileCountLimitExceeded(long limit) {
        Log.e(TAG, "Mapbox tile count limit exceeded: " + limit);
    }

    private void loadOriginArray() {
        originPlaces = router.getPlaces();
        String[] ITEMS = new String[originPlaces.size()];
        for (int i = 0; i < originPlaces.size(); i++) {
            ITEMS[i] = originPlaces.get(i).placeName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ITEMS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        originSelector.setAdapter(adapter);
        originSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                routeButton.setVisibility(View.GONE);
                router.setOrigin(originPlaces.get(position));
                loadDestinationArray(position);
                addOriginMarker(originPlaces.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadDestinationArray(int position) {
        destinationPlaces = originPlaces;
        destinationPlaces.remove(position);
        String[] ITEMS = new String[destinationPlaces.size()];
        for (int i = 0; i < destinationPlaces.size(); i++) {
            ITEMS[i] = destinationPlaces.get(i).placeName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ITEMS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        destinationSelector.setAdapter(adapter);
        destinationSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                routeButton.setVisibility(View.VISIBLE);
                router.setDestination(destinationPlaces.get(position));
                addDestinationMarker(destinationPlaces.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (view.getId()) {
            case R.id.placeOriginSelector:

                break;
            case R.id.placeDestinationSelector:

                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onComplete(List<double[]> points) {
        progressDialog.dismiss();
        drawPolyline(points);
    }

    @Override
    public void onError() {

    }

    @Override
    public void onClick(View v) {
        showProgressDialog();
        router.routeWithGraphhopper();
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading, please wait");
        progressDialog.setMessage("Calculating route information");
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        progressDialog.dismiss();
    }
}
