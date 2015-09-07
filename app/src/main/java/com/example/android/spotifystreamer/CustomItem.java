//CustomItem: class that has string attributes that is used for the main activity search and results listing

package com.example.android.spotifystreamer;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Neel Bedekar on 6/18/2015.
 */
public class CustomItem implements Parcelable{
    private String arName;
    private String imName;
    private String artistId;

    public CustomItem(String imName, String arName, String artistId){        //constructor to initialize variables
        this.artistId = artistId;
        this.imName = imName;
        this.arName = arName;
    }

    private CustomItem(Parcel in){
        arName = in.readString();
        imName = in.readString();
        artistId = in.readString();
    }

    public void writeToParcel(Parcel parcel, int i){
        parcel.writeString(arName);
        parcel.writeString(imName);
        parcel.writeString(artistId);
    }

    public int describeContents(){
        return 0;
    }

    public static final Parcelable.Creator<CustomItem> CREATOR = new Parcelable.Creator<CustomItem>() {
        @Override
        public CustomItem createFromParcel(Parcel in) {
            return new CustomItem(in);
        }
        @Override
        public CustomItem[] newArray(int size) {
            return new CustomItem[size];
        }
    };

    //getter methods
    public String getarName(){
        return arName;
    }

    public String getArtistId(){
        return artistId;
    }

    public String getImName(){
        return imName;
    }

}


