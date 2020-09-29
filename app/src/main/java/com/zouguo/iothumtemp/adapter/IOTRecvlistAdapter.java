package com.zouguo.iothumtemp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zouguo.iothumtemp.R;
import com.zouguo.iothumtemp.bean.IOTDeviceInfos;

import java.util.List;

public class IOTRecvlistAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<IOTDeviceInfos> iotDeviceInfosList;
    private int VIEW_CHOICE = 0; //0--LinearLayoutManager;1--GridLayoutManager
    private int[] onlineStatus = {R.mipmap.icon_offline, R.mipmap.icon_online, R.mipmap.icon_unknow};//设备状态标志图

    public IOTRecvlistAdapter(Context context, List<IOTDeviceInfos> iotDeviceInfosList){
        this.context = context;
        this.iotDeviceInfosList = iotDeviceInfosList;
    }

    public interface OnItemListener{
        void onItemClickListener(View view, int postion);
        void onItemLongClickListener(View view, int position);
    }

    private OnItemListener onItemListener;

    public void setOnItemListener(OnItemListener onItemListener){
        this.onItemListener = onItemListener;
    }

    //改变视图
    public void changeView(int type){
        VIEW_CHOICE = type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        MyViewHolder myViewHolder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recv_devices_infos,viewGroup,false));
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {
        IOTDeviceInfos iotDeviceInfos = iotDeviceInfosList.get(position);
        ((MyViewHolder)viewHolder).item_recv_devices_name.setText(iotDeviceInfos.getDname());
        ((MyViewHolder)viewHolder).item_recv_devices_temp.setText(iotDeviceInfos.getDtemp());
        ((MyViewHolder)viewHolder).item_recv_devices_hum.setText(iotDeviceInfos.getDhum());
        ((MyViewHolder)viewHolder).item_recv_devices_freshtime.setText(iotDeviceInfos.getDtime());
        ((MyViewHolder)viewHolder).item_recv_devices_online.setImageResource(onlineStatus[2]);
        ((MyViewHolder)viewHolder).item_recv_devices_location.setText(iotDeviceInfos.getDlocation());

        ((MyViewHolder)viewHolder).item_recv_devices_fold_name.setText(iotDeviceInfos.getDname());
        ((MyViewHolder)viewHolder).item_recv_devices_fold_temp.setText(iotDeviceInfos.getDtemp());
        ((MyViewHolder)viewHolder).item_recv_devices_fold_hum.setText(iotDeviceInfos.getDhum());
        ((MyViewHolder)viewHolder).item_recv_devices_fold_freshtime.setText(iotDeviceInfos.getDtime());
        ((MyViewHolder)viewHolder).item_recv_devices_fold_online.setImageResource(onlineStatus[2]);

        if(onItemListener != null){
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = viewHolder.getLayoutPosition();
                    onItemListener.onItemClickListener(viewHolder.itemView, pos);
                }
            });

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = viewHolder.getLayoutPosition();
                    onItemListener.onItemLongClickListener(viewHolder.itemView, pos);
                    return true;
                }
            });
        }

        if(VIEW_CHOICE == 0){
            //LinearLayoutManager布局
            ((MyViewHolder)viewHolder).item_recv_devices_unfold_fl.setVisibility(View.VISIBLE);
            ((MyViewHolder)viewHolder).item_recv_devices_fold_fl.setVisibility(View.GONE);
        }else if(VIEW_CHOICE == 1){
            //GridLayoutManager布局
            ((MyViewHolder)viewHolder).item_recv_devices_unfold_fl.setVisibility(View.GONE);
            ((MyViewHolder)viewHolder).item_recv_devices_fold_fl.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position, @NonNull List payloads) {
        if(payloads.isEmpty()){
            //无载荷
            onBindViewHolder(viewHolder, position);
        }else{
            List payloadList = (List) payloads.get(0);
            String loadsType = payloadList.get(0).toString();

            if(loadsType.equals("status")){
                //更新节点设备在线状态
                int loadsLabel = Integer.parseInt(payloadList.get(1).toString());
                String loadsTime = payloadList.get(2).toString();

                ((MyViewHolder)viewHolder).item_recv_devices_online.setImageResource(onlineStatus[loadsLabel]);
                ((MyViewHolder)viewHolder).item_recv_devices_fold_online.setImageResource(onlineStatus[loadsLabel]);

                if(loadsLabel == 1){
                    ((MyViewHolder)viewHolder).item_recv_devices_freshtime.setText(loadsTime);
                    ((MyViewHolder)viewHolder).item_recv_devices_fold_freshtime.setText(loadsTime);
                }
            }else if(loadsType.equals("state")){
                //更新节点设备遥测数据，同时更新在线状态
                int loadsLabel = Integer.parseInt(payloadList.get(1).toString());
                String loadsData = payloadList.get(2).toString();
                String loadsTime = payloadList.get(3).toString();

                if(loadsLabel == 0){
                    //更新温度
                    ((MyViewHolder)viewHolder).item_recv_devices_temp.setText(loadsData);

                    ((MyViewHolder)viewHolder).item_recv_devices_fold_temp.setText(loadsData);
                }else if(loadsLabel == 1){
                    //更新湿度
                    ((MyViewHolder)viewHolder).item_recv_devices_hum.setText(loadsData);

                    ((MyViewHolder)viewHolder).item_recv_devices_fold_hum.setText(loadsData);
                }else if(loadsLabel == 2){
                    //更新电压
                    int nowVoltage = Integer.parseInt(loadsData);

                    int maxVoltage = ((MyViewHolder)viewHolder).item_recv_devices_voltage.getMax();
                    if(nowVoltage > maxVoltage){
                        nowVoltage = maxVoltage;
                    }
                    ((MyViewHolder)viewHolder).item_recv_devices_voltage.setProgress(nowVoltage);
                }

                //收到数据，直接更新在线状态
                ((MyViewHolder)viewHolder).item_recv_devices_online.setImageResource(onlineStatus[1]);
                ((MyViewHolder)viewHolder).item_recv_devices_freshtime.setText(loadsTime);

                ((MyViewHolder)viewHolder).item_recv_devices_fold_online.setImageResource(onlineStatus[1]);
                ((MyViewHolder)viewHolder).item_recv_devices_fold_freshtime.setText(loadsTime);
            }
        }
    }

    @Override
    public int getItemCount() {
        return iotDeviceInfosList.size();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder{
        TextView item_recv_devices_name;
        TextView item_recv_devices_temp;
        TextView item_recv_devices_hum;
        TextView item_recv_devices_freshtime;
        ImageView item_recv_devices_online;
        TextView item_recv_devices_location;
        ProgressBar item_recv_devices_voltage;
        FrameLayout item_recv_devices_unfold_fl;
        FrameLayout item_recv_devices_fold_fl;

        TextView item_recv_devices_fold_name;
        TextView item_recv_devices_fold_temp;
        TextView item_recv_devices_fold_hum;
        TextView item_recv_devices_fold_freshtime;
        ImageView item_recv_devices_fold_online;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            item_recv_devices_name = itemView.findViewById(R.id.item_recv_devices_name);
            item_recv_devices_temp = itemView.findViewById(R.id.item_recv_devices_temp);
            item_recv_devices_hum = itemView.findViewById(R.id.item_recv_devices_hum);
            item_recv_devices_freshtime = itemView.findViewById(R.id.item_recv_devices_freshtime);
            item_recv_devices_online = itemView.findViewById(R.id.item_recv_devices_online);
            item_recv_devices_location = itemView.findViewById(R.id.item_recv_devices_location);
            item_recv_devices_voltage = itemView.findViewById(R.id.item_recv_devices_voltage);
            item_recv_devices_unfold_fl = itemView.findViewById(R.id.item_recv_devices_unfold_fl);
            item_recv_devices_fold_fl = itemView.findViewById(R.id.item_recv_devices_fold_fl);

            //折叠布局
            item_recv_devices_fold_name = itemView.findViewById(R.id.item_recv_devices_fold_name);
            item_recv_devices_fold_temp = itemView.findViewById(R.id.item_recv_devices_fold_temp);
            item_recv_devices_fold_hum = itemView.findViewById(R.id.item_recv_devices_fold_hum);
            item_recv_devices_fold_freshtime = itemView.findViewById(R.id.item_recv_devices_fold_freshtime);
            item_recv_devices_fold_online = itemView.findViewById(R.id.item_recv_devices_fold_online);
        }
    }
}
