package com.example.lu.rrcs;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.UUID;

public class Information extends AppCompatActivity {

    //取目前溫溼度值
    private TextView TEMP;
    private TextView HUM;
    String temp_value;
    String hum_value;

    RequestQueue rq;
    String URL_temp ="https://api.thingspeak.com/channels/498519/fields/1/last.json?api_key=2HL268HGV6GP17IK&timezone=Asia/Taipei";
    String URL_hum ="https://api.thingspeak.com/channels/498519/fields/2/last.json?api_key=2HL268HGV6GP17IK&timezone=Asia/Taipei";


    //從資料庫取的的溫溼度範圍
    String temp_low;
    String temp_high;
    String hum_low;
    String hum_high;

    Double temp_value_double;
    Double hum_value_double;
    Double temp_low_double;
    Double temp_high_double;
    Double hum_low_double;
    Double hum_high_double;

    private ImageButton switch_HOT;
    private ImageButton switch_WATER;
    private ImageButton switch_STICK;
    private ImageButton switch_SCREEN;

    private ImageButton mScanBtn;
    private ImageButton mDiscoverBtn;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;

    Intent intent_name;
    Bundle bundle_name;
    String pet_name;
    String auto_onoff;  //       0為手動        1為自動

    //判斷功能按鈕按了幾次
    int click_hot = 0;
    int click_water = 0 ;
    int click_stick = 1;
    int click_screen = 1;

    private Handler mHandler;
    // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread;
    // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null;
    // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString ("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1;
    // used to identify adding bluetooth names  用於識別添加藍芽名稱
    private final static int MESSAGE_READ = 2;
    // used in bluetooth handler to identify message update   用於藍牙處理程序來識別消息更新
    private final static int CONNECTING_STATUS = 3;
    // used in bluetooth handler to identify message status  用於藍牙處理程序來識別消息狀態


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        findID();

        //----------------------------------------取得選去的寵物名稱
        intent_name = getIntent();
        bundle_name = intent_name.getExtras();
        pet_name = bundle_name.getString("pet_name");
        auto_onoff = bundle_name.getString("auto_onoff");    //       0為手動        1為自動

        //---------------------------連接DB
        Database db = new Database(getApplicationContext());

        //----------------透過DB取得temp_low
        temp_low = db.getTempLow(pet_name);
        // Toast.makeText(Information.this,temp_low,Toast.LENGTH_SHORT).show();

        //----------------透過DB取得temp_high
        temp_high = db.getTempHigh(pet_name);

        //----------------透過DB取得hum_low
        hum_low = db.getHumLow(pet_name);

        //----------------透過DB取得hum_high
        hum_high = db.getHumHigh(pet_name);

        //----------------取溫溼度值
        rq = Volley.newRequestQueue(this);
        JSON_GetTempValue();
        JSON_GetHumValue();


        temp_low_double = Double.parseDouble(temp_low);
        temp_high_double = Double.parseDouble(temp_high);
        hum_low_double = Double.parseDouble(hum_low);
        hum_high_double = Double.parseDouble(hum_high);


