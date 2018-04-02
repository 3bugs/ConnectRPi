package com.promlert.connectrpi;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import static com.promlert.connectrpi.QrCodeLoginActivity.KEY_QR_CODE_TEXT;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private static final int REQUEST_SCAN_QR_CODE = 1;

    private final HashMap<String, int[]> mDeviceDataMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDeviceDataMap.put(
                "device0001",
                new int[]{18, 23}
        );
        mDeviceDataMap.put(
                "device0002",
                new int[]{16, 12}
        );

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(actionBar.getTitle() + " - Version: " + versionName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, QrCodeLoginActivity.class);
                startActivityForResult(intent, REQUEST_SCAN_QR_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SCAN_QR_CODE) {
            if (resultCode == RESULT_OK) {
                String qrText = data.getStringExtra(KEY_QR_CODE_TEXT);
                Toast.makeText(MainActivity.this, qrText, Toast.LENGTH_SHORT).show();

                for (Map.Entry<String, int[]> entry : mDeviceDataMap.entrySet()) {
                    String deviceName = entry.getKey();

                    if (deviceName.equalsIgnoreCase(qrText)) {
                        int[] pinNumberArray = entry.getValue();

                        Intent intent = new Intent(MainActivity.this, RpiControlActivity.class);
                        intent.putExtra(RpiControlActivity.KEY_DEVICE_NAME, deviceName);
                        intent.putExtra(RpiControlActivity.KEY_PIN_NUMBER_ARRAY, pinNumberArray);
                        startActivity(intent);
                        return;
                    }
                }

                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Invalid QR code, try again.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();

            } else if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "User canceled qr code scan.");
            }
        }
    }
}
