package com.aaron.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2016/8/18.
 */
public class MusicInfo implements Parcelable{
//    long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
    public long id;
    public String title;
    public String artist;
    public String album;
    public long albumId;
    public long duration;
    public long size;
    public String url;
    public MusicInfo(){

    }

    public MusicInfo(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        duration = in.readLong();
        size = in.readLong();
        url = in.readString();
        albumId = in.readLong();
    }

    public static final Creator<MusicInfo> CREATOR = new Creator<MusicInfo>() {
        @Override
        public MusicInfo createFromParcel(Parcel in) {
            return new MusicInfo(in);
        }

        @Override
        public MusicInfo[] newArray(int size) {
            return new MusicInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(title);
        parcel.writeString(artist);
        parcel.writeString(album);
        parcel.writeLong(duration);
        parcel.writeLong(size);
        parcel.writeString(url);
        parcel.writeLong(albumId);
    }

}
