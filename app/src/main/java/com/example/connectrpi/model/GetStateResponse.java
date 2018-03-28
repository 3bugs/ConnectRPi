package com.example.connectrpi.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetStateResponse {
    @SerializedName("error_code")
    public int errorCode;
    @SerializedName("error_message")
    public String errorMessage;
    @SerializedName("error_message_more")
    public String errorMessageMore;
    @SerializedName("data_list")
    public List<IoPin> ioPinList;
}
