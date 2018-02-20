package com.pepin_prestini.elim.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.pepin_prestini.elim.myapplication.Utils.Places.Place;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Adrien on 16/02/2018.
 */

public class PlaceAdapter extends ArrayAdapter<Place> {
    Context context;
    int layoutResourceId;
    ArrayList<Place> places = null;


    /*public PlaceAdapter(Context context, int layoutResourceId, Place[] data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        places = data;
    }*/

    public PlaceAdapter(Context applicationContext, int row, ArrayList<Place> places) {
        super(applicationContext, row, places);
        this.places = places;
        context = applicationContext;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PlaceHolder holder = null;

        if(row == null)
        {
            holder = new PlaceHolder();
            LayoutInflater inflater =LayoutInflater.from(getContext());
            if (inflater != null) {
                row = inflater.inflate(R.layout.row, parent, false);
            }


            holder.imgIcon = row.findViewById(R.id.listview_image);
            holder.txtTitle = row.findViewById(R.id.listview_item_title);
            holder.txtDescription = row.findViewById(R.id.listview_item_short_description);
            row.setTag(holder);
        }
        else
        {
            holder = (PlaceHolder)row.getTag();
        }

        Place place = places.get(position);
        holder.txtTitle.setText(place.nom);
        holder.txtDescription.setText(place.adresse);
        //holder.imgIcon.setImageResource(R.mipmap.logo_app);
        Picasso.with(context).load(place.imagePath).into(holder.imgIcon);
        notifyDataSetChanged();
        return row;
    }

    public Place getPlace(int position){
        return places.get(position);
    }
    static class PlaceHolder
    {
        ImageView imgIcon;
        TextView txtTitle;
        TextView txtDescription;
    }
}
