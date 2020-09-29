package com.zouguo.iothumtemp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.zouguo.iothumtemp.R;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MyAppWidgetProviderAlpha extends AppWidgetProvider {
    private String TAG = "zouguo3";
    private final Intent WIDGET_SERVICE_INTENT = new Intent("android.appwidget.action.WIDGET_SERVICE_INTENT");
//    private final String ACTION_WIDGET_UPDATE_ALL = "android.appwidget.action.APPWIDGET_UPDATE";
    private final String ACTION_WIDGET_UPDATE_ALL = "com.zouguo.iothumtemp.widget.UPDATE_ALL";
    private static Set idsSet = new HashSet();

    private static final int BTN_TEST = 1112;

    static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager, Set hashSet){
        Iterator iterator = hashSet.iterator();

        while (iterator.hasNext()){
            int appID = ((Integer)iterator.next()).intValue();

            CharSequence cs = new Date().toString();
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.app_widget_layout);
            rv.setTextViewText(R.id.widget_test, cs);

//            Intent intent = new Intent();
//            intent.setClass(context, MyAppWidgetProviderAlpha.class);
//            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
//            intent.setData(Uri.parse("custom:" + BTN_TEST));
//            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0 );
//
//            rv.setOnClickPendingIntent(R.id.widget_control_left, pi);

            appWidgetManager.updateAppWidget(appID, rv);
        }
    }

    private PendingIntent getPendingItent(Context context, int btnId){
        Intent intent = new Intent();
        intent.setClass(context, MyAppWidgetProviderAlpha.class);
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.setData(Uri.parse("custom:" + BTN_TEST));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pi;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if(ACTION_WIDGET_UPDATE_ALL.equals(action)){
            Log.d(TAG,"ACTION_WIDGET_UPDATE_ALL");
            //更新广播
            updateAppWidgets(context, AppWidgetManager.getInstance(context), idsSet);
        }else if(intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)){
            Log.d(TAG,"CATEGORY_ALTERNATIVE");
            //按钮点击
            Uri data = intent.getData();
            int btnId = Integer.parseInt(data.getSchemeSpecificPart());

            if(btnId == BTN_TEST){
                Log.d(TAG, "Click");
            }
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");
        for (int appWidgetId : appWidgetIds){
            idsSet.add(Integer.valueOf(appWidgetId));
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        WIDGET_SERVICE_INTENT.setPackage(context.getPackageName());
        context.startService(WIDGET_SERVICE_INTENT);

        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        WIDGET_SERVICE_INTENT.setPackage(context.getPackageName());
        context.stopService(WIDGET_SERVICE_INTENT);

        super.onDisabled(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);

        Log.d(TAG, "onRestored");
    }
}
