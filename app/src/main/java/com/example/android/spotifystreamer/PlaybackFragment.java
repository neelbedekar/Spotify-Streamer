package com.example.android.spotifystreamer;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;


public class PlaybackFragment extends android.support.v4.app.DialogFragment {
    private ImageView albumImage;
    private TextView albumName;
    private TextView artistName;
    private TextView trackName;
    private ImageButton previous;
    public static ImageButton playPause;
    private ImageButton next;
    public static boolean isPlaying;
    private CustomTrack track;
    private ArrayList<CustomTrack> tracks;
    private int position;
    private View rootView;
    public static SeekBar mSeekBar;
    private OnMediaListener mCallback;

    public PlaybackFragment(){
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null&&savedInstanceState.containsKey("tracks")){
            tracks = savedInstanceState.getParcelableArrayList("tracks");
            if(savedInstanceState.containsKey("position")) position = savedInstanceState.getInt("position");
        }
        else if (getArguments() != null) {
            if(getArguments().containsKey("tracks")) tracks = getArguments().getParcelableArrayList("tracks");
            if(getArguments().containsKey("position")) position = getArguments().getInt("position");
        }
        track = tracks.get(position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(savedInstanceState!=null) isPlaying = savedInstanceState.getBoolean("isPlaying");
        else {
            isPlaying = getArguments().containsKey("play");
        }
        rootView = inflater.inflate(R.layout.fragment_playback, container, false);
        mCallback.copyText((TextView) rootView.findViewById(R.id.beginning_text),(TextView) rootView.findViewById(R.id.ending_text));
        mSeekBar = (SeekBar) rootView.findViewById(R.id.track_seekbars);
        mCallback.setOnListen();
        setArgs(track);
        previous = (ImageButton) rootView.findViewById(R.id.previousButton);
        playPause = (ImageButton) rootView.findViewById(R.id.playPauseButton);
        if(isPlaying)playPause.setImageResource(R.drawable.ic_media_pause);
        else playPause.setImageResource(R.drawable.ic_media_play);
        next = (ImageButton) rootView.findViewById(R.id.nextButton);

        playPause.setOnClickListener(playClick);
        previous.setOnClickListener(previousClick);
        next.setOnClickListener(nextClick);

        return rootView;
    }

    //plays/pauses the dialog, whichever is appropriate
    View.OnClickListener playClick = new View.OnClickListener() {
        public void onClick(View v) {
            mCallback.onPlaySelected();
            if(isPlaying) playPause.setImageResource(R.drawable.ic_media_play);
            else playPause.setImageResource(R.drawable.ic_media_pause);
            isPlaying = !isPlaying;
        }
    };

    //dialog goes to previous track
    View.OnClickListener previousClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(position>0){
                track = tracks.get(position-1);
                position--;
                setArgs(track);
                playPause.setImageResource(R.drawable.ic_media_pause);
                mCallback.changeCurrFrag(tracks,position);
                mCallback.onTrackPlaying(position);
                isPlaying = true;
            }
            else{
                Toast.makeText(getActivity(),"First track; cannot go previous.",Toast.LENGTH_SHORT).show();
            }
        }
    };

    //advances dialog to next track
    View.OnClickListener nextClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(position<tracks.size()-1){
                track = tracks.get(position+1);
                position++;
                setArgs(track);
                playPause.setImageResource(R.drawable.ic_media_pause);
                mCallback.changeCurrFrag(tracks,position);
                mCallback.onTrackPlaying(position);
                isPlaying = true;
            }
            else{
                Toast.makeText(getActivity(),"Last track; cannot go next.",Toast.LENGTH_SHORT).show();
            }
        }
    };

    //initializes the views in the dialog
    public void setArgs(CustomTrack track){
        artistName = (TextView) rootView.findViewById(R.id.artist_name_textview);
        albumName = (TextView) rootView.findViewById(R.id.album_name_textview);
        artistName.setText(track.getArtistName());
        albumName.setText(track.getalbumName());
        albumImage = (ImageView) rootView.findViewById(R.id.album_imageview);
        Picasso.with(getActivity()).load(track.getBigImage()).resize(250,250).centerCrop().into(albumImage);
        trackName = (TextView) rootView.findViewById(R.id.track_name_textview);
        trackName.setText(track.getTrackName());
    }

    //Interface with callback methods to main activity
    public interface OnMediaListener{
        void onPlaySelected();
        void onTrackPlaying(int position);
        void changeCurrFrag(ArrayList<CustomTrack> tracks, int position);
        void setOnListen();
        void copyText(TextView start, TextView end);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnMediaListener) activity;
        } catch (ClassCastException e) {
            // throw new ClassCastException(activity.toString()
            //         + " must implement OnArtistSelectedListener");
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isPlaying", isPlaying);
        /*if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            outState.putInt("currentPos", mediaPlayer.getCurrentPosition());
        }
        else if (mediaPlayer.getCurrentPosition()>0){
            outState.putInt("currentPos", mediaPlayer.getCurrentPosition());
        }*/
        outState.putParcelableArrayList("tracks", tracks);

        outState.putInt("position",position);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
