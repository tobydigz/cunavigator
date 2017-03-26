package xyz.digzdigital.cunavigator.navigator;

import android.content.Context;
import android.os.AsyncTask;


import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Digz on 23/03/2017.
 */

public class Router {
    private ArrayList<Place> places;
    private Place origin;
    private Place destination;
    private Context context;
    private onRoute onRouteListener;
    private RoadManager roadManager;

    public Router(Context context) {
        this.context = context;
    }

    public ArrayList<Place> getPlaces() {
        if (places == null || places.size() == 0) createPlaces();
        return places;
    }


    private void createPlaces() {
        places = new ArrayList<>();
        places.add(Place.ESTHER_HALL);
        places.add(Place.MARY_HALL);
        places.add(Place.DEBORAH_HALL);
        places.add(Place.LYDIA_HALL);
        places.add(Place.DORCAS_HALL);
        places.add(Place.PETER_HALL);
        places.add(Place.JOHN_HALL);
        places.add(Place.PAUL_HALL);
        places.add(Place.JOSEPH_HALL);
        places.add(Place.DANIEL_HALL);
        places.add(Place.PG_FEMALE_HALL);
        places.add(Place.PG_MALE_HALL);
        places.add(Place.ROUNDABOUT);
        places.add(Place.CLR);
        places.add(Place.CHAPEL);
        places.add(Place.ALDC);
        places.add(Place.CST);
        places.add(Place.CUCRID);
        places.add(Place.CBSS);
        places.add(Place.CEDS);
        places.add(Place.MECH);
        places.add(Place.CVE);
        places.add(Place.EIE);
        places.add(Place.PET);
        places.add(Place.LT);
        places.add(Place.ICT_1);
        places.add(Place.ICT_2);
        places.add(Place.CAFE_1);
        places.add(Place.CAFE_2);
        places.add(Place.CAFE_PG);
        places.add(Place.CUGH);
    }

    public void setOrigin(Place origin) {
        this.origin = origin;
    }

    public void setDestination(Place destination) {
        this.destination = destination;
    }

    public void route() {
        AssetsFileReader assetsFileReader = new AssetsFileReader(context);
        assetsFileReader.setJsonNode(origin.uid() + "_" + destination.uid());
        List<double[]> points = assetsFileReader.getPointsJsonFromFile();
        onRouteListener.onComplete(points);
    }

    public void routeWithGraphhopper(){
        // roadManager = new OSRMRoadManager(context);
        roadManager = new GraphHopperRoadManager("7bbb6502-eb6c-49c9-92d1-7077796fd104", false);
        ArrayList<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(new GeoPoint(origin.latitude(), origin.longitude()));

        GetRoadData task = new GetRoadData();
        task.execute(geoPoints);

    }

    private void computeRoute(Polyline polyline) {
        List<double[]> points = new ArrayList<>();
        double[] point = new double[2];
        for (GeoPoint geoPoint:polyline.getPoints() ){

            point[0] = geoPoint.getLatitude();
            point[1] = geoPoint.getLongitude();
            points.add(point);
        }
        onRouteListener.onComplete(points);
    }

    public void setOnRouteListener(onRoute onRouteListener) {
        this.onRouteListener = onRouteListener;
    }

    public interface onRoute {
        void onComplete(List<double[]> points);

        void onError();
    }

    private class GetRoadData extends AsyncTask<ArrayList<GeoPoint>, Void, Road>{

        @Override
        protected Road doInBackground(ArrayList<GeoPoint>... params) {
            ArrayList<GeoPoint> geoPoints = params[0];
            return roadManager.getRoad(geoPoints);
        }

        @Override
        protected void onPostExecute(Road road){
            Polyline polyline = RoadManager.buildRoadOverlay(road);
            computeRoute(polyline);
        }
    }
}
