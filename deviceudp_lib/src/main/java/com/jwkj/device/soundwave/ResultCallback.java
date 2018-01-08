package com.jwkj.device.soundwave;

import com.jwkj.device.shake.ShakeListener;


public abstract class ResultCallback extends ShakeListener {

    public abstract void onStopSend();
}
