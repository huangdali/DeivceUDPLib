package com.jwkj.device.shake;


import com.jwkj.device.entity.LocalDevice;

/**
 * 搜索过程监听器
 * Created by hdl on 2017/4/11.
 */

public abstract class ShakeListener {
    /**
     * 搜索开始的时候回调
     */
    public void onStart() {
    }

    /**
     * 搜索发生错误的时候开始回调
     */
    public void onError(Throwable throwable) {
    }

    /**
     * 每搜索到一台设备就回调一次
     *
     * @param device
     */
    public abstract void onNext(LocalDevice device);


    /**
     * 搜索结束的时候回调
     */
    public void onCompleted() {
    }
}
