package com.app.sdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.device.Constants;
import com.fgtit.device.FPModule;

public class MainActivity extends ActionBarActivity {

	private FPModule fpm=new FPModule();
    
    private byte bmpdata[]=new byte[Constants.RESBMP_SIZE];
    private int bmpsize=0;
    
    private byte refdata[]=new byte[Constants.TEMPLATESIZE*4];
    private int refsize=0;
    
    private byte matdata[]=new byte[Constants.TEMPLATESIZE*4];
    private int matsize=0;
    
    private int worktype=0;
    
	private TextView	tvDevStatu,tvFpStatu;
	private ImageView 	ivFpImage=null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
		
        initView();
        tvDevStatu.setText(String.valueOf(fpm.getDeviceType()));
      
        fpm.InitMatch();
        fpm.SetContextHandler(this, mHandler);
        fpm.SetTimeOut(Constants.TIMEOUT_LONG);
        fpm.SetLastCheckLift(true);
    }
    
    private Handler mHandler = new Handler(){
        @Override
    	public void handleMessage(Message msg){
    		switch (msg.what){
    			case Constants.FPM_DEVICE:
    				switch(msg.arg1){
    				case Constants.DEV_OK:
    					tvFpStatu.setText("Open Device OK");
    					break;
    				case Constants.DEV_FAIL:
    					tvFpStatu.setText("Open Device Fail");
    					break;
    				case Constants.DEV_ATTACHED:
    					tvFpStatu.setText("USB Device Attached");
    					break;
    				case Constants.DEV_DETACHED:
    					tvFpStatu.setText("USB Device Detached");
    					break;
    				case Constants.DEV_CLOSE:
    					tvFpStatu.setText("Device Close");
    					break;
    				}
    				break;
    			case Constants.FPM_PLACE:
    				tvFpStatu.setText("Place Finger");
    				break;
    			case Constants.FPM_LIFT:
    				tvFpStatu.setText("Lift Finger");
    				break;
           	 	case Constants.FPM_GENCHAR:{
       	 			if(msg.arg1==1){
       	 				if(worktype==0){
       	 					tvFpStatu.setText("Generate Template OK");
       	 					matsize=fpm.GetTemplateByGen(matdata);       	 				
       	 					if(fpm.MatchTemplate(refdata, refsize,matdata,matsize,80))
       	 						tvFpStatu.setText(String.format("Match OK"));
       	 					else
       	 					tvFpStatu.setText(String.format("Match Fail"));
       	 				}else{
       	 					tvFpStatu.setText("Enrol Template OK");
       	 					refsize=fpm.GetTemplateByGen(refdata);
       	 				}
       	 			}else{
       	 				tvFpStatu.setText("Generate Template Fail");
       	 			}
       	 			}
       	 			break;
           	 	case Constants.FPM_NEWIMAGE:{
           	 		bmpsize=fpm.GetBmpImage(bmpdata);
       	 			Bitmap bm1=BitmapFactory.decodeByteArray(bmpdata, 0, bmpsize);
       	 			ivFpImage.setImageBitmap(bm1);
       	 			}
       	 			break; 
           	 	case Constants.FPM_TIMEOUT:
           	 		tvFpStatu.setText("Time Out");
           	 		break;
    		}
        }  
    };
    
	@Override
	protected void onDestroy(){
		super.onDestroy();
	}
	
    @Override
	protected void onResume() {
		super.onResume();		
		fpm.ResumeRegister();
		fpm.OpenDevice();
    }
    
    /*
	@Override
	protected void onPause() {		
		super.onPause();
		fpm.PauseUnRegister();
		fpm.CloseDevice();
	}
	*/

	@Override
	protected void onStop() {		
		super.onStop();
		fpm.PauseUnRegister();
		fpm.CloseDevice();
	}

	private void initView(){
		
		tvDevStatu=(TextView)findViewById(R.id.textView1);
		tvFpStatu=(TextView)findViewById(R.id.textView2);
		ivFpImage=(ImageView)findViewById(R.id.imageView1);
		
		final Button btn_enrol=(Button)findViewById(R.id.button1);
		final Button btn_capture=(Button)findViewById(R.id.button2);
				
		btn_enrol.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				if(fpm.GenerateTemplate(4)){
					worktype=1;
				}else{
					Toast.makeText(MainActivity.this, "Busy", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		btn_capture.setOnClickListener(new View.OnClickListener(){
			@Override			
			public void onClick(View v) {
				if(fpm.GenerateTemplate(1)){
					worktype=0;
				}else{
					Toast.makeText(MainActivity.this, "Busy", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}


}
