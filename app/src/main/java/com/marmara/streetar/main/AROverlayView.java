package com.marmara.streetar.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.marmara.streetar.R;
import com.marmara.streetar.helper.LocationHelper;
import com.marmara.streetar.main.pages.HomeFragment;
import com.marmara.streetar.model.ARPoint;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AROverlayView extends View {
    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    public final int rad = 1000;

    // ARCamera arcamera;
    MainActivity main;

    public AROverlayView(Context context) {
        super(context);
        this.context = context;
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Log.d("onDraw", "onDraw running...");

        double distAtoB = 0;
        if (currentLocation == null) {
            return;
        }
        final int radius = 80;
        int color = Color.parseColor("#ff6262");
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint1.setStyle(Paint.Style.FILL);
        paint1.setColor(color);
        paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        /*paint.setTextSize(50);*/
        if (MainPresenter.arPoints.size() == MainPresenter._scratch.size()) {
            for (int i = 0; i < MainPresenter._scratch.size() && distAtoB >= 0; i++) {
                try {
                    distAtoB = distance(currentLocation, MainPresenter.arPoints.get(i));
                }catch (Exception e){
                    Log.e("Error:", "ArOverlayView, distance(), invalid size 20");
                }
                float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
                float[] pointInECEF = LocationHelper.WSG84toECEF(MainPresenter.arPoints.get(i).getLocation());
                float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);
                float[] cameraCoordinateVector = new float[4];
                Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix,
                        0, pointInENU, 0);

                // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
                // if z > 0, the point will display on the opposite
                if (cameraCoordinateVector[2] < 0) {
                    float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
                    float y = ((0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight());

                    double basedistance = (rad / (distAtoB + 1)) / (MainPresenter._scratch.get(i).getWidth()) * 150 + 60;

                    basedistance = basedistance*1.45;

                    y = (float) (((i*basedistance)/canvas.getHeight() - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight());
                    //(float) (basedistance* i) * ((canvas.getHeight()- 100)/canvas.getHeight());

                    if (distAtoB < rad && MainPresenter._scratch.get(i) != null && basedistance > 0) {
                        //least poi size = 50
                        //double boyut = (MainPresenter._scratch.get(i).getWidth() / (distAtoB) + 50);
                        Bitmap scaledBitmap = scaleDown(MainPresenter._scratch.get(i), basedistance, true);
                        MainPresenter.arPoints.get(i).setSize(basedistance);
                        MainPresenter.arPoints.get(i).setX_start(x);
                        MainPresenter.arPoints.get(i).setY_start(y);
                        canvas.drawCircle(x+50, y+50, (float) (basedistance + 10), paint1);
                        canvas.drawCircle(x+50, y+50, (float) (basedistance), paint);
                        //Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.untitled);
                        //canvas.drawBitmap(background, x - 27, y - 30, null);
                        canvas.drawBitmap(scaledBitmap, x, y, null);
                        //Log.d("Boyut: ", " Distance: " + distAtoB +
                        //       " denenen: " + basedistance + " Name: " + MainPresenter.placeResults.get(i).getName());
                    }
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < MainPresenter.arPoints.size(); i++) {
                    if (((int) event.getX() > MainPresenter.arPoints.get(i).getX_start()) && ((MainPresenter.arPoints.get(i).getX_start() + MainPresenter.arPoints.get(i).getSize()) > (int) event.getX())) {
                        if ((int) event.getY() >= MainPresenter.arPoints.get(i).getY_start() && (MainPresenter.arPoints.get(i).getY_start() + MainPresenter.arPoints.get(i).getSize()) > (int) event.getY()) {
                            Toast.makeText(context, MainPresenter.arPoints.get(i).getName() +
                                    "\n" + MainPresenter.placeResults.get(i).getVicinity(), Toast.LENGTH_SHORT).show();
                            Log.d("TOUCHED", "POI: " + i);
                            //HomeFragment.mainPresenter.navigateToDetailActivity(i);
                        }
                    }
                }
                return true;
        }
        return false;
    }

    public static Bitmap scaleDown(Bitmap realImage, double maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());
        if (width < 0 || height < 0 || width == 0 || height == 0) {
            Log.d("ERROR:", "Bitmap scaleDown * * * * * * * * * * * * *");
            width = 1;
            height = 1;
        }
        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;

    }

    public static double distance(Location currentLocation, ARPoint POILocation) {
        double dist = 0;
        double radTokm = Math.PI / 180;
        double radius = 6378.137; // Radius of earth in KM
        double lat1 = currentLocation.getLatitude();
        double lat2 = POILocation.getLocation().getLatitude();
        double dLon = radTokm * (currentLocation.getLongitude() - POILocation.getLocation().getLongitude());
        double dLat = radTokm * (currentLocation.getLatitude() - POILocation.getLocation().getLatitude());
        double result = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1 * Math.PI / 180) *
                Math.cos(lat2 * Math.PI / 180) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        dist = (float) 2 * Math.atan2(Math.sqrt(result), Math.sqrt(1 - result)) * radius * 1000;
        // dist = (float) Math.sqrt (radTokm*(currentLocation.getLongitude()-POILocation.getLocation().getLongitude())*radTokm*(currentLocation.getLongitude()-POILocation.getLocation().getLongitude())
        //       + (currentLocation.getLatitude()-POILocation.getLocation().getLatitude())*(currentLocation.getLatitude()-POILocation.getLocation().getLatitude()));
        return dist;
    }
}
