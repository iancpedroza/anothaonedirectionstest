package com.example.android.anothaonedirectionstest;

import android.Manifest;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.anothaonedirectionstest.models.PlaceInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, LoaderManager.LoaderCallbacks<ArrayList<Object>>{

    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final String LOG_TAG = MapActivity.class.getSimpleName();
    private AutoCompleteTextView mStartingText;
    private AutoCompleteTextView mDestinationText;
    private TextView mTestText;
    private TextView mTestText2;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter1;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter2;
    private GoogleApiClient mGoogleApiClient;
    private GeoApiContext mGeoApiContext;
    private PlaceInfo mStart = new PlaceInfo();
    private PlaceInfo mDestination = new PlaceInfo();
    private ImageButton mMyLocation;
    private Location mCurrentLocation;
    private ImageButton mDirections;
    private int Attempt = 0;
    private List<Polyline> polylines = new ArrayList<Polyline>();
    private int route;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mStartingText = (AutoCompleteTextView) findViewById(R.id.startpoint);
        mDestinationText = (AutoCompleteTextView) findViewById(R.id.destination);
        mTestText = (TextView) findViewById(R.id.testtext);
        mTestText2 = (TextView) findViewById(R.id.testtext2);
        mMyLocation = (ImageButton) findViewById(R.id.mylocation);
        mDirections = (ImageButton) findViewById(R.id.directions);
        getLocationPermission();
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private void init(){
        Log.d(LOG_TAG, "init: initializing");
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_map_api_key))
                    .build();
        }

        mStartingText.setOnItemClickListener(mAutocompleteClickListener1);
        mDestinationText.setOnItemClickListener(mAutocompleteClickListener2);

        mPlaceAutocompleteAdapter1 = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);
        mPlaceAutocompleteAdapter2 = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);

        mStartingText.setAdapter(mPlaceAutocompleteAdapter1);
        mDestinationText.setAdapter(mPlaceAutocompleteAdapter2);

        mStartingText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate(mStartingText.getText().toString());
                    Log.d(LOG_TAG, "trying over here");
                }

                return false;
            }
        });
        mDestinationText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate(mDestinationText.getText().toString());
                    Log.d(LOG_TAG, "also trying over here");
                }

                return false;
            }
        });
        mMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "onClick: clicked mylocation icon");
                hideSoftKeyboard();
                getDeviceLocation();
                mMap.clear();
                for (int i = 0; i < polylines.size();i++){
                    polylines.get(i).remove();
                }
                route = 0;
                calculateDirections();

            }
        });
        mDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "onClick: clicked mylocation icon");
                hideSoftKeyboard();
                for (int i = 0; i < polylines.size();i++){
                    polylines.get(i).remove();
                }
                route = 1;
                calculateDirections();

            }
        });
        hideSoftKeyboard();
    }
    private void geoLocate(String searchString){
        Log.d(LOG_TAG, "geoLocate: geolocating");

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(LOG_TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(LOG_TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(LOG_TAG, "onMapReady: map is ready");
        mMap = googleMap;
        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();


        }
    }

    private void getDeviceLocation(){
        Log.d(LOG_TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(LOG_TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15f));
                            mCurrentLocation = currentLocation;

                        }else{
                            Log.d(LOG_TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(LOG_TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }
    private void initMap(){
        Log.d(LOG_TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }
    private void getLocationPermission(){
        Log.d(LOG_TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(LOG_TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(LOG_TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(LOG_TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }
    @Override
    public Loader<ArrayList<Object>> onCreateLoader(int i, Bundle bundle) {
        mMap.clear();
        for (int j = 0; j < polylines.size();j++){
            polylines.get(j).remove();
        }
        String URL = "";
        if (route == 0){
            URL = "https://mean-newt-52.localtunnel.me/hotspots/"
                    + mCurrentLocation.getLatitude() + "/" + mCurrentLocation.getLongitude()
                    + "/0/0/0";
        } else {
            URL = "https://mean-newt-52.localtunnel.me/hotspots/"
                    + mStart.getLatlng().latitude + "/" + mStart.getLatlng().longitude
                    + "/" + mDestination.getLatlng().latitude + "/" + mDestination.getLatlng().longitude
                    + "/1";
        }

        return new CoordinateLoader(this, URL, route);
    }
    @Override
    public void onLoadFinished(Loader<ArrayList<Object>> loader, ArrayList<Object> master) {
        // Hide loading indicator because the data has been loaded
        Log.e(LOG_TAG, "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
        mTestText.setText("bazinga");
        if (route == 0){
            ArrayList<double[]> coor = (ArrayList)master.get(0);
            setHotspots(coor);
        } else {
            ArrayList<double[]> coor = (ArrayList)master.get(0);
            setHotspots(coor);


            ArrayList<Integer[]> colors = (ArrayList)master.get(1);
            ArrayList<String[]> polies = (ArrayList)master.get(2);
            Integer[] actualcolors = colors.get(0);
            int colorsize = actualcolors.length;
            if (colorsize == 1){
                List<LatLng> polypoints = new LinkedList<LatLng>();
                String[] polygamy = polies.get(0);
                for (int i = 0; i < polygamy.length; i++){
                    PolylineOptions options = new PolylineOptions();
                    options.color(Color.GREEN);
                    options.width(10);
                    options.addAll(PolyUtil.decode(polygamy[i]));
                    mMap.addPolyline(options);
                    List<LatLng> thing = options.getPoints();
                    polypoints.addAll(thing);
                    polylines.add(this.mMap.addPolyline(options));
                }
                zoomRoute(polypoints);
            } else if (colorsize == 2){
                int num = actualcolors[0];
                List<LatLng> polypoints = new LinkedList<LatLng>();
                String[] polygamy = polies.get(num);
                for (int i = 0; i < polygamy.length; i++){
                    PolylineOptions options = new PolylineOptions();
                    options.color(Color.GREEN);
                    options.width(10);
                    options.addAll(PolyUtil.decode(polygamy[i]));
                    mMap.addPolyline(options);
                    List<LatLng> thing = options.getPoints();
                    polypoints.addAll(thing);
                    polylines.add(this.mMap.addPolyline(options));
                }
                zoomRoute(polypoints);
                int numm = actualcolors[1];
                String[] polygammy = polies.get(numm);
                for (int i = 0; i < polygammy.length; i++){
                    PolylineOptions options = new PolylineOptions();
                    options.color(Color.RED);
                    options.width(10);
                    options.addAll(PolyUtil.decode(polygammy[i]));
                    mMap.addPolyline(options);
                    polylines.add(this.mMap.addPolyline(options));
                }
            } else if (colorsize == 3){
                int num = actualcolors[0];
                List<LatLng> polypoints = new LinkedList<LatLng>();
                String[] polygamy = polies.get(num);
                for (int i = 0; i < polygamy.length; i++){
                    PolylineOptions options = new PolylineOptions();
                    options.color(Color.GREEN);
                    options.width(10);
                    options.addAll(PolyUtil.decode(polygamy[i]));
                    mMap.addPolyline(options);
                    List<LatLng> thing = options.getPoints();
                    polypoints.addAll(thing);
                    polylines.add(this.mMap.addPolyline(options));
                }
                zoomRoute(polypoints);
                int numm = actualcolors[1];
                String[] polygammy = polies.get(numm);
                for (int i = 0; i < polygammy.length; i++){
                    PolylineOptions options = new PolylineOptions();
                    options.color(Color.YELLOW);
                    options.width(10);
                    options.addAll(PolyUtil.decode(polygammy[i]));
                    mMap.addPolyline(options);
                    polylines.add(this.mMap.addPolyline(options));
                }
                int nummm = actualcolors[2];
                String[] polygammmy = polies.get(nummm);
                for (int i = 0; i < polygammmy.length; i++){
                    PolylineOptions options = new PolylineOptions();
                    options.color(Color.RED);
                    options.width(10);
                    options.addAll(PolyUtil.decode(polygammmy[i]));
                    mMap.addPolyline(options);
                    polylines.add(this.mMap.addPolyline(options));
                }
            } else if (colorsize == 4){
                int num = actualcolors[0];
                List<LatLng> polypoints = new LinkedList<LatLng>();
                String[] polygamy = polies.get(num);
                for (int i = 0; i < polygamy.length; i++){
                    PolylineOptions options = new PolylineOptions();
                    options.color(Color.GREEN);
                    options.width(10);
                    options.addAll(PolyUtil.decode(polygamy[i]));
                    mMap.addPolyline(options);
                    List<LatLng> thing = options.getPoints();
                    polypoints.addAll(thing);
                    polylines.add(this.mMap.addPolyline(options));
                }
                zoomRoute(polypoints);
                int numm = actualcolors[1];
                String[] polygammy = polies.get(numm);
                for (int i = 0; i < polygammy.length; i++){
                    PolylineOptions options = new PolylineOptions();
                    options.color(Color.YELLOW);
                    options.width(10);
                    options.addAll(PolyUtil.decode(polygammy[i]));
                    mMap.addPolyline(options);
                    polylines.add(this.mMap.addPolyline(options));
                }
                int nummm = actualcolors[2];
                String[] polygammmy = polies.get(nummm);
                for (int i = 0; i < polygammmy.length; i++){
                    PolylineOptions options = new PolylineOptions();
                    options.color(Color.rgb(255,165,0));
                    options.width(10);
                    options.addAll(PolyUtil.decode(polygammmy[i]));
                    mMap.addPolyline(options);
                    polylines.add(this.mMap.addPolyline(options));
                }
                int nummmm = actualcolors[3];
                String[] polygammmmy = polies.get(nummmm);
                for (int i = 0; i < polygammmmy.length; i++){
                    PolylineOptions options = new PolylineOptions();
                    options.color(Color.RED);
                    options.width(10);
                    options.addAll(PolyUtil.decode(polygammmmy[i]));
                    mMap.addPolyline(options);
                    polylines.add(this.mMap.addPolyline(options));
                }

            }
        }

    }
    @Override
    public void onLoaderReset(Loader<ArrayList<Object>> loader) {
        // Loader reset, so we can clear out our existing data.
        mMap.clear();
        for (int i = 0; i < polylines.size();i++){
            polylines.get(i).remove();
        }

    }
    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
    private AdapterView.OnItemClickListener mAutocompleteClickListener1 = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter1.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback1);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback1 = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(LOG_TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);
            mStart.setLatlng(place.getLatLng());

            try{
                mStart = new PlaceInfo();
                mStart.setLatlng(place.getLatLng());
                Log.d(LOG_TAG, "onResult: latlng: " + place.getLatLng());
                mStart.setName(place.getName().toString());
                Log.d(LOG_TAG, "onResult: name: " + place.getName());
                mStart.setAddress(place.getAddress().toString());
                Log.d(LOG_TAG, "onResult: address: " + place.getAddress());
//                mPlace.setAttributions(place.getAttributions().toString());
//                Log.d(TAG, "onResult: attributions: " + place.getAttributions());
                mStart.setId(place.getId());
                Log.d(LOG_TAG, "onResult: id:" + place.getId());
                mStart.setRating(place.getRating());
                Log.d(LOG_TAG, "onResult: rating: " + place.getRating());
                mStart.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(LOG_TAG, "onResult: phone number: " + place.getPhoneNumber());
                mStart.setWebsiteUri(place.getWebsiteUri());
                Log.d(LOG_TAG, "onResult: website uri: " + place.getWebsiteUri());

                Log.d(LOG_TAG, "onResult: place: " + mStart.toString());
            }catch (NullPointerException e){
                Log.e(LOG_TAG, "onResult: NullPointerException: " + e.getMessage() );
            }
            mTestText.setText(String.valueOf(place.getViewport().getCenter().latitude)+" + "+String.valueOf(place.getViewport().getCenter().longitude));
            places.release();
        }
    };
    private AdapterView.OnItemClickListener mAutocompleteClickListener2 = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter2.getItem(i);
            final String placeId = item.getPlaceId();

            com.google.android.gms.common.api.PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback2);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback2 = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(LOG_TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);
            mDestination.setLatlng(place.getLatLng());

            try{
                mDestination = new PlaceInfo();
                mDestination.setLatlng(place.getLatLng());
                Log.d(LOG_TAG, "onResult: latlng: " + place.getLatLng());
                mDestination.setName(place.getName().toString());
                Log.d(LOG_TAG, "onResult: name: " + place.getName());
                mDestination.setAddress(place.getAddress().toString());
                Log.d(LOG_TAG, "onResult: address: " + place.getAddress());
//                mPlace.setAttributions(place.getAttributions().toString());
//                Log.d(TAG, "onResult: attributions: " + place.getAttributions());
                mDestination.setId(place.getId());
                Log.d(LOG_TAG, "onResult: id:" + place.getId());
                mDestination.setRating(place.getRating());
                Log.d(LOG_TAG, "onResult: rating: " + place.getRating());
                mDestination.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(LOG_TAG, "onResult: phone number: " + place.getPhoneNumber());
                mDestination.setWebsiteUri(place.getWebsiteUri());
                Log.d(LOG_TAG, "onResult: website uri: " + place.getWebsiteUri());

                Log.d(LOG_TAG, "onResult: place: " + mDestination.toString());
            }catch (NullPointerException e){
                Log.e(LOG_TAG, "onResult: NullPointerException: " + e.getMessage() );
            }
            mTestText2.setText(String.valueOf(place.getViewport().getCenter().latitude)+" + "+String.valueOf(place.getViewport().getCenter().longitude));
            places.release();
        }
    };
    private void calculateDirections(){

        Log.e(LOG_TAG, "calculateDirections: calculating directions.");
        if (Attempt == 0){
            Attempt += 1;
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(1, null, this);
        } else {
            getLoaderManager().restartLoader(1, null, this);
        }


    }

    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (mMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 50;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }
    public void setHotspots(ArrayList<double[]> coordinates){
        for (int i = 0; i<coordinates.size(); i++){
            double lat = coordinates.get(i)[0];
            double lon = coordinates.get(i)[1];
            LatLng hotspot = new LatLng(lat,lon);
            mMap.addMarker(new MarkerOptions().position(hotspot).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }
}
