package com.promlert.connectrpi;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.promlert.connectrpi.etc.UiUpdater;
import com.promlert.connectrpi.etc.Utils;
import com.promlert.connectrpi.iab.IabHelper;
import com.promlert.connectrpi.iab.IabResult;
import com.promlert.connectrpi.iab.Inventory;
import com.promlert.connectrpi.iab.Purchase;
import com.promlert.connectrpi.iab.SkuDetails;
import com.promlert.connectrpi.model.GetStateResponse;
import com.promlert.connectrpi.model.IoPin;
import com.promlert.connectrpi.model.SetOutputResponse;
import com.promlert.connectrpi.net.ApiClient;
import com.promlert.connectrpi.net.WebServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RpiControlActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = RpiControlActivity.class.getName();

    private static final String BASE64_ENCODED_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxMaG8SiK31k1LzkODuYUxJzidFyFDSL9u9/yfBJ7XnPw8JSNMHhe45J7tm6LIYy4l/QkrSv1J7qb7PS9GFVgDKPJQrVa0PJOJiY133Z1fDMfcFcyFBB2WvHgmoO2JXkfGYbtTpyZiuDA2gDiCn3At+Oo4sP3yUsUnfOR36Pllp33AIUyGz0yyMLuZanYaLWXhE4KCRyQNtkNT4NcnofWFw8jRIgvrdBjMgXMLHCBwiJuB5+D1xTUyY06D2Dg6hXW20dVEpQnX+WiScZMc3BShXPokkdtoVf818H5njLxrewNxwAnjL62ePXUheP9GRB4PgiH4u8InPkPNXDbdM3vJwIDAQAB";
    protected static final String KEY_DEVICE_NAME = "device_name";
    protected static final String KEY_PIN_NUMBER_ARRAY = "pin_number_array";

    private static final String SKU_USAGE_TIME_1_HOUR = "usage_time_1_hour";
    private static final String SKU_USAGE_TIME_2_HOUR = "usage_time_2_hour";
    private static final String SKU_USAGE_TIME_3_HOUR = "usage_time_3_hour";
    private static final int REQUEST_CODE_IAB = 1;

    private static int PIN_1_NUMBER = 18, PIN_2_NUMBER = 23;
    private static int[] PIN_NUMBER_ARRAY;

    private ProgressDialog mProgressDialog;

    private WebServices mWebServices;
    private UiUpdater mUiUpdater;

    private final List<SkuDetails> mSkuDetailsList = new ArrayList<>();
    private IabHelper mBillingHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rpi_control);

        Intent intent = getIntent();

        String deviceName = intent.getStringExtra(KEY_DEVICE_NAME);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && deviceName != null) {
            actionBar.setTitle(actionBar.getTitle() + " - " + deviceName);
        }

        PIN_NUMBER_ARRAY = intent.getIntArrayExtra(KEY_PIN_NUMBER_ARRAY);

        if (PIN_NUMBER_ARRAY != null) {
            PIN_1_NUMBER = PIN_NUMBER_ARRAY[0];
            PIN_2_NUMBER = PIN_NUMBER_ARRAY[1];
        } else {
            new AlertDialog.Builder(RpiControlActivity.this)
                    .setMessage("Unexpected error!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .show();
        }

        bindBillingService();
        initRetrofit();
        setupViews();
        setupUiUpdater();
    }

    private void bindBillingService() {
        mBillingHelper = new IabHelper(this, BASE64_ENCODED_PUBLIC_KEY);
        mBillingHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.e(TAG, "Problem setting up In-app Billing: " + result);
                    return;
                }

                // In-app Billing is fully setup!
                List<String> skuList = new ArrayList<>();
                skuList.add(SKU_USAGE_TIME_1_HOUR);
                skuList.add(SKU_USAGE_TIME_2_HOUR);
                skuList.add(SKU_USAGE_TIME_3_HOUR);
                try {
                    mBillingHelper.queryInventoryAsync(true, skuList, null, new IabHelper.QueryInventoryFinishedListener() {
                        @Override
                        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                            if (result.isFailure()) {
                                return;
                            }

                            try {
                                Purchase purchase = inventory.getPurchase(SKU_USAGE_TIME_1_HOUR);
                                if (purchase != null) {
                                    doConsumePurchase(purchase);
                                }
                            } catch (IabHelper.IabAsyncInProgressException e) {
                                Log.e(TAG, "Error consuming purchase 3: " + e.getMessage());
                                e.printStackTrace();
                            }

                            mSkuDetailsList.clear();
                            mSkuDetailsList.add(inventory.getSkuDetails(SKU_USAGE_TIME_1_HOUR));
                            mSkuDetailsList.add(inventory.getSkuDetails(SKU_USAGE_TIME_2_HOUR));
                            mSkuDetailsList.add(inventory.getSkuDetails(SKU_USAGE_TIME_3_HOUR));

                            for (SkuDetails skuDetails : mSkuDetailsList) {
                                if (skuDetails != null) {
                                    Log.i(TAG, "-----");
                                    Log.i(TAG, "SKU Title: " + skuDetails.getTitle());
                                    Log.i(TAG, "SKU Description: " + skuDetails.getDescription());
                                    Log.i(TAG, "SKU Price: " + skuDetails.getPrice());
                                    Log.i(TAG, "SKU: " + skuDetails.getSku());
                                } else {
                                    Log.i(TAG, "No SKU item!");
                                }
                            }
                        }
                    });
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void doConsumePurchase(Purchase purchase) throws IabHelper.IabAsyncInProgressException {
        mBillingHelper.consumeAsync(
                purchase,
                mConsumeFinishedListener
        );
    }

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                @Override
                public void onConsumeFinished(Purchase purchase, IabResult result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, "Consume success: " + purchase.getSku());
                    } else {
                        Log.i(TAG, "Consume error!: " + purchase.getSku());
                    }
                }
            };

    private void initRetrofit() {
        Retrofit retrofit = ApiClient.getClient();
        mWebServices = retrofit.create(WebServices.class);
    }

    private void setupViews() {
        Button turnOnLed1Button = findViewById(R.id.turn_on_led_1_button);
        Button turnOnLed2Button = findViewById(R.id.turn_on_led_2_button);
        turnOnLed1Button.setOnClickListener(this);
        turnOnLed2Button.setOnClickListener(this);
    }

    private void setupUiUpdater() {
        mUiUpdater = new UiUpdater(new Runnable() {
            @Override
            public void run() {
                updateUi();
            }
        });
    }

    private void updateUi() {
        Call<GetStateResponse> call = mWebServices.getStatePin(PIN_NUMBER_ARRAY);
        call.enqueue(new GetStateCallback(RpiControlActivity.this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindBillingService();
    }

    private void unbindBillingService() {
        if (mBillingHelper != null) {
            try {
                mBillingHelper.dispose();
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
        mBillingHelper = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUiUpdater.startUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUiUpdater.stopUpdates();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.turn_on_led_1_button:
                showPurchaseOptions(PIN_1_NUMBER, "LED 1");
                break;
            case R.id.turn_on_led_2_button:
                showPurchaseOptions(PIN_2_NUMBER, "LED 2");
                break;
        }
    }

    private void showPurchaseOptions(final int pinNumber, String caption) {
        String[] dialogItems = new String[mSkuDetailsList.size()];

        for (int i = 0; i < mSkuDetailsList.size(); i++) {
            SkuDetails skuDetails = mSkuDetailsList.get(i);
            String text = String.format(
                    Locale.getDefault(),
                    "%s (%s)",
                    skuDetails.getDescription(),
                    skuDetails.getPrice()
            );
            dialogItems[i] = text;
        }

        new AlertDialog.Builder(this)
                .setTitle("Purchase Options for Starting " + caption)
                .setItems(dialogItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        try {
                            doPurchase(pinNumber, mSkuDetailsList.get(position));
                        } catch (IabHelper.IabAsyncInProgressException e) {
                            Utils.showModalOkDialog(
                                    RpiControlActivity.this,
                                    null,
                                    "Error purchasing: " + e.getLocalizedMessage(),
                                    null
                            );
                            e.printStackTrace();
                        }
                    }
                })
                .show();
    }

    private void doPurchase(final int pinNumber, SkuDetails skuDetails) throws IabHelper.IabAsyncInProgressException {
        mBillingHelper.launchPurchaseFlow(
                this,
                skuDetails.getSku(),
                REQUEST_CODE_IAB,
                new IabHelper.OnIabPurchaseFinishedListener() {
                    @Override
                    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                        if (result.isFailure()) {
                            if (result.getResponse() == -1005) {
                                Utils.showModalOkDialog(
                                        RpiControlActivity.this,
                                        null,
                                        "Purchase canceled",
                                        null
                                );
                            } else {
                                Utils.showModalOkDialog(
                                        RpiControlActivity.this,
                                        null,
                                        "Error purchasing: " + result,
                                        null
                                );
                            }
                            return;
                        }

                        int interval = 0;
                        switch (purchase.getSku()) {
                            case SKU_USAGE_TIME_1_HOUR:
                                interval = 1;
                                break;
                            case SKU_USAGE_TIME_2_HOUR:
                                interval = 2;
                                break;
                            case SKU_USAGE_TIME_3_HOUR:
                                interval = 3;
                                break;
                        }

                        Toast.makeText(
                                RpiControlActivity.this,
                                "You've purchased usage time of " + interval + " hour(s).",
                                Toast.LENGTH_LONG
                        ).show();

                        mProgressDialog = ProgressDialog.show(
                                RpiControlActivity.this,
                                null,
                                "กำลังเชื่อมต่อ RPi...",
                                true
                        );

                        Call<SetOutputResponse> call = mWebServices.turnOnPin(pinNumber, interval);
                        call.enqueue(new SetOutputCallback(
                                RpiControlActivity.this,
                                pinNumber
                        ));

                        try {
                            doConsumePurchase(purchase);
                        } catch (IabHelper.IabAsyncInProgressException e) {
                            Log.e(TAG, "Error consuming purchase: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mBillingHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mBillingHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    static class SetOutputCallback implements Callback<SetOutputResponse> {

        RpiControlActivity mActivity;
        ImageView mLed1ImageView, mLed2ImageView;
        Button mTurnOnLed1Button, mTurnOnLed2Button;
        ProgressDialog mProgressDialog;

        int mPinNumber;

        SetOutputCallback(RpiControlActivity activity, int pinNumber) {
            this.mActivity = activity;
            this.mLed1ImageView = activity.findViewById(R.id.led_1_image_view);
            this.mLed2ImageView = activity.findViewById(R.id.led_2_image_view);
            this.mTurnOnLed1Button = activity.findViewById(R.id.turn_on_led_1_button);
            this.mTurnOnLed2Button = activity.findViewById(R.id.turn_on_led_2_button);
            this.mProgressDialog = activity.mProgressDialog;

            this.mPinNumber = pinNumber;
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
                    if (responseBody.currentState == 0) {
                        if (mPinNumber == PIN_1_NUMBER) {
                            mLed1ImageView.setImageResource(R.drawable.ic_light_off);
                        } else if (mPinNumber == PIN_2_NUMBER) {
                            mLed2ImageView.setImageResource(R.drawable.ic_light_off);
                        }
                    } else if (responseBody.currentState == 1) {
                        if (mPinNumber == PIN_1_NUMBER) {
                            mLed1ImageView.setImageResource(R.drawable.ic_light_on);
                        } else if (mPinNumber == PIN_2_NUMBER) {
                            mLed2ImageView.setImageResource(R.drawable.ic_light_on);
                        }
                    }
                } else {
                    Utils.showModalOkDialog(
                            mActivity,
                            "Error",
                            responseBody.errorMessage,
                            null
                    );
                    String logErrorMessage = String.format(
                            Locale.getDefault(),
                            "Error #%d: %s (%s)",
                            responseBody.errorCode,
                            responseBody.errorMessage,
                            responseBody.errorMessageMore
                    );
                    Log.e(TAG, logErrorMessage);
                }
            }
        }

        @Override
        public void onFailure(@NonNull Call<SetOutputResponse> call, @NonNull Throwable t) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            Utils.showModalOkDialog(
                    mActivity,
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
                    for (IoPin ioPin : responseBody.ioPinList) {
                        if (ioPin.pinNumber == PIN_1_NUMBER) {
                            if (ioPin.state == 0) {
                                mLed1ImageView.setImageResource(R.drawable.ic_light_off);
                            } else if (ioPin.state == 1) {
                                mLed1ImageView.setImageResource(R.drawable.ic_light_on);
                            }
                        } else if (ioPin.pinNumber == PIN_2_NUMBER) {
                            if (ioPin.state == 0) {
                                mLed2ImageView.setImageResource(R.drawable.ic_light_off);
                            } else if (ioPin.state == 1) {
                                mLed2ImageView.setImageResource(R.drawable.ic_light_on);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onFailure(@NonNull Call<GetStateResponse> call, @NonNull Throwable t) {
            // do nothing
        }
    }
}
