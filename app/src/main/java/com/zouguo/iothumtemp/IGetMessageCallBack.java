package com.zouguo.iothumtemp;

public interface IGetMessageCallBack {
    void setMessageArrived(String topic, String message, String logs);
    void setConnectStatus(boolean isConnected);
    void setOnFailure(Throwable throwable);
}
