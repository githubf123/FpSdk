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


        mFpSdk.onStatusChange(String.valueOf(fpm.getDeviceType()), null);


        fpm.InitMatch();
        fpm.SetContextHandler(activityContext, getHandler());
        fpm.SetTimeOut(Constants.TIMEOUT_LONG);
        fpm.SetLastCheckLift(true);
    }
    
    private Handler getHandler() {
        return new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
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
                                byte matdata[] = new byte[Constants.TEMPLATESIZE];
                                fpm.GetTemplateByGen(matdata);
                                mFpSdk.onFpDetected(matdata);
                            } else {
                                byte refdata[] = new byte[Constants.TEMPLATESIZE * 4];
                                fpm.GetTemplateByGen(refdata);
                                mFpSdk.onFpDetected(refdata);
                            }
                        } else {
                            mFpSdk.onFpFail("Generate Template Fail");
                        }
                    }
                    break;
                    case Constants.FPM_NEWIMAGE: {
                        fpm.GetBmpImage(bmpdata);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bmpdata, 0, bmpdata.length);
                        mFpSdk.onStatusChange(null, bmp);
                    }
                    break;
                    case Constants.FPM_TIMEOUT:
                        mFpSdk.onStatusChange("Time Out", null);
                        break;
                }
                return true;
            }
        });
    }

    public void openSdk() {
        fpm.ResumeRegister();
        fpm.OpenDevice();
    }

    public void closeSdk() {
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

    public interface IFpSdk {
        void onStatusChange(String status, Bitmap bmp);

        void onFpDetected(byte[] metadata);

        void onFpFail(String error);
    }

}
