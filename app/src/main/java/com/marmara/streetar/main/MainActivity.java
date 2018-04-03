package com.marmara.streetar.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.marmara.streetar.POJO.Example;
import com.marmara.streetar.R;
import com.marmara.streetar.RetrofitMaps;
import com.marmara.streetar.model.ARPoint;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SensorEventListener, LocationListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    final static String TAG = "ARActivity";
    private SurfaceView surfaceView;
    private FrameLayout cameraContainerLayout;
    private AROverlayView arOverlayView;
    private Camera camera;
    private ARCamera arCamera;
    protected static List<ARPoint> arPoints;
    //private TextView tvCurrentLocation;

    private SensorManager sensorManager;
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 0;//1000 * 60 * 1; // 1 minute

    private LocationManager locationManager;
    public Location location;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    boolean locationServiceAvailable;

    double latitude;
    double longitude;
    private int PROXIMITY_RADIUS = 500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        arPoints = new ArrayList<ARPoint>();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        if (!isGooglePlayServicesAvailable()) {
            Log.d("onCreate", "Google Play Services not available. Ending Test case.");
            finish();
        } else {
            Log.d("onCreate", "Google Play Services available. Continuing.");
        }

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        cameraContainerLayout = (FrameLayout) findViewById(R.id.camera_container_layout);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        arOverlayView = new AROverlayView(this);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset)
            {
                super.onDrawerSlide(drawerView, slideOffset);
                drawer.bringChildToFront(drawerView);
                drawer.requestLayout();
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void build_retrofit_and_get_response(String type) {
        String url = "https://maps.googleapis.com/maps/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitMaps service = retrofit.create(RetrofitMaps.class);
        Call<Example> call = service.getNearbyPlaces(type, latitude + "," + longitude, PROXIMITY_RADIUS);
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {

                try {
                    //mMap.clear();
                    // This loop will go through all the results and add marker on each location.

                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        final Double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                        final Double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                        final String placeName = response.body().getResults().get(i).getName();
                        String vicinity = response.body().getResults().get(i).getVicinity();
                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng latLng = new LatLng(lat, lng);
                        //Toast.makeText(HomeActivity.this, placeName, Toast.LENGTH_SHORT).show();

                        arPoints.add(new ARPoint(placeName,lat, lng, 0));
                        // Position of Marker on Map
                        //markerOptions.position(latLng);
                        // Adding Title to the Marker
                        //markerOptions.title(placeName + " : " + vicinity);
                        // Adding Marker to the Camera.
                        //Marker m = mMap.addMarker(markerOptions);
                        // Adding colour to the marker
                        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        // move map camera
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                    }
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                Log.d("onFailure", t.toString());
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged", "entered");

        //mLastLocation = location;
        //if (mCurrLocationMarker != null) {
        //    mCurrLocationMarker.remove();
        //}
        //Place current location marker
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        Toast.makeText(MainActivity.this, latitude + "," + longitude, Toast.LENGTH_SHORT).show();

        //arPoints.clear();
        build_retrofit_and_get_response("schools");

        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //MarkerOptions markerOptions = new MarkerOptions();
        //markerOptions.position(latLng);
        //markerOptions.title("Current Position");

        // Adding colour to the marker
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

        // Adding Marker to the Map
        //mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        Log.d("onLocationChanged", String.format("latitude:%.3f longitude:%.3f", latitude, longitude));

        Log.d("onLocationChanged", "Exit");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrixFromVector = new float[16];
            float[] projectionMatrix = new float[16];
            float[] rotatedProjectionMatrix = new float[16];

            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sensorEvent.values);

            if (arCamera != null) {
                projectionMatrix = arCamera.getProjectionMatrix();
            }

            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            this.arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    protected void onPause() {
        releaseCamera();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestLocationPermission();
        requestCameraPermission();
        registerSensors();
        initAROverlayView();
    }

    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            initARCameraView();
        }
    }

    private void registerSensors() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void initAROverlayView() {
        if (arOverlayView.getParent() != null) {
            ((ViewGroup) arOverlayView.getParent()).removeView(arOverlayView);
        }
        cameraContainerLayout.addView(arOverlayView);
    }

    public void initARCameraView() {
        reloadSurfaceView();

        if (arCamera == null) {
            arCamera = new ARCamera(this, surfaceView);
        }
        if (arCamera.getParent() != null) {
            ((ViewGroup) arCamera.getParent()).removeView(arCamera);
        }
        cameraContainerLayout.addView(arCamera);
        arCamera.setKeepScreenOn(true);
        initCamera();
    }

    private void initCamera() {
        int numCams = Camera.getNumberOfCameras();
        if (numCams > 0) {
            try {
                camera = Camera.open();
                camera.startPreview();
                arCamera.setCamera(camera);
            } catch (RuntimeException ex) {
                Toast.makeText(this, "Camera not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void reloadSurfaceView() {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }

        cameraContainerLayout.addView(surfaceView);
    }


    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }

    public void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSIONS_CODE);
        } else {
            initLocationService();
        }
    }

    private void initLocationService() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            this.locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
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

    private void updateLatestLocation() {
        if (arOverlayView != null && location != null) {
            arOverlayView.updateCurrentLocation(location);
            //tvCurrentLocation.setText(String.format("lat: %s \nlon: %s \naltitude: %s \n",
            //location.getLatitude(), location.getLongitude(), location.getAltitude()));
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }
}
