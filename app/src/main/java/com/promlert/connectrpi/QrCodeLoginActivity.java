package com.promlert.connectrpi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrCodeLoginActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final String TAG = QrCodeLoginActivity.class.getName();
    protected static final String KEY_QR_CODE_TEXT = "qr_code_text";

    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_login);

        mScannerView = findViewById(R.id.scanner_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result result) {
        String msg = String.format(
                "Code: %s, Format: %s",
                result.getText(),
                result.getBarcodeFormat().toString()
        );
        Log.i(TAG, msg);

        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);

        Intent intent = new Intent();
        intent.putExtra(KEY_QR_CODE_TEXT, result.getText());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        super.onBackPressed();
    }
}
