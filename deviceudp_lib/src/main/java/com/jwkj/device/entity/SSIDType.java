package com.jwkj.device.entity;

/**
 * SSID的加密类型
 * Created by HDL on 2017/12/28.
 *
 * @author HDL
 */
public enum SSIDType {

    /**
     * WPA类型
     */
    PSK("wpa", 2),
    /**
     * WPA类型
     */
    EAP("eap", 3),
    /**
     * WEP类型
     */
    WEP("wep", 1),
    /**
     * WEP类型
     */
    UNKNOW("unknow", -1),
    /**
     * 默认
     */
    NONE("default", 0);
    /**
     * 枚举的名字
     */
    private String name;
    /**
     * 枚举具体对应的值
     */
    private int value;

    /**
     * 获取当前的枚举值
     *
     * @return
     */
    public int getValue() {
        return value;
    }

    SSIDType(String name, int value) {
        this.name = name;
        this.value = value;
    }
}
