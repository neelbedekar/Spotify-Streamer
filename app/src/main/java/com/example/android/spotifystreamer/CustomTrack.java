//CustomTrack: class that has string attributes that is used for the top tracks activity results listing

package com.example.android.spotifystreamer;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Neel Bedekar on 6/18/2015.
 */
public class CustomTrack implements Parcelable{
    private String albumName;
    private String imName;
    private String trackName;
    private String previewUrl;
    private String artistName;
    private String bigImage;

    public CustomTrack(String albumName, String imName, String trackName, String previewUrl,String artistName,String bigImage){        //constructor to initialize variables
        this.albumName = albumName;
        this.imName = imName;
        this.trackName = trackName;
        this.previewUrl = previewUrl;
        this.artistName = artistName;
        this.bigImage = bigImage;
    }

    private CustomTrack(Parcel in){
        albumName = in.readString();
        imName = in.readString();
        trackName = in.readString();
        previewUrl = in.readString();
        artistName = in.readString();
        bigImage = in.readString();
    }

    public void writeToParcel(Parcel parcel, int i){
        parcel.writeString(albumName);
        parcel.writeString(imName);
        parcel.writeString(trackName);
        parcel.writeString(previewUrl);
        parcel.writeString(artistName);
        parcel.writeString(bigImage);
    }

    public int describeContents(){
        return 0;
    }

    public static final Parcelable.Creator<CustomTrack> CREATOR = new Parcelable.Creator<CustomTrack>() {
        @Override
        public CustomTrack createFromParcel(Parcel in) {
            return new CustomTrack(in);
        }
        @Override
        public CustomTrack[] newArray(int size) {
            return new CustomTrack[size];
        }
    };

    //getter methods
    public String getalbumName(){
        return albumName;
    }

    public String getimName(){
        return imName;
    }

    public String getTrackName(){
        return trackName;
    }

    public String getPreviewUrl(){
        return previewUrl;
    }

    public String getArtistName(){
        return artistName;
    }

    public String getBigImage() {
        return bigImage;
    }

}


