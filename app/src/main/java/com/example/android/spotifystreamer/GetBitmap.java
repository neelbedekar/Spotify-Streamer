//GetBitmap: simple class that extends AsyncTask to populate a bitmap on a separate thread

package com.example.android.spotifystreamer;

import android.os.AsyncTask;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by Neel Bedekar on 7/31/2015.
 */
class GetBitmap extends AsyncTask<String,Void,Void>{
    @Override
    protected Void doInBackground(String... params) {
        String imUrl = params[0];
        try {
            if(MainActivity.mTwoPane)
                MainActivity.bitmap = Picasso.with(MainActivityFragment.context).load(imUrl).resize(100,100).centerCrop().get();
            else
                MainActivity.bitmap = Picasso.with(MainActivityFragment.context).load(imUrl).resize(200,200).centerCrop().get();
        }catch (IOException e){

        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        MainActivity.loaded = true;
        Log.i(MainActivity.class.getSimpleName(),"hello");
    }
}
