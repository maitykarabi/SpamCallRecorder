package com.android.internal.telephony;

import android.content.Context;

public interface ITelephony {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
                    double aDouble, String aString);
   // void timer();
    boolean endCall();
    void silenceRinger();
    void answerRingingCall();
    void disconnectPhoneItelephony(Context context);
}