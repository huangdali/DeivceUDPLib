package com.jwkj.device.apmode;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hdl.udpsenderlib.UDPResult;
import com.hdl.udpsenderlib.UDPResultCallback;
import com.hdl.udpsenderlib.UDPSender;
import com.jwkj.device.entity.APDeviceConfig;
import com.jwkj.device.entity.SSIDType;
import com.jwkj.device.utils.MUtils;
import com.p2p.core.utils.MyUtils;

import java.nio.ByteBuffer;

/**
 * AP模式的管理器
 */
public class APManager {
    private String ssid = "";
    private String pwd;
    private static APManager manager;
    private int port = 8899;
    private ResultCallback callback;
    private boolean isCanReceive = true;//是否可以接收
    private Context mContext;
    private APDeviceConfig apDeviceConfig;

    private APManager() {
    }

    public static APManager getInstance() {
        if (manager == null) {
            manager = new APManager();
        }
        return manager;
    }

    public APManager with(Context context) {
        this.mContext = context.getApplicationContext();
        checkContextIsNull();
        return this;
    }

    /**
     * 检测上下文对象是否为空
     */
    private void checkContextIsNull() {
        if (mContext == null) {
            throw new NullPointerException("context is null,please call SoundWaveSender.getInstance().with(context).***");
        }
    }

    public APDeviceConfig getApDeviceConfig() {
        return apDeviceConfig;
    }

    public APManager setApDeviceConfig(APDeviceConfig apDeviceConfig) {
        if (TextUtils.isEmpty(apDeviceConfig.getDeviceID())) {
            try {
                apDeviceConfig.setDeviceID(MUtils.getAPDeviceId(apDeviceConfig.getWifiSSID()));
            } catch (Exception e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.onError(new Throwable("A device that is not a AP mode"));
                }
            }
        }
        if (apDeviceConfig.getType() == SSIDType.NONE) {
            checkContextIsNull();
            apDeviceConfig.setType(MUtils.getSSIDType(mContext));
        }
        this.apDeviceConfig = apDeviceConfig;
        return this;
    }

    /**
     * 设置wifi
     *
     * @param ssid wifi名字
     * @param pwd  密码
     * @return
     */
    public APManager setWifi(String ssid, String pwd) {
        if (ssid.startsWith("GW_AP_")) {
            if (callback != null) {
//                throw new IllegalArgumentException("A device that is not a AP mode");
                callback.onError(new Throwable("A device that is not a AP mode"));
                stopReceive();
                stopSend();
            }
        }
        this.ssid = ssid;
        this.pwd = pwd;
        return this;
    }

    public void send(ResultCallback callback) {
//        if (this.callback == null) {
        this.callback = callback;
//        }
        if (isCanReceive) {
            isCanReceive = false;
            startReceive();
        }
    }

    /**
     * 开始接收数据，单次任务只能调用一次
     */
    private void startReceive() {
        if (apDeviceConfig == null) {
            System.err.println("\n\n\n没有设置ApDeviceConfig对象\n\n\n");
            callback.onError(new Throwable("No call APManager.getInstance().with(mContext).setApDeviceConfig(***)"));
            return;
        }
        if (!apDeviceConfig.getWifiSSID().startsWith("GW_AP_")) {
            if (callback != null) {
                System.err.println("\n\n\n " + ssid + " 不是AP模式的设备,请设置 GW_AP_ 开头的WIFI\n\n\n");
                callback.onError(new Throwable("A device that is not a AP mode"));
                return;
            }
        }
        UDPSender.getInstance()
                .setInstructions(apDeviceConfig.getSendData())
                .schedule(10, 100)
                .setLocalReceivePort(port)
                .setTargetPort(port)
                .setTargetIp(MyUtils.getAPDeviceIp(mContext))
                .start(new UDPResultCallback() {
                    @Override
                    public void onNext(UDPResult result) {
                        Log.e("hdltag", "onNext(APManager.java:134):" + result);
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        buffer.put(result.getResultData());
                        int cmd = MyUtils.bytesToInt(result.getResultData(), 0);
                        Log.e("hdltag", "onNext(APManager.java:138):cmd=" + cmd);
                        if (cmd == 17) {
                            Log.e("hdltag", "onNext(APManager.java:140):设备收到wifi名字和密码了");
                            Log.e("hdltag", "onNext(APManager.java:141):设备已经收到了，停止接收，然后再发送确认wifi");
                            UDPSender.getInstance().stop();
                            final byte[] receiveData = {52, 0, 0, 0};//确认链接wifi
                            UDPSender.getInstance()
                                    .setInstructions(receiveData)
                                    .schedule(10, 100)
                                    .setLocalReceivePort(port)
                                    .setTargetPort(port)
                                    .setTargetIp(MyUtils.getAPDeviceIp(mContext))
                                    .start(new UDPResultCallback() {
                                        @Override
                                        public void onNext(UDPResult result) {
                                            Log.e("hdltag", "onNext(APManager.java:ipco):收到确认消息了" + result);

                                            int cmd = MyUtils.bytesToInt(result.getResultData(), 4);
                                            if (cmd == 0) {
                                                Log.e("hdltag", "onNext(APManager.java:156):设备联网成功");
                                            }
                                            String id = String.valueOf(MyUtils.bytesToInt(result.getResultData(), 16));
                                            if (!apDeviceConfig.getDeviceID().equals(id)) {
                                                Log.e("hdltag", "onNext(APManager.java:161):不是当前设备（ID = "+apDeviceConfig.getDeviceID()+"）回的信息");
                                                return;
                                            }
                                            if (callback != null) {
                                                callback.onConfigPwdSuccess();
                                            }
                                        }

                                        /**
                                         * 请求开始的时候回调
                                         */
                                        @Override
                                        public void onStart() {
                                            super.onStart();
                                            Log.e("hdltag", "onStart(APManager.java:169):确认消息发送了");
                                        }

                                        /**
                                         * 请求结束的时候回调
                                         */
                                        @Override
                                        public void onCompleted() {
                                            super.onCompleted();
                                            Log.e("hdltag", "onCompleted(APManager.java:178):确认消息完成了");
                                        }

                                        /**
                                         * 当发生错误的时候回调
                                         *
                                         * @param throwable 错误信息
                                         */
                                        @Override
                                        public void onError(Throwable throwable) {
                                            super.onError(throwable);
                                            if (callback != null) {
                                                callback.onError(throwable);
                                            }
                                            Log.e("hdltag", "onError(APManager.java:192):出错了" + throwable);
                                        }
                                    });

                        }
                    }

                    /**
                     * 请求开始的时候回调
                     */
                    @Override
                    public void onStart() {
                        super.onStart();
                        Log.e("hdltag", "onStart(APManager.java:205):start");
                    }

                    /**
                     * 请求结束的时候回调
                     */
                    @Override
                    public void onCompleted() {
                        super.onCompleted();
                        Log.e("hdltag", "onCompleted(APManager.java:214):");
                    }

                    /**
                     * 当发生错误的时候回调
                     *
                     * @param throwable 错误信息
                     */
                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        if (callback != null) {
                            callback.onError(throwable);
                        }
                        Log.e("hdltag", "onError(APManager.java:228):" + throwable.toString());
                    }
                });
    }

    /**
     * 停止接收数据
     */
    private void stopReceive() {
        isCanReceive = true;//停止任务了要复位
        if (UDPSender.getInstance() != null) {
            UDPSender.getInstance().stop();
        }
    }


    /**
     * 停止发送声波
     *
     * @return
     */
    public APManager stopSend() {
        stopReceive();
        return this;
    }
}
