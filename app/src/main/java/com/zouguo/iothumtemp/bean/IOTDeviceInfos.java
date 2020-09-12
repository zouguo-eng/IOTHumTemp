package com.zouguo.iothumtemp.bean;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class IOTDeviceInfos extends LitePalSupport {
    private int id;
    private String dname;
    @Column(unique = true, nullable = false)
    private String dnodename;
    private int dnodetype;
    private String dnodelabel;
    //0--离线；1--在线；2--未知
    private int dstatus;
    private String dtemptopic;
    private String dhumtopic;
    private String dtemp;
    private String dhum;
    private float dvoltage;
    private String dtime;
    private String dlocation;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDname() {
        return dname;
    }

    public void setDname(String dname) {
        this.dname = dname;
    }

    public String getDnodename() {
        return dnodename;
    }

    public void setDnodename(String dnodename) {
        this.dnodename = dnodename;
    }

    public int getDnodetype() {
        return dnodetype;
    }

    public void setDnodetype(int dnodetype) {
        this.dnodetype = dnodetype;
    }

    public String getDnodelabel() {
        return dnodelabel;
    }

    public void setDnodelabel(String dnodelabel) {
        this.dnodelabel = dnodelabel;
    }

    public int getDstatus() {
        return dstatus;
    }

    public void setDstatus(int dstatus) {
        this.dstatus = dstatus;
    }

    public String getDtemptopic() {
        return dtemptopic;
    }

    public void setDtemptopic(String dtemptopic) {
        this.dtemptopic = dtemptopic;
    }

    public String getDhumtopic() {
        return dhumtopic;
    }

    public void setDhumtopic(String dhumtopic) {
        this.dhumtopic = dhumtopic;
    }

    public String getDtemp() {
        return dtemp;
    }

    public void setDtemp(String dtemp) {
        this.dtemp = dtemp;
    }

    public String getDhum() {
        return dhum;
    }

    public void setDhum(String dhum) {
        this.dhum = dhum;
    }

    public float getDvoltage() {
        return dvoltage;
    }

    public void setDvoltage(float dvoltage) {
        this.dvoltage = dvoltage;
    }

    public String getDtime() {
        return dtime;
    }

    public void setDtime(String dtime) {
        this.dtime = dtime;
    }

    public String getDlocation() {
        return dlocation;
    }

    public void setDlocation(String dlocation) {
        this.dlocation = dlocation;
    }
}
