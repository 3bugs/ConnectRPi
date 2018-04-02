package com.promlert.connectrpi.net;

import com.promlert.connectrpi.model.GetStateResponse;
import com.promlert.connectrpi.model.SetOutputResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface WebServices {

    @FormUrlEncoded
    @POST("led.php/turn_on/{pin_number}")
    Call<SetOutputResponse> turnOnPin(
            @Path("pin_number") int pinNumber,
            @Field("interval") int intervalInMinutes
    );

    @FormUrlEncoded
    @POST("led.php/turn_off/{pin_number}")
    Call<SetOutputResponse> turnOffPin(
            @Path("pin_number") int pinNumber,
            @Field("interval") int intervalInMinutes
    );

    @FormUrlEncoded
    @POST("led.php/get_state")
    Call<GetStateResponse> getStatePin(
            //@Path("pin_number") int pinNumber
            @Field("pin_numbers[]") int[] pinNumberArray
    );

    //@POST("led.php/get_state")
    //Call<GetStateResponse> getStateAllPin();

}
