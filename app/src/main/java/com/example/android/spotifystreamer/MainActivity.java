//MainActivity: Main Activity that uses multiple fragments to display each portion of the application

package com.example.android.spotifystreamer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.spotifystreamer.service.PlayService.MediaBinder;
import com.example.android.spotifystreamer.service.PlayService;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements MainActivityFragment.OnArtistSelectedListener, TrackActivityFragment.OnTrackSelectedListener, PlaybackFragment.OnMediaListener, SeekBar.OnSeekBarChangeListener{

    public static boolean mTwoPane;
    public static Context context;
    private boolean textDone;
    private PlayService playService;
    private Intent playIntent;
    private boolean musicBound = false;
    public static ArrayList<CustomTrack> trackList;
    private String countryCode;
    int position = -1;
    public static boolean isPlaying;
    private boolean activated;
    private PlaybackFragment playbackFragment;
    private TextView startText;
    private TextView endText;
    private ShareActionProvider shareActionProvider;
    NotificationManager notificationManager;
    Notification n;
    public static Bitmap bitmap;
    public static boolean loaded;
    private RemoteViews remoteViews;

    //Saves the state of numerous variables, to recreate once the activity is restarted
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isPlaying", isPlaying);
        outState.putParcelableArrayList("trackList", trackList);
        outState.putInt("position",position);
        outState.putBoolean("activated", activated);
        if(playbackFragment!=null){
            Bundle bundle = playbackFragment.getArguments();
            if(playService!=null&&!playService.isPlaying()) bundle.remove("play");
            outState.putBundle("playbackBundle", bundle);
        }
    }

    //overrides onStop() to unbind the service when the activity is destroyed
    @Override
    protected void onStop() {
        super.onStop();
        if(musicBound){
            unbindService(musicConnection);
            musicBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        loaded = false;
        super.onCreate(savedInstanceState);
        //instantiates objects based on if the device has been previously created
        if(savedInstanceState==null) {
            isPlaying = false;
            activated = false;
            Thread thread = new Thread(run);
            thread.start();
        }
        else{
            isPlaying = savedInstanceState.getBoolean("isPlaying");
            trackList = savedInstanceState.getParcelableArrayList("trackList");
            position = savedInstanceState.getInt("position");
            activated = savedInstanceState.getBoolean("activated");
            if(savedInstanceState.containsKey("playbackBundle")){
                playbackFragment = new PlaybackFragment();
                playbackFragment.setArguments(savedInstanceState.getBundle("playbackBundle"));
            }
        }
        setContentView(R.layout.activity_main);
        textDone = (savedInstanceState!=null);
        //the variable mTwoPane is intiailized, which will be used later in the program to perform different actions for the phone and tablet UI
        if (findViewById(R.id.track_container) != null) {
            mTwoPane = true;
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            TrackActivityFragment trackActivityFragment = new TrackActivityFragment();

            // Add the fragment to the 'track_container' FrameLayout
            if(savedInstanceState==null)getSupportFragmentManager().beginTransaction()
                    .add(R.id.track_container, trackActivityFragment,"trackFragment").commit();
        }
        else{
            mTwoPane = false;
            if(savedInstanceState==null)getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment,new MainActivityFragment(),"searchFragment").commit();
        }

        //instantiates the notificationManager
        notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }



    //ServiceConnection used to activate the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaBinder binder = (MediaBinder)service;
            //get service
            playService = binder.getService();
            if(trackList!=null&&!activated) {
                playService.setTrackList(trackList);
                activated = true;
            }
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public void hideKeyboard(EditText text) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(text.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onTrackPlaying(int position) {
        if(playService!=null)   playTrack(position);
    }


    //method that accesses the service object to play the track. In addition, the notification is created and issued in this method.
    public void playTrack(int position) {
        playService.setTrack(position);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // build notification
// the addAction re-use the same intent to keep the example SHORT

        remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);
        n = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setContentIntent(pIntent)
                .setContent(remoteViews)
                .setAutoCancel(true).build();
        remoteViews.setTextViewText(R.id.track_name_notif, playService.getCurrTrack().get(position).getTrackName());
        GetBitmap g = new GetBitmap();
        if(bitmap!=null) bitmap = null;
        g.execute(playService.getCurrTrack().get(position).getimName());
        try {
            Thread.sleep(200);
        } catch (InterruptedException i) {
            return;
        }
        loaded = false;
        remoteViews.setImageViewBitmap(R.id.imagenotileft, bitmap);
        playService.setRemoteViews(remoteViews, n);
        Intent pauseIntent = new Intent("pause");
        pauseIntent.setAction("pause");
        Intent playIntent = new Intent("play");
        playIntent.setAction("play");
        Intent prevIntent = new Intent("prev");
        prevIntent.setAction("prev");
        Intent nextIntent = new Intent("next");
        nextIntent.setAction("next");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,pauseIntent,0);
        remoteViews.setOnClickPendingIntent(R.id.pauseNotif, pendingIntent);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(this,0,playIntent,0);
        remoteViews.setOnClickPendingIntent(R.id.playNotif, pendingIntentPlay);
        PendingIntent pendingIntentPrev = PendingIntent.getBroadcast(this,0,prevIntent,0);
        remoteViews.setOnClickPendingIntent(R.id.previousNotif, pendingIntentPrev);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(this,0,nextIntent,0);
        remoteViews.setOnClickPendingIntent(R.id.nextNotif, pendingIntentNext);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean on = prefs.getBoolean("main", false);

        notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (!on) notificationManager.notify(0, n);
        if (PlaybackFragment.mSeekBar != null)
            PlaybackFragment.mSeekBar.setOnSeekBarChangeListener(this);
        playService.playTrack(false);
        if(shareActionProvider!=null&&playService!=null){
            shareActionProvider.setShareIntent(createShareTrackIntent(true));
        }else{
            Toast.makeText(this, "No track url to share", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void copyText(TextView start, TextView end) {
        startText = start;
        endText = end;
    }

    @Override
    public void setOnListen() {
        PlaybackFragment.mSeekBar.setOnSeekBarChangeListener(this);
    }

    //Runnable used in a separate thread to update the seekbar and allow for scrubbing
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            while(playService==null){
                try{
                    Thread.sleep(1000);
                }catch(Exception e){
                    return;
                }
            }
            while(!playService.isPrepared()){
                try{
                    Thread.sleep(1000);
                }catch(Exception e){
                    return;
                }
            }
            int currentpos = 0;
            PlaybackFragment.mSeekBar.setProgress(currentpos);
            int total = playService.getDuration();
            if(PlaybackFragment.mSeekBar==null) {
                return;
            }
            PlaybackFragment.mSeekBar.setMax(total);
            while (playService!=null){
                while(!playService.isGoing()){
                    try{
                        Thread.sleep(1000);
                    }
                    catch(InterruptedException e){
                        return;
                    }
                }
                PlaybackFragment.mSeekBar.setMax(total);
                try {
                    if(playService.isGoing()) {
                        currentpos = playService.getProgress();
                        PlaybackFragment.mSeekBar.setProgress(currentpos);
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    };

    //callback method from MainActivityFragment. called when an artist item in the list view is selected
    @Override
    public void onArtistSelected(String artistId) {
        TrackActivityFragment trackActivityFragment;
        if(mTwoPane) {
            trackActivityFragment = (TrackActivityFragment)
                    getSupportFragmentManager().findFragmentByTag("trackFragment");
        }
        else{
            trackActivityFragment = new TrackActivityFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, trackActivityFragment, "trackFragment")
                    .addToBackStack("tag").commit();
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        countryCode = prefs.getString(getString(R.string.pref_country_code_key),getString(R.string.pref_country_code_default));
        if(trackActivityFragment !=null) trackActivityFragment.search(artistId, countryCode);
    }

    //the following methods are identical but are callbacks from different fragments
    @Override
    public void changeCurrFrag(ArrayList<CustomTrack> tracks, int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putParcelableArrayList("tracks", tracks);
        playbackFragment = new PlaybackFragment();
        playbackFragment.setArguments(bundle);
    }

    @Override
    public void copyDialog(ArrayList<CustomTrack> tracks, int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putParcelableArrayList("tracks", tracks);
        playbackFragment = new PlaybackFragment();
        playbackFragment.setArguments(bundle);
    }

    //copies the list in the top tracks view's adapter over, and if playIntent is null, it is initialized and the service started
    @Override
    public void copyTrackList(ArrayList<CustomTrack> tracks,boolean play) {
        trackList = tracks;
        if(playIntent==null) {
            playIntent = new Intent(this, PlayService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            //playIntent.putExtra(PlayService.PLAYBACK_URL_KEY, previewUrl);
            startService(playIntent);
        }
    }

    //callback method from TrackActivityFragment to play the selected listview's track
    @Override
    public void onTrackSelected(ArrayList<CustomTrack> tracks, int position, boolean state) {
            String thisUrl = playService.getCurrentUrl(false);
            playService.setTrackList(tracks);
        if(playService!=null&&tracks.get(position).getPreviewUrl().equals(thisUrl)){
            if(playbackFragment!=null&&isPlaying){
                Bundle bundle = playbackFragment.getArguments();
                if(bundle.containsKey("play")){
                    bundle.remove("play");
                }
                if(playService!=null&&playService.isPlaying()){
                    bundle.putBoolean("play", true);
                }
                playbackFragment = new PlaybackFragment();
                playbackFragment.setArguments(bundle);
                if(mTwoPane){
                    playbackFragment.show(getSupportFragmentManager(),"tracks");
                }
                else {
                    playbackFragment.show(getSupportFragmentManager(),"tracks");
                }
            }
            else{
                Toast.makeText(this,"No track is currently playing",Toast.LENGTH_SHORT).show();
            }
        }
        else {
            PlaybackFragment f = new PlaybackFragment();
            this.position = position;
            trackList = tracks;
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            if (!thisUrl.equals(tracks.get(position).getPreviewUrl()) && !state) {
                bundle.putBoolean("play", true);
            } else {
                if (playService.isPlaying()) {
                    bundle.putBoolean("play", true);
                }
            }
            bundle.putParcelableArrayList("tracks", tracks);
            f.setArguments(bundle);
            if (mTwoPane) {
                f.show(getSupportFragmentManager(), "tracks");
            }
            else{
                f.show(getSupportFragmentManager(), "tracks");
            }
            if (playService != null) {
                playTrack(position);
            }
        }
    }

    //method to create the shareIntent
    private Intent createShareTrackIntent(boolean active){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        if(active) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, playService.getCurrentUrl(true));
        }
        return shareIntent;
    }

    //inflates the main menu, and instantiates the shareActionProvider
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        return true;
    }

    //code to handle the selection of the settings menu as well as the now playing button.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if(id == R.id.action_now_playing){
            if(playbackFragment!=null&&isPlaying){
                Bundle bundle = playbackFragment.getArguments();
                if(bundle.containsKey("play")){
                    bundle.remove("play");
                }
                if(playService!=null&&playService.isPlaying()){
                    bundle.putBoolean("play", true);
                }
                playbackFragment = new PlaybackFragment();
                playbackFragment.setArguments(bundle);
                if(mTwoPane) {
                    playbackFragment.show(getSupportFragmentManager(), "tracks");
                }
                else{
                    playbackFragment.show(getSupportFragmentManager(), "tracks");
                }
            }
            else{
                Toast.makeText(this,"No track is currently playing",Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //the adapter in TrackActivityFragment is updated when back is pressed
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(TrackActivityFragment.customTrackAdapter.isEmpty()) {
                TrackActivityFragment.customTrackAdapter.addAll(playService.getCurrTrack());
        }
    }


    @Override
    public void onPlaySelected() {
        if(playService!=null) {
            PlaybackFragment.isPlaying = playService.playPause();
        }
    }


    //next three methods handle displaying progress in the seekbar with scrubbing, as well as showing playback time
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        playService.goTo(seekBar.getProgress());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int prog;
        if(startText!=null&&endText!=null){
            String start = "";
            String end = "";
            prog = seekBar.getProgress()/1000;
            if(prog<10) start +="0:0";
            else start +="0:";
            if((30-prog)<10) end += "0:0";
            else end += "0:";
            start += (new Integer(prog)).toString();
            end += (new Integer(30-prog)).toString();
            startText.setText(start);
            endText.setText(end);
        }
    }
}
