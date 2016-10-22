package com.isrhacks.godetroit;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RoutesActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private String fromLocation;
    private String toLocation;
    private long time = 0;
    private String constraint;
    private String transportMode;
    private static String token;

    private int primaryColor;
    private int primaryAccent;

    RecyclerView recycler;
    LinearLayoutManager layoutManager;
    RouteAdapter routeAdapter;

    static Context context;
    static int lastRoute = 0;
    static ArrayList<Route> routes = new ArrayList<>();
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

//    int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.BLACK};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        context = this;
        Intent intent = getIntent();

        fromLocation = intent.getStringExtra("from");
        toLocation = intent.getStringExtra("to");
        String timeStr = intent.getStringExtra("time");
        constraint = intent.getStringExtra("time_constraint");
        transportMode = intent.getStringExtra("transportation");

        getSupportActionBar().setTitle(transportMode + " Routes");

        //convert timeStr to epoch time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddhh:mmZ", Locale.ENGLISH);
        try {
            Date date = sdf.parse(timeStr);
            time = date.getTime() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(constraint + ": " + time);

        routes = new ArrayList<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SharedPreferences prefs = getSharedPreferences(TripActivity.MY_PREFERENCES, MODE_PRIVATE);
        token = prefs.getString("jwt", null);

        recycler = (RecyclerView) findViewById(R.id.routes_recycler);
        recycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        String[] test = new String[]{"22N 5 min", "15 min", "50", "FAR", "ISR", "13N 4 min", "12 min", "75", "Main Library", "Transit Plaza", "12W 3 min", "12 min", "100", "PAR", "Wright and Stoughton", "50E 15 min", "30 min", "80", "ISR", "Illini Union"};
        routeAdapter = new RouteAdapter(test);
        recycler.setAdapter(routeAdapter);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        primaryColor = getResources().getColor(R.color.colorPrimary);
        primaryAccent = getResources().getColor(R.color.colorAccent);
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

    public void updateRouteAdapter() {
        System.out.println("We are now updating the adapter");
    }

    public Marker addMarker(double lat, double lng, String title) {
        LatLng newMarker = new LatLng(lat, lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newMarker));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12));
        return mMap.addMarker(new MarkerOptions().position(newMarker).title(title));

    }

    public void queryRoute(String from, String to) {
        String baseUrl = "https://maps.googleapis.com/maps/api/directions/json";
        String fromParam = "origin=" + from;
        String toParam = "destination=" + to;
        String transit = "";
        if (transportMode != null && transportMode.length() > 0) {
            transit = "mode=" + transportMode.toLowerCase();
        } else {
            transit = "mode=transit";
        }
        String alternatives = "alternatives=true";
        String constraintParam = "";
        //check if we are using depart by or arrive by and use correct format
        if (constraint.equals("Depart By") && time > 0) {
            constraintParam = "departure_time=" + time;
        } else if (constraint.equals("Arrive By") && time > 0) {
            constraintParam = "arrival_time=" + time;
        }

        String key = "key=AIzaSyC4Z2nrjk3V54cOiGVfEFwYjndbr4uZbaw";
        String parameters = "?" + fromParam + "&" + toParam + "&" + transit + "&" + alternatives + "&" + constraintParam + "&" + key;
        String urlstr = baseUrl + parameters;
        urlstr = urlstr.replace(" ", "+");
        System.out.println(urlstr);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlstr,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseDirectionsJson(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    ArrayList<Step> stepsArr; //holds the arr of steps for an individual route

    public void parseDirectionsJson(String result) {
        System.out.println("JSON Parsing is now starting");
        JSONObject json;

        try {
            json = new JSONObject(result);
            JSONArray jsonRoutes = json.getJSONArray("routes");
            //iterate through each route that is given
            for (int j = 0; j < jsonRoutes.length(); j++) {
                final boolean finalRoute = (j + 1 >= jsonRoutes.length());
                stepsArr = new ArrayList<>();
                JSONObject legs = jsonRoutes.getJSONObject(j).getJSONArray("legs").getJSONObject(0);
                String elapsedTime = legs.getJSONObject("duration").getString("text");
                JSONArray steps = legs.getJSONArray("steps");

                //iterate through each step of the route
                for (int i = 0; i < steps.length(); i++) {
                    final boolean finalNode = (i + 1 >= steps.length());
                    final JSONObject step = steps.getJSONObject(i);
                    LatLng start;
                    LatLng end;
                    String mode;
                    String polylinePoints;
                    String instruction;

                    instruction = android.text.Html.fromHtml(step.getString("html_instructions")).toString();
                    JSONObject jsonStart = step.getJSONObject("start_location");
                    start = new LatLng(jsonStart.getDouble("lat"), jsonStart.getDouble("lng"));

                    JSONObject jsonEnd = step.getJSONObject("end_location");
                    end = new LatLng(jsonEnd.getDouble("lat"), jsonEnd.getDouble("lng"));
                    mode = step.getString("travel_mode");

                    int color;

                    if (mode.equals("WALKING")) {
                        color = primaryAccent;
                    } else {
                        color = primaryColor;
                    }
                    polylinePoints = step.getJSONObject("polyline").getString("points");
                    Polyline routeline = mMap.addPolyline(new PolylineOptions().addAll(PolyUtil.decode(polylinePoints)).width(15).color(color));
                    routeline.setVisible(false);

                    final Step stepNode;
                    if (mode.equals("TRANSIT")) {
                        JSONObject transitDetails = step.getJSONObject("transit_details");
                        JSONObject departureStopStop = transitDetails.getJSONObject("departure_stop");
                        JSONObject endStop = transitDetails.getJSONObject("arrival_stop");

                        String startBus = departureStopStop.getString("name");
                        String endBus = endStop.getString("name");
                        stepNode = new Step(start, end, mode, routeline, startBus, endBus);
                        stepNode.transitDetails = transitDetails;

                    } else {
                        stepNode = new Step(start, end, mode, routeline);
                    }
                    stepNode.finalNode = finalNode;
                    stepNode.setInstruction(instruction);
                    Marker m1 = addMarker(stepNode.start.latitude, stepNode.start.longitude, stepNode.instruction);
                    if (finalNode) {
                        stepNode.instruction = "Arrived";
                    }
                    Marker m2 = addMarker(stepNode.end.latitude, stepNode.end.longitude, stepNode.instruction);

                    m1.setVisible(false);
                    m2.setVisible(false);

                    stepNode.setMarkers(m1, m2);

                    //Query for crime rate
                    String urlstr = "http://GoDetroit-dev.us-east-1.elasticbeanstalk.com/crimes?lat=" + start.latitude + "&long=" + start.longitude + "&rad=" + 250;
                    // Instantiate the RequestQueue.
                    RequestQueue queue = Volley.newRequestQueue(this);
                    // Request a string response from the provided URL.
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, urlstr,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    parseCrimeIndex(response, stepNode);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });
                    // Add the request to the RequestQueue.
                    queue.add(stringRequest);
                    numRequests++;
                    stepsArr.add(stepNode);
                }
                Route route = new Route(stepsArr, elapsedTime, 0);
                routes.add(route);
            }


            //call final callback to update route adapter
//            updateRouteAdapter();

        } catch (JSONException e) {
            e.printStackTrace();
        }
//        System.out.println(json);
    }

    int numRequests = 0;

    public void parseCrimeIndex(String result, Step step) {
        numRequests--;
        int crimerate = Integer.parseInt(result);
        step.setCrimeIndex(crimerate);
//        //if this is the final node call another method to sum up values and create the route object
//        if(step.finalNode)
//        {
//
//        }
        if (numRequests == 0) {
            finalUpdate();
        }
    }

    public void finalUpdate() {
        //sum all the crime ratings
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            ArrayList<Step> steps = route.route;
            int crimeTotal = 0;
            for (int j = 0; j < steps.size(); j++) {
                Step step = steps.get(j);
                crimeTotal += step.crimeIndex;

            }
            DecimalFormat twoDecimals = new DecimalFormat("#.##");
            route.crimeRating = Double.valueOf(twoDecimals.format(crimeTotal / (double) steps.size()));
        }

        int indexSafest = 0;
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            if (routes.get(indexSafest).crimeRating > route.crimeRating) {
                indexSafest = i;
            }
        }
        routes.get(indexSafest).name = "Safest Route";

        printRoutes();
        updateAdapters();


    }

    public void updateAdapters() {
        recycler = (RecyclerView) findViewById(R.id.routes_recycler);
        recycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);

        String[] cardData = new String[5 * routes.size()];
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            int counter = 5 * i;
            cardData[counter] = route.name;
            cardData[counter + 1] = route.time;
            cardData[counter + 2] = "Crime Rating: " + route.crimeRating;
            cardData[counter + 3] = "Start";
            cardData[counter + 4] = "End";
        }
