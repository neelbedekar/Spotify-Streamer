//CustomAdapter: custom adapter that extends ArrayAdapter to have an adapter of CustomItem objects that will display artist search results

package com.example.android.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Neel Bedekar on 6/19/2015.
 */
public class CustomAdapter extends ArrayAdapter<CustomItem> {
    LayoutInflater myLayoutInflater;
    ArrayList<CustomItem> customItems;
    public CustomAdapter(Context context, int layout, ArrayList<CustomItem> items){     //constructor that invokes parent constructor and initializes variables
        super(context,layout, items);
        customItems = items;
        myLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {             //overrides getView(), which is called every time the contents of the adapter are modified
        View myView = convertView;
        if (myView == null) {
            myView = myLayoutInflater.inflate(R.layout.list_item_result, parent, false);
        }
        int imSize;
        //views that hold artist name and image; they are associated with views in the layout, list_item_result
        TextView myTextView = (TextView) myView.findViewById(R.id.list_item_artist_textview);
        ImageView myImageView = (ImageView) myView.findViewById(R.id.list_item_result_imageview);
        if (myImageView != null&&!customItems.isEmpty()) {
            if(MainActivity.mTwoPane) imSize = 50;
            else imSize = 200;
            Picasso.with(getContext()).load(customItems.get(position).getImName()).resize(imSize,imSize).into(myImageView);
        }
        if (myTextView != null&&!customItems.isEmpty()) {
            myTextView.setText(customItems.get(position).getarName());
        }
        return myView;
    }

    public ArrayList<CustomItem> getList(){
        return customItems;
    }

}