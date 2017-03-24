package xyz.digzdigital.cunavigator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import static xyz.digzdigital.cunavigator.navigator.Place.CHAPEL;

public class NavigationActivity extends AppCompatActivity {

    private MapView map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        map = (MapView) findViewById(R.id.mapview);
        map.setTileSource(new XYTileSource("YOUR MAP SOURCE", 0, 18, 256, ".jpg", new String[] {}));
        //....
        map.setUseDataConnection(false); //optional, but a good way to prevent loading from the network and test your zip loading.
        IMapController mapController = map.getController();
        mapController.setZoom(16);
        GeoPoint startPoint = new GeoPoint(CHAPEL.latitude(), CHAPEL.longitude());
        mapController.setCenter(startPoint);
    }
}
