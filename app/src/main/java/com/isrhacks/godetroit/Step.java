package com.isrhacks.godetroit;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

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
    public Polyline route;
    public Step(LatLng start, LatLng end, String mode, Polyline route)
    {
        this.start = start;
        this.end = end;
        this.mode = mode;
        this.route = route;
    }

    public Step(LatLng start, LatLng end, String mode, Polyline route, String startbus, String endbus)
    {
        this.start = start;
        this.end = end;
        this.mode = mode;
        this.route = route;
        this.startbus = startbus;
        this.endbus = endbus;
    }

    public String toString()
    {
        if(mode.equals("TRANSIT"))
        {
            return "Starts at: " + start.toString() + " Ends at: " + end.toString() + " Using: " + mode + " Bus Start: " + startbus + ", Bus End: " + endbus;
        }
        return "Starts at: " + start.toString() + " Ends at: " + end.toString() + " Using: " + mode;
    }
}
