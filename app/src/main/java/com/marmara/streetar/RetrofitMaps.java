package com.marmara.streetar;

/**
 * Created by r_rab on 3/13/2018.
 */
import com.marmara.streetar.POJO.Example;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by navneet on 17/7/16.
 */
public interface RetrofitMaps {

    /*
     * Retrofit get annotation with our URL
     * And our method that will return us details of student.
     */
    @GET("api/place/nearbysearch/json?sensor=true&key=AIzaSyCJ9dWciwMfsSPv0inv5-S-Uusb-Eov06Y")
    Call<Example> getNearbyPlaces(@Query("type") String type, @Query("location") String location, @Query("radius") int radius);

}