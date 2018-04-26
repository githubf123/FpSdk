package com.example.handsets;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.sdk.FpSdk;

public class MainActivity extends AppCompatActivity implements FpSdk.IFpSdk {

    private TextView tvDevStatu;
    private ImageView ivFpImage = null;
    private FpSdk fpSdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        fpSdk = new FpSdk(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fpSdk.onResume();
        fpSdk.openSdk();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fpSdk.onPause();
        fpSdk.closeSdk();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fpSdk.release();
    }

    private void initView() {
        tvDevStatu = (TextView) findViewById(R.id.textView1);
        ivFpImage = (ImageView) findViewById(R.id.imageView1);

        final Button open = (Button) findViewById(R.id.button1);
        final Button btn_capture = (Button) findViewById(R.id.button2);
        final Button close = (Button) findViewById(R.id.button3);
        final Button btn_register = (Button) findViewById(R.id.button4);

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fpSdk.openSdk();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fpSdk.closeSdk();
            }
        });

        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fpSdk.generateTemplate1();
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fpSdk.generateTemplate4();
            }
        });
    }

    @Override
    public void onStatusChange(String status) {
        if (status != null) tvDevStatu.setText(status);
    }

    @Override
    public void showLiftFinger(Bitmap bmp) {
        ivFpImage.setImageBitmap(bmp);
    }

    @Override
    public void onDeviceOpen() {
        tvDevStatu.setText("Open Device Ok");
    }

    @Override
    public void onDeviceFail(String error) {
        tvDevStatu.setText(error);
    }

    @Override
    public void showPlaceFinger() {
        tvDevStatu.setText("Place Finger");
    }

    @Override
    public void onFpDetected(byte[] metadata) {
        tvDevStatu.setText(metadata.toString() + " Length : " + metadata.length);
    }

}
