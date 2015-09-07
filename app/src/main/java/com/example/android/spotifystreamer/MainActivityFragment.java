//MainActivityFragment:Fragment class of the first and main activity, which allows the user to search for an artist, and displays search results

package com.example.android.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    public static CustomAdapter searchAdapter;          //static adapter, so that it can be referenced in the asynctask, which isn't part of this class
    public static Context context;                      //static context, so that the main activity can be referenced in the asynctask
    private ArrayList<CustomItem> list;
    OnArtistSelectedListener mCallback;
    private EditText text;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){                //Overrides onCreate; if the state has been saved, sets list to the value of the previous list
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null || !savedInstanceState.containsKey("artists")){
            list = new ArrayList<CustomItem>();
        }
        else{
            list = savedInstanceState.getParcelableArrayList("artists");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootview = inflater.inflate(R.layout.fragment_main, container, false);

        text = (EditText) rootview.findViewById(R.id.artist_name_editText);
        text.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String query;
                if (actionId == EditorInfo.IME_ACTION_SEND) {         //once the text has been entered, the keyboard is hidden and the search is performed
                    mCallback.hideKeyboard(text);
                    query = text.getText().toString();
                    if (!(query.isEmpty())) {
                        if (TrackActivityFragment.customTrackAdapter != null) {
                            if(MainActivity.mTwoPane)   TrackActivityFragment.customTrackAdapter.clear();
                        }
                        search(query);
                    } else {
                        Toast.makeText(getActivity(), "Please enter an artist", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });

        context = getActivity();
        searchAdapter = new CustomAdapter(getActivity(),R.layout.list_item_result, list);    //search adapter is initialized, set to the artist item layout file
        ListView listview = (ListView) rootview.findViewById(R.id.listview_artist_search_results);
        listview.setAdapter(searchAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (MainActivity.mTwoPane) {
                    mCallback.onArtistSelected(searchAdapter.getItem(position).getArtistId());
                } else {
                    mCallback.onArtistSelected(searchAdapter.getItem(position).getArtistId());
                /*CustomItem item = searchAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), TrackActivity.class);
                intent.putExtra(intent.EXTRA_TEXT, item.getArtistId());          //passes artist id, as well as name(next line), to be used in the track activity
                intent.putExtra(intent.EXTRA_TITLE, item.getarName());
                startActivity(intent);*/
                }
            }
        });

        return rootview;
    }

    @Override
    public  void onSaveInstanceState(Bundle outState){              //adds a parcelable arraylist of CustomItem objects
        list = searchAdapter.getList();
        outState.putParcelableArrayList("artists", list);
        super.onSaveInstanceState(outState);
    }

    private void search(String query){          //given a query, a FetchArtistQuery object is created, and the adapter is updated
        FetchArtistQuery fetch = new FetchArtistQuery();
        fetch.execute(query);
    }

    //Interface with callback methods to main activity
    public interface OnArtistSelectedListener {
        void onArtistSelected(String artistId);
        void hideKeyboard(EditText text);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);


        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnArtistSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnArtistSelectedListener");
        }
    }



}


