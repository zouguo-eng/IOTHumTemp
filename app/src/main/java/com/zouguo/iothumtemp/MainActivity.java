package com.zouguo.iothumtemp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.zouguo.iothumtemp.adapter.IOTRecvlistAdapter;
import com.zouguo.iothumtemp.assistutils.ItemRecvDeviceDecoration;
import com.zouguo.iothumtemp.bean.IOTDeviceInfos;

import org.litepal.LitePal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IGetMessageCallBack{
    private MyServiceConnection serviceConnection;
    private MQTTService mqttService;

    private List<IOTDeviceInfos> iotDeviceInfosList;
    private IOTRecvlistAdapter iotRecvlistAdapter;
    private RecyclerView main_rcv_listdevices;
    private FloatingActionButton main_fab_adddevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        main_fab_adddevice = findViewById(R.id.main_fab_adddevice);
        main_rcv_listdevices = findViewById(R.id.main_rcv_listdevices);
        iotDeviceInfosList = new ArrayList<>();

        main_rcv_listdevices.addItemDecoration(new ItemRecvDeviceDecoration(8));

        iotRecvlistAdapter = new IOTRecvlistAdapter(MainActivity.this, iotDeviceInfosList);
        main_rcv_listdevices.setAdapter(iotRecvlistAdapter);

        LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        main_rcv_listdevices.setLayoutManager(llm);

        iotRecvlistAdapter.setOnItemListener(new IOTRecvlistAdapter.OnItemListener() {
            @Override
            public void onItemClickListener(View view, int parentPostion) {
                Toast.makeText(MainActivity.this, "点击", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClickListener(View view, final int parentPostion) {
                //更新设备信息
                View dialogAddDeviceView = getLayoutInflater().inflate(R.layout.view_dialog_adddevice,null);

                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(dialogAddDeviceView);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                DisplayMetrics dm = getResources().getDisplayMetrics();
                int displayWidth = dm.widthPixels;
                int displayHeight = dm.heightPixels;
                android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
                p.width = (int)(displayWidth * 0.85);
//                p.height = (int)(displayHeight * 0.35);
                dialog.getWindow().setAttributes(p);

                final Spinner node_type = dialogAddDeviceView.findViewById(R.id.view_dialog_node_type_sp);
                final EditText node_label = dialogAddDeviceView.findViewById(R.id.view_dialog_node_label_et);
                final EditText node_alias = dialogAddDeviceView.findViewById(R.id.view_dialog_node_alias_et);
                final EditText node_location = dialogAddDeviceView.findViewById(R.id.view_dialog_node_location_et);
                Button add = dialogAddDeviceView.findViewById(R.id.view_dialog_add_btn);
                Button cancel = dialogAddDeviceView.findViewById(R.id.view_dialog_cancel_btn);
                Button delete = dialogAddDeviceView.findViewById(R.id.view_dialog_delete_btn);
                add.setText("更新");
                delete.setVisibility(View.VISIBLE);

                //获取当前设备的参数，并填充界面
                IOTDeviceInfos iotDeviceInfos = iotDeviceInfosList.get(parentPostion);
                final int id = iotDeviceInfos.getId();
                int preNodeType = iotDeviceInfos.getDnodetype();
                String preNodeLabel = iotDeviceInfos.getDnodelabel();
                String preNodeAlias = iotDeviceInfos.getDname();
                String preNodeLoation = iotDeviceInfos.getDlocation();

                node_type.setSelection(preNodeType);
                node_label.setText(preNodeLabel);
                node_alias.setText(preNodeAlias);
                node_location.setText(preNodeLoation);

                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String node_label_title = node_label.getText().toString();
                        String node_alias_title = node_alias.getText().toString();
                        String node_location_title = node_location.getText().toString();

                        if(node_label_title.equals("")){
                            return;
                        }

                        if(node_alias_title.equals("")){
                            node_alias_title = "N/A";
                        }

                        if(node_location_title.equals("")){
                            node_location_title = "N/A";
                        }

                        //获取NodeType
                        int type_index = node_type.getSelectedItemPosition();
                        String device_name = "";
                        String temptopic = "";
                        String humtopic = "";

                        if(type_index == 0){
                            //esp
                            device_name = "esp_node_sensor_" + node_label_title;
                            temptopic = device_name + "/sensor/" + "esp_" + node_label_title + "_temperature/state";
                            humtopic = device_name + "/sensor/" + "esp_" + node_label_title + "_humidity/state";
                        }else if(type_index == 1){
                            //nodemcu
                            device_name = "nodemcu_node_sensor_" + node_label_title;
                            temptopic = device_name + "/sensor/" + "nodemcu_" + node_label_title + "_temperature/state";
                            humtopic = device_name + "/sensor/" + "nodemcu_" + node_label_title + "_humidity/state";
                        }

                        long currentTime = System.currentTimeMillis();
                        String fTime = new SimpleDateFormat("yy-MM-dd").format(currentTime);

                        //更新列表
                        IOTDeviceInfos iotDeviceInfos = LitePal.find(IOTDeviceInfos.class, id);
                        iotDeviceInfos.setDname(node_alias_title);
                        iotDeviceInfos.setDnodename(device_name);
                        iotDeviceInfos.setDnodetype(type_index);
                        iotDeviceInfos.setDnodelabel(node_label_title);
                        iotDeviceInfos.setDstatus(2);
                        iotDeviceInfos.setDtemptopic(temptopic);
                        iotDeviceInfos.setDhumtopic(humtopic);
                        iotDeviceInfos.setDtemp("N/A");
                        iotDeviceInfos.setDhum("N/A");
                        iotDeviceInfos.setDvoltage(-1);
                        iotDeviceInfos.setDlocation(node_location_title);
                        iotDeviceInfos.setDtime(fTime);
                        //更新数据库
                        iotDeviceInfos.save();

                        iotDeviceInfosList.remove(parentPostion);
                        iotRecvlistAdapter.notifyItemRemoved(parentPostion);

                        iotDeviceInfosList.add(parentPostion, iotDeviceInfos);
                        iotRecvlistAdapter.notifyItemInserted(parentPostion);

                        Log.d("zouguo","设备列表：" + iotDeviceInfosList.size());
                        dialog.dismiss();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LitePal.delete(IOTDeviceInfos.class, id);

                        //刷新列表
                        iotDeviceInfosList.remove(parentPostion);
                        iotRecvlistAdapter.notifyItemRemoved(parentPostion);

                        dialog.dismiss();
                    }
                });
            }
        });

        serviceConnection = new MyServiceConnection();
        serviceConnection.setIGetMessageCallBack(MainActivity.this);

        Intent intent = new Intent(this, MQTTService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        main_fab_adddevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //添加新设备
                View dialogAddDeviceView = getLayoutInflater().inflate(R.layout.view_dialog_adddevice,null);

                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(dialogAddDeviceView);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                DisplayMetrics dm = getResources().getDisplayMetrics();
                int displayWidth = dm.widthPixels;
                int displayHeight = dm.heightPixels;
                android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
                p.width = (int)(displayWidth * 0.85);
//                p.height = (int)(displayHeight * 0.35);
                dialog.getWindow().setAttributes(p);

                final Spinner node_type = dialogAddDeviceView.findViewById(R.id.view_dialog_node_type_sp);
                final EditText node_label = dialogAddDeviceView.findViewById(R.id.view_dialog_node_label_et);
                final EditText node_alias = dialogAddDeviceView.findViewById(R.id.view_dialog_node_alias_et);
                final EditText node_location = dialogAddDeviceView.findViewById(R.id.view_dialog_node_location_et);
                Button add = dialogAddDeviceView.findViewById(R.id.view_dialog_add_btn);
                Button cancel = dialogAddDeviceView.findViewById(R.id.view_dialog_cancel_btn);

                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String node_label_title = node_label.getText().toString();
                        String node_alias_title = node_alias.getText().toString();
                        String node_location_title = node_location.getText().toString();

                        if(node_label_title.equals("")){
                            return;
                        }

                        if(node_alias_title.equals("")){
                            node_alias_title = "N/A";
                        }

                        if(node_location_title.equals("")){
                            node_location_title = "N/A";
                        }

                        //获取NodeType
                        int type_index = node_type.getSelectedItemPosition();
                        String device_name = "";
                        String temptopic = "";
                        String humtopic = "";

                        if(type_index == 0){
                            //esp
                            device_name = "esp_node_sensor_" + node_label_title;
                            temptopic = device_name + "/sensor/" + "esp_" + node_label_title + "_temperature/state";
                            humtopic = device_name + "/sensor/" + "esp_" + node_label_title + "_humidity/state";
                        }else if(type_index == 1){
                            //nodemcu
                            device_name = "nodemcu_node_sensor_" + node_label_title;
                            temptopic = device_name + "/sensor/" + "nodemcu_" + node_label_title + "_temperature/state";
                            humtopic = device_name + "/sensor/" + "nodemcu_" + node_label_title + "_humidity/state";
                        }

                        //首先查询数据库中是否已添加该设备
                        List<IOTDeviceInfos> queryIOTList = LitePal.where("dnodename = ?", device_name).find(IOTDeviceInfos.class);

                        if(!queryIOTList.isEmpty()){
                            //已存在当前待添加设备
                            Toast.makeText(MainActivity.this, "当前设备已存在", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        long currentTime = System.currentTimeMillis();
                        String fTime = new SimpleDateFormat("yy-MM-dd").format(currentTime);

                        //添加到列表
                        IOTDeviceInfos iotDeviceInfos = new IOTDeviceInfos();
                        iotDeviceInfos.setDname(node_alias_title);
                        iotDeviceInfos.setDnodename(device_name);
                        iotDeviceInfos.setDnodetype(type_index);
                        iotDeviceInfos.setDnodelabel(node_label_title);
                        iotDeviceInfos.setDstatus(2);
                        iotDeviceInfos.setDtemptopic(temptopic);
                        iotDeviceInfos.setDhumtopic(humtopic);
                        iotDeviceInfos.setDtemp("N/A");
                        iotDeviceInfos.setDhum("N/A");
                        iotDeviceInfos.setDvoltage(-1);
                        iotDeviceInfos.setDlocation(node_location_title);
                        iotDeviceInfos.setDtime(fTime);
                        //存入数据库
                        iotDeviceInfos.save();

                        iotDeviceInfosList.add(iotDeviceInfos);
                        iotRecvlistAdapter.notifyItemInserted(iotDeviceInfosList.size());

                        dialog.dismiss();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        //初始化数据，读取自本地数据库
        List<IOTDeviceInfos> queryIOTList = LitePal.findAll(IOTDeviceInfos.class);
        if(!queryIOTList.isEmpty()){
            //读取设备列表
            for(IOTDeviceInfos iot : queryIOTList){
                iotDeviceInfosList.add(iot);
                iotRecvlistAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_records:
                break;
            case R.id.action_update:
                break;
            case R.id.action_about:
                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                adb.setTitle("关于");
                adb.setMessage("软件基于MQTT协议,Eclipse Paho MQTT;\n图片素材取自阿里素材库;\nGithub：zouguo-eng");
                adb.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                adb.create().show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setMessageArrived(String topic, String message, String logs) {
//        mqttService = serviceConnection.getMqttService();
//        mqttService.toCreateNotification(message);

        long currentTime = System.currentTimeMillis();
        String fTime = new SimpleDateFormat("yy-MM-dd HH:mm:ss").format(currentTime);

        String[] topicArray = topic.split("/");
        String deviceName = topicArray[0];

        Log.d("zouguo1","TOPIC：" + topic);
        Log.d("zouguo","Topic设备名：" + deviceName);

        int nodeIndex = -1;
        for(int i = 0; i < iotDeviceInfosList.size(); i++){
            if(iotDeviceInfosList.get(i).getDnodename().equals(deviceName)){
                //该Topic的DeviceName在节点列表中，位置为i
                nodeIndex = i;
                Log.d("zouguo","匹配到设备：" + nodeIndex);
            }
        }

        List payloadList = new ArrayList();
        if(nodeIndex != -1){
            //属于节点列表中设备的数据
            if(topic.endsWith("status")){
                //更新nodeIndex设备的在线状态
                payloadList.add(0,"status");

                if(message.equals("offline")){
                    payloadList.add(1,0);
                }else if(message.equals("online")){
                    payloadList.add(1,1);
                }else{
                    payloadList.add(1,2);
                }

                payloadList.add(2,fTime);
                iotRecvlistAdapter.notifyItemChanged(nodeIndex, payloadList);
            }else if(topic.endsWith("state")){
                //更新nodeIndex设备的遥测数据
                String deviceNode = topicArray[2];

                Log.d("zouguo","更新节点State：" + deviceNode);

                //判断是温度还是湿度
                if(deviceNode.contains("_temperature")){
                    Log.d("zouguo","更新温度");

                    //温度
                    payloadList.add(0,"state");
                    payloadList.add(1,0);//标记位--温度
                    payloadList.add(2,message + "℃");
                    payloadList.add(3, fTime);

                    iotRecvlistAdapter.notifyItemChanged(nodeIndex, payloadList);
                }else if(deviceNode.contains("_humidity")){
                    Log.d("zouguo","更新湿度");
                    //湿度
                    payloadList.add(0,"state");
                    payloadList.add(1,1);//标记位--湿度
                    payloadList.add(2,message + "%");
                    payloadList.add(3, fTime);

                    iotRecvlistAdapter.notifyItemChanged(nodeIndex, payloadList);
                }else if(deviceNode.contains("_voltage")){
                    //电压
                    float curVoltage = Float.valueOf(message) - 3.0f;
                    float curVoltages = curVoltage * 10;
                    int curPower = Math.round(curVoltages);

                    Log.d("zouguo","更新电压" + curPower);

                    //电压
                    payloadList.add(0,"state");
                    payloadList.add(1,2);//标记位--电压
                    payloadList.add(2, curPower);
                    payloadList.add(3, fTime);

                    iotRecvlistAdapter.notifyItemChanged(nodeIndex, payloadList);
                }
            }
        }
    }

    @Override
    public void setConnectStatus(boolean isConnected) {

    }

    @Override
    public void setOnFailure(Throwable throwable) {

    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK
            && event.getAction() == KeyEvent.ACTION_DOWN
            && event.getRepeatCount() == 0){

            //回到桌面
            Intent goHome = new Intent(Intent.ACTION_MAIN);
            goHome.addCategory(Intent.CATEGORY_HOME);
            startActivity(goHome);
        }

        return false;
    }
}
