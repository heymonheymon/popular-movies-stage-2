package com.android.portfolio.popularmovies;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchMovieTask extends AsyncTask<Void, Void, Movie[]> {
    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    private IFetchMovieCallback fetchMovieCallback;

    public FetchMovieTask(IFetchMovieCallback fetchMovieCallback) {
        this.fetchMovieCallback = fetchMovieCallback;
    }

    private Movie[] getMovieDataFromJson(String movieJsonStr)
            throws JSONException {

        final String IMAGE_BASE_URL =
                "http://image.tmdb.org/t/p/";
        final String SIZE = "w342";
        final String MDB_RESULTS = "results";
        final String MDB_ORG_TITLE = "original_title";
        final String MDB_POSTER_PATH = "poster_path";
        final String MDB_OVERVIEW = "overview";
        final String MDB_RELEASE_DATE = "release_date";
        final String MDB_VOTE_AVG = "vote_average";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray resultsArray = movieJson.getJSONArray(MDB_RESULTS);

        int len = resultsArray.length();
        Movie[] movieResults = new Movie[len];

        for(int i = 0; i < len; i++) {
            JSONObject movieDetail = resultsArray.getJSONObject(i);

            String originalTitle = movieDetail.getString(MDB_ORG_TITLE);
            String posterPath = movieDetail.getString(MDB_POSTER_PATH);
            String overView = movieDetail.getString(MDB_OVERVIEW);
            String voteAverage = movieDetail.getString(MDB_VOTE_AVG);
            String releaseDate = movieDetail.getString(MDB_RELEASE_DATE);

            StringBuilder sb = new StringBuilder(IMAGE_BASE_URL).append(SIZE).append(posterPath);

            movieResults[i] = new Movie(originalTitle, sb.toString(),
                    overView, voteAverage, releaseDate);
        }

        return movieResults;
    }

    @Override
    protected Movie[] doInBackground(Void... params) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        try {



            URL url = new URL(fetchMovieCallback.buildUri());

            // Create the request to the movie db, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                movieJsonStr = null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                movieJsonStr = null;
            }
            movieJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            movieJsonStr = null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        if(null != movieJsonStr) {
            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Movie[] movies) {
        fetchMovieCallback.onFetchMovieTaskComplete(movies);
    }
}
