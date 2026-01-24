package com.dasi.sse.gateway;

import com.dasi.sse.dto.CheckWeatherHttpResponse;
import com.dasi.sse.dto.SearchAddressHttpResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

import java.util.Map;

public interface IAmapHttp {

    @GET("weather/weatherInfo")
    Call<CheckWeatherHttpResponse> checkWeather(@QueryMap Map<String, String> query);

    @GET("geocode/geo")
    Call<SearchAddressHttpResponse> searchAddress(@QueryMap Map<String, String> query);

}
