package com.marmara.streetar.detail;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.marmara.streetar.R;
import com.marmara.streetar.main.MainPresenter;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements DetailView {
    @BindView(R.id.placeName)
    TextView placeName;
    @BindView(R.id.vicinity)
    TextView vicinity;
    @BindView(R.id.openingHours)
    TextView openingHours;
    @BindView(R.id.rating)
    TextView rating;
    @BindView(R.id.priceLevel)
    TextView priceLevel;
    //@BindView(R.id.icon)
    //TextView icon;
    @BindView(R.id.type)
    TextView type;
    @BindView(R.id.placeIcon)
    ImageView placeIcon;
    //@BindView(R.id.photos)
    //TextView photos;
    static boolean forThread1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ButterKnife.bind(this);

        Bundle b = getIntent().getExtras();
        int id = b.getInt("poiId");
        placeName.setText(MainPresenter.placeResults.get(id).getName() != null ?
                MainPresenter.placeResults.get(id).getName().toString(): "-");
        vicinity.setText(MainPresenter.placeResults.get(id).getVicinity() != null ?
                MainPresenter.placeResults.get(id).getVicinity().toString(): "-");
        rating.setText(MainPresenter.placeResults.get(id).getRating() != null ?
                MainPresenter.placeResults.get(id).getRating().toString(): "-");
        priceLevel.setText(MainPresenter.placeResults.get(id).getPriceLevel() != null ?
                MainPresenter.placeResults.get(id).getPriceLevel().toString(): "-");
        type.setText(MainPresenter.placeResults.get(id).getTypes().get(0) != null ?
                MainPresenter.placeResults.get(id).getTypes().get(0).toString(): "-");
        //photos.setText(MainPresenter.placeResults.get(id).getPhotos().get(0) != null ?
        //        MainPresenter.placeResults.get(id).getPhotos().get(0).toString(): "");
        //icon.setText(MainPresenter.placeResults.get(id).getIcon() != null ?
        //        MainPresenter.placeResults.get(id).getIcon().toString(): "-");
        openingHours.setText(MainPresenter.placeResults.get(id).getOpeningHours() != null ?
                MainPresenter.placeResults.get(id).getOpeningHours().toString(): "-");

        forThread1 = true;
        LoadBitmap loadBitmap = new LoadBitmap(id);
        loadBitmap.execute();

        while(forThread1){

        }
        placeIcon.setImageBitmap(loadBitmap.bitmap);
    }

    public class LoadBitmap extends AsyncTask<Void, Integer, Bitmap> {
        Bitmap bitmap;
        Integer i;

        private LoadBitmap(int i) {
            this.i = i;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL url = new URL(MainPresenter.placeResults.get(i).getIcon());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() != 200) {
                    Log.e("ERROR:", "not 200!");
                }
                conn.connect();
                InputStream is = conn.getInputStream();

                BufferedInputStream bis = new BufferedInputStream(is);
                try {
                    bitmap = BitmapFactory.decodeStream(bis);
                    forThread1 = false;
                } catch (OutOfMemoryError ex) {
                    bitmap = null;
                }
                bis.close();
                is.close();
            } catch (Exception e) {
                Log.e("Exception:", "Detail Activity, LoadBitmap ***********");
            }
            return bitmap;
        }
    }
}
