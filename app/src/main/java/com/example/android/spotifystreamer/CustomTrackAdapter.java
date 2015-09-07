//CustomTrackAdapter: Custom Adapter that extends array adapter to handle an ArrayList of Track objects, which is the top tracks list

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

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Neel Bedekar on 6/20/2015.
 */
public class CustomTrackAdapter extends ArrayAdapter<CustomTrack> {

    ArrayList<CustomTrack> tracks;
    LayoutInflater myLayoutInflater;
    public CustomTrackAdapter(Context context, int layout, ArrayList<CustomTrack> tracks){        //invokes parent constructor and initializes the track and layout inflater objects
        super(context,layout, tracks);
        this.tracks = tracks;
        myLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {         //override the getView(), which is called each time the adapter's contents are modified
        View myView = convertView;
            if (myView == null) {
                myView = myLayoutInflater.inflate(R.layout.list_track_result, parent, false);
            }

            int imSize;
            //three views that represent parts of the list_track_result layout
            TextView albumTextView = (TextView) myView.findViewById(R.id.list_album_search_textView);
            TextView trackTextView = (TextView) myView.findViewById(R.id.list_track_search_textView);
            ImageView myImageView = (ImageView) myView.findViewById(R.id.list_track_result_imageview);

            if (myImageView != null&&(!tracks.isEmpty())) {         //picasso loads the image for the album into the imageview
                if(MainActivity.mTwoPane) imSize = 50;
                else imSize = 200;
                Picasso.with(getContext()).load(tracks.get(position).getimName()).resize(imSize,imSize).centerCrop().into(myImageView);
            }

            if (albumTextView != null&&(!tracks.isEmpty())) {       //album name is set
                albumTextView.setText(tracks.get(position).getalbumName());
            }
            if (trackTextView != null&&(!tracks.isEmpty())) {       //track name is set
                trackTextView.setText(tracks.get(position).getTrackName());
            }
        return myView;
    }

    public ArrayList<CustomTrack> getList(){
        return tracks;
    }

}
