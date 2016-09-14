package com.android.portfolio.popularmovies;

public interface IFetchMovieCallback {
    public void onFetchMovieTaskComplete(Movie[] movies);
    public String buildUri();
    public void displayToast();
}
