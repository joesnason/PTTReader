package com.joesnason.pttreader.socket;

import android.os.Handler;

/**
 * Created by joesnason on 2016/4/22.
 */
public class SocketManager {

    private Thread mConnThread = null;
    private Handler UIHandler = null;
    private socketThread socketThread;
    public SocketManager(){

    }

    public void setHandler(Handler handler){

        UIHandler = handler;
    }

    public void doConnect(){
        socketThread = new socketThread();
        socketThread.setHandler(UIHandler);
        socketThread.start();
    }

    public void sendCommand(String command){
        socketThread.sendCommand(command);

    }

    public void disconnet(){

        socketThread.setConnectState(false);
        if(mConnThread != null) {
            mConnThread.interrupt();
        }
    }
}
