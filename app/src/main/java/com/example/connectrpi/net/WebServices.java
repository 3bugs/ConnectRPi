package com.example.connectrpi.net;

import com.example.connectrpi.model.SetOutputResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface WebServices {

    @FormUrlEncoded
    @POST("led.php/{action}/{pin_number}")
    Call<SetOutputResponse> setOutput(
            @Path("action") String action,
            @Path("pin_number") int pinNumber,
            @Field("interval") int intervalInMinutes
    );

}
