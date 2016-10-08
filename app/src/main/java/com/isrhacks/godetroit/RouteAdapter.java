package com.isrhacks.godetroit;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created by harry on 10/8/16.
 */

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    String[] routes;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView cardView;
        public TextView routeView;
        public TextView timeView;
        public TextView crimeView;
        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.route_card);
            routeView = (TextView) view.findViewById(R.id.route);
            timeView = (TextView) view.findViewById(R.id.total_time);
            crimeView = (TextView) view.findViewById(R.id.crime_text);
        }
    }

    public RouteAdapter(String[] r){
        routes = r;
    }

    @Override
    public RouteAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new card_layout_routes
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout_routes, parent, false);
        // set the card_layout_routes's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView crimeText = (TextView) v.findViewById(R.id.crime_text);
                crimeText.setVisibility(View.VISIBLE);
            }
        });
        holder.routeView.setText(routes[3*position]);
        holder.timeView.setText(routes[3*position+1]);
        holder.crimeView.setText(routes[3*position+2]);
    }

    @Override
    public int getItemCount() {
        return routes.length/2;
    }
}