//        String[] test = new String[] {"22N 5 min", "15 min", "50", "FAR", "ISR", "13N 4 min", "12 min", "75","Main Library", "Transit Plaza", "12W 3 min", "12 min", "100","PAR", "Wright and Stoughton", "50E 15 min", "30 min", "80", "ISR", "Illini Union"};
        routeAdapter = new RouteAdapter(cardData);

        recycler.setAdapter(routeAdapter);
    }

    public void printRoutes() {
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            ArrayList<Step> steps = route.route;
            System.out.println();
            System.out.println("Start of route " + i + " takes " + route.time + " minutes with crime of " + route.crimeRating);
            for (Step step : steps) {
                System.out.println(step.toString());
            }
        }
    }


    public static void displayRoute(int route) {
        routes.get(lastRoute).hideRoute();
        lastRoute = route;
        routes.get(route).displayRoute();


    }

    public static void hideRoute(int route) {
        routes.get(route).hideRoute();
    }

    public void postTransitData(final String transitData) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest sr = new StringRequest(Request.Method.POST, "http://godetroit-dev.us-east-1.elasticbeanstalk.com/passengerSchedule", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("Posted");
                System.out.println(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
                System.out.println(error.getMessage());
                //    postUsernameResponse.requestEndedWithError(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
//                params.
                params.put("transitData", transitData);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "bearer " + token);
                params.put("Content-Type", "application/json");
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String httpPostBody = transitData;
                // usually you'd have a field with some values you'd want to escape, you need to do it yourself if overriding getBody. here's how you do it
//                try {
//                    httpPostBody=httpPostBody+"&randomFieldFilledWithAwkwardCharacters="+URLEncoder.encode("{{%stuffToBe Escaped/","UTF-8");
//                } catch (UnsupportedEncodingException exception) {
//                    Log.e("ERROR", "exception", exception);
//                    // return null and don't pass any POST string if you encounter encoding error
//                    return null;
//                }
                System.out.println(httpPostBody);
                return httpPostBody.getBytes();
            }

        };
        queue.add(sr);
    }

    int selectedRoute = 0;

    public void startNav(View v) {

        int index = (int) v.getTag();
        selectedRoute = index;
        //send route to backend
        ArrayList<Step> steps = routes.get(index).route;
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            if (step.mode.equals("TRANSIT")) {
                System.out.println("We are beginning a post");
                postTransitData(step.transitDetails.toString());
            }
        }

        wipeView();
    }

    public void wipeView() {
        RecyclerView recycle = (RecyclerView) findViewById(R.id.routes_recycler);
        LinearLayout linlay = (LinearLayout) findViewById(R.id.details);
        linlay.removeView(recycle);
        LinearLayout invislay = (LinearLayout) findViewById(R.id.invisDetails);
        Route route = routes.get(selectedRoute);
        ((TextView) findViewById(R.id.timetext)).setText("Time: " + route.time);
        ((TextView) findViewById(R.id.crimetext)).setText("Crime Rating: " + route.crimeRating);
        invislay.setVisibility(View.VISIBLE);
    }


    public void notifyCircle(View v){
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            System.out.println("Finding Location");
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            mMap.setMyLocationEnabled(true);
        }
        SmsManager smsManager = SmsManager.getDefault();
        for(int i = 0; i < 5; i++){
            SharedPreferences preferences = getSharedPreferences(TripActivity.MY_PREFERENCES, MODE_PRIVATE);
            String next = preferences.getString("number"+String.valueOf(i), "0");
            if(!next.equals("0") && !next.equals(null)){
                double lat;
                double lng;
                if(mLastLocation == null){
                    lat = TripActivity.mLastLocation.getLatitude();
                    lng = TripActivity.mLastLocation.getLongitude();
                }else {
                    lat = mLastLocation.getLatitude();
                    lng = mLastLocation.getLongitude();
                }
                String geoUri = "http://maps.google.com/maps?q=loc:" + lat + "," + lng +"(" + "EmergencyContactLocation" + ")";
                String message = "Send Help! I am currently at Latitude: "+lat
                        +", Longitude: "+lng+"\n"+geoUri;
                System.out.println(message);
                smsManager.sendTextMessage(next, null, message, null, null);
            }
        }
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
}