        //-----------------------------------------------------------------連芽連線------------------------------------------------------------
        mBTArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // 詢問藍芽裝置權限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        //定義執行緒 當收到不同的指令做對應的內容
        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg){

                if(msg.what == CONNECTING_STATUS){
                    //收到CONNECTING_STATUS 顯示以下訊息
                    if(msg.arg1 == 1)
                        Toast.makeText(Information.this,"Connected to Device: " + (String)(msg.obj),Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(Information.this,"Connection Failed",Toast.LENGTH_SHORT).show();
                }
            }
        };

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(Information.this,"Status: Bluetooth not found",Toast.LENGTH_SHORT).show();

            Toast.makeText(getApplicationContext(),"Bluetooth device not found!",Toast.LENGTH_SHORT).show();
        }
        else {

            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn(v);

                }
            });

            mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                   // Toast.makeText(Information.this,temp_value,Toast.LENGTH_SHORT).show();

                    discover(v);

                }
            });
        }

        //------------------------------------------------------------定義控制設備的按鈕-----------------------------------------------------------
        //final Handler handler = new Handler();
        //燈
        switch_HOT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(auto_onoff.equals("0")) {

                    if (click_hot % 2 != 0) { //目前是開的狀態 要關閉
                        if (mConnectedThread != null) //First check to make sure thread created
                            mConnectedThread.write("3");
                        switch_HOT.setBackgroundResource(R.drawable.hot1);
                        click_hot++;

//                        handler.postDelayed(new Runnable(){
//
//                            @Override
//                            public void run() {
//
//                                switch_HOT.setBackgroundResource(R.drawable.hot_on1);
//                                click_hot++;
//                            }}, 7873);


                    }
                    else if (click_hot % 2 == 0) { //目前是關的狀態   要開啟

                        if (mConnectedThread != null) //First check  make sure thread created
                            mConnectedThread.write("2");
                        switch_HOT.setBackgroundResource(R.drawable.hot_on1);
                        click_hot++;

//                        handler.postDelayed(new Runnable(){
//
//                            @Override
//                            public void run() {
//
//                                switch_HOT.setBackgroundResource(R.drawable.hot1);
//                                click_hot++;
//                            }}, 7873);
                    }

                }
            }
        });

        //灑水器
        switch_WATER.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(auto_onoff.equals("0")) {
                    if (click_water % 2 != 0) { //目前是開的狀態  要關閉
                        if (mConnectedThread != null) //First check to make sure thread created
                            mConnectedThread.write("5");
                        switch_WATER.setBackgroundResource(R.drawable.water1);
                        click_water++;

//                        handler.postDelayed(new Runnable(){
//
//                            @Override
//                            public void run() {
//
//                                switch_WATER.setBackgroundResource(R.drawable.water_on1);
//                                click_water++;
//                            }}, 7873);

                    }
                    else if (click_water % 2 == 0) {//目前是關的狀態  要開啟

                        if (mConnectedThread != null) //First check  make sure thread created
                            mConnectedThread.write("4");
                        switch_WATER.setBackgroundResource(R.drawable.water_on1);
                        click_water++;

//                        handler.postDelayed(new Runnable(){
//
//                            @Override
//                            public void run() {
//
//                                switch_WATER.setBackgroundResource(R.drawable.water1);
//                                click_water++;
//                            }}, 7873);
                    }

                }
            }
        });

        //加溫棒／墊
        switch_STICK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(auto_onoff.equals("0")) {
                    if (click_stick % 2 != 0) { //目前是關的狀態  要開啟
                        if (mConnectedThread != null) //First check to make sure thread created
                            mConnectedThread.write("6");
                        switch_STICK.setBackgroundResource(R.drawable.stick_on1);
                        click_stick ++;

                    }
                    else if (click_stick  % 2 == 0) {//目前是開的狀態  要關閉

                        if (mConnectedThread != null) //First check  make sure thread created
                            mConnectedThread.write("7");
                        switch_STICK.setBackgroundResource(R.drawable.stick2);
                        click_stick ++;
                    }



                }
            }
        });

        //開關布幕
        switch_SCREEN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if(auto_onoff.equals("0")) {
                        if (click_screen % 2 != 0) { //關閉
                            if (mConnectedThread != null) //First check to make sure thread created
                                mConnectedThread.write("9");
                            switch_SCREEN.setBackgroundResource(R.drawable.screen1);
                        } else if (click_screen % 2 == 0) {

                            if (mConnectedThread != null) //First check  make sure thread created
                                mConnectedThread.write("8");
                            switch_SCREEN.setBackgroundResource(R.drawable.screen_on1);
                        }
                        click_screen++;
                    }

            }
        });

    }
//    private void Judgment_hot(){
//
//        if(temp_value_double < temp_low_double){    //開燈
//            switch_HOT.setBackgroundResource(R.drawable.hot_on1);
//            click_hot++;
//        }
////        if(temp_value_double > temp_high_double){   //關燈
////            switch_HOT.setBackgroundResource(R.drawable.hot1);
////            click_hot++;
////        }
//
//    }

