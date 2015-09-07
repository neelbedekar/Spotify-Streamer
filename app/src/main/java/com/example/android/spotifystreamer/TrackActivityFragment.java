//TrackActivityFragment: Fragment for track activity, which takes in an artist's id and displays the accompanying top 10 tracks, album names, and album thumbnails

package com.example.android.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class TrackActivityFragment extends Fragment {

    public static Context context;      //static variable to toast if the track list is empty
    private String artistId;            //holds value of the artist's id, passed in through the intent
    public static CustomTrackAdapter customTrackAdapter;
    private ArrayList<CustomTrack> trackList;
    private OnTrackSelectedListener mCallback;
    public TrackActivityFragment(){
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_track, container, false);
        context = getActivity();
        ListView listView = (ListView) rootview.findViewById(R.id.listview_track_search_results);
        customTrackAdapter = new CustomTrackAdapter(getActivity(), R.layout.list_track_result, new ArrayList<CustomTrack>());
        if(savedInstanceState!=null && savedInstanceState.containsKey("tracks")) {     //if the savedInstanceState doesn't exist, the query must be performed, and only done so once, so as to avoid repeating every time the activity is recreated
            trackList = savedInstanceState.getParcelableArrayList("tracks");
            customTrackAdapter.addAll(trackList);
        }
        if(MainActivity.mTwoPane||!MainActivity.mTwoPane) {
            mCallback.copyTrackList(customTrackAdapter.getList(),MainActivity.isPlaying);
        }
        trackList = customTrackAdapter.getList();
        listView.setAdapter(customTrackAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(customTrackAdapter.getList()==null) {
                    if(trackList!=null) {
                    mCallback.copyDialog(trackList, position);
                    mCallback.onTrackSelected(trackList, position, false);
                    }
                }
                else{
                    mCallback.copyDialog(customTrackAdapter.getList(), position);
                    mCallback.onTrackSelected(customTrackAdapter.getList(), position, false);
                }
            }
        });

        return rootview;
    }

    //Interface with callback methods to main activity
    public interface OnTrackSelectedListener {
        void onTrackSelected(ArrayList<CustomTrack> tracks, int position, boolean state);
        void copyTrackList(ArrayList<CustomTrack> tracks,boolean play);
        void copyDialog(ArrayList<CustomTrack> tracks, int position);
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnTrackSelectedListener) activity;
        } catch (ClassCastException e) {
            // throw new ClassCastException(activity.toString()
            //         + " must implement OnArtistSelectedListener");
        }
    }

    public void search(String artistId, String countryCode){
        FetchTrackQuery fetch = new FetchTrackQuery(countryCode);
        fetch.execute(artistId);
    }

    public  void onSaveInstanceState(Bundle outState){      //adds a parcelable arraylist of CustomTrack objects
        trackList = customTrackAdapter.getList();
        outState.putParcelableArrayList("tracks", trackList);
        super.onSaveInstanceState(outState);
    }

    public void update(ArrayList<CustomTrack> list){
        customTrackAdapter.clear();
        customTrackAdapter.addAll(list);
    }
}

