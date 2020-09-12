package com.zouguo.iothumtemp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.UUID;

public class MQTTService extends Service {

    public static final String TAG = MQTTService.class.getSimpleName();

    private static MqttAndroidClient client;
    private MqttConnectOptions conOpt;
    private IGetMessageCallBack IGetMessageCallBack;

    private String host = "tcp://xxx:xxx";
    private String userName = "xxx";
    private String passWord = "xxx";

    private static String publishTopic = "appnotify";    //发布消息到该Topic
    private static String nodeStatusTopic = "+/status";      //订阅节点状态主题，OnLine和OffLine
    private static String nodeDatasTopic = "+/sensor/+/state";      //订阅节点数据主题，包括温湿度(不指定设备)
    private static String willTopic = "appnotifywill";      //发布遗嘱到该Topic
    private String clientId = "";//客户端标识

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(getClass().getName(), "onCreate");

        //查看本地是否已生成ClientID
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String preClientID = sp.getString("your_mqtt_clientid","");
        if(preClientID.isEmpty()){
            //生成一个ClientID，用于连接MQTT
            UUID uuid = UUID.randomUUID();
            clientId = uuid.toString() + System.currentTimeMillis();

            sp.edit().putString("your_mqtt_clientid", clientId).apply();
        }else{
            clientId = preClientID;
        }

        init();
    }

    public static void publish(String msg){
        String topic = publishTopic;
        Integer qos = 0;
        Boolean retained = false;
        try {
            if (client != null){
                client.publish(topic, msg.getBytes(), qos.intValue(), retained.booleanValue());
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        // 服务器地址（协议+地址+端口号）
        String uri = host;
        client = new MqttAndroidClient(this, uri, clientId);
        // 设置MQTT监听并且接受消息
        client.setCallback(mqttCallback);

        conOpt = new MqttConnectOptions();
        // 清除缓存
        conOpt.setCleanSession(true);
        // 设置超时时间，单位：秒
        conOpt.setConnectionTimeout(10);
        // 心跳包发送间隔，单位：秒
        conOpt.setKeepAliveInterval(20);
        // 用户名
        conOpt.setUserName(userName);
        // 密码
        conOpt.setPassword(passWord.toCharArray());     //将字符串转换为字符串数组

        // last will message
        boolean doConnect = true;
        String message = "{\"terminal_uid\":\"" + clientId + "\"}";
        Log.e(getClass().getName(), "message是:" + message);
        String topic = willTopic;
        Integer qos = 0;
        Boolean retained = false;
        if ((!message.equals("")) || (!topic.equals(""))) {
            // 最后的遗嘱
            // MQTT本身就是为信号不稳定的网络设计的，所以难免一些客户端会无故的和Broker断开连接。
            //当客户端连接到Broker时，可以指定LWT，Broker会定期检测客户端是否有异常。
            //当客户端异常掉线时，Broker就往连接时指定的topic里推送当时指定的LWT消息。

            try {
                conOpt.setWill(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
            } catch (Exception e) {
                Log.i(TAG, "Exception Occured", e);
                doConnect = false;
                iMqttActionListener.onFailure(null, e);
            }
        }

        if (doConnect) {
            doClientConnection();
        }

    }


    @Override
    public void onDestroy() {
        stopSelf();
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /** 连接MQTT服务器 */
    private void doClientConnection() {
        if (!client.isConnected() && isConnectIsNormal()) {
            try {
                client.connect(conOpt, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();

                if (IGetMessageCallBack != null){
                    IGetMessageCallBack.setOnFailure(e.getCause());
                }
            }
        }

    }

    // MQTT是否连接成功
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 ");
            try {
                // 订阅nodeStatusTopic话题
                client.subscribe(nodeStatusTopic,1);
                // 订阅nodeDatasTopic话题
                client.subscribe(nodeDatasTopic,1);

                if (IGetMessageCallBack != null){
                    IGetMessageCallBack.setConnectStatus(true);
                }
            } catch (MqttException e) {
                e.printStackTrace();

                if (IGetMessageCallBack != null){
                    IGetMessageCallBack.setOnFailure(e.getCause());
                }
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            // 连接失败，重连
            doClientConnection();

            if (IGetMessageCallBack != null){
                IGetMessageCallBack.setConnectStatus(false);
            }
        }
    };

    // MQTT监听并且接受消息
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

            String str1 = new String(message.getPayload());
            String str2 = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
            if (IGetMessageCallBack != null){
                IGetMessageCallBack.setMessageArrived(topic, str1, str2);
            }

            Log.i(TAG, "messageArrived:" + str1);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        public void connectionLost(Throwable arg0) {
            // 失去连接，重连
            doClientConnection();

            if (IGetMessageCallBack != null){
                IGetMessageCallBack.setConnectStatus(false);
            }
        }
    };

    /** 判断网络是否连接 */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
            return false;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.e(getClass().getName(), "onBind");
        return new CustomBinder();
    }

    public void setIGetMessageCallBack(IGetMessageCallBack IGetMessageCallBack){
        this.IGetMessageCallBack = IGetMessageCallBack;
    }

    public class CustomBinder extends Binder {
        public MQTTService getService(){
            return MQTTService.this;
        }
    }

    public void toCreateNotification(String message){
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(this,MQTTService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);//3、创建一个通知，属性太多，使用构造器模式

        Notification notification = builder
                .setTicker("收到消息")
                .setSmallIcon(R.mipmap.icon_humtemp)
                .setContentTitle("订阅消息")
                .setContentText(message)
                .setContentInfo("内容信息")
                .setContentIntent(pendingIntent)//点击后才触发的意图，“挂起的”意图
                .setAutoCancel(true)        //设置点击之后notification消失
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        startForeground(0, notification);
        notificationManager.notify(0, notification);

    }
}
