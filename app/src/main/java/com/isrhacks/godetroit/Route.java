package com.isrhacks.godetroit;

import java.util.ArrayList;

/**
 * Created by david on 10/8/16.
 */

public class Route
{
    //List of all the steps in order to complete the route
    public ArrayList<Step> route;

    //time needed to complete this route
    public int time;

    //average crime index
    public double crimeRating;



    public Route(ArrayList<Step> route, int time, double crimeRating)
    {
        this.route = route;
        this.time = time;
        this.crimeRating = crimeRating;
    }

    public void displayRoute()
    {
        for(int i = 0; i < route.size(); i++)
        {
            Step step = route.get(i);
            step.polyline.setVisible(true);
            step.marker1.setVisible(true);
            step.marker2.setVisible(true);
        }
    }

    public void hideRoute()
    {
        for(int i = 0; i < route.size(); i++)
        {
            Step step = route.get(i);
            step.polyline.setVisible(false);
            step.marker1.setVisible(false);
            step.marker2.setVisible(false);
        }
    }

}
