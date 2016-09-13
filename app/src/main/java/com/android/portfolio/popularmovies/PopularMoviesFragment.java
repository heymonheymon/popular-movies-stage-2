package com.android.portfolio.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class PopularMoviesFragment extends Fragment {
    private final String LOG_TAG = PopularMoviesFragment.class.getSimpleName();

    private MovieAdaptor mMovieAdapter;

    private ArrayList<Movie> movieList;

    public PopularMoviesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            movieList = new ArrayList<Movie>();
        }
        else {
            movieList = savedInstanceState.getParcelableArrayList("movies");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", movieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mMovieAdapter = new MovieAdaptor(getActivity(), movieList);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movie);
        gridView.setAdapter(mMovieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = mMovieAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra("com.android.portfolio.popularmovies.Movie", movie);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void updateMovie(){
        FetchMovieTask movieTask = new FetchMovieTask();
        movieTask.execute();
    }

    @Override
    public void onStart(){
        super.onStart();
        updateMovie();
    }

    public class FetchMovieTask extends AsyncTask<Void, Void, Movie[]> {
        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

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

                StringBuilder sb = new StringBuilder(IMAGE_BASE_URL)
                        .append(SIZE)
                        .append(posterPath);

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

                // Build movies uri
                final String MOVIE_POPULAR_BASE_URL =
                        "http://api.themoviedb.org/3/movie/popular?";
                final String MOVIE_TOP_RATED_BASE_URL =
                        "http://api.themoviedb.org/3/movie/top_rated?";

                final String API_KEY_PARAM = "api_key";
                Uri builtUri = null;
                SharedPreferences sharedPrefs =
                        PreferenceManager.getDefaultSharedPreferences(getActivity());
                String movieCategory = sharedPrefs.getString(
                        getString(R.string.pref_movie_category_key),
                        getString(R.string.pref_movie_category_default));

                if(movieCategory.equals(getString(R.string.pref_movie_category_popular))){
                    builtUri = Uri.parse(MOVIE_POPULAR_BASE_URL).buildUpon()
                            .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                            .build();
                } else if(movieCategory.equals(getString(R.string.pref_movie_category_top_rated))) {
                    builtUri = Uri.parse(MOVIE_TOP_RATED_BASE_URL).buildUpon()
                            .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                            .build();
                } else {
                    Log.d(LOG_TAG, "Movie Category not found: " + movieCategory);
                }

                URL url = new URL(builtUri.toString());

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
            if(null != movies) {
                mMovieAdapter.clear();
                for (Movie movie : movies) {
                    mMovieAdapter.add(movie);
                }
            }

            SharedPreferences sharedPrefs =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            String movieCategory = sharedPrefs.getString(
                    getString(R.string.pref_movie_category_key),
                    getString(R.string.pref_movie_category_default));

            if(movieCategory.equals(getString(R.string.pref_movie_category_popular))){
                getActivity().setTitle("Popular Movies");
            } else if(movieCategory.equals(getString(R.string.pref_movie_category_top_rated))) {
                getActivity().setTitle("Top Rated Movies");
            } else {
                Log.d(LOG_TAG, "Movie Category not found: " + movieCategory);
            }
        }
    }
}
