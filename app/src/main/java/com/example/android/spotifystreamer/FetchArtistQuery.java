//FetchArtistQuery:Extends AsyncTask to query the Spotify wrapper and update the adapter with an ArrayList of CustomItem objects that show artist names and pictures
package com.example.android.spotifystreamer;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;


class FetchArtistQuery extends AsyncTask<String, Void, ArrayList<CustomItem>> {

    boolean internet;
    List<Artist> list;
    SpotifyApi api;
    SpotifyService spotify;
    ArrayList<CustomItem> customlist;
    protected ArrayList<CustomItem> doInBackground(String...params) {       //takes in an array of strings, with the first element being the search query, and returns a List of CustomItems
        internet = true;            //variable that in onPostExecute dictates the type of Toast to display
        api = new SpotifyApi();
        spotify = api.getService();
        customlist = new ArrayList<>();
        try {
            ArtistsPager results = spotify.searchArtists(params[0]);
            if(results!= null) {
                list = results.artists.items;
                String url;
                for (Artist artist : list) {                //search results are traversed; if no image exists, a url to another image is used
                    if (artist.images.size() > 0)
                        url = artist.images.get(0).url;
                    else
                        url = "http://www.aof-clan.com/AoFWiki/images/6/60/No_Image_Available.png"; //I didn't want to put the url here, but I couldn't add it to the strings.xml file without an error
                    customlist.add(new CustomItem(url, artist.name, artist.id));
                }
            }
        }
        catch(RetrofitError | NullPointerException n){      //catch RetrofitError as well as NullPointerException, usually for bad internet, but generally signifying a bad request
            Log.e(MainActivityFragment.class.getSimpleName(),n.getMessage());
            internet = false;
        }
        return customlist;
    }
    protected void onPostExecute(ArrayList<CustomItem> items){          //takes in a list of CustomItems, and updates the adapter based on its contents
        if(items != null){
            MainActivityFragment.searchAdapter.clear();
            if(items.isEmpty()) {
                if(internet) {
                    Toast.makeText(MainActivityFragment.context, "Artist name not found. Please enter another", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivityFragment.context, "Internet connection appears to be faulty. Please find a stronger connection", Toast.LENGTH_SHORT).show();
                }
            }
            for(CustomItem item: items){
                MainActivityFragment.searchAdapter.add(item);
            }
        }
    }
}
