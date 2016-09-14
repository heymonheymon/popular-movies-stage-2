package com.android.portfolio.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class PopularMoviesFragment extends Fragment implements IFetchMovieCallback {
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
        FetchMovieTask movieTask = new FetchMovieTask(this);
        movieTask.execute();
    }

    @Override
    public void onStart(){
        super.onStart();
        updateMovie();
    }

    @Override
    public void onFetchMovieTaskComplete(Movie[] movies) {
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

    @Override
    public String buildUri() {
        // Build movies uri
        Uri builtUri = null;
        final String MOVIE_POPULAR_BASE_URL =
                "http://api.themoviedb.org/3/movie/popular?";
        final String MOVIE_TOP_RATED_BASE_URL =
                "http://api.themoviedb.org/3/movie/top_rated?";

        final String API_KEY_PARAM = "api_key";
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
            return null;
        }
        return builtUri.toString();
    }
}
