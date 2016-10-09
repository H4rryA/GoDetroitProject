package com.isrhacks.godetroit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoutesActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String fromLocation;
    private String toLocation;
    private static String token;

    RecyclerView recycler;
    LinearLayoutManager layoutManager;
    RouteAdapter routeAdapter;

    static Context context;
    static int lastRoute = 0;
    static ArrayList<Route> routes = new ArrayList<>();


//    int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.BLACK};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        context = this;
        Intent intent = getIntent();
        fromLocation = intent.getStringExtra("from");
        toLocation = intent.getStringExtra("to");
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

    public void updateRouteAdapter()
    {
        System.out.println("We are now updating the adapter");
    }

    public Marker addMarker(double lat, double lng)
    {
        LatLng newMarker = new LatLng(lat, lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newMarker));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(11));
        return mMap.addMarker(new MarkerOptions().position(newMarker).title("New Marker"));

    }

    public Marker addStepMarker(Step step)
    {
        return addMarker(step.start.latitude, step.start.longitude);
//        addMarker(step.end.latitude, step.end.longitude);
    }

    public void queryRoute(String from, String to)
    {
        String baseUrl = "https://maps.googleapis.com/maps/api/directions/json";
        String fromParam = "origin=" + from;
        String toParam = "destination=" + to;
        String transit = "mode=transit";
        String alternatives = "alternatives=true";
        String key = "key=AIzaSyC4Z2nrjk3V54cOiGVfEFwYjndbr4uZbaw";
        String parameters = "?" + fromParam + "&" + toParam + "&" + transit + "&" + alternatives + "&" + key;
        String urlstr = baseUrl + parameters;
        urlstr = urlstr.replace(" ", "+");
        System.out.println(urlstr);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlstr,
            new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    parseDirectionsJson(response);
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {

            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    ArrayList<Step> stepsArr; //holds the arr of steps for an individual route
    public void parseDirectionsJson(String result)
    {
        System.out.println("JSON Parsing is now starting");
        JSONObject json;

        try {
            json = new JSONObject(result);
            JSONArray jsonRoutes = json.getJSONArray("routes");
            //iterate through each route that is given
            for(int j = 0; j < jsonRoutes.length(); j++)
            {
                final boolean finalRoute = (j + 1 >= jsonRoutes.length());
                stepsArr = new ArrayList<>();
                JSONArray steps = jsonRoutes.getJSONObject(j).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");

                //iterate through each step of the route
                for (int i = 0; i < steps.length(); i++)
                {
                    final boolean finalNode = (i + 1 >= steps.length());
                    final JSONObject step = steps.getJSONObject(i);
                    LatLng start;
                    LatLng end;
                    String mode;
                    String polylinePoints;

                    polylinePoints = step.getJSONObject("polyline").getString("points");
                    Polyline routeline = mMap.addPolyline(new PolylineOptions().addAll(PolyUtil.decode(polylinePoints)).width(15).color(Color.BLUE));
                    routeline.setVisible(false);
                    JSONObject jsonStart = step.getJSONObject("start_location");
                    start = new LatLng(jsonStart.getDouble("lat"), jsonStart.getDouble("lng"));

                    JSONObject jsonEnd = step.getJSONObject("end_location");
                    end = new LatLng(jsonEnd.getDouble("lat"), jsonEnd.getDouble("lng"));
                    mode = step.getString("travel_mode");

                    final Step stepNode;
                    if (mode.equals("TRANSIT"))
                    {
                        JSONObject transitDetails = step.getJSONObject("transit_details");
                        JSONObject departureStopStop = transitDetails.getJSONObject("departure_stop");
                        JSONObject endStop = transitDetails.getJSONObject("arrival_stop");

                        String startBus = departureStopStop.getString("name");
                        String endBus = endStop.getString("name");
                        stepNode = new Step(start, end, mode, routeline, startBus, endBus);
                        stepNode.transitDetails = transitDetails;

                    }
                    else
                    {
                        stepNode = new Step(start, end, mode, routeline);
                    }
                    stepNode.finalNode = finalNode;
                    Marker m1 = addMarker(stepNode.start.latitude, stepNode.start.longitude);
                    Marker m2 = addMarker(stepNode.end.latitude, stepNode.end.longitude);

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
                                public void onResponse(String response)
                                {
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
                Route route = new Route(stepsArr, 10, 0);
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
    public void parseCrimeIndex(String result, Step step)
    {
        numRequests--;
        int crimerate = Integer.parseInt(result);
        System.out.println("This is what I needed: " + result);
        step.setCrimeIndex(crimerate);
//        //if this is the final node call another method to sum up values and create the route object
//        if(step.finalNode)
//        {
//
//        }
        if(numRequests == 0)
        {
            finalUpdate();
        }
    }

    public void finalUpdate()
    {
        //sum all the crime ratings
        for(int i = 0; i < routes.size(); i++)
        {
            Route route = routes.get(i);
            ArrayList<Step> steps = route.route;
            int crimeTotal = 0;
            for(int j = 0; j < steps.size(); j++)
            {
                Step step = steps.get(j);
                crimeTotal += step.crimeIndex;

            }
            route.crimeRating = crimeTotal / (double) steps.size();
        }
        printRoutes();
        updateAdapters();


    }

    public void updateAdapters()
    {
        recycler = (RecyclerView) findViewById(R.id.routes_recycler);
        recycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);

        String[] cardData = new String[5 * routes.size()];
        for(int i = 0; i < routes.size(); i++)
        {
            Route route = routes.get(i);
            int counter = 5 * i;
            cardData[counter] = "Route " + i;
            cardData[counter + 1] = route.time + " min";
            cardData[counter + 2] = "Crime Rate: " + route.crimeRating;
            cardData[counter + 3] = "Start";
            cardData[counter + 4] = "End";
        }
//        String[] test = new String[] {"22N 5 min", "15 min", "50", "FAR", "ISR", "13N 4 min", "12 min", "75","Main Library", "Transit Plaza", "12W 3 min", "12 min", "100","PAR", "Wright and Stoughton", "50E 15 min", "30 min", "80", "ISR", "Illini Union"};
        routeAdapter = new RouteAdapter(cardData);

        recycler.setAdapter(routeAdapter);
    }

    public void printRoutes()
    {
        for(int i = 0; i < routes.size(); i++)
        {
            Route route = routes.get(i);
            ArrayList<Step> steps = route.route;
            System.out.println();
            System.out.println("Start of route " + i + " takes " + route.time + " minutes with crime of " + route.crimeRating);
            for(Step step: steps)
            {
                System.out.println(step.toString());
            }
        }
    }


    public static void displayRoute(int route)
    {
        routes.get(lastRoute).hideRoute();
        lastRoute = route;
        routes.get(route).displayRoute();

        //send route to backend
        ArrayList<Step> steps = routes.get(lastRoute).route;
        for(int i = 0; i < steps.size(); i++)
        {
            Step step = steps.get(i);
            if(step.mode.equals("TRANSIT"))
            {
                postTransitData(step.transitDetails.toString());
                System.out.println("This is the details we are posting\n" + step.transitDetails.toString());
            }
        }
    }

    public static void postTransitData(final String transitData) {
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
                //    postUsernameResponse.requestEndedWithError(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("transitData", transitData);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "bearer " + token);
                return params;
            }

        };
        queue.add(sr);
    }

    public void startNav(View v)
    {

    }
}





