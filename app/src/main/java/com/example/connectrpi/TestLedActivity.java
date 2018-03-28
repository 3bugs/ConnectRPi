package com.example.connectrpi;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.connectrpi.etc.Utils;
import com.example.connectrpi.model.SetOutputResponse;
import com.example.connectrpi.net.ApiClient;
import com.example.connectrpi.net.WebServices;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TestLedActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = TestLedActivity.class.getName();

    private WebServices mServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_led);

        Retrofit retrofit = ApiClient.getClient();
        mServices = retrofit.create(WebServices.class);

        ToggleButton led1ToggleButton = findViewById(R.id.led_1_toggle_button);
        ToggleButton led2ToggleButton = findViewById(R.id.led_2_toggle_button);

        led1ToggleButton.setOnCheckedChangeListener(this);
        led2ToggleButton.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean state) {
        Call<SetOutputResponse> call;

        String action;
        if (state) {
            action = "turn_on";
        } else {
            action = "turn_off";
        }

        switch (compoundButton.getId()) {
            case R.id.led_1_toggle_button:
                call = mServices.setOutput(action, 18, 0);
                call.enqueue(new WebServicesCallback(TestLedActivity.this, null));
                break;
            case R.id.led_2_toggle_button:
                call = mServices.setOutput(action, 23, 0);
                call.enqueue(new WebServicesCallback(TestLedActivity.this, null));
                break;
        }
    }

    static class WebServicesCallback implements Callback<SetOutputResponse> {

        Context mContext;
        ProgressDialog mProgressDialog;

        WebServicesCallback(Context context, ProgressDialog progressDialog) {
            this.mContext = context;
            this.mProgressDialog = progressDialog;
        }

        @Override
        public void onResponse(@NonNull Call<SetOutputResponse> call,
                               @NonNull Response<SetOutputResponse> response) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }

            SetOutputResponse responseBody = response.body();
            if (responseBody != null) {
                int errorCode = responseBody.errorCode;

                if (errorCode == 0) {
                    Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT).show();
                } else {
                    Utils.showModalOkDialog(
                            mContext,
                            "Error #" + errorCode,
                            responseBody.errorMessage,
                            null
                    );
                    Log.e(TAG, responseBody.errorMessageMore);
                }
            }
        }

        @Override
        public void onFailure(@NonNull Call<SetOutputResponse> call, @NonNull Throwable t) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            Utils.showModalOkDialog(
                    mContext,
                    "Error",
                    "เกิดข้อผิดพลาดในการเชื่อมต่อ Server",
                    null
            );
        }
    }
}
