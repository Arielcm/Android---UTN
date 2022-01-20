package com.cmeza.courseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    EditText citytxt;
    Button SaveBt, ClearBt;
    JSONObject saved = new JSONObject();
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    TextView tv;
    String url="api.openweathermap.org/data/2.5/weather?q={city name}&appid={API key}";
    String apikey="97757d0adf66f65d8137d2b1bcbc274c";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        citytxt = findViewById(R.id.city_text);
        SaveBt = findViewById(R.id.bt_save);
        ClearBt = findViewById(R.id.bt_clear);
        tv = findViewById(R.id.tv);

        init();
        Intent intent = getIntent();
        if(intent.getIntExtra("position", -1) != -1){
            try{
                String s = citytxt.getText().toString();
                    if(!preferences.getString("saved", "").equals("")){
                        saved=new JSONObject(preferences.getString("saved", ""));
                    }
                citytxt.setText(saved.getString("saved"+intent.getIntExtra("position",0)));
                s=saved.getString("saved"+intent.getIntExtra("position",0));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        ClearBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = citytxt.getText().toString();
                if(s.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Ingrese Ciudad!", Toast.LENGTH_SHORT).show();
                }else{
                    citytxt.setText("");
                    tv.setText("");
                }
            }
        });

        SaveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = citytxt.getText().toString();
                if(!s.equals("")){
                    try{
                        if(!preferences.getString("saved", "").equals(""))
                            saved = new JSONObject(preferences.getString("saved",""));
                        saved.put("saved"+saved.length(),s);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("testing", saved+"");
                    editor.putString("saved", saved.toString());
                    editor.apply();
                    citytxt.setText("");
                    Intent intent1 = new Intent(MainActivity.this,MainActivity2.class);
                    startActivity(intent1);
                }
            }
        });
    }

    public void getweather(View v){

        Retrofit retrofit=new Retrofit.Builder().baseUrl("https://api.openweathermap.org/data/2.5/").addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherapi myapi=retrofit.create(weatherapi.class);
        Call<Example> exampleCall = myapi.getweather(citytxt.getText().toString().trim(),apikey);
        exampleCall.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {
                try {
                    if (response.code() == 404) {
                        Toast.makeText(MainActivity.this, "Verificar Ciudad", Toast.LENGTH_LONG).show();
                    } else if (!(response.isSuccessful())) {
                        Toast.makeText(MainActivity.this, response.code(), Toast.LENGTH_LONG).show();
                    }
                    Example mydata = response.body();
                    Main main = mydata.getMain();
                    Double temp = main.getTemp();
                    Integer temperature = (int) (temp - 273.15);
                    tv.setText("La temperatura actual en "+citytxt.getText().toString()+" es de "+String.valueOf(temperature) + "°C");
                } catch (Resources.NotFoundException e) {
                    Toast.makeText(MainActivity.this, "Verificar Ciudad", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {

                Toast.makeText(MainActivity.this,t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }


    private void  init(){
        preferences = getSharedPreferences("text", Context.MODE_PRIVATE);
        editor = preferences.edit();
        citytxt = findViewById(R.id.city_text);
        SaveBt = findViewById(R.id.bt_save);
        ClearBt = findViewById(R.id.bt_clear);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.city_save){
            if(preferences.getString("saved","").equals("")){
                Toast.makeText(getApplicationContext(),"Ninguna Ciudad Guardada", Toast.LENGTH_SHORT).show();
            }else{
                Intent intent = new Intent(MainActivity.this,MainActivity2.class);
                startActivity(intent);
            }
        }

        return super.onOptionsItemSelected(item);
    }
}