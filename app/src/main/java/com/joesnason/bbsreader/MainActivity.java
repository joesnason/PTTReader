package com.joesnason.bbsreader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends Activity {

    public int PORT = 23;
    public String HOST = "140.112.172.3";
    private String HOSTNAME = "ptt.cc";
    private Socket mSocket;
    private boolean mIsconnet = false;
    private Thread mConnThread = null;
    private static String TAG = "bbeReader";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button connbtn = (Button) findViewById(R.id.connButton);
        connbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doConnection();
                Log.d("jojo","onClick");
            }
        });

    }


    public void doConnection(){


        mConnThread = new Thread(new socketThread());
        mConnThread.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        mIsconnet = false;

        if(mConnThread != null) {
            mConnThread.interrupt();
        }
    }

    public class socketThread implements Runnable {

        @Override
        public void run() {

            // do DN Lookup
            try {
                InetAddress pttIP = (InetAddress.getByName(HOSTNAME));
                if(!(pttIP.equals(""))){
                    HOST = pttIP.getHostAddress();
                    Log.d(TAG,"get ptt IP: " + HOST);
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }


            try {
                mSocket = new Socket(HOST,PORT);
                Log.d(TAG,"try to connect");

                InputStream inputstream = mSocket.getInputStream();
                mIsconnet = true;

                byte[] arrayOfByte = new byte[1000];

                while(mIsconnet){

                    int j = 0;
                    try {


                        int i = arrayOfByte.length;
                        j = inputstream.read(arrayOfByte, 0, i);

                        if (j == -1) {
                            throw new Exception("Connection fail");
                        }
                    } catch (Exception e) {
                        mIsconnet = false;
                        mSocket.close();

                        e.printStackTrace();
                        mSocket = null;

                    }

                    final String strData = new String(arrayOfByte, 0,j,"BIG5");



                    Log.d(TAG,"get Data: " + strData);

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
