package com.marmara.streetar;

import com.marmara.streetar.model.NearByApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Parth Dave on 31/3/17.
 * Spaceo Technologies Pvt Ltd.
 * parthd.spaceo@gmail.com
 */

public interface NearByApi {
    //calisan api key ornekten
    //AIzaSyDN7RJFmImYAca96elyZlE5s_fhX-MMuhk
    //bizim api key
    //AIzaSyCdmgnkvCQJCH3t3c1svIuadNvKwsEuY_g
    @GET("api/place/nearbysearch/json?sensor=true&key=AIzaSyCdmgnkvCQJCH3t3c1svIuadNvKwsEuY_g")
    Call<NearByApiResponse> getNearbyPlaces(@Query("type") String type, @Query("location") String location, @Query("radius") int radius);
}
