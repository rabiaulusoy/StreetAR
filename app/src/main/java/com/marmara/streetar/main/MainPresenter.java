package com.marmara.streetar.main;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.marmara.streetar.MyApplication;
import com.marmara.streetar.model.ARPoint;
import com.marmara.streetar.model.NearByApiResponse;
import com.marmara.streetar.model.Result;


import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainPresenter implements LocationListener {
    MainView mainView;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 0;//1000 * 60 * 1; // 1 minute
    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;
    private int PROXIMITY_RADIUS = 500;
    final static String TAG = "ARActivity";
    public LocationManager locationManager;
    public static Location location;
    boolean first = true;

    double latitude, prevlatitude;
    double longitude, prevlongitude;
    double threshold = 0.000001;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    boolean locationServiceAvailable;
    AROverlayView arOverlayView;
    TextView tvCurrentLocation;
    public static List<ARPoint> arPoints;
    public static List<Result> placeResults;
    public static List<Bitmap> _scratch;
    static boolean forThread;
    public String placeType;

    public MainPresenter(MainView mainView) {
        this.mainView = mainView;
        this.arPoints = new ArrayList<ARPoint>();
        this._scratch = new ArrayList<Bitmap>();
    }

    public List<ARPoint> getPlaces(String placeType, Location location) {
        this.placeType = placeType;
        Call<NearByApiResponse> call = MyApplication.getApp().getApiService().getNearbyPlaces(placeType, location.getLatitude() + "," + location.getLongitude(), PROXIMITY_RADIUS);
        call.enqueue(new Callback<NearByApiResponse>() {
            @Override
            public void onResponse(Call<NearByApiResponse> call, Response<NearByApiResponse> response) {
                try {
                    arPoints.clear();
                    _scratch.clear();
                    placeResults = response.body().getResults();
                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        Double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                        Double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                        String placeName = response.body().getResults().get(i).getName();
                        String vicinity = response.body().getResults().get(i).getVicinity();

                        arPoints.add(new ARPoint(placeName, lat, lng, 0));

                        forThread = true;
                        LoadBitmap loadBitmap = new LoadBitmap(i);
                        loadBitmap.execute();
                        while (forThread) {
                            //Log.d("in while:", "*");
                        }
                    }
                } catch (Exception e) {
                    Log.e("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<NearByApiResponse> call, Throwable t) {
                Log.e("onFailure", t.toString());
                t.printStackTrace();
                PROXIMITY_RADIUS += 10000;
            }
        });
        return arPoints;
    }

    public void requestLocationPermission(final Activity activity, AROverlayView arOverlayView,
                                          TextView tvCurrentLocation, String placeType) {
        this.arOverlayView = arOverlayView;
        this.tvCurrentLocation = tvCurrentLocation;
        this.placeType = placeType;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSIONS_CODE);
        } else {
            initLocationService(activity);
        }
    }

    public void initLocationService(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            locationManager = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);
            // Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isNetworkEnabled && !isGPSEnabled) {
                // cannot get location
                this.locationServiceAvailable = false;
            }
            this.locationServiceAvailable = true;

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    updateLatestLocation();
                }
            }

            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateLatestLocation();
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public void updateLatestLocation() {
        if (arOverlayView != null && location != null) {
            arOverlayView.updateCurrentLocation(location);
            tvCurrentLocation.setText(String.format("lat: %s \nlon: %s \naltitude: %s \n",
                    location.getLatitude(), location.getLongitude(), location.getAltitude()));
        }
    }

    public void initAROverlayView(FrameLayout cameraContainerLayout) {
        if (arOverlayView.getParent() != null) {
            ((ViewGroup) arOverlayView.getParent()).removeView(arOverlayView);
        }
        cameraContainerLayout.addView(arOverlayView);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        this.location.setAltitude(0);
        //TODO define a location threshold
        Log.d("onLocationChanged", "onLocationChanged: " + String.format("latitude:%.3f longitude:%.3f", latitude, longitude));
        prevlatitude = latitude;
        prevlongitude = longitude;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        updateLatestLocation();

        if (Math.abs(prevlatitude - latitude) < threshold || Math.abs(prevlongitude - longitude) < threshold
                || first == true) {
            if (this.placeType == "") {
                arPoints = getPlaces("", location);
                first = false;
            }
            else{
                arPoints = getPlaces(this.placeType, location);
            }
        }
    }

    public void navigateToDetailActivity(int i){
        mainView.navigateToDetail(i);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public class LoadBitmap extends AsyncTask<Bitmap, Integer, Bitmap> {
        Bitmap _scratch;
        Integer i;

        private LoadBitmap(int i) {
            this.i = i;
        }

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            try {
                forThread = false;
                URL url = new URL(MainPresenter.placeResults.get(i).getIcon());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() != 200) {
                    Log.e("ERROR:", "not 200!");
                }
                conn.connect();
                InputStream is = conn.getInputStream();

                BufferedInputStream bis = new BufferedInputStream(is);
                try {
                    _scratch = BitmapFactory.decodeStream(bis);
                    MainPresenter._scratch.add(_scratch);
                } catch (OutOfMemoryError ex) {
                    _scratch = null;
                }
                bis.close();
                is.close();
            } catch (Exception e) {
                Log.e("Exception:", "MainPresenter, LoadBitmap ***********");
            }
            return _scratch;
        }
    }

    public void releaseCamera(Camera camera, ARCamera arCamera) {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }
}
