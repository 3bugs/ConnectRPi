package com.promlert.connectrpi.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Promlert on 2018-03-28.
 */

public class IoPin {
    @SerializedName("pin_number")
    public final int pinNumber;
    @SerializedName("state")
    public final int state;

    public IoPin(int pinNumber, int state) {
        this.pinNumber = pinNumber;
        this.state = state;
    }
}
