package xyz.digzdigital.cunavigator.google;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import xyz.digzdigital.cunavigator.R;
import xyz.digzdigital.cunavigator.google.adapter.PlaceAutoCompleteAdapter;

public class OnlineMapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    private static final String LOG_TAG = OnlineMapsActivity.class.getSimpleName();
    private static final LatLngBounds BOUNDS_NIGERIA = new LatLngBounds(new LatLng(5.065341647205726, 2.9987719580531),
            new LatLng(9.9, 5.9));
    private static final int[] COLORS = new int[]{
            R.color.colorPrimaryDark,
            R.color.colorPrimary,
            R.color.colorPrimaryLight,
            R.color.colorAccent,
            R.color.primary_dark_material_light
    };
    private GoogleMap map;
    private AutoCompleteTextView startAutoComplete, endAutoComplete;
    private ImageButton send;
    private LatLng start, end;
    private GoogleApiClient googleApiClient;
    private PlaceAutoCompleteAdapter adapter;
    private ProgressBar loadingIndicator;
    private ProgressDialog progressDialog;
    private List<Polyline> polylines = new ArrayList<>();
    private List<LatLng> pathPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setupGoogleServices();
        startAutoComplete = (AutoCompleteTextView) findViewById(R.id.startAutoComplete);
        endAutoComplete = (AutoCompleteTextView) findViewById(R.id.endAutoComplete);
        send = (ImageButton) findViewById(R.id.send);
        loadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);
        send.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);

        setClickListeners();
        setTextWatchers();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setupGoogleServices() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    private void setUpPlaceAutoCompleteAdapter() {
        adapter = new PlaceAutoCompleteAdapter(this, android.R.layout.simple_list_item_1, googleApiClient, BOUNDS_NIGERIA, null);
        setPlaceAdapterToView(adapter);
    }

    private void setPlaceAdapterToView(PlaceAutoCompleteAdapter adapter) {
        startAutoComplete.setAdapter(adapter);
        endAutoComplete.setAdapter(adapter);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        setUpPlaceAutoCompleteAdapter();
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                LatLngBounds bounds = OnlineMapsActivity.this.map.getProjection().getVisibleRegion().latLngBounds;
                adapter.setBounds(BOUNDS_NIGERIA);
            }
        });

        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(6.667876, 3.151196));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

        map.moveCamera(center);
        map.animateCamera(zoom);

    }

    @Override
    public void onClick(View v) {
        route();
    }

    private void setTextWatchers() {
        /*
        These text watchers set the start and end points to null because once there's
        * a change after a value has been selected from the dropdown
        * then the value has to reselected from dropdown to get
        * the correct location.
        * */
        startAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int startNum, int before, int count) {
                setStartToNull();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        endAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                setEndToNull();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setClickListeners() {
        /*
        * Sets the start and destination points based on the values selected
        * from the autocomplete text views.
        * */
        startAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                onStartAutocompleteClicked(position);

            }
        });
        endAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                onDestinationAutocompleteClicked(position);

            }
        });
    }

    private void setStartToNull() {
        if (start != null) start = null;
    }

    private void setEndToNull() {
        if (end != null) end = null;
    }

    private void onDestinationAutocompleteClicked(int position) {
        final PlaceAutoCompleteAdapter.PlaceAutocomplete item = adapter.getItem(position);
        final String placeId = String.valueOf(item.placeId);
        Log.i(LOG_TAG, "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                .getPlaceById(googleApiClient, placeId);
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(PlaceBuffer places) {
                if (!places.getStatus().isSuccess()) {
                    // Request did not complete successfully
                    Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                    places.release();
                    return;
                }
                // Get the Place object from the buffer.
                final Place place = places.get(0);
                end = place.getLatLng();
            }
        });
    }


    private void onStartAutocompleteClicked(int position) {
        final PlaceAutoCompleteAdapter.PlaceAutocomplete item = adapter.getItem(position);
        final String placeId = String.valueOf(item.placeId);
        Log.i(LOG_TAG, "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                .getPlaceById(googleApiClient, placeId);
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(PlaceBuffer places) {
                if (!places.getStatus().isSuccess()) {
                    // Request did not complete successfully
                    Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                    places.release();
                    return;
                }
                // Get the Place object from the buffer.
                final Place place = places.get(0);
                start = place.getLatLng();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void showProgressDialog(String title, String message) {
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        progressDialog.dismiss();
    }


    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void route() {
        if (start == null || end == null) {
            if (start == null) {
                if (getTextOfOriginField().length() > 0) {
                    setErrorOnOriginTextField("Choose location from dropdown.");
                } else {
                    showToast("Please choose a starting point.");
                }
            }
            if (end == null) {
                if (getTextOfDestinationField().length() > 0) {
                    setErrorOnDestinationTextField("Choose location from dropdown.");
                } else {
                    showToast("Please choose a destination.");
                }
            }
        } else {
            showProgressDialog("Please wait.", "Fetching route information.");
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.WALKING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(start, end)
                    .build();
            routing.execute();
        }
    }

    private String getTextOfOriginField() {
        return startAutoComplete.getText().toString();
    }

    private String getTextOfDestinationField() {
        return endAutoComplete.getText().toString();
    }

    private void setErrorOnOriginTextField(String error) {
        startAutoComplete.setError(error);
    }

    private void setErrorOnDestinationTextField(String error) {
        endAutoComplete.setError(error);
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        dismissProgressDialog();
        if (e != null) {
            showToast("Error: " + e.getMessage());
        } else {
            showToast("Something went wrong, Try again");
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int i) {
        dismissProgressDialog();
        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

        centerCamera(center);
        zoomCamera(zoom);


        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        // for (int i = 0; i < route.size(); i++) {

        //In case of more than 5 alternative routes
        int colorIndex = /*i*/1 % COLORS.length;

        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(getResources().getColor(COLORS[colorIndex]));
        polyOptions.width(10 + /*i '*' */ 3);
        polyOptions.addAll(route.get(/*i*/0).getPoints());
        Polyline polyline = drawOnMap(polyOptions);
        polylines.add(polyline);

        pathPoints = polyline.getPoints();
        showToast("Route " + (/*i*/1 + 1) + ": distance - " + route.get(/*i*/0).getDistanceValue() + ": duration - " + route.get(/*i*/0).getDurationValue());
        // }

        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(start);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
        addMapMarker(options);


        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
        addMapMarker(options);


    }

    public void centerCamera(CameraUpdate center) {
        map.moveCamera(center);
    }

    public void zoomCamera(CameraUpdate zoom) {
        map.moveCamera(zoom);
    }

    public Polyline drawOnMap(PolylineOptions polylineOptions) {
        return map.addPolyline(polylineOptions);
    }

    public void addMapMarker(MarkerOptions markerOptions) {
        map.addMarker(markerOptions);
    }
    @Override
    public void onRoutingCancelled() {

    }
}
