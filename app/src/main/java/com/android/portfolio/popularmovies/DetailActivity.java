package com.android.portfolio.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {
        private static String LOG_TAG = DetailFragment.class.getSimpleName();
        private Movie mMovieObj;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            Intent intent = getActivity().getIntent();
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            if (null != intent && intent.hasExtra("com.android.portfolio.popularmovies.Movie") ) {
                Movie movie = intent.getExtras().getParcelable("com.android.portfolio.popularmovies.Movie");
                ((TextView) rootView.findViewById(R.id.detail_title)).setText(movie.title);
                Picasso.with(getContext()).load(movie.thumbNail).into(((ImageView) rootView.findViewById((R.id.detail_thumbNail))));
                ((TextView) rootView.findViewById(R.id.detail_release_date)).setText(movie.releaseDate);
                ((TextView) rootView.findViewById(R.id.detail_vote_average)).setText(movie.voteAverage);
                ((TextView) rootView.findViewById(R.id.detail_overview)).setText(movie.overView);
            }
            return rootView;
        }
    }
}

