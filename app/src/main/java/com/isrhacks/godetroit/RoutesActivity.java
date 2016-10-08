package com.isrhacks.godetroit;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RoutesActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String fromLocation;
    private String toLocation;

    RecyclerView recycler;
    LinearLayoutManager layoutManager;
    RouteAdapter routeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        Intent intent = getIntent();
        fromLocation = intent.getStringExtra("from");
        toLocation = intent.getStringExtra("to");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        recycler = (RecyclerView) findViewById(R.id.routes_recycler);
        recycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        String[] test = new String[] {"22N 5 min", "15 min", "50", "FAR", "ISR", "13N 4 min", "12 min", "75","Main Library", "Transit Plaza", "12W 3 min", "12 min", "100","PAR", "Wright and Stoughton", "50E 15 min", "30 min", "80", "ISR", "Illini Union"};
        routeAdapter = new RouteAdapter(test);
        recycler.setAdapter(routeAdapter);
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
        mMap = googleMap;
        queryRoute(fromLocation, toLocation);
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void addMarker(double lat, double lng)
    {
        LatLng newMarker = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(newMarker).title("New Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newMarker));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(11));

    }

    public void addStepMarker(Step step)
    {
        addMarker(step.start.latitude, step.start.longitude);
//        addMarker(step.end.latitude, step.end.longitude);
    }

    public void queryRoute(String from, String to)
    {
        String baseUrl = "https://maps.googleapis.com/maps/api/directions/json";
        String fromParam = "origin=" + from;
        String toParam = "destination=" + to;
        String transit = "mode=transit";
        String key = "key=AIzaSyC4Z2nrjk3V54cOiGVfEFwYjndbr4uZbaw";
        String parameters = "?" + fromParam + "&" + toParam + "&" + transit + "&" + key;
        String urlstr = baseUrl + parameters;
        System.out.println(urlstr);

        new DirectionRequest().execute(urlstr);
    }

    class DirectionRequest extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urlStrings) {
            URL url;
            HttpURLConnection urlConnection = null;
            String result = "null";
            try {
                String urlstr = urlStrings[0];
                System.out.println(urlstr);
//            String parameters = "?origin=75+9th+Ave+New+York,+NY&destination=MetLife+Stadium+1+MetLife+Stadium+Dr+East+Rutherford,+NJ+07073&mode=transit";
                url = new URL(urlstr);

                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                result = sb.toString();

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return result;
        }

        protected void onProgressUpdate(Integer... progress) {
            // TODO You are on the GUI thread, and the first element in
            // the progress parameter contains the last progress
            // published from doInBackground, so update your GUI
        }

        protected void onPostExecute(String result) {
            System.out.println("JSON Parsing is now starting");
            JSONObject jsonRoutes = null;
            ArrayList<Step> stepsArr = new ArrayList<Step>();
            try {
                jsonRoutes = new JSONObject(result);

                JSONArray steps = jsonRoutes.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
                //populate the steps array with all important information
                for(int i = 0; i < steps.length(); i++)
                {
                    JSONObject step = steps.getJSONObject(i);
                    LatLng start;
                    LatLng end;
                    String mode;
                

                    JSONObject jsonStart = step.getJSONObject("start_location");
                    start = new LatLng(jsonStart.getDouble("lat"), jsonStart.getDouble("lng"));

                    JSONObject jsonEnd = step.getJSONObject("end_location");
                    end = new LatLng(jsonEnd.getDouble("lat"), jsonEnd.getDouble("lng"));
                    mode = step.getString("travel_mode");

                    if(mode.equals("TRANSIT"))
                    {
                        JSONObject transitDetails = step.getJSONObject("transit_details");
                        JSONObject departureStopStop = transitDetails.getJSONObject("departure_stop");
                        JSONObject endStop = transitDetails.getJSONObject("arrival_stop");

                        String startBus = departureStopStop.getString("name");
                        String endBus = endStop.getString("name");

                        stepsArr.add(new Step(start, end, mode, startBus, endBus));
                    }
                    else
                    {
                        stepsArr.add(new Step(start, end, mode));
                    }
                }

                for(int i = 0; i < stepsArr.size(); i++)
                {
                    Step step = stepsArr.get(i);
                    addStepMarker(step);
                    System.out.println(step);
                }

            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println(jsonRoutes);

            // Processing is complete, result contains the number of
            // results you processed
        }
    }

    public void startNav(){

    }
}





