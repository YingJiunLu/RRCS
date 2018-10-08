package com.example.lu.rrcs;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Insert extends AppCompatActivity {

    private EditText petname;
    private ImageButton insert_enter;
    private Spinner spinner_petkind, spinner_temp_low, spinner_temp_high, spinner_hum_low,spinner_hum_high ;
    private String name,kind,temp_low,temp_high,hum_low,hum_high;
    private TextView hum_range;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        //bar
        assert getSupportActionBar()!=null;
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        //設定初始值
        kind = " 澤龜";
        temp_low = "30";
        temp_high = "33";
        hum_low= "24";
        hum_high = "26";

        //抓id
        FindId();


        //spinner 種類
        final String[] pet_kind = {"澤龜","守宮"};

        ArrayAdapter array_pet_kind = new ArrayAdapter<>(Insert.this, android.R.layout.simple_spinner_dropdown_item, pet_kind);
        spinner_petkind.setAdapter(array_pet_kind);

        spinner_petkind.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                kind = adapterView.getSelectedItem().toString();

                if(kind.equals("澤龜")){


                    hum_range.setText("理想水溫範圍 :");
                    final String[] string_temp_low = {"30","31","32","33","34"};
                    final String[] string_temp_high = {"33","34","35","36","37"};

                    final String[] string_hum_low = {"24","25","26","27","28"};
                    final String[] string_hum_high = {"26","27","28","29","30"};

                    ArrayAdapter array_temp_low = new ArrayAdapter<>(Insert.this, android.R.layout.simple_spinner_dropdown_item, string_temp_low);
                    spinner_temp_low.setAdapter(array_temp_low);

                    spinner_temp_low.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            //temp_low = string_temp_low[i];
                            temp_low = adapterView.getSelectedItem().toString();
                            //Toast.makeText(Add.this,temp_low,Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    ArrayAdapter<String> array_temp_high = new ArrayAdapter<>(Insert.this, android.R.layout.simple_spinner_dropdown_item, string_temp_high);
                    spinner_temp_high.setAdapter(array_temp_high);

                    spinner_temp_high.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            temp_high = string_temp_high[i];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    ArrayAdapter<String> array_hum_low = new ArrayAdapter<>(Insert.this, android.R.layout.simple_spinner_dropdown_item, string_hum_low);
                    spinner_hum_low.setAdapter(array_hum_low);

                    spinner_hum_low.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            hum_low = string_hum_low[i];

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    ArrayAdapter<String> array_hum_high = new ArrayAdapter<>(Insert.this, android.R.layout.simple_spinner_dropdown_item, string_hum_high);
                    spinner_hum_high.setAdapter(array_hum_high);

                    spinner_hum_high.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            hum_high = string_hum_high[i];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                }

                //如果是守宮
                else{
                    hum_range.setText("理想濕度範圍 :");

                    final String[] string_temp_low = {"23","24","25","26","27"};
                    final String[] string_temp_high = {"28","29","30","31","32"};

                    final String[] string_hum_low = {"30","40","50"};
                    final String[] string_hum_high = {"50","60","70"};

                    ArrayAdapter<String> array_temp_low = new ArrayAdapter<>(Insert.this, android.R.layout.simple_spinner_dropdown_item, string_temp_low);
                    spinner_temp_low.setAdapter(array_temp_low);

                    spinner_temp_low.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            temp_low = string_temp_low[i];

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    ArrayAdapter<String> array_temp_high = new ArrayAdapter<>(Insert.this, android.R.layout.simple_spinner_dropdown_item, string_temp_high);
                    spinner_temp_high.setAdapter(array_temp_high);

                    spinner_temp_high.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            temp_high = string_temp_high[i];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    ArrayAdapter<String> array_hum_low = new ArrayAdapter<>(Insert.this, android.R.layout.simple_spinner_dropdown_item, string_hum_low);
                    spinner_hum_low.setAdapter(array_hum_low);

                    spinner_hum_low.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            hum_low = string_hum_low[i];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    ArrayAdapter<String> array_hum_high = new ArrayAdapter<>(Insert.this, android.R.layout.simple_spinner_dropdown_item, string_hum_high);
                    spinner_hum_high.setAdapter(array_hum_high);

                    spinner_hum_high.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            hum_high = string_hum_high[i];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

            insert_enter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    name = petname.getText().toString();

                    if(petname.getText().toString().equals("")) {

                        Intent intent_pet_name = new Intent(Insert.this, Function.class);
                        startActivity(intent_pet_name);
                        Insert.this.finish();
                    }
                    else{
                        //將寵物名稱新增到資料庫並顯示出來Spinner
                        Database db = new Database(getApplicationContext());
                        db.insertName(name, kind, temp_low, temp_high, hum_low, hum_high);

                        Intent intent_pet_name = new Intent(Insert.this, Function.class);
                        startActivity(intent_pet_name);
                        Insert.this.finish();
                    }

                }
            });

    }

    public void FindId() {
        hum_range = (TextView)findViewById(R.id.hum_range);
        petname = (EditText)findViewById(R.id.editText_insert_pet_name);
        insert_enter = (ImageButton)findViewById(R.id.insert_enter);
        spinner_petkind = (Spinner)findViewById(R.id.spinner_insert_pet_kind);
        spinner_temp_low = (Spinner)findViewById(R.id.spinner_temp_low);
        spinner_temp_high = (Spinner)findViewById(R.id.spinner_temp_high);
        spinner_hum_low = (Spinner)findViewById(R.id.spinner_hum_low);
        spinner_hum_high = (Spinner)findViewById(R.id.spinner_hum_high);
    }

}
