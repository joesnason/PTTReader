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





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.joesnason.pttreader.R.layout.activity_main);

        Button connbtn = (Button) findViewById(R.id.connButton);
        Button guestbtn = (Button) findViewById(R.id.guestBtn);


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


        guestbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                socketManager.sendCommand("guest");
                Log.d(TAG,"enter button");
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




}
