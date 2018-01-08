package com.jwkj.device.entity;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * 本地局域网搜索到的设备对象
 * Created by hdl on 2017/4/11.
 */

public class LocalDevice implements Serializable, Comparable<LocalDevice> {
    /**
     * 设备id
     */
    private String id;
    /**
     * 设备的IP
     */
    private String IP;
    /**
     * 设备名字
     */
    private String name;
    /**
     * @deprecated 固件版本已经不通过此字段返回了
     * 设备的版本
     */
    private int version;
    /**
     * 设备是否有密码的标记
     */
    private int flag = 1;
    /**
     * @deprecated 固件版本已经不通过此字段返回了
     * 固件版本信息
     */
    private int rtspflag;
    /**
     * 设备类型
     */
    private int type = 0;
    /**
     * 设备子类型
     */
    private int subType;
    /**
     * 是否发现新id
     */
    private boolean isFoundNewId;
    /**
     * 新的设备id，isFoundNewId=true时才有值
     */
    private int contactNewId;
    /**
     * 本地p2p端口
     */
    private int localP2PPort;
    /**
     * 本地p2p的ip地址
     */
    private int localP2PRes;
    /**
     * 客户id
     */
    private int customId = 0;
    /**
     * mac信息
     */
    private String mac;

    /**
     * 返回的结果(考虑到此库可能更新没有那么及时，对外提供结果数组)
     */
    private byte[] resultData;

    public LocalDevice() {
    }

    public String getId() {
        return "" + id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIP() {
        return "" + IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     * @deprecated 固件版本已经不通过此字段返回了
     */
    public String getVersion() {
        return "" + ((rtspflag >> 4) & 0x1);
    }

    /**
     * @param version
     * @deprecated 固件版本已经不通过此字段返回了
     */
    public void setVersion(int version) {
        this.version = version;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return
     * @deprecated 固件版本已经不通过此字段返回了
     */
    public int getRtspflag() {
        return (rtspflag >> 2) & 1;
    }

    /**
     * @param rtspflag
     * @deprecated 固件版本已经不通过此字段返回了
     */
    public void setRtspflag(int rtspflag) {
        this.rtspflag = rtspflag;
    }

    public int getSubType() {
        return subType;
    }

    public boolean isFoundNewId() {
        return isFoundNewId;
    }

    public void setFoundNewId(boolean foundNewId) {
        isFoundNewId = foundNewId;
    }

    public int getContactNewId() {
        return contactNewId;
    }

    public void setContactNewId(int contactNewId) {
        this.contactNewId = contactNewId;
    }

    public int getLocalP2PPort() {
        return localP2PPort;
    }

    public void setLocalP2PPort(int localP2PPort) {
        this.localP2PPort = localP2PPort;
    }

    public int getLocalP2PRes() {
        return localP2PRes;
    }

    public void setLocalP2PRes(int localP2PRes) {
        this.localP2PRes = localP2PRes;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    public int getCustomId() {
        return customId;
    }

    public void setCustomId(int customId) {
        this.customId = customId;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public byte[] getResultData() {
        return resultData;
    }

    public void setResultData(byte[] resultData) {
        this.resultData = resultData;
    }

    @Override
    public String toString() {
        return "LocalDevice{" +
                "id='" + id + '\'' +
                ", IP='" + IP + '\'' +
                ", name='" + name + '\'' +
                ", version=" + version +
                ", flag=" + flag +
                ", rtspflag=" + rtspflag +
                ", type=" + type +
                ", subType=" + subType +
                ", isFoundNewId=" + isFoundNewId +
                ", contactNewId=" + contactNewId +
                ", localP2PPort=" + localP2PPort +
                ", localP2PRes=" + localP2PRes +
                ", customId=" + customId +
                ", mac='" + mac + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NonNull LocalDevice o) {
        return id.compareTo(o.getId());
    }
}
