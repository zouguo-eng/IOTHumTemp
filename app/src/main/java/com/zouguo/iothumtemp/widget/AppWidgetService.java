package com.zouguo.iothumtemp.widget;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AppWidgetService extends Service {
    private static final String TAG = "zouguo3";

    private final String ACTION_WIDGET_UPDATE_ALL = "com.zouguo.iothumtemp.widget.UPDATE_ALL";
    private static final int UPDATE_TIME = 5000;
    private UpdateThread mUpdateThread;
    private Context mContext;
    private int count = 0;

    @Override
    public void onCreate() {
        mUpdateThread = new UpdateThread();
        mUpdateThread.start();
        mContext = this.getApplicationContext();

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if(mUpdateThread != null){
            mUpdateThread.interrupt();
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private class UpdateThread extends Thread{
        @Override
        public void run() {
            super.run();

            count = 0;
            while (true){
                count++;

                try {
                    Intent updateIntent = new Intent(ACTION_WIDGET_UPDATE_ALL);
                    updateIntent.setPackage(mContext.getPackageName());
                    mContext.sendBroadcast(updateIntent);

                    Thread.sleep(UPDATE_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
