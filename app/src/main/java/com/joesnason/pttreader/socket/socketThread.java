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
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by joesnason on 2016/4/22.
 */
public class socketThread extends Thread {

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

    private byte[] crlf = new byte[2];

    // telnet command
    public final static byte SE =         (byte)240;
    public final static byte NOP =        (byte)241;
    public final static byte DM =         (byte)242;
    public final static byte BRK =        (byte)243;
    public final static byte IP =         (byte)244;
    public final static byte AO =         (byte)245;
    public final static byte AYT =        (byte)246;
    public final static byte EC =         (byte)247;
    public final static byte EL =         (byte)248;
    public final static byte GA =         (byte)249;
    public final static byte SB =         (byte)250;
    public final static byte WILL =       (byte)251;
    public final static byte WONT =       (byte)252;
    public final static byte DO =         (byte)253;
    public final static byte DONT =       (byte)254;
    public final static byte IAC =        (byte)255;



    int will_count =0;
    int wont_count = 0;
    int do_count = 0;
    int dont_count = 0;
    int sb_count = 0;
    int se_count = 0;
    int other = 0;


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
                    Log.d(TAG," close socket");
                    break;

                }

                parse(buf, datalen);

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

    public void sendCommand(String command){

        if(mSocket == null)
            return;


        byte[] senddata  = new byte[command.length() + 2];
        int senddataptr = 0;
        System.arraycopy(command.getBytes(),0,senddata,0,command.length());
        senddataptr = command.length();

        senddata[senddataptr++] = 13;  //  add /n
        senddata[senddataptr++] = 10;  //  add /r



        try {
            OutputStream outputstream = mSocket.getOutputStream();

            try {
                outputstream.write(senddata);
                outputstream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "send command success");
    }

    public Boolean getConnectState(){
        return mIsconnet;
    }

    public void setConnectState(Boolean isconnect){
        mIsconnet = isconnect;
    }



    public int parse(byte[] data, int len){

        int bufpos = 0;
        int count = 0;

        for(bufpos = 0;bufpos <= len; bufpos++){

            if(len <= 0) {
                return 0;
            }

            if(bufpos == data.length || bufpos == len){
                break;
            }

            if(data[bufpos] == IAC){
                count++;
                bufpos++;
                Log.d(TAG,"buf have IAC: " + count);
                dispatchCommand(data[bufpos]);
                Log.d(TAG,"value: " + data[bufpos+1]);
                continue;
            }

        }

        Log.d(TAG,"buf have control code: " + count);

        return count;
    }

    public void dispatchCommand(byte b){


        switch (b){
            case WILL:
                will_count++;
                break;
            case WONT:
                wont_count++;
                break;
            case DO:
                do_count++;
                break;
            case DONT:
                dont_count++;
                break;
            case SB:
                sb_count++;
                break;
            case SE:
                se_count++;
                break;
            default:
                other++;
                break;
        }


        Log.d(TAG, "WILL: " + will_count + "  WONT: " + wont_count + " DO: " + do_count + " DONT: " + dont_count + " SB: " + sb_count + " SE: " + se_count + " other: " + other);
    }

}
