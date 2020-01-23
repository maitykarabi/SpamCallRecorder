package com.spm_record.Phone_record_spam;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
//import android.support.annotation.NonNull;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.iid.FirebaseInstanceId;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.UUID;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;
import static androidx.constraintlayout.widget.Constraints.TAG;
//import static com.example.phnrecorder.disconnectTelephone.*;

public class TService extends Service {

    char p;
    int n;
    MediaRecorder recorder;
    int seconds;
    File audiofile;
    String name, phonenumber;
    String audio_format;
    public String Audio_Type;
    int audioSource;
    Context context;
    // private Handler handler;
    //Timer timer;
    int spam=7;
    int spam_f=0;
    public String mFileName;
    String inCall, s;
    int i = 1;
    Boolean offHook = false, ringing = false;
    Toast toast;
    Boolean isOffHook = false;
    ITelephony telephony;
    //    char p = 'A';
//    int n = 0;
    private boolean recordstarted = false;
    private StorageReference mstorage_new;
    private StorageReference mstorage;
    DatabaseReference rootRef,demoRef,rootRef2,demoRef2,rootRef3,demoRef3;
    DatabaseReference rootRef1,demoRef1;

    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    private CallBr br_call;

    //Snackbar mySnackbar = Snackbar.make(view, stringId, duration);

    @Override
    public void onDestroy() {
        Log.d("service", "destroy");

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        this.br_call = new CallBr();
        this.registerReceiver(this.br_call, filter);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public class CallBr extends BroadcastReceiver {
        Bundle bundle;
        String state;

        public boolean wasRinging = false;



        public String findNameByNumber(String num) {
            String res = null;
            try {
                Log.d(TAG, "activity: " );
                ContentResolver resolver = getApplicationContext().getContentResolver();
                Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num));
                Cursor c = resolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

                if (c != null) { // cursor not null means number is found contactsTable
                    if (c.moveToFirst()) {   // so now find the contact Name
                        res = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                    }
                    c.close();
                }
                if (c == null) {
                    return null;
                }
            } catch (Exception ex) {
            }
            return res;
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_IN)) {
                if ((bundle = intent.getExtras()) != null) {
                    state = bundle.getString(TelephonyManager.EXTRA_STATE);
                    if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        inCall = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        s = findNameByNumber(inCall);
                        n = inCall.length();
                        p = inCall.charAt(n - 1);
                        wasRinging = true;
                        if (s == null)
                            Toast.makeText(context, "IN : " + inCall + " UnknownNumber", LENGTH_LONG).show();
                        else
                            Toast.makeText(context, "IN : " + inCall + s, LENGTH_LONG).show();


                        // Toast.makeText(context, "IN : " , Toast.LENGTH_LONG).show();
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        if (wasRinging == true && s == null) {
                            Log.d(TAG, "answer: " );
                            Toast.makeText(context, "ANSWERED", LENGTH_LONG).show();
                            mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
                            mFileName += "/call_record.wav";

                            recorder = new MediaRecorder();
                            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                            recorder.setOutputFile(mFileName);
                            try {
                                recorder.prepare();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            recorder.start();
                            recordstarted = true;

                            //if (p == '7') {
                             //   Toast.makeText(getApplicationContext(), "SPAM CALL", LENGTH_LONG).show();
                                xtimer();
                                ytimer();
                              //  ztimer();

                           // }
                        }

                    } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        wasRinging = false;
                        Toast.makeText(context, "REJECTED", LENGTH_LONG).show();
                        if (recordstarted) {
                            recorder.stop();
                            recordstarted = false;
                            String uniqueID = UUID.randomUUID().toString();
                            mstorage = FirebaseStorage.getInstance().getReference();
                            StorageReference filepath = mstorage.child("Audio").child("real.wav");

                            Uri uri = Uri.fromFile(new File(mFileName));
                            filepath.putFile(uri);
                            Toast.makeText(context, "uploaded...", LENGTH_LONG).show();
                            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(getApplicationContext(), "Recorded successfully", LENGTH_LONG).show();
                                }
                            });
                            filepath.putFile(uri).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Record unsucessfull888888", LENGTH_LONG).show();
                                }
                            });


                        }                            //uploadAudio()                        }
                    }
                }
            }



        }

    }


    //@Override
    public void xtimer() {
        seconds = 30;
        CountDownTimer countDownTimer = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long l) {
                l = (int) l / 1000;
                if (l == 1) {
                    String uniqueID = UUID.randomUUID().toString();
                    mstorage_new = FirebaseStorage.getInstance().getReference();
                    StorageReference filepath = mstorage_new.child("Audio").child("spam" + inCall).child("real.wav");

                    Uri uri = Uri.fromFile(new File(mFileName).getAbsoluteFile());
                    filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext(), "Recorded successfully1234", LENGTH_LONG).show();
                        }
                    });
                    filepath.putFile(uri).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Record unsucessfull123658", LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onFinish() {
                //disconnectPhoneItelephony(context);
                // seconds=20;

            }
        }.start();
    }

    public void ytimer() {
        seconds = 50;
        CountDownTimer countDownTimer = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long l) {
                l = (int) l / 1000;
                if (l == 1) {
                    String uniqueID = UUID.randomUUID().toString();
                    mstorage = FirebaseStorage.getInstance().getReference();
                    final StorageReference filepath = mstorage.child("Audio_full").child(uniqueID + "call" + inCall).child("real.wav");

                    Uri uri = Uri.fromFile(new File(mFileName).getAbsoluteFile());
                    filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                          //  Toast.makeText(getApplicationContext(), "Recorded successfully789548", LENGTH_LONG).show();
                        }
                    });
                   // AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
                    //database reference pointing to root of database
                    rootRef = FirebaseDatabase.getInstance().getReference();

