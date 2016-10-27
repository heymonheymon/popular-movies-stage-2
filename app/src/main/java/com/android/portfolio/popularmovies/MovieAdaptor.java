package com.android.portfolio.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieAdaptor extends ArrayAdapter<Movie> {
    private static final String LOG_TAG = MovieAdaptor.class.getSimpleName();

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public ImageView imageView;
    }

    public MovieAdaptor(Activity context, List<Movie> movies) {
        super(context, 0, movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_movie, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.grid_item_movie_imageview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Movie movie = getItem(position);

        Picasso.with(getContext()).load(movie.thumbNail).into(holder.imageView);

        return convertView;
    }
}
