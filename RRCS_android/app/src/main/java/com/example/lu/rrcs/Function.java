package com.example.lu.rrcs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.widget.Toast.LENGTH_LONG;

public class Function extends AppCompatActivity{
    //跑馬燈
    private Gallery mGallery;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Handler mHandler = new Handler();
    private int[] Pics = {R.drawable.turtle,R.drawable.reptile} ;


    //從資料庫取的的溫溼度範圍
    String temp_low;
    String temp_high;
    String hum_low;
    String hum_high;

    //上傳溫溼度範圍
    Http_Get HG;

    Spinner spinner_pet_name;
    Switch switch_auto_onoff;
    ImageButton button_infor,button_insert,button_video;

    String pet_name;
    String auto_onoff = "0"; //       0為手動        1為自動

    //放寵物名稱的list
    List<String> list_pet_name;
    //換頁
    Intent intent_add,intent_infor,intent_video;
    Bundle bundle_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function);

        //抓id
        FindId();

        //取得spinner名稱的資料
        loadSpinnerData();

        //跑馬燈
        mGallery = ((Gallery) findViewById(R.id.gallery));
        //Pics為要顯示圖片的Resources陣列
        mGallery.setAdapter(new ImageAdapter(this , Pics));
        //圖片不透明
        mGallery.setUnselectedAlpha( 255 );
        //圖片不漸層，漸層長度為0
        mGallery.setFadingEdgeLength( 0 );
        //圖片不重疊，圖片間距為0
        mGallery.setSpacing(0);
        //圖片一開始顯示在第幾張設定在Integer.MAX_VALUE/2的位置(Integer.MAX_VALUE為int的最高值)
        mGallery.setSelection(Integer.MAX_VALUE/2);
        //圖片在切換圖片的速度
        mGallery.setAnimationDuration(2000);


        //自動開/關設備
        switch_auto_onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked)  //自動
                    //  Toast.makeText(Function.this,"已開啟自動開/關設備功能",Toast.LENGTH_SHORT).show();
                    auto_onoff = "1";

                else          //手動
                    // Toast.makeText(Function.this,"已關閉自動開/關設備功能",Toast.LENGTH_SHORT).show();

                    auto_onoff = "0";
            }
        });

        HG = new Http_Get();


        // 判斷選擇了哪一個寵物名稱
        spinner_pet_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override

            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pet_name = adapterView.getSelectedItem().toString();
                //Toast.makeText(Function.this,pet_name,Toast.LENGTH_SHORT).show();

                //---------------------------連接DB
                Database db = new Database(getApplicationContext());

                //----------------透過DB取得temp_low
                temp_low = db.getTempLow(pet_name);
                // Toast.makeText(Information.this,temp_low,Toast.LENGTH_SHORT).show();

                //----------------透過DB取得temp_high
                temp_high = db.getTempHigh(pet_name);
               // Toast.makeText(Function.this,temp_high,Toast.LENGTH_SHORT).show();


                //----------------透過DB取得hum_low
                hum_low = db.getHumLow(pet_name);

                //----------------透過DB取得hum_high
                hum_high = db.getHumHigh(pet_name);

                //上傳溫溼度範圍的網址
                String range_Url ="https://api.thingspeak.com/update?api_key=L2CP4H6H0777GZNC&field1="+temp_low+"&field2="+temp_high+"&field3="+hum_low+"&field4="+hum_high;
                HG.Get(range_Url);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //查看溫溼度
        button_insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intent_add = new Intent(Function.this,Information.class);
                bundle_name = new Bundle();
                bundle_name.putString("pet_name",pet_name);
                bundle_name.putString("auto_onoff",auto_onoff);
                intent_add.putExtras(bundle_name);
                startActivity(intent_add);
            }
        });

        //新增寵物
        button_infor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent_infor = new Intent(Function.this,Insert.class);
                startActivity(intent_infor);
            }
        });

        //查看即時影像
        button_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intent_video = new Intent(Function.this,Video.class);
                startActivity(intent_video);
            }
        });




    }

    public void FindId(){
        spinner_pet_name = (Spinner)findViewById(R.id.spinner_pet_name);
        switch_auto_onoff = (Switch)findViewById(R.id.switch_auto_onoff);
        button_insert = (ImageButton)findViewById(R.id.imageButton_insert);
        button_infor = (ImageButton)findViewById(R.id.imageButton_infor);
        button_video = (ImageButton)findViewById(R.id.imageButton_video);
    }



    //抓資料庫的值顯示在Spinner
    private void loadSpinnerData() {

        Database db = new Database(getApplicationContext());
        //抓資料庫NAME欄位裡的值放入List 內
        list_pet_name = db.getAllLabels();

        //將List放入Spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list_pet_name);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_pet_name.setAdapter(dataAdapter);
    }


    //------------------------------------跑馬燈----------------------------------------------
    @Override
//程式暫停時將自動輪播功能的時間計時清除
    protected void onPause()
    {
        super.onPause();
        //------------------------------------跑馬燈----------------------------------------------
        if(mTimer!=null)
        {
            mTimer.cancel();
            mTimer=null;
        }

        if(mTimerTask!=null)
        {
            mTimerTask.cancel();
            mTimerTask=null;
        }


    }

    @Override
//程式回復時建立自動輪播的時間計時
    protected void onResume()
    {
        super.onResume();
        //-----------------------------------------跑馬燈-----------------------------------------
        //因下方會重新new Timer，避免重複佔據系統不必要的資源，在此確認mTimer是否為null
        if(mTimer!=null)
        {
            mTimer.cancel();
            mTimer=null;
        }

        //因下方會重新new TimerTask，避免重複佔據系統不必要的資源，在此確認mTimerTask是否為null
        if(mTimerTask!=null)
        {
            mTimerTask.cancel();
            mTimerTask=null;
        }

        //建立Timer
        mTimer = new Timer();
        //建立TimerTask
        mTimerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                mHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //每3秒觸發時要做的事
                        //scroll 3
                        mGallery.onScroll(null, null, 3, 0);
                        //向右滾動
                        mGallery.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT , null);
                    }
                });
            }
        };

        //從3秒開始，每3秒觸發一次，每三秒自動滾動
        mTimer.schedule(mTimerTask, 3000, 3000);



    }


    //建立BaseAdapter並將此Adapter置入Gallery內
    public class ImageAdapter extends BaseAdapter
    {
        private Context mContext ;
        private int[] mPics ;

        public ImageAdapter(Context c , int[] pics)
        {
            this.mContext = c;
            mPics = pics ;
        }

        @Override
        //Gallery圖片總數為int的最大值，目的為無限迴圈
        public int getCount()
        {
            return Integer.MAX_VALUE;
        }

        @Override
        //目前圖片位置除以圖片總數量的餘數
        public Object getItem(int position)
        {
            return position % mPics.length ;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            //建立圖片
            ImageView img = new ImageView(this.mContext);
            //將圖片置入img，置入的圖片為目前位置的圖片除以圖片總數取餘數，此餘數為圖片陣列的圖片位置
            img.setImageResource(mPics[position % mPics.length]);
            //保持圖片長寬比例
            img.setAdjustViewBounds(true);
            //縮放為置中
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            //設置圖片長寬
            img.setLayoutParams(new Gallery.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            //回傳此建立的圖片
            return img;
        }
    }
}
