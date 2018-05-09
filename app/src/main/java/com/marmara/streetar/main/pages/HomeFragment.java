package com.marmara.streetar.main.pages;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.marmara.streetar.R;
import com.marmara.streetar.detail.DetailActivity;
import com.marmara.streetar.main.ARCamera;
import com.marmara.streetar.main.AROverlayView;
import com.marmara.streetar.main.MainPresenter;
import com.marmara.streetar.main.MainView;
import com.marmara.streetar.model.ARPoint;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.SENSOR_SERVICE;

public class HomeFragment extends Fragment implements MainView, SensorEventListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;

    public static MainPresenter mainPresenter;
    public AROverlayView arOverlayView;
    public ARCamera arCamera;
    public TextView tvCurrentLocation;
    public Camera camera;

    public FrameLayout cameraContainerLayout;
    public SurfaceView surfaceView;
    public SensorManager sensorManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate:", "on CREATE - - - - - - - - - - -");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainPresenter = new MainPresenter(this);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("onViewCreated:", "on VIEW CREATED - - - - - - - - - - -");
        getActivity().setTitle("StreetView AR");

        arOverlayView = new AROverlayView(getActivity().getApplicationContext());
        sensorManager = (SensorManager) getActivity().getApplicationContext().getSystemService(SENSOR_SERVICE);
        surfaceView = (SurfaceView) getView().findViewById(R.id.surface_view);
        cameraContainerLayout = (FrameLayout) getView().findViewById(R.id.camera_container_layout);
        tvCurrentLocation = (TextView) getView().findViewById(R.id.tv_current_location);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("onResume:", "on RESUME - - - - - - - - - - -");
        mainPresenter.requestLocationPermission(this.getActivity(), arOverlayView,
                tvCurrentLocation, "");
        requestCameraPermission();
        registerSensors();
        mainPresenter.initAROverlayView(cameraContainerLayout);
    }

    @Override
    public void onPause() {
        Log.d("onPause:", "on PAUSE - - - - - - - - - - -");
        mainPresenter.releaseCamera(camera, arCamera);
        super.onPause();
    }

    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            getActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            if (surfaceView.getParent() != null) {
                ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
            }

            cameraContainerLayout.addView(surfaceView);
            if (arCamera == null) {
                arCamera = new ARCamera(getActivity(), surfaceView);
            }
            if (arCamera.getParent() != null) {
                ((ViewGroup) arCamera.getParent()).removeView(arCamera);
            }
            cameraContainerLayout.addView(arCamera);
            arCamera.setKeepScreenOn(true);

            int numCams = Camera.getNumberOfCameras();
            if (numCams > 0) {
                try {
                    camera = Camera.open();
                    camera.startPreview();
                    arCamera.setCamera(camera);
                } catch (RuntimeException ex) {
                    Toast.makeText(getActivity(), "Camera not found", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void registerSensors() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
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
    public void navigateToDetail(int poiId) {
        Intent a = new Intent(getContext(), DetailActivity.class);
        Bundle b = new Bundle();
        b.putInt("poiId", poiId);
        a.putExtras(b);
        startActivity(a);
    }
}
