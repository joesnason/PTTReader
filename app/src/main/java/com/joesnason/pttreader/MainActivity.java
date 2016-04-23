package com.joesnason.pttreader;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.joesnason.pttreader.socket.SocketManager;


public class MainActivity extends Activity {

    final boolean DEBUG_DATA_MODE = false;

    public final static int REFLASH_CONTENT = 1;

    private static String TAG = "bbeReader";
    private TextView contentView;
    private Handler UIhandler;
    private SocketManager socketManager = null;



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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.joesnason.pttreader.R.layout.activity_main);

        Button connbtn = (Button) findViewById(com.joesnason.pttreader.R.id.connButton);


        UIhandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case REFLASH_CONTENT:
                        contentView.setText(msg.obj.toString());
                        break;

                    case 2:


                    default:
                        break;

                }
            }
        };

        socketManager = new SocketManager();
        socketManager.setHandler(UIhandler);

        connbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socketManager.doConnect();

            }
        });

        contentView = (TextView) findViewById(com.joesnason.pttreader.R.id.contentView);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.joesnason.pttreader.R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.joesnason.pttreader.R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        socketManager.disconnet();

    }



    public int filte(byte[] data, int len){

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
