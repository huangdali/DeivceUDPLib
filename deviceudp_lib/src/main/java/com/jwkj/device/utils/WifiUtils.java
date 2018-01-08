package com.jwkj.device.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.jwkj.device.entity.SSIDType;

import java.util.List;

public class WifiUtils {
    private WifiManager wifiManager;
    private Context context;
    public final static String APTag = "GW_AP_";
    private WifiLock wifiLock;
    private static WifiUtils mWifiUtils;

    private WifiUtils() {
    }

    public static WifiUtils getInstance() {
        if (mWifiUtils == null) {
            synchronized (WifiUtils.class) {
                if (mWifiUtils == null) {
                    mWifiUtils = new WifiUtils();
                }
            }
        }
        return mWifiUtils;
    }

    public WifiUtils with(Context context) {
        this.context = context.getApplicationContext();
        checkContextIsNull();
        wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        return this;
    }

    private void checkContextIsNull() {
        if (context == null) {
            throw new NullPointerException("context is null,please call WifiUtils.getInstance().with(context).***");
        }
    }

    /**
     * 创建WiFi锁
     *
     * @param lockName
     * @return
     */
    public WifiLock creatWifiLock(String lockName) {
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, lockName);
        return wifiLock;
    }

    public void wifiLock() {
        if (wifiLock == null) {
            creatWifiLock(APTag);
        }
        wifiLock.acquire();
    }

    public void wifiUnlock() {
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }
    }


    public boolean getIsOpen() {
        return wifiManager.isWifiEnabled();
    }

    /**
     * 连接wifi
     *
     * @param SSID     wifi名字
     * @param password 密码
     * @param type     类型
     * @throws Exception
     */
    public void connectWifi(String SSID, String password, SSIDType type) throws Exception {
        if (wifiManager != null) {
            if (getIsOpen()) {
                connectHandler(SSID, password, type);
            } else {
                // 打开WIFI
                if (openWifi()) {
                    connectHandler(SSID, password, type);
                } else {
                    // 打开Wifi失败
                    throw new Exception("open wifi fail");
                }
            }
        }
    }

    /**
     * 打开wifi功能
     *
     * @return
     */
    public boolean openWifi() {
        boolean bRet = true;
        if (!wifiManager.isWifiEnabled()) {
            bRet = wifiManager.setWifiEnabled(true);
        }
        return bRet;
    }

    /**
     * 连接监听
     *
     * @param SSID
     * @param Password
     * @param type
     * @return
     * @throws Exception
     */
    public boolean connectHandler(String SSID, String Password, SSIDType type) throws Exception {
        WifiConfiguration wifi = this.isExsits(SSID);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (wifi == null) {
            WifiConfiguration wifiConfig = this.createWifiInfo(SSID, Password, type);
            if (wifiConfig != null) {
                int netID = wifiManager.addNetwork(wifiConfig);
                if (netID >= 0) {
                    if (!connectWifi(netID)) {
                        throw new Exception(SSID + "Is not exsits and connectWifi error return :" + netID);
                    }
                } else {
                    throw new Exception(SSID + "Is not exsits addNetwork error return :" + netID);
                }
            } else {
                throw new Exception(SSID + "Is not exsits createWifiInfo error return null");
            }
            return true;
        } else {
            if (currentapiVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (!connectWifi(wifi.networkId)) {
                    throw new Exception(SSID + "exsist and connectWifi error return :" + wifi.networkId);
                }
                return true;
            } else {
                boolean isremove = wifiManager.removeNetwork(wifi.networkId);
                WifiConfiguration wifiConfig = this
                        .createWifiInfo(SSID, Password, type);
                if (wifiConfig != null) {
                    int netID = wifiManager.addNetwork(wifiConfig);
                    if (netID >= 0) {
                        if (!connectWifi(netID)) {
                            throw new Exception(SSID + "Exsits and connectWifi error return :" + netID);
                        }
                    } else {
                        throw new Exception(SSID + "Exsits addNetwork error return :" + netID);
                    }
                } else {
                    throw new Exception(SSID + "Exsits createWifiInfo error return null");
                }
                return true;
            }
        }
    }

    /**
     * 链接指定wifi
     **/
    public boolean connectWifi(int netId) {
        boolean connectResult = wifiManager.enableNetwork(netId, true);
        if (connectResult) {
            wifiManager.saveConfiguration();
            wifiManager.reconnect();
        }
        return connectResult;

    }

    /**
     * 查看以前是否也配置过这个网络
     */
    public WifiConfiguration isExsits(String SSID) {
        if (SSID == null || wifiManager == null) {
            return null;
        }
        List<WifiConfiguration> existingConfigs = wifiManager
                .getConfiguredNetworks();
        if (existingConfigs == null) {
            return null;
        }
        WifiConfiguration wifiConfiguration = null;
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")
                    || existingConfig.SSID.equals(SSID)) {
                wifiConfiguration = existingConfig;
                return existingConfig;
            }
        }
        return wifiConfiguration;
    }

    /**
     * 创建wifi信息
     *
     * @param SSID
     * @param password
     * @param type
     * @return
     */
    public WifiConfiguration createWifiInfo(String SSID, String password, SSIDType type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        boolean has = false;
        try {
            has = containsTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        config.SSID = "\"" + SSID + "\"";
        if (type == SSIDType.NONE) {
            config.wepKeys[0] = "\"" + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (type == SSIDType.WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (type == SSIDType.PSK) {
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            if (password == null || password.equals("")) {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            } else {
                config.preSharedKey = "\"" + password + "\"";
                config.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.TKIP);
                // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                config.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.CCMP);
            }
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            return null;
        }
        return config;
    }

    // 检查指定的wifi是否存在列表中
    public boolean isScanExist(String SSid) {
        List<ScanResult> lists = getLists();
        if (lists != null && lists.size() > 0) {
            for (int i = 0; i < lists.size(); i++) {
                if (lists.get(i).SSID.equals(SSid)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<ScanResult> getLists() {
        wifiManager.startScan();
        List<ScanResult> lists = wifiManager.getScanResults();
        return lists;
    }

    public boolean isConnectWifi(String SSID) {
        if (wifiManager == null) {
            return false;
        }
        String connectSSID = wifiManager.getConnectionInfo().getSSID();
        int workId = wifiManager.getConnectionInfo().getNetworkId();
        if (TextUtils.isEmpty(connectSSID) || workId == -1) {
            return false;
        }
        return connectSSID.equals("\"" + SSID + "\"")
                || connectSSID.equals(SSID);
    }

    public void setWiFiEnAble(boolean enable) {
        if (wifiManager != null) {
            wifiManager.setWifiEnabled(enable);
        }
    }

    /**
     * 仅断开wifi，并不忘记网络
     *
     * @param ssid wifi名
     */
    public void DisConnectWifi(String ssid) {
        if (isConnectWifi(ssid)) {
            wifiManager.disconnect();
        }
    }

    /**
     * 断开某个WiFi，并忘记网络
     *
     * @param ssid
     */
    public void disConnectWifi(String ssid) {
        if (isConnectWifi(ssid)) {
            wifiManager.disconnect();
        }
        WifiConfiguration wifi = this.isExsits(ssid);
        if (wifi != null) {
            wifiManager.removeNetwork(wifi.networkId);
        }
    }


    /**
     * 判断是否连接上wifi
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                if (mWiFiNetworkInfo.isAvailable()) {
                    return mWiFiNetworkInfo.isConnected();
                }
                return false;
            }
        }
        return false;
    }

    /**
     * 判断是否是移动数据连接
     *
     * @param context
     * @return
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                if (mMobileNetworkInfo.isAvailable()) {
                    return mMobileNetworkInfo.isConnected();
                }
                return false;
            }
        }
        return false;
    }

    public boolean containsTest() throws Exception {
        List<ScanResult> list = getLists();
        for (ScanResult result : list) {
            return result.SSID.contains("\"");
        }
        throw new Exception("containsTest no wifi info");
    }

    /**
     * 是否连接上网络
     *
     * @return
     */
    public boolean isConnectNetwork() {
        ConnectivityManager mConnectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //检查网络连接
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        if (info == null || !mConnectivity.getBackgroundDataSetting()) {
            return false;
        }
        return true;
    }
}
