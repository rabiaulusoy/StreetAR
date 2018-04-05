package com.marmara.streetar.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.util.Log;
import android.view.View;

import com.marmara.streetar.R;
import com.marmara.streetar.helper.LocationHelper;
import com.marmara.streetar.model.ARPoint;

/**
 * Created by ntdat on 1/13/17.
 */

public class AROverlayView extends View {
    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    public final int rad = 1000;

    public AROverlayView(Context context) {
        super(context);

        this.context = context;

        //Demo points
//        MainActivity.arPoints.add(new ARPoint("Ben",40.9767,29.1145, 0));
//        MainActivity.arPoints.add(new ARPoint("1",40.9767989,29.1142503, 0));
//        MainActivity.arPoints.add(new ARPoint("2",40.9761111,29.1153741, 0));
//        MainActivity.arPoints.add(new ARPoint("3",40.9774156,29.1139304, 0));
//        MainActivity.arPoints.add(new ARPoint("4",40.9796915,29.1105926, 0));
        //add(new ARPoint("Dolmabahçe Sarayı", 41.0411299, 28.9964835, 0));
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation){
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        double distAtoB = 0;
        super.onDraw(canvas);

        if (currentLocation == null) {
            return;
        }

        final int radius = 50;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(50);
        //distance current location to other pois

        for (int i = 0; i < MainActivity.arPoints.size() && distAtoB >= 0; i ++) {
            distAtoB = distance(currentLocation,MainActivity.arPoints.get(i));
            float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
            float[] pointInECEF = LocationHelper.WSG84toECEF(MainActivity.arPoints.get(i).getLocation());
            float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
            // if z > 0, the point will display on the opposite
            if (cameraCoordinateVector[2] < 0) {
                float x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * canvas.getWidth();
                float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * canvas.getHeight()/2;


                Bitmap _scratch = BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher_foreground);
                if(distAtoB < rad) {
                    //least poi size = 50
                    double basedistance =(rad/(distAtoB+1))/(_scratch.getWidth()-130)*100   + 60;
                    double boyut = (_scratch.getWidth() / (distAtoB) + 50);
                    Bitmap scaledBitmap = scaleDown(_scratch, basedistance, true);
                    canvas.drawBitmap(scaledBitmap, x, y, null);
                    Log.d("Boyut: ", "boyut: " + boyut + " Distance: " + distAtoB + " denenen: " + basedistance + " Name: " + MainActivity.arPoints.get(i).getName());
                }

                canvas.drawText(MainActivity.arPoints.get(i).getName(), x - ((15 * MainActivity.arPoints.get(i).getName().length() )/ 2), y - 20, paint);
                //Toast.makeText(context, "Current Location Latitude: "+currentLocation.getLatitude()+ " longitude: "+ currentLocation.getLongitude()
                //        + " altitude: "+ currentLocation.getAltitude()
                //        + "\nheigt: "+_scratch.getHeight() + "\ndistance:" + distAtoB + " to "+ HomeActivity.arPoints.get(i).getName() , Toast.LENGTH_LONG).show();
            }
        }
    }
    public static Bitmap scaleDown(Bitmap realImage, double maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    public static double distance (Location currentLocation, ARPoint POILocation){
        double dist = 0;
        double radTokm = Math.PI / 180;
        double radius = 6378.137; // Radius of earth in KM
        double lat1 = currentLocation.getLatitude();
        double lat2 = POILocation.getLocation().getLatitude();
        double dLon = radTokm*(currentLocation.getLongitude()-POILocation.getLocation().getLongitude());
        double dLat = radTokm*(currentLocation.getLatitude()-POILocation.getLocation().getLatitude());
        double result = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *Math.sin(dLon/2) * Math.sin(dLon/2);
        dist= (float) 2 * Math.atan2(Math.sqrt(result), Math.sqrt(1-result))*radius*1000;
        // dist = (float) Math.sqrt (radTokm*(currentLocation.getLongitude()-POILocation.getLocation().getLongitude())*radTokm*(currentLocation.getLongitude()-POILocation.getLocation().getLongitude())
         //       + (currentLocation.getLatitude()-POILocation.getLocation().getLatitude())*(currentLocation.getLatitude()-POILocation.getLocation().getLatitude()));
        return dist;

    }
}
