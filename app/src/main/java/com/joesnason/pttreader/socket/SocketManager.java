package com.joesnason.pttreader.socket;

import android.os.Handler;

/**
 * Created by joesnason on 2016/4/22.
 */
public class SocketManager {

    private Thread mConnThread = null;
    private Handler UIHandler = null;
    private socketThread socketrunnable;
    public SocketManager(){

    }

    public void setHandler(Handler handler){

        UIHandler = handler;
    }

    public void doConnect(){
        socketrunnable = new socketThread();
        socketrunnable.setHandler(UIHandler);
        mConnThread = new Thread(socketrunnable);
        mConnThread.start();
    }

    public void disconnet(){

        socketrunnable.setConnectState(false);
        if(mConnThread != null) {
            mConnThread.interrupt();
        }
    }
}
