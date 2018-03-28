package com.example.connectrpi;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static com.example.connectrpi.QrCodeLoginActivity.KEY_QR_CODE_TEXT;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private static final int REQUEST_SCAN_QR_CODE = 1;
    private static final String DUMMY_PASS_CODE = "abc123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                String QrText = data.getStringExtra(KEY_QR_CODE_TEXT);

                if (DUMMY_PASS_CODE.equals(QrText)) {
                    Toast.makeText(MainActivity.this, "QR code OK.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(MainActivity.this, RpiControlActivity.class);
                    startActivity(intent);
                } else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Invalid QR code, try again.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // todo:
                                }
                            })
                            .show();
                }
            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }
}
