package com.joesnason.pttreader.socket;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.joesnason.pttreader.Config;
import com.joesnason.pttreader.MainActivity;
import com.joesnason.pttreader.filter.filter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by joesnason on 2016/4/22.
 */
public class socketThread implements Runnable {

    public String HOST = "140.112.172.3"; // default IP
    final boolean DEBUG_DATA_MODE = false;

    private String filename = "pttfileBIG5.txt";
    private FileOutputStream  filewriter;
    private Socket mSocket;
    private byte[] buf;
    private boolean mIsconnet = false;
    private StringBuilder contenttringBuilder = new StringBuilder();

    private static String TAG = "bbeReader";

    private Handler UIHandler = null;

    @Override
    public void run() {
        // do DN Lookup
        try {
            InetAddress pttIP = (InetAddress.getByName(Config.HOSTNAME));
            if(!(pttIP.equals(""))) {
                HOST = pttIP.getHostAddress();
                Log.d(TAG, "get ptt IP: " + HOST);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


        if(DEBUG_DATA_MODE) {
            File pttfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/BBS/", filename);
            File pttfolder = pttfile.getParentFile();

            if(pttfolder != null)
                pttfolder.mkdirs();


            if(!pttfile.exists()) {
                try {
                    pttfile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "create new file");
            }

            try {
                filewriter = new FileOutputStream(pttfile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            mSocket = new Socket(HOST, Config.PORT);
            Log.d(TAG,"try to connect");

            InputStream inputstream = mSocket.getInputStream();
            mIsconnet = true;

            buf = new byte[4096];

            while(mIsconnet){

                int datalen = 0;
                try {


                    int i = buf.length;
                    datalen = inputstream.read(buf, 0, i);

                    if (datalen == -1) {
                        throw new Exception("Connection fail");
                    }
                } catch (Exception e) {
                    mIsconnet = false;
                    mSocket.close();
                    if(DEBUG_DATA_MODE) {
                        filewriter.close();
                    }
                    e.printStackTrace();
                    mSocket = null;

                }

                //filter(buf, datalen);

                //Log.d("jojo", "return j = " + datalen);
                final String strData = new String(buf, 0, datalen,"BIG5");

                if(DEBUG_DATA_MODE) {
                    filewriter.write(strData.getBytes());
                }

                if (UIHandler != null) {
                    Message Msg = new Message();
                    Msg.what = MainActivity.REFLASH_CONTENT;
                    Msg.obj = contenttringBuilder.append(filter.filter_all_Control(strData)); //disable ASCII Escape Sequence
                    UIHandler.sendMessage(Msg);
                    Log.d(TAG, "get Data: " + strData);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setHandler(Handler handler){

        UIHandler = handler;
    }

    public Boolean getConnectState(){
        return mIsconnet;
    }

    public void setConnectState(Boolean isconnect){
        mIsconnet = isconnect;
    }

}
