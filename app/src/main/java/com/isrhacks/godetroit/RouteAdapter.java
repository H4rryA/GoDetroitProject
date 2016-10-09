package com.isrhacks.godetroit;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;


/**
 * Created by harry on 10/8/16.
 */

class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    String[] routes;
    private boolean expandedView = false;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView routeView;
        TextView timeView;
        TextView crimeView;
        TextView startStop;
        TextView endStop;
        Button startRoute;
        ViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.route_card);
            routeView = (TextView) view.findViewById(R.id.route);
            timeView = (TextView) view.findViewById(R.id.total_time);
            crimeView = (TextView) view.findViewById(R.id.crime_text);
            startStop = (TextView) view.findViewById(R.id.startStop);
            endStop = (TextView) view.findViewById(R.id.endStop);
            startRoute = (Button) view.findViewById(R.id.start_route);
        }
    }

    RouteAdapter(String[] r){
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
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //TODO:FIXTHISCLOSINGOPENINGMESSMONKEYS
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(holder.cardView);
                if(!expandedView) {
                    RoutesActivity.displayRoute(position);
                    holder.crimeView.setVisibility(View.VISIBLE);
                    holder.startStop.setVisibility(View.VISIBLE);
                    holder.endStop.setVisibility(View.VISIBLE);
                    holder.startRoute.setVisibility(View.VISIBLE);
                    holder.startRoute.setTag(position);
                    expandedView = true;
                }else{
                    RoutesActivity.hideRoute(position);
                    holder.crimeView.setVisibility(View.GONE);
                    holder.startStop.setVisibility(View.GONE);
                    holder.endStop.setVisibility(View.GONE);
                    holder.startRoute.setVisibility(View.GONE);
                    expandedView = false;
                }
            }
        });
        holder.routeView.setText(routes[5*position]);
        holder.timeView.setText(routes[5*position+1]);
        holder.crimeView.setText(routes[5*position+2]);
        holder.startStop.setText(routes[5*position+3]);
        holder.endStop.setText(routes[5*position+4]);
    }

    @Override
    public int getItemCount() {
        return routes.length/5;
    }

}