//database reference pointing to demo node
                    demoRef = rootRef.child("spam");
                    //demoRef.setValue("Hello");
                    //demoRef.child("val").addListenerForSingleValueEvent(new ValueEventListener() {
                    demoRef.child("val").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // This method is called once with the initial value and again
                                // whenever data at this location is updated.
                                //String value = dataSnapshot.getValue(String.class);
                                int spam= dataSnapshot.getValue(int.class);

                                Log.d(TAG, "Value is: " + spam);
                               // for(int loop=1;loop<2;loop++) {
                                    if (spam >= 5) {
                                        //     spam_f=1;
                                        showNotification();
                                    //    break;

                                        demoRef.child("val").setValue(4);
                                        //     Toast.makeText(getApplicationContext(), "alart......", LENGTH_LONG).show();
                                    } //else {
                                      //  showNotification_gen();
                                      //  break;
                                 //   }
                              //  }

                            }



                            @Override
                            public void onCancelled(DatabaseError error) {
                                // Failed to read value
                                Log.w(TAG, "Failed to read value.", error.toException());
                            }
                        });
                    if (spam>=4)
                    {
                        //Log.d(TAG, "Value is: " + spam_f);
                        //addNotification();
                        //showNotification();
                       // addNotification1();


                       //Toast.makeText(getApplicationContext(), "alart......", LENGTH_LONG).show();
                    }
                    //filepath.putFile(uri).addOnFailureListener(new OnFailureListener() {
                  //      @Override
                  //      public void onFailure(@NonNull Exception e) {
                            //Toast.makeText(getApplicationContext(), "Record unsucessfull123658", LENGTH_LONG).show();
                 //       }
                 //   });
                }



            }

            @Override
            public void onFinish() {

                // seconds=20;

            }

        }.start();

        }
    public void ztimer() {
        seconds = 54;
        CountDownTimer countDownTimer = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long l) {
                l = (int) l / 1000;
                if (l == 1) {
                    // String uniqueID = UUID.randomUUID().toString();
                    // mstorage = FirebaseStorage.getInstance().getReference();
                    // final StorageReference filepath = mstorage.child("Audio").child(uniqueID + "manyumishra" + inCall).child("real.mp3");

                    // Uri uri = Uri.fromFile(new File(mFileName).getAbsoluteFile());
                    //filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    //    @Override
                    //    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    //  Toast.makeText(getApplicationContext(), "Recorded successfully789548", LENGTH_LONG).show();
                    //   }
                    // });
                    // AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
                    //database reference pointing to root of database
                    rootRef1 = FirebaseDatabase.getInstance().getReference();

//database reference pointing to demo node
                    demoRef1 = rootRef1.child("spam");
                    //demoRef.setValue("Hello");
                    //demoRef.child("val").addListenerForSingleValueEvent(new ValueEventListener() {
                    demoRef1.child("val").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.
                            //String value = dataSnapshot.getValue(String.class);
                            int spam= dataSnapshot.getValue(int.class);

                            Log.d(TAG, "Value is: " + spam);
                            if (spam<3)
                            {
                                //     spam_f=1;
                                //showNotification();
                                demoRef1.child("val").setValue(4);
                                     Toast.makeText(getApplicationContext(), "alart......", LENGTH_LONG).show();
                            }

                        }



                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });
                    if (spam>=4)
                    {
                        //Log.d(TAG, "Value is: " + spam_f);
                        //addNotification();
                        //showNotification();
                        // addNotification1();


                        //Toast.makeText(getApplicationContext(), "alart......", LENGTH_LONG).show();
                    }
                    //filepath.putFile(uri).addOnFailureListener(new OnFailureListener() {
                    //      @Override
                    //      public void onFailure(@NonNull Exception e) {
                    //Toast.makeText(getApplicationContext(), "Record unsucessfull123658", LENGTH_LONG).show();
                    //       }
                    //   });
                }



            }

            @Override
            public void onFinish() {

                // seconds=20;

            }

        }.start();

    }
    public void addNotification(){
        //Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define


        NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.stat_notify_more)
                //.setSound(alarmSound)
                .setContentText("Spam Call.... Rejected....");
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //builder.setSound(alarmSound);
        manager.notify(1, builder.build());
        Intent notificationIntent=new Intent(this,TService.class);
        PendingIntent contentIntent=PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
       // builder.setLights(Color.BLUE, 500, 500);
       // long[] pattern = {500,500,500,500,500,500,500,500,500};
        builder.setAutoCancel(true);
        builder.setLights(Color.BLUE, 500, 500);
        long[] pattern = {500,500,500,500,500,500,500,500,500};
        builder.setVibrate(pattern);

        //builder.setVibrate(pattern);
       // Notification notification = builder.build();
        //long[] vibrate = { 0, 100, 200, 300 };
        //notification.vibrate = vibrate;
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        if(alarmSound == null){
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if(alarmSound == null){
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }


        //builder.setStyle(new NotificationCompat.InboxStyle());
       // NotificationManager manager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //NotificationManager manager1=(NotificationManager) getSystemService()
       // manager.notify(0,builder.build());

    }


    void showNotification() {

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "YOUR_CHANNEL_ID")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(alarmSound)// notification icon
                .setContentTitle("spam call") // title for notification
                .setContentText("May be spam...Reject..")// message for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(getApplicationContext(), TService.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
        //rootRef2 = FirebaseDatabase.getInstance().getReference();