//    private void Judgment_water(){
//
//        if(hum_value_double < hum_low_double){     //開水
//            switch_WATER.setBackgroundResource(R.drawable.water_on1);
//            click_water++;
//        }
////        if(hum_value_double > hum_high_double){     //關水
////            switch_WATER.setBackgroundResource(R.drawable.water1);
////            click_water++;
////        }
//    }

    // ---------------------------------------------------------取溫度----------------------------------------------------------------------
    private void JSON_GetTempValue(){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,URL_temp,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //  System.out.println(response.toString());
                try {

                    temp_value = Double.valueOf(response.getDouble("field1")).toString();
                    TEMP.setText(temp_value);

                    temp_value_double = Double.parseDouble(temp_value);

                    temp_low_double = Double.parseDouble(temp_low);
                    temp_high_double = Double.parseDouble(temp_high);
                    hum_low_double = Double.parseDouble(hum_low);
                    hum_high_double = Double.parseDouble(hum_high);
//                    Judgment_hot();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        },new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                //  System.out.append(error.getMessage());
            }
        });
        rq.add(jsonObjectRequest);
    }

    // ---------------------------------------------------------取濕度----------------------------------------------------------------------
    private void JSON_GetHumValue(){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,URL_hum,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //  System.out.println(response.toString());
                try {

                    hum_value = Double.valueOf(response.getDouble("field2")).toString();
                    HUM.setText(hum_value);
                    hum_value_double = Double.parseDouble(hum_value);
                    //Judgment_water();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        },new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                //  System.out.append(error.getMessage());
            }
        });
        rq.add(jsonObjectRequest);
    }


    // ---------------------------------------------------------    取ID    ----------------------------------------------------------------------
    private void findID(){
        TEMP = (TextView)findViewById(R.id.textView_temp);
        HUM= (TextView)findViewById(R.id.textView_hum);
        switch_HOT = (ImageButton)findViewById(R.id.switch_hot);
        switch_WATER = (ImageButton)findViewById(R.id.switch_water);
        switch_STICK = (ImageButton)findViewById(R.id.switch_stick);
        switch_SCREEN = (ImageButton)findViewById(R.id.switch_screen);
        mScanBtn = (ImageButton) findViewById(R.id.scan);
        mDiscoverBtn = (ImageButton)findViewById(R.id.discover);
        mDevicesListView = (ListView)findViewById(R.id.devicesListView);

    }

    // ---------------------------------------------------------藍芽連接------------------------------------------------------------------------
    private void bluetoothOn(View view){
        if (!mBTAdapter.isEnabled()) {//如果藍芽沒開啟
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);//跳出視窗
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            //開啟設定藍芽畫面

            Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    //定義當按下跳出是否開啟藍芽視窗後要做的內容
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data){
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                Toast.makeText(Information.this,"Enabled",Toast.LENGTH_SHORT).show();

            }
            else
                Toast.makeText(Information.this,"Disabled",Toast.LENGTH_SHORT).show();

        }
    }

    private void bluetoothOff(View view){
        mBTAdapter.disable(); // turn off bluetooth

        Toast.makeText(getApplicationContext(),"Bluetooth turned Off",Toast.LENGTH_SHORT).show();
    }

    private void discover(View view){
        // Check if the device is already discovering
        if(mBTAdapter.isDiscovering()){ //如果已經找到裝置
            mBTAdapter.cancelDiscovery(); //取消尋找
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) { //如果沒找到裝置且已按下尋找
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery(); //開始尋找
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void listPairedDevices(View view){
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new
            AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

                    if(!mBTAdapter.isEnabled()) {
                        Toast.makeText(getBaseContext(), "Bluetooth not on",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(Information.this,"Connecting...",Toast.LENGTH_SHORT).show();

                    // Get the device MAC address, which is the last 17 chars in the View
                    String info = ((TextView) v).getText().toString();
                    final String address = info.substring(info.length() - 17);
                    final String name = info.substring(0,info.length() - 17);

                    // Spawn a new thread to avoid blocking the GUI one
                    new Thread()
                    {
                        public void run() {
                            boolean fail = false;
                            //取得裝置MAC找到連接的藍芽裝置
                            BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                            try {
                                mBTSocket = createBluetoothSocket(device);
                                //建立藍芽socket
                            } catch (IOException e) {
                                fail = true;
                                Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                            }
                            // Establish the Bluetooth socket connection.
                            try {
                                mBTSocket.connect(); //建立藍芽連線
                            } catch (IOException e) {
                                try {
                                    fail = true;
                                    mBTSocket.close(); //關閉socket
                                    //開啟執行緒 顯示訊息
                                    mHandler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                                } catch (IOException e2) {
                                    //insert code to deal with this
                                    Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                            if(fail == false) {
                                //開啟執行緒用於傳輸及接收資料
                                mConnectedThread = new ConnectedThread(mBTSocket);
                                mConnectedThread.start();
                                //開啟新執行緒顯示連接裝置名稱
                                mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name).sendToTarget();
                            }
                        }
                    }.start();
                }
            };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws
            IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        SystemClock.sleep(100);
                        //pause and wait for rest of data
                        bytes = mmInStream.available();
                        // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes);
                        // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

}
