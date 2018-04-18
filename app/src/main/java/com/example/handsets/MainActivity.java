package com.example.handsets;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.sdk.FpSdk;

public class MainActivity extends AppCompatActivity implements FpSdk.IFpSdk {

    private TextView tvDevStatu, tvFpStatu;
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
        fpSdk.openSdk();
    }


    @Override
    protected void onPause() {
        super.onPause();
        fpSdk.closeSdk();
    }


    private void initView() {
        tvDevStatu = (TextView) findViewById(R.id.textView1);
        tvFpStatu = (TextView) findViewById(R.id.textView2);
        ivFpImage = (ImageView) findViewById(R.id.imageView1);

        final Button pause = (Button) findViewById(R.id.button1);
        final Button resume = (Button) findViewById(R.id.button3);
        final Button btn_capture = (Button) findViewById(R.id.button2);
        final Button btn_register = (Button) findViewById(R.id.button4);


        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fpSdk.closeSdk();
            }
        });

        resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fpSdk.openSdk();
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
    public void onStatusChange(String status, Bitmap bmp) {
        if (status != null) tvDevStatu.setText(status);
        if (bmp != null) ivFpImage.setImageBitmap(bmp);
    }


    @Override
    public void onFpDetected(byte[] metadata) {
        tvDevStatu.setText(metadata.toString() + " Length : " + metadata.length);
    }

    @Override
    public void onFpFail(String error) {
        tvDevStatu.setText(error);
    }
}
