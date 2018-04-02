package com.promlert.connectrpi.model;

import com.google.gson.annotations.SerializedName;

public class SetOutputResponse {
    @SerializedName("error_code")
    public int errorCode;
    @SerializedName("error_message")
    public String errorMessage;
    @SerializedName("error_message_more")
    public String errorMessageMore;
    @SerializedName("state")
    public int currentState;
}
