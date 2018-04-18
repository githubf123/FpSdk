package com.app.sdk;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;

import com.fgtit.device.Constants;
import com.fgtit.device.FPModule;

public class FpSdk {
    private FPModule fpm = new FPModule();

    private byte bmpdata[] = new byte[Constants.RESBMP_SIZE];
    private int bmpsize = 0;

    private byte refdata[] = new byte[Constants.TEMPLATESIZE * 4];

    private byte matdata[] = new byte[Constants.TEMPLATESIZE];

    private int worktype = 0;

    private IFpSdk mFpSdk;

    /**
     * Call at onCreate.
     */
    public FpSdk(Activity activityContext, IFpSdk fpSdk) {
        mFpSdk = fpSdk;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());

        initView();
        mFpSdk.onStatusChange(String.valueOf(fpm.getDeviceType()), null);
        //tvDevStatu.setText();

        fpm.InitMatch();
        fpm.SetContextHandler(activityContext, mHandler);
        fpm.SetTimeOut(Constants.TIMEOUT_LONG);
        fpm.SetLastCheckLift(true);
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.FPM_DEVICE:
                    switch (msg.arg1) {
                        case Constants.DEV_OK:
                            mFpSdk.onStatusChange("Open Device OK", null);
                            break;
                        case Constants.DEV_FAIL:
                            mFpSdk.onStatusChange("Open Device Fail", null);
                            break;
                        case Constants.DEV_ATTACHED:
                            mFpSdk.onStatusChange("USB Device Attached", null);
                            break;
                        case Constants.DEV_DETACHED:
                            mFpSdk.onStatusChange("USB Device Detached", null);
                            break;
                        case Constants.DEV_CLOSE:
                            mFpSdk.onStatusChange("Device Close", null);
                            break;
                    }
                    break;
                case Constants.FPM_PLACE:
                    mFpSdk.onStatusChange("Place Finger", null);
                    break;
                case Constants.FPM_LIFT:
                    mFpSdk.onStatusChange("Lift Finger", null);
                    break;
                case Constants.FPM_GENCHAR: {
                    if (msg.arg1 == 1) {
                        if (worktype == 0) {
                            mFpSdk.onStatusChange("Generate Template OK", null);
                            fpm.GetTemplateByGen(matdata);
                            if (fpm.MatchTemplate(refdata, refdata.length, matdata, matdata.length, 80))
                                mFpSdk.onFpDetected(matdata);
                            else
                                mFpSdk.onMatchFail("Match Fail");
                        } else {
                            //tvFpStatu.setText("Enrol Template OK");
                            fpm.GetTemplateByGen(refdata);
                        }
                    } else {
                        mFpSdk.onStatusChange("Generate Template Fail", null);
                    }
                }
                break;
                case Constants.FPM_NEWIMAGE: {
                    bmpsize = fpm.GetBmpImage(bmpdata);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bmpdata, 0, bmpsize);
                    mFpSdk.onStatusChange(null, bmp);
                }
                break;
                case Constants.FPM_TIMEOUT:
                    mFpSdk.onStatusChange("Time Out", null);
                    break;
            }
        }
    };

    public void onResume() {
        fpm.ResumeRegister();
        fpm.OpenDevice();
    }

    public void onPause() {
        try {
            fpm.PauseUnRegister();
            fpm.CloseDevice();
        } catch (IllegalArgumentException e) {
            mFpSdk.onStatusChange(e.toString(), null);
        }
    }

    public boolean matchFP(byte[] fp1, byte[] fp2, int score) {
        return fpm.MatchTemplate(fp1, fp1.length, fp2, fp2.length, score);
    }

    public void generateTemplate1() {
        if (fpm.GenerateTemplate(1)) worktype = 0;
        else mFpSdk.onStatusChange("Busy", null);
    }

    public void generateTemplate4() {
        if (fpm.GenerateTemplate(4)) worktype = 1;
        else mFpSdk.onStatusChange("Busy", null);
    }

    private void initView() {
     /*   tvDevStatu = (TextView) findViewById(R.id.textView1);
        tvFpStatu = (TextView) findViewById(R.id.textView2);
        ivFpImage = (ImageView) findViewById(R.id.imageView1);

        final Button btn_enrol = (Button) findViewById(R.id.button1);
        final Button btn_capture = (Button) findViewById(R.id.button2);

        btn_enrol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fpm.GenerateTemplate(4)) {
                    worktype = 1;
                } else {
                    Toast.makeText(FpSdk.this, "Busy", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fpm.GenerateTemplate(1)) {
                    worktype = 0;
                } else {
                    Toast.makeText(FpSdk.this, "Busy", Toast.LENGTH_SHORT).show();
                }
            }
        });*/
    }

    public interface IFpSdk {
        void onStatusChange(String status, Bitmap bmp);

        void onFpDetected(byte[] metadata);

        void onMatchFail(String error);
    }

}
