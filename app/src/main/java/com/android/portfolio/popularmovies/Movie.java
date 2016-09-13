package com.android.portfolio.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {

    String title;
    String thumbNail;
    String overView;
    String voteAverage;
    String releaseDate;

    public Movie(String title, String thumbNail,
                 String overView, String voteAverage, String releaseDate) {
        this.title = title;
        this.thumbNail = thumbNail;
        this.overView = overView;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
    }

    private Movie(Parcel in) {
        this.title = in.readString();
        this.thumbNail = in.readString();
        this.overView = in.readString();
        this.voteAverage = in.readString();
        this.releaseDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(thumbNail);
        dest.writeString(overView);
        dest.writeString(voteAverage);
        dest.writeString(releaseDate);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }
    };

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("title = ").append(this.title).append("\n")
                .append("thumbNail = ").append(this.thumbNail).append("\n")
                .append("overView = ").append(this.overView).append("\n")
                .append("voteAverage = ").append(this.voteAverage).append("\n")
                .append("releaseDate = ").append(this.releaseDate);

        return sb.toString();
    }
}
