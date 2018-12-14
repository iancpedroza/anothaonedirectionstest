package com.example.android.anothaonedirectionstest;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class QueryUtils {
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();
    private QueryUtils() {
    }

    public static ArrayList<Object> setRoutes(String requestUrl, int route) {
        // Create URL object
        URL url = createUrl(requestUrl);
        Log.v(LOG_TAG, "url created");

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Earthquake}s
        return drawLines(jsonResponse, route);

    }
    private static ArrayList<Object> drawLines(String JSON, int route) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(JSON)) {
            return null;
        }
        Log.e(LOG_TAG, "STARTED PARSING");

        // Create an empty ArrayList that we can start adding earthquakes to
        ArrayList<Object> master = new ArrayList<>();
        ArrayList<double[]> coordinates = new ArrayList<>();
        ArrayList<String[]> polylineArray = new ArrayList<>();
        ArrayList<Integer[]> colorArray = new ArrayList<>();
        Log.e(LOG_TAG, "INITIALIZED ARRAYLISTS");

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string


            // Extract the JSONArray associated with the key called "features",
            // which represents a list of features (or earthquakes).
            //JSONArray firstArray = baseJsonResponse.getJSONArray("routes").getJSONObject(0)
            //.getJSONArray("legs").getJSONObject(0).getJSONArray("steps");


            if (route == 0){
                JSONObject baseJsonResponse = new JSONObject(JSON);
                JSONArray firstArray = baseJsonResponse.getJSONArray("points");
                for (int i = 0; i < firstArray.length(); i++){
                    double[] hotspot = {firstArray.getJSONArray(i).getDouble(1),firstArray.getJSONArray(i).getDouble(3)};
                    coordinates.add(hotspot);
                }
                master.add(coordinates);
            } else {
                JSONArray jsonarray = new JSONArray(JSON);
                JSONObject points = jsonarray.getJSONObject(0);
                JSONObject colors = jsonarray.getJSONObject(1);
                JSONObject polyy = jsonarray.getJSONObject(2);
                JSONArray firstArray = points.getJSONArray("points");

                JSONArray colorsArray = colors.getJSONArray("colors");
                Log.e(LOG_TAG, "COLORS1");
                Integer[] color = new Integer[colorsArray.length()];
                Log.e(LOG_TAG, "COLORS2");
                for (int i = 0; i < colorsArray.length(); i++){
                    color[i] = colorsArray.getInt(i);
                }
                colorArray.add(color);
                Log.e(LOG_TAG, "MADE COLOR ARRAY LIST");

                int size = firstArray.length();

                if (size == 1){
                    JSONArray sndArray = firstArray.getJSONArray(0);
                    for (int i = 0; i < sndArray.length(); i++){
                        double[] hotspot = {sndArray.getJSONArray(i).getDouble(1),sndArray.getJSONArray(i).getDouble(3)};
                        coordinates.add(hotspot);
                    }
                } else if (size == 2){
                    JSONArray sndArray = firstArray.getJSONArray(0);
                    for (int i = 0; i < sndArray.length(); i++){
                        double[] hotspot = {sndArray.getJSONArray(i).getDouble(1),sndArray.getJSONArray(i).getDouble(3)};
                        coordinates.add(hotspot);
                    }
                    JSONArray trdArray = firstArray.getJSONArray(1);
                    for (int i = 0; i < trdArray.length(); i++){
                        double[] hotspot = {trdArray.getJSONArray(i).getDouble(1),trdArray.getJSONArray(i).getDouble(3)};
                        coordinates.add(hotspot);
                    }
                } else if (size == 3){
                    JSONArray sndArray = firstArray.getJSONArray(0);
                    for (int i = 0; i < sndArray.length(); i++){
                        double[] hotspot = {sndArray.getJSONArray(i).getDouble(1),sndArray.getJSONArray(i).getDouble(3)};
                        coordinates.add(hotspot);
                    }
                    JSONArray trdArray = firstArray.getJSONArray(1);
                    for (int i = 0; i < trdArray.length(); i++){
                        double[] hotspot = {trdArray.getJSONArray(i).getDouble(1),trdArray.getJSONArray(i).getDouble(3)};
                        coordinates.add(hotspot);
                    }
                    JSONArray fthArray = firstArray.getJSONArray(2);
                    for (int i = 0; i < fthArray.length(); i++){
                        double[] hotspot = {fthArray.getJSONArray(i).getDouble(1),fthArray.getJSONArray(i).getDouble(3)};
                        coordinates.add(hotspot);
                    }
                } else{
                    JSONArray sndArray = firstArray.getJSONArray(0);
                    for (int i = 0; i < sndArray.length(); i++){
                        double[] hotspot = {sndArray.getJSONArray(i).getDouble(1),sndArray.getJSONArray(i).getDouble(3)};
                        coordinates.add(hotspot);
                    }
                    JSONArray trdArray = firstArray.getJSONArray(1);
                    for (int i = 0; i < trdArray.length(); i++){
                        double[] hotspot = {trdArray.getJSONArray(i).getDouble(1),trdArray.getJSONArray(i).getDouble(3)};
                        coordinates.add(hotspot);
                    }
                    JSONArray fthArray = firstArray.getJSONArray(2);
                    for (int i = 0; i < fthArray.length(); i++){
                        double[] hotspot = {fthArray.getJSONArray(i).getDouble(1),fthArray.getJSONArray(i).getDouble(3)};
                        coordinates.add(hotspot);
                    }
                    JSONArray fifArray = firstArray.getJSONArray(3);
                    for (int i = 0; i < fifArray.length(); i++){
                        double[] hotspot = {fifArray.getJSONArray(i).getDouble(1),fifArray.getJSONArray(i).getDouble(3)};
                        coordinates.add(hotspot);
                    }
                }
                Log.e(LOG_TAG, "MADE COORDINATE ARRAY LIST");

                master.add(coordinates);


                master.add(colorArray);
                JSONArray polylinesArray = polyy.getJSONArray("polylines");
                for (int i = 0; i < polylinesArray.length(); i++){
                    JSONArray path = polylinesArray.getJSONArray(i);
                    String[] polys = new String[path.length()];
                    for (int j = 0; j < path.length(); j++){
                        polys[j] = path.getJSONObject(j).getString("points");
                    }
                    polylineArray.add(polys);
                }
                Log.e(LOG_TAG, "MADE POLYLINE ARRAY LIST");
                master.add(polylineArray);


            }


        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("Hotspots", "Problem parsing the hotspot JSON results", e);
        }
        Log.v(LOG_TAG, "Coordinates returned");
        return master;
    }
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.connect();
            Log.v(LOG_TAG, "Connection set");

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the hotspot JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        Log.v(LOG_TAG, "Stream read from");
        return output.toString();
    }
}
