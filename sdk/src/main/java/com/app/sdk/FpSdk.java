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
    private Handler mHandler;

    /**
     * Call at onCreate.
     */
    public FpSdk(Activity activityContext, IFpSdk fpSdk) {
        mFpSdk = fpSdk;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());


        mFpSdk.onStatusChange(String.valueOf(fpm.getDeviceType()));

        initHandler();
        fpm.InitMatch();
        fpm.SetContextHandler(activityContext, mHandler);
        fpm.SetTimeOut(Constants.TIMEOUT_LONG);
        fpm.SetLastCheckLift(true);
    }

    /**
     * Call at onResume.
     */
    public void onResume() {
        fpm.ResumeRegister();
    }

    /**
     * Call at onPause.
     */
    public void onPause() {
        try {
            fpm.PauseUnRegister();
        } catch (IllegalArgumentException e) {
            mFpSdk.onStatusChange(e.toString());
        }
    }

    /**
     * Always call after {@link #onResume()}
     */
    public void openSdk() {
        fpm.OpenDevice();
    }

    /**
     * Always call after {@link #onPause()}
     */
    public void closeSdk() {
        fpm.CloseDevice();
    }

    /**
     * Call at onDestroy.
     */
    public void release() {
        mHandler = null;
    }

    /**
     * Call to match fingerprints
     *
     * @param fp1   fingerprint one.
     * @param fp2   fingerprint two.
     * @param score score of matching.
     * @return true or false.
     */
    public boolean matchFP(byte[] fp1, byte[] fp2, int score) {
        return fpm.MatchTemplate(fp1, fp1.length, fp2, fp2.length, score);
    }

    /**
     * Cancels fp reading.
     */
    public void cancel() {
        fpm.Cancle();
        mFpSdk.onStatusChange("Cancel");
    }

    /**
     * Ready to listen 1 fingerprint.
     */
    public void generateTemplate1() {
        if (fpm.GenerateTemplate(1)) worktype = 0;
        else mFpSdk.onStatusChange("Busy");
    }

    /**
     * Ready to listen 4 fingerprints.
     */
    public void generateTemplate4() {
        if (fpm.GenerateTemplate(4)) worktype = 1;
        else mFpSdk.onStatusChange("Busy");
    }

    private void initHandler() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.FPM_DEVICE:
                        switch (msg.arg1) {
                            case Constants.DEV_OK:
                                mFpSdk.onStatusChange("Open Device OK");
                                break;
                            case Constants.DEV_FAIL:
                                mFpSdk.onStatusChange("Open Device Fail");
                                break;
                            case Constants.DEV_ATTACHED:
                                mFpSdk.onStatusChange("USB Device Attached");
                                break;
                            case Constants.DEV_DETACHED:
                                mFpSdk.onStatusChange("USB Device Detached");
                                break;
                            case Constants.DEV_CLOSE:
                                mFpSdk.onStatusChange("Device Close");
                                break;
                        }
                        break;
                    case Constants.FPM_PLACE:
                        mFpSdk.showPlaceFinger();
                        break;
                    case Constants.FPM_LIFT:
                        mFpSdk.onStatusChange("Lift Finger");
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
                            mFpSdk.onStatusChange("Generate Template Fail");
                        }
                    }
                    break;
                    case Constants.FPM_NEWIMAGE: {
                        fpm.GetBmpImage(bmpdata);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bmpdata, 0, bmpdata.length);
                        mFpSdk.showLiftFinger(bmp);
                    }
                    break;
                    case Constants.FPM_TIMEOUT:
                        mFpSdk.onStatusChange("Time Out");
                        break;
                }
                return true;
            }
        });
    }

    public interface IFpSdk {
        void onStatusChange(String status);

        void onFpDetected(byte[] metadata);

        void showPlaceFinger();

        void showLiftFinger(Bitmap bmp);
    }

}
