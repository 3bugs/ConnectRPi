package com.example.connectrpi;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.connectrpi.etc.UiUpdater;
import com.example.connectrpi.etc.Utils;
import com.example.connectrpi.model.GetStateResponse;
import com.example.connectrpi.model.SetOutputResponse;
import com.example.connectrpi.net.ApiClient;
import com.example.connectrpi.net.WebServices;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RpiControlActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = RpiControlActivity.class.getName();

    private ImageView mLed1ImageView, mLed2ImageView;
    private TextView mTestTextView;
    private WebServices mServices;
    private UiUpdater mUiUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rpi_control);

        Retrofit retrofit = ApiClient.getClient();
        mServices = retrofit.create(WebServices.class);

        Button turnOnLed1Button = findViewById(R.id.turn_on_led_1_button);
        Button turnOnLed2Button = findViewById(R.id.turn_on_led_2_button);
        mLed1ImageView = findViewById(R.id.led_1_image_view);
        mLed2ImageView = findViewById(R.id.led_2_image_view);

        turnOnLed1Button.setOnClickListener(this);
        turnOnLed2Button.setOnClickListener(this);

        mTestTextView = findViewById(R.id.test_text_view);

        setupUiUpdater();
    }

    private void setupUiUpdater() {
        mUiUpdater = new UiUpdater(new Runnable() {
            @Override
            public void run() {
                mTestTextView.setText(mTestTextView.getText().toString() + ">");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUiUpdater.startUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUiUpdater.stopUpdates();
    }

    @Override
    public void onClick(View view) {
        Call<SetOutputResponse> call;

        final ProgressDialog progressDialog = ProgressDialog.show(
                RpiControlActivity.this,
                null,
                "กำลังเชื่อมต่อ...",
                true
        );

        switch (view.getId()) {
            case R.id.turn_on_led_1_button:
                call = mServices.setOutput("turn_on", 18, 3);
                call.enqueue(new SetOutputCallback(
                        RpiControlActivity.this,
                        progressDialog,
                        mLed1ImageView
                ));
                break;
            case R.id.turn_on_led_2_button:
                call = mServices.setOutput("turn_on", 23, 1);
                call.enqueue(new SetOutputCallback(
                        RpiControlActivity.this,
                        progressDialog,
                        mLed2ImageView
                ));
                break;
        }
    }

    static class SetOutputCallback implements Callback<SetOutputResponse> {

        Context mContext;
        ProgressDialog mProgressDialog;
        ImageView mLedImageView;

        SetOutputCallback(Context context, ProgressDialog progressDialog, ImageView ledImageView) {
            this.mContext = context;
            this.mProgressDialog = progressDialog;
            this.mLedImageView = ledImageView;
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
                    //Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT).show();

                    if (responseBody.currentState == 0) {
                        mLedImageView.setImageResource(R.drawable.ic_light_off);
                    } else if (responseBody.currentState == 1) {
                        mLedImageView.setImageResource(R.drawable.ic_light_on);
                    }
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

    static class GetStateCallback implements Callback<GetStateResponse> {

        RpiControlActivity mActivity;
        ImageView mLed1ImageView, mLed2ImageView;
        Button mTurnOnLed1Button, mTurnOnLed2Button;

        GetStateCallback(RpiControlActivity activity) {
            this.mActivity = activity;
            this.mLed1ImageView = activity.findViewById(R.id.led_1_image_view);
            this.mLed2ImageView = activity.findViewById(R.id.led_2_image_view);
            this.mTurnOnLed1Button = activity.findViewById(R.id.turn_on_led_1_button);
            this.mTurnOnLed2Button = activity.findViewById(R.id.turn_on_led_2_button);
        }

        @Override
        public void onResponse(@NonNull Call<GetStateResponse> call,
                               @NonNull Response<GetStateResponse> response) {
            GetStateResponse responseBody = response.body();
            if (responseBody != null) {
                int errorCode = responseBody.errorCode;

                if (errorCode == 0) {

                }
            }
        }

        @Override
        public void onFailure(@NonNull Call<GetStateResponse> call, @NonNull Throwable t) {

        }
    }
}