//database reference pointing to demo node
//        demoRef2 = rootRef2.child("spam");
        //demoRef.setValue("Hello");
        //demoRef.child("val").addListenerForSingleValueEvent(new ValueEventListener() {
//        demoRef2.child("val").addValueEventListener(new ValueEventListener() {
  //          @Override
 //           public void onDataChange(DataSnapshot dataSnapshot) {
 //               // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //String value = dataSnapshot.getValue(String.class);
 //               int spam= dataSnapshot.getValue(int.class);

                //Log.d(TAG, "Value is: " + spam);
 //               if (spam>=4)
 //               {
                    //     spam_f=1;
                    //showNotification();
 //                   demoRef2.child("val").setValue(3);
                    //     Toast.makeText(getApplicationContext(), "alart......", LENGTH_LONG).show();
  //              }

  //          }



   //         @Override
 //           public void onCancelled(DatabaseError error) {
 //               // Failed to read value
 //               Log.w(TAG, "Failed to read value.", error.toException());
 ///           }
 //       });
    }
    void showNotification_gen() {

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "YOUR_CHANNEL_ID")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(alarmSound)// notification icon
                .setContentTitle("genuine call") // title for notification
                .setContentText("May be not spam...Continue..")// message for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(getApplicationContext(), TService.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());

        rootRef3 = FirebaseDatabase.getInstance().getReference();

//database reference pointing to demo node
        demoRef3 = rootRef3.child("spam");
        //demoRef.setValue("Hello");
        //demoRef.child("val").addListenerForSingleValueEvent(new ValueEventListener() {
        demoRef3.child("val").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //String value = dataSnapshot.getValue(String.class);
                int spam= dataSnapshot.getValue(int.class);

                //Log.d(TAG, "Value is: " + spam);
                if (spam<4)
                {
                    //     spam_f=1;
                    //showNotification();
                    demoRef3.child("val").setValue(4);
                    //     Toast.makeText(getApplicationContext(), "alart......", LENGTH_LONG).show();
                }

            }



            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }




//    private void disconnectPhoneItelephony(Context context) {
//        ITelephony telephonyService;
//        TelephonyManager telephony = (TelephonyManager)
//                context.getSystemService(Context.TELEPHONY_SERVICE);
//        try {
//            Class c = Class.forName(telephony.getClass().getName());
//            Method m = c.getDeclaredMethod("getITelephony");
//            m.setAccessible(true);
//            telephonyService = (ITelephony) m.invoke(telephony);
//            //timer();
//            telephonyService.endCall();
//            Toast.makeText(getApplicationContext(), "YESTIMER", LENGTH_LONG).show();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}

