package com.jwkj.device.entity;

import com.p2p.core.utils.MyUtils;

/**
 * AP模式设备的配置项
 * Created by HDL on 2017/12/28.
 *
 * @author HDL
 */

public class APDeviceConfig {
    /**
     * 设备id
     */
    private String deviceID = "";
    /**
     * wifi名字
     */
    private String wifiSSID = "";
    /**
     * wifi密码
     */
    private String wifiPwd = "";
    /**
     * 加密方式
     */
    private SSIDType type = SSIDType.NONE;
    /**
     * 设备密码
     */
    private String devicePwd = "";

    public APDeviceConfig() {
    }

    public APDeviceConfig(String deviceID, String wifiSSID, String wifiPwd, SSIDType type, String devicePwd) {
        this.deviceID = deviceID;
        this.wifiSSID = wifiSSID;
        this.wifiPwd = wifiPwd;
        this.type = type;
        this.devicePwd = devicePwd;
    }

    public APDeviceConfig(String wifiSSID, String wifiPwd, String devicePwd) {
        this.wifiSSID = wifiSSID;
        this.wifiPwd = wifiPwd;
        this.devicePwd = devicePwd;
    }

    /**
     * 获取发送数据的byte数组
     *
     * @return
     */
    public byte[] getSendData() {
        return MyUtils.getApSendWifi(deviceID, wifiSSID, wifiPwd, type.getValue(), devicePwd);
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getWifiSSID() {
        return wifiSSID;
    }

    public void setWifiSSID(String wifiSSID) {
        this.wifiSSID = wifiSSID;
    }

    public String getWifiPwd() {
        return wifiPwd;
    }

    public void setWifiPwd(String wifiPwd) {
        this.wifiPwd = wifiPwd;
    }

    public SSIDType getType() {
        return type;
    }

    public void setType(SSIDType type) {
        this.type = type;
    }

    public String getDevicePwd() {
        return devicePwd;
    }

    public void setDevicePwd(String devicePwd) {
        this.devicePwd = devicePwd;
    }

    @Override
    public String toString() {
        return "APDeviceConfig{" +
                "deviceID='" + deviceID + '\'' +
                ", wifiSSID='" + wifiSSID + '\'' +
                ", wifiPwd='" + wifiPwd + '\'' +
                ", type='" + type + '\'' +
                ", devicePwd='" + devicePwd + '\'' +
                '}';
    }
}
