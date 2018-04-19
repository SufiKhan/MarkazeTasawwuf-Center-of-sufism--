package com.markazetasawwuf.Adapter;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sierrasolutionsmacuser5 on 17/1/17.
 */
public class Taleem implements Parcelable {
    public String teaching;
    public String teacher;
    public int res;

    public Taleem(String teaching, String teacher, int res) {
        this.teaching = teaching;
        this.teacher = teacher;
        this.res = res;
    }
    private Taleem(Parcel in) {
        teaching = in.readString();
        teacher = in.readString();
        res = in.readInt();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(teaching);
        parcel.writeString(teacher);
        parcel.writeInt(res);
    }
    public static final Creator<Taleem> CREATOR = new Creator<Taleem>() {
        public Taleem createFromParcel(Parcel in) {
            return new Taleem(in);
        }

        public Taleem[] newArray(int size) {
            return new Taleem[size];

        }
    };

    // all get , set method
}

