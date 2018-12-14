package com.example.android.anothaonedirectionstest;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;

public class CoordinateLoader extends AsyncTaskLoader<ArrayList<Object>> {
    /** Tag for log messages */
    private static final String LOG_TAG = CoordinateLoader.class.getName();

    /** Query URL */
    private String mUrl;
    private int mRoute;
    public CoordinateLoader(Context context, String url, int route) {
        super(context);
        mUrl = url;
        mRoute = route;
    }
    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public ArrayList<Object> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of earthquakes.
        ArrayList<Object> master = QueryUtils.setRoutes(mUrl, mRoute);
        return master;
    }
}
