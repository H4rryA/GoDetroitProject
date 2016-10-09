package com.isrhacks.godetroit;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONObject;

/**
 * Created by david on 10/8/16.
 */

public class Step
{
    public LatLng start;
    public LatLng end;
    public String mode;
    public String startbus;
    public String endbus;
    public Polyline polyline;
    public Marker marker1;
    public Marker marker2;
    public int crimeIndex;
    public boolean finalNode = false;

    //transit-details
    public JSONObject transitDetails;

    public Step(LatLng start, LatLng end, String mode, Polyline polyline)
    {
        this.start = start;
        this.end = end;
        this.mode = mode;
//        this.route = route;
        this.polyline = polyline;
    }

    public Step(LatLng start, LatLng end, String mode, Polyline polyline, String startbus, String endbus)
    {
        this.start = start;
        this.end = end;
        this.mode = mode;
//        this.route = route;
        this.startbus = startbus;
        this.endbus = endbus;
        this.polyline = polyline;
    }

    public void setMarkers(Marker m1, Marker m2)
    {
        marker1 = m1;
        marker2 = m2;
    }

    public void setCrimeIndex(int index)
    {
        crimeIndex = index;
    }

    public String toString()
    {
        if(mode.equals("TRANSIT"))
        {
            return "Crime: " + crimeIndex + ", Starts at: " + start.toString() + " Ends at: " + end.toString() + " Using: " + mode + " Bus Start: " + startbus + ", Bus End: " + endbus;
        }
        return "Crime: " + crimeIndex + ", Starts at: " + start.toString() + " Ends at: " + end.toString() + " Using: " + mode;
    }
}
