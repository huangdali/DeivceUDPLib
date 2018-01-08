package com.jwkj.device.shake;

import com.hdl.udpsenderlib.UDPResult;
import com.hdl.udpsenderlib.UDPResultCallback;
import com.hdl.udpsenderlib.UDPSender;
import com.jwkj.device.entity.LocalDevice;
import com.jwkj.device.entity.ShakeData;

/**
 * 扫描管理器
 * Created by hdl on 2017/4/11.
 */

public class ShakeManager {
    /**
     * 搜索管理器，保持单例
     */
    private static ShakeManager manager = null;
    /**
     * 结果回调handler
     */
    public ShakeListener listener;
    /**
     * 搜索时间
     */
    private long searchTime = 10 * 1000;
    /**
     * 搜索次数
     */
    private int searchCount = 1;
    /**
     * 上一次与下一次搜索的间隔时间
     */
    private long searchDelay = 3000;
    /**
     * 有些定制商只希望搜到自己的设备id
     */
    private int[] iCustomer;

    private ShakeManager() {
    }

    public static ShakeManager getInstance() {
        if (null == manager) {
            synchronized (ShakeManager.class) {
                manager = new ShakeManager();
            }
        }
        return manager;
    }

    /**
     * 设置搜索的策略
     *
     * @param sendCount 发送的次数
     * @param delay     每次发送的间隔
     * @return
     */
    public ShakeManager schedule(int sendCount, long delay) {
        this.searchCount = sendCount;
        this.searchDelay = delay;
        return this;
    }

    /**
     * 设置搜索超时时间
     *
     * @param time 搜索时间  时间的毫秒值（eg:1000）
     * @return 当前对象
     */
    public ShakeManager setSearchTime(int time) {
        this.searchTime = time;
        return this;
    }

    public int[] getiCustomer() {
        return iCustomer;
    }

    /**
     * 有些定制商只希望搜到自己的设备id.
     * 如果没有此需求的不要设置任何值（不调用此方法即可）
     *
     * @param iCustomer
     * @return
     */
    public ShakeManager setiCustomer(int[] iCustomer) {
        this.iCustomer = iCustomer;
        return this;
    }

    /**
     * 开始扫描
     *
     * @param listener
     */
    public void shaking(final ShakeListener listener) {
        this.listener = listener;
        ShakeData data = new ShakeData();
        if (iCustomer != null && iCustomer.length > 0) {
            data.setiCustomer(iCustomer);
        }
        data.setCmd(ShakeData.Cmd.CMD_SHAKE_DEVICE);
        UDPSender.getInstance()
                .setTargetPort(ShakeData.Cmd.CMD_SHAKE_DEVICE_DEFAULT_PORT)//设置目标端口
                .setLocalReceivePort(ShakeData.Cmd.CMD_SHAKE_DEVICE_DEFAULT_PORT)//设置接收端口
                .setInstructions(ShakeData.getBytes(data))//发送的指令
                .setReceiveTimeOut(searchTime)//持续searchTime毫秒未接收到消息就结束本次任务
                .schedule(searchCount, searchDelay)//总的重复执行searchCount次任务，每次执行的间隔为searchDelay秒
                .start(new UDPResultCallback() {//开始发送
                    /**
                     * 请求开始的时候回调
                     */
                    @Override
                    public void onStart() {
                        listener.onStart();
                    }

                    /**
                     * 每拿到一个结果的时候就回调
                     *
                     * @param result 请求的结果
                     */
                    @Override
                    public void onNext(UDPResult result) {
                        LocalDevice device = ShakeData.getDevice(result);
                        if (device != null) {
                            listener.onNext(device);
                        }
                    }

                    /**
                     * 当发生错误的时候回调
                     *
                     * @param throwable 错误信息
                     */
                    @Override
                    public void onError(Throwable throwable) {
                        listener.onError(throwable);
                    }

                    /**
                     * 请求结束的时候回调
                     */
                    @Override
                    public void onCompleted() {
                        listener.onCompleted();
                    }
                });
    }


    /**
     * 是否正在搜索
     *
     * @return 搜索状态
     */
    public boolean isShaking() {
        return UDPSender.getInstance().isRunning();
    }

    /**
     * 关闭搜索任务
     */
    public void closeShake() {
        if (isShaking()) {
            UDPSender.getInstance().stop();
        }
    }
}
