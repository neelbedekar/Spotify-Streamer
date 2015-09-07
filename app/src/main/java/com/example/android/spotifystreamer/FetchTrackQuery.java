
//FetchTrackQuery:Extends AsyncTask to perform top tracks search in background and update the adapter with the top tracks list
package com.example.android.spotifystreamer;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;


class FetchTrackQuery extends AsyncTask<String, Void, List<CustomTrack>> {

    Tracks tracks;
    List<CustomTrack> trackList;
    SpotifyApi api;
    SpotifyService spotify;
    boolean internet;
    String imUrl;
    String bigIm;
    String countryCode;

    //constructor to pass in the country code as specified by the user
    public FetchTrackQuery(String countryCode){
        this.countryCode = countryCode;
    }

    protected List<CustomTrack> doInBackground(String...params) {     //takes in an array of strings, the first item of which is the artist's id, and returns the list of top tracks
        internet = true;            //variable that in onPostExecute dictates the type of Toast to display
        trackList = new ArrayList<>();
        try {
            api = new SpotifyApi();
            spotify = api.getService();
            HashMap<String, Object> queryString = new HashMap<>();       //used to get country code, which is required for top tracks
            queryString.put(SpotifyService.COUNTRY,countryCode);
            Log.i(MainActivity.class.getSimpleName(), queryString.get(SpotifyService.COUNTRY).toString());
            tracks = spotify.getArtistTopTrack(params[0], queryString);
            for(Track t: tracks.tracks){
                if(t.album.images.size()>0) {
                    imUrl = t.album.images.get(0).url;
                }
                else imUrl = "http://www.aof-clan.com/AoFWiki/images/6/60/No_Image_Available.png";
                bigIm = imUrl;
                for(Image im: t.album.images){
                    if(im.height==650) bigIm = im.url;
                }
                trackList.add(new CustomTrack(t.album.name,imUrl,t.name,t.preview_url,t.artists.get(0).name,bigIm));
            }
            return trackList;
        }
        catch(RetrofitError | NullPointerException n){                  //catch RetrofitError as well as NullPointerException, usually for bad internet, but generally signifying a bad request
            internet = false;
            return new ArrayList<>();
        }
    }
    protected void onPostExecute(List<CustomTrack> items){            //takes in a list of top tracks, and updates the track adapter accordingly
        if(items!=null) {
            if(items.isEmpty()) {
                if(internet) {
                    Toast.makeText(TrackActivityFragment.context, "Top Tracks List name not found. Please enter another", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(TrackActivityFragment.context, "Internet connection appears to be faulty. Please find a stronger connection", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                TrackActivityFragment.customTrackAdapter.clear();
                for (int i = 0; i < items.size(); i++) {
                    TrackActivityFragment.customTrackAdapter.add(items.get(i));
                }
            }
        }
    }
}


