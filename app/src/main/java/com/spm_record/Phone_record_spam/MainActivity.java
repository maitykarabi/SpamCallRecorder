package com.spm_record.Phone_record_spam;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
//import android.support.v7.app.AppCompatActivity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.widget.Toast.LENGTH_LONG;
import static androidx.constraintlayout.widget.Constraints.TAG;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_CONTACTS;
//import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.PROCESS_OUTGOING_CALLS;
import static android.Manifest.permission.READ_PHONE_STATE;
//import static android.Manifest.permission.STORAGE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;



public class MainActivity extends AppCompatActivity {
    private static  final int MY_PERMISSION_SEND_REQUEST=0;
    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //if(CheckPermissions()) {

            Toast.makeText(getApplicationContext(), "The App Started", Toast.LENGTH_LONG).show();
            makestart();
      //  }
      //  else
      //  {
     //       RequestPermissions();
            //if(CheckPermissions()) {

            //    Toast.makeText(getApplicationContext(), "The App Started", Toast.LENGTH_LONG).show();
            //    makestart();
           // }
      //  }

      //  try {
            // Initiate DevicePolicyManager.

       // } catch (Exception e) {
      //      e.printStackTrace();
      //  }

    }
    public void makestart(){

        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminName = new ComponentName(this, DeviceAdminDemo.class);

        if (!mDPM.isAdminActive(mAdminName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Click on Activate button to secure your application.");
            startActivityForResult(intent, REQUEST_CODE);
            Log.d(TAG, "activity: "+REQUEST_CODE );

        } else {
            // mDPM.lockNow();
            // Intent intent = new Intent(MainActivity.this,
            // TrackDeviceService.class);
            // startService(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       // if (REQUEST_CODE == requestCode) {
            Log.d(TAG, "Tservice: " );
            Intent intent = new Intent(MainActivity.this, TService.class);
            startService(intent);
      //  }
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length> 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] ==  PackageManager.PERMISSION_GRANTED;
                    boolean permissionToContact=grantResults[2] ==  PackageManager.PERMISSION_GRANTED;
                    boolean permissionToPhone=grantResults[3] ==  PackageManager.PERMISSION_GRANTED;
                    //boolean permissionToCalllog=grantResults[4] ==  PackageManager.PERMISSION_GRANTED;
                    boolean permissionTophonestate=grantResults[4] ==  PackageManager.PERMISSION_GRANTED;
                    boolean permissionTointernet=grantResults[5] ==  PackageManager.PERMISSION_GRANTED;
                    //boolean permissionToreadphonestate=grantResults[7] ==  PackageManager.PERMISSION_GRANTED;
                    boolean permissionTomodifyaudiosettings=grantResults[6] ==  PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore && permissionToContact && permissionToPhone  && permissionTophonestate && permissionTointernet  && permissionTomodifyaudiosettings) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                        makestart();
                        //Toast.makeText(getApplicationContext(), "Request", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE);
        //int result4 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CALL_LOG);
        int result5 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);
        int result6 = ContextCompat.checkSelfPermission(getApplicationContext(), INTERNET);
        //int result7 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);
        int result8 = ContextCompat.checkSelfPermission(getApplicationContext(), MODIFY_AUDIO_SETTINGS);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED  && result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED  && result5 == PackageManager.PERMISSION_GRANTED && result6 == PackageManager.PERMISSION_GRANTED  && result8 == PackageManager.PERMISSION_GRANTED;
    }
    private void RequestPermissions() {
        Toast.makeText(getApplicationContext(), "Request", Toast.LENGTH_LONG).show();
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_CONTACTS,CALL_PHONE,READ_PHONE_STATE,INTERNET,MODIFY_AUDIO_SETTINGS}, REQUEST_AUDIO_PERMISSION_CODE);
    }

}
