//PlayService: Service that conducts the MediaPlayer playback

package com.example.android.spotifystreamer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.android.spotifystreamer.CustomTrack;
import com.example.android.spotifystreamer.MainActivity;
import com.example.android.spotifystreamer.MainActivityFragment;
import com.example.android.spotifystreamer.PlaybackFragment;
import com.example.android.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by Neel Bedekar on 7/24/2015.
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener{
    private MediaPlayer mediaPlayer;
    private ArrayList<CustomTrack> trackList;
    private ArrayList<CustomTrack> currTrack;
    private int position;
    public static String currentUrl;
    private boolean prepared;
    private boolean going;
    private final IBinder mediaBind = new MediaBinder();
    private RemoteViews remoteViews;
    private String newIm;
    private Notification n;
    private boolean mTwoPane;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mediaBind;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prepared = false;
        position = 0;
        mediaPlayer = new MediaPlayer();
        initializePlayer();
        mTwoPane = MainActivity.mTwoPane;
    }


    //broadcast receiver to handle clicking the notification playback control buttons
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("play")) {
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                }
            }
            else if (action.equals("pause")){
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
            }
            else if(action.equals("prev")){
                if(position>=1) {
                    position--;
                    playTrack(true);
                    changeNotifImage(currTrack.get(position).getimName());
                    try{
                        Thread.sleep(1000);
                    }
                    catch(InterruptedException e){
                        return;
                    }
                }
            }
            else if(action.equals("next")){
                if(position<currTrack.size()-1) {
                    position++;
                    playTrack(true);
                    changeNotifImage(currTrack.get(position).getimName());
                    try{
                        Thread.sleep(1000);
                    }
                    catch(InterruptedException e){
                        return;
                    }
                }
            }
        }
    };

    public void initializePlayer(){
        mediaPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    public void setRemoteViews(RemoteViews remoteViews, Notification n){
        this.remoteViews = remoteViews;
        this.n = n;
    }

    public void setTrackList(ArrayList<CustomTrack> trackList){
        currTrack = trackList;
        this.trackList = trackList;

    }

    //runnable to populate the Bitmap, which must be done in a separate thread. This method also issues the
    //new notification based on the new album image. Thus, it is called when the previous or next track is selected
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            try {
                Bitmap b;
                if(mTwoPane) {
                    b = Picasso.with(getApplicationContext()).load(newIm).resize(100, 100).centerCrop().get();
                }
                else{
                    b = Picasso.with(getApplicationContext()).load(newIm).resize(200, 200).centerCrop().get();
                }
                Intent intent = new Intent(MainActivityFragment.context, MainActivity.class);
                PendingIntent pIntent = PendingIntent.getActivity(MainActivityFragment.context, 0, intent, 0);
                remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);
                n = new Notification.Builder(MainActivityFragment.context)
                        .setSmallIcon(R.drawable.notification_template_icon_bg)
                        .setContentIntent(pIntent)
                        .setContent(remoteViews)
                        .setAutoCancel(true).build();
                remoteViews.setImageViewBitmap(R.id.imagenotileft, b);
                remoteViews.setTextViewText(R.id.track_name_notif, currTrack.get(position).getTrackName());
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(0, n);
            }catch (IOException i){
                return;
            }
        }
    };

    public void changeNotifImage(String newIm){
        this.newIm = newIm;
        Thread thread = new Thread(run);
        thread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    //if notifications are on, a notification is created, and the track is played.
    //However, if a track is playing in the background, and the selected track is the same as the one playing, this function returns before playing the track again
    public void playTrack(boolean notif){
        if(!notif) {
            IntentFilter pauseIntentFilter = new IntentFilter("pause");
            registerReceiver(broadcastReceiver, pauseIntentFilter);
            IntentFilter playIntentFilter = new IntentFilter("play");
            registerReceiver(broadcastReceiver, playIntentFilter);
            IntentFilter prevIntentFilter = new IntentFilter("prev");
            registerReceiver(broadcastReceiver, prevIntentFilter);
            IntentFilter nextIntentFilter = new IntentFilter("next");
            registerReceiver(broadcastReceiver, nextIntentFilter);
        }
        going = false;
        String url = trackList.get(position).getPreviewUrl();
        if(!notif) {
            if ((currentUrl != null && currentUrl == url) || (currentUrl != null && currTrack != null && currentUrl == currTrack.get(position).getPreviewUrl()))
                    return;
            currentUrl = url;
        }
        currTrack = trackList;
        mediaPlayer.reset();
        try{
            mediaPlayer.setDataSource(url);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mediaPlayer.prepareAsync();
    }

    public ArrayList<CustomTrack> getCurrTrack(){
        return currTrack;
    }

    public String getCurrentUrl(boolean share){
        if(!share) {
            if (currTrack != null && going) return currTrack.get(position).getPreviewUrl();
            else return "false";
        }
        else{
            return currTrack.get(position).getPreviewUrl();
        }
    }

    //called when the play/pause button in the dialog is clicked; it handles both responses appropriately
    public boolean playPause(){
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            return true;
        }
        else{
            mediaPlayer.start();
            going = true;
            currentUrl = trackList.get(position).getPreviewUrl();
            MainActivity.isPlaying = true;
            return false;
        }
    }

    public int getProgress(){
        if(mediaPlayer!=null) return mediaPlayer.getCurrentPosition();
        else return 0;
    }

    public int getDuration(){
        return mediaPlayer.getDuration();
    }

    public boolean isPrepared(){
        return prepared;
    }

    public void goTo(int progress){
        mediaPlayer.seekTo(progress);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        PlaybackFragment.playPause.setImageResource(R.drawable.ic_media_play);
        PlaybackFragment.isPlaying = false;
        currentUrl = "random";
        MainActivity.isPlaying = false;
        going = false;
    }

    public boolean isGoing(){
        return going;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    //once the media player is prepared, variables are initialized
    @Override
    public void onPrepared(MediaPlayer mp) {
        prepared = true;
        mp.seekTo(0);
        mp.start();
        going = true;
        MainActivity.isPlaying = true;
    }

    public void setTrack(int index){
        position = index;
    }

    //Binder class that returns the current playService
    public class MediaBinder extends Binder{
        public PlayService getService(){
            return PlayService.this;
        }
    }
}
