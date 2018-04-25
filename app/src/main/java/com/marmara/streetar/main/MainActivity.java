package com.marmara.streetar.main;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.location.LocationServices;
import com.marmara.streetar.MyApplication;
import com.marmara.streetar.model.NearByApiResponse;
import com.marmara.streetar.R;
import com.marmara.streetar.model.ARPoint;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.graphics.drawable.PathInterpolatorCompat.EPSILON;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SensorEventListener, LocationListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    final static String TAG = "ARActivity";
    private SurfaceView surfaceView;
    private FrameLayout cameraContainerLayout;
    private AROverlayView arOverlayView;
    private Camera camera;
    private ARCamera arCamera;
    protected static List<ARPoint> arPoints;
    //private TextView tvCurrentLocation;


    private SensorManager sensorManager;
    private List<Sensor> sensors;
    //private Sensor sensorGrav, sensorMag; //low pass filter için kullanılan hardware sensörler için

    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 0;//1000 * 60 * 1; // 1 minute

    static final float ALPHA = 0.5f; //for low-pass filtering

    private LocationManager locationManager;
    public Location location;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    boolean locationServiceAvailable;

    double latitude, prevlatitude;
    double longitude, prevlongitude;
    private int PROXIMITY_RADIUS = 500;

    Fragment fragment;

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
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                drawer.bringChildToFront(drawerView);
                drawer.requestLayout();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        requestLocationPermission();
        requestCameraPermission();
        registerSensors();
        initAROverlayView();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    public void findPlaces(String placeType) {
        if (prevlatitude != latitude && prevlongitude != longitude) {
            arPoints.clear();
        }
        Call<NearByApiResponse> call = MyApplication.getApp().getApiService().getNearbyPlaces(placeType, location.getLatitude() + "," + location.getLongitude(), PROXIMITY_RADIUS);
        call.enqueue(new Callback<NearByApiResponse>() {
            @Override
            public void onResponse(Call<NearByApiResponse> call, Response<NearByApiResponse> response) {
                try {

                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        Double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                        Double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                        String placeName = response.body().getResults().get(i).getName();
                        String vicinity = response.body().getResults().get(i).getVicinity();
                        //TODO
                        if (prevlatitude != latitude && prevlongitude != longitude) {
                            arPoints.add(new ARPoint(placeName, lat, lng, 0));
                        }
                    }
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<NearByApiResponse> call, Throwable t) {
                Log.d("onFailure", t.toString());
                t.printStackTrace();
                PROXIMITY_RADIUS += 10000;
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d("onLocationChanged", "entered");
            this.location = location;
            prevlatitude = latitude;
            prevlongitude = longitude;
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            findPlaces("schools");
            //Toast.makeText(MainActivity.this, latitude + "," + longitude, Toast.LENGTH_SHORT).show();
            Log.d("onLocationChanged", String.format("latitude:%.3f longitude:%.3f", latitude, longitude));

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            surfaceView.setVisibility(View.VISIBLE);
            requestLocationPermission();
            requestCameraPermission();
            registerSensors();
            initAROverlayView();
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
        fragment = null;
        switch (item.getItemId()) {
            case R.id.nav_home:
                surfaceView.setVisibility(View.VISIBLE);
                requestLocationPermission();
                requestCameraPermission();
                registerSensors();
                initAROverlayView();
                break;
            case R.id.nav_favorites:
                fragment = new FavoritesFragment();
                releaseCamera();
                surfaceView.setVisibility(View.GONE);
                break;
            case R.id.nav_categories:
                fragment = new CategoriesFragment();
                releaseCamera();
                surfaceView.setVisibility(View.GONE);
                break;
            case R.id.nav_settings:
                fragment = new SettingsFragment();
                releaseCamera();
                surfaceView.setVisibility(View.GONE);
                break;
            default:
                surfaceView.setVisibility(View.VISIBLE);
                requestLocationPermission();
                requestCameraPermission();
                registerSensors();
                initAROverlayView();
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.addToBackStack("fragment");
            ft.replace(R.id.camera_container_layout, fragment);
            ft.commit();
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
            float[] myRotationVector;
            myRotationVector = sensorEvent.values.clone();
            myRotationVector = lowPass(sensorEvent.values.clone(),myRotationVector);
            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, myRotationVector);
            if (arCamera != null) {
                projectionMatrix = arCamera.getProjectionMatrix();
            }
            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            this.arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
        }
        /*
        //En pure hali
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
        }*/

        /*
        //Bu asıl low pass filter fakat çalışmadı ilginçtir ki
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            myRotationVector = lowPass(sensorEvent.values.clone(),myRotationVector);

            linear_acceleration[0] = sensorEvent.values[0] - myRotationVector[0];
            linear_acceleration[1] = sensorEvent.values[1] - myRotationVector[1];
            linear_acceleration[2] = sensorEvent.values[2] - myRotationVector[2];



            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector,linear_acceleration );
            if (arCamera != null) {
                projectionMatrix = arCamera.getProjectionMatrix();
            }
            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            this.arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
        }else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            myRotationVector = sensorEvent.values.clone();
            // This time step's delta rotation to be multiplied by the current rotation
            // after computing it from the gyro sample data.
            if (timestamp != 0) {
                final float dT = (sensorEvent.timestamp - timestamp) * NS2S;
                // Axis of the rotation sample, not normalized yet.
                float axisX = sensorEvent.values[0];
                float axisY = sensorEvent.values[1];
                float axisZ = sensorEvent.values[2];

                // Calculate the angular speed of the sample
                float omegaMagnitude = (float)Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

                // Normalize the rotation vector if it's big enough to get the axis
                if (omegaMagnitude > EPSILON) {
                    axisX /= omegaMagnitude;
                    axisY /= omegaMagnitude;
                    axisZ /= omegaMagnitude;
                }

                // Integrate around this axis with the angular speed by the time step
                // in order to get a delta rotation from this sample over the time step
                // We will convert this axis-angle representation of the delta rotation
                // into a quaternion before turning it into the rotation matrix.
                float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
                float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);

                linear_acceleration[0] = sinThetaOverTwo * axisX;
                linear_acceleration[1] = sinThetaOverTwo * axisY;
                linear_acceleration[2] = sinThetaOverTwo * axisZ;
                //linear_acceleration[3] = cosThetaOverTwo;
            }
            timestamp = sensorEvent.timestamp;
            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector,linear_acceleration );
            if (arCamera != null) {
                projectionMatrix = arCamera.getProjectionMatrix();
            }
            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            this.arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);


        }*/

    }


    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = ALPHA*output[i] + (1-ALPHA) * input[i];
        }
        return output;
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
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Could not connect google api", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
    }

    @Override
    protected void onPause() {
        releaseCamera();
        super.onPause();
        //Burası da esas low pass filter çalışsa gerekli olacaktı
        /*sensorManager.unregisterListener(this, sensorGrav);
        sensorManager.unregisterListener(this, sensorMag);
        sensorManager = null;*/
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                    }

                } else {
                    Toast.makeText(this, "Location Permission has been denied, can not search the places you want.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @SuppressLint("RestrictedApi")
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
        }
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
        // Low pass filter için hardware sensorleri initialize ediliyor.
        /*//sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            sensorGrav = sensors.get(0);
        }

        sensors = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        if (sensors.size() > 0) {
            sensorMag = sensors.get(0);
        }

        sensorManager.registerListener(this, sensorGrav, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorMag, SensorManager.SENSOR_DELAY_NORMAL);*/
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            } else {// No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
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
                googleAPI.getErrorDialog(this, result, 0).show();
            }
            return false;
        }
        return true;
    }
}
