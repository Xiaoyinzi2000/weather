package Fann.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


//主界面显示关注的城市的天气信息 可关注多个城市 左右滑动可切换城市
//可以点击右上角的图标显示刷新或关注城市列表 可以进入关注城市界面，该界面能够显示所有关注的城市 长按某个城市即可选择是否删除该城市
//城市管理页面底部有一个按钮，点击可以进入搜索城市界面 搜索id即可在下方界面显示城市的天气信息，并显示关注按钮，点击关注后即可添加到关注列表

//主界面 最顶部醒目位置显示当前城市、当前温度
//设置常驻城市后 删除该城市 常驻城市信息也会被删除即sp内容√
//在点击城市列表某个城市后，将回到主界面，不调用OnRestart函数来刷新，只手动刷新√
//增加一个按钮以显示常驻城市天气√

/**
 *
 */
public class MainActivity extends AppCompatActivity {
    private TextView currentWeather;
    private ListView futureWeather;
    private List<FutureWeather> weatherList= new ArrayList<>();//当前城市未来天气的列表
    public static final int CAST_INFO=0;
    public static final int CURRENT_INFO=1;
    private FloatingActionButton Home;
    private Gson gson=new Gson();
    private Weather weather=new Weather();
    private CWeather livesWeather=new CWeather();
    private String currentId="";
    private TextView updateTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentWeather=findViewById(R.id.currentWeather);
        futureWeather=findViewById(R.id.futureWeather);
        updateTime=findViewById(R.id.updateTime);
        Home=findViewById(R.id.homeButton);
        Home.setOnClickListener(v->{
            showHomeWeather();
        });
        showHomeWeather();//一开始展示常驻城市天气
    }

    public void showHomeWeather() {
        SharedPreferences homePrefs=getSharedPreferences("Home",MODE_PRIVATE);
        String homeId=homePrefs.getString("Id","0");
        updateTime.setVisibility(View.VISIBLE);
        if(homeId.equals("0"))
        {
            currentWeather.setVisibility(View.GONE);
            futureWeather.setVisibility(View.GONE);
            updateTime.setVisibility(View.GONE);
            return;
        }
        //将当前所要查看id的修改为常驻城市Id
        currentId=homeId;
        getCurrentWeather(homeId);
        getFutureWeather(homeId);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @SuppressLint("SetTextI18n")
    void showCurrentWeather(Lives lives) {
        currentWeather.setVisibility(View.VISIBLE);
        /*reportTime.setVisibility(View.VISIBLE);
        humidity.setVisibility(View.VISIBLE);*/
        String cityName=lives.getCity()+"\n";
        String currentTemp= lives.getTemperature()+"℃\n";

        String weather=lives.getWeather()+"\n";
        String humidity="湿度 "+lives.getHumidity()+"%";


        String Info=cityName+currentTemp+weather+humidity;


        SpannableString ss = new SpannableString(Info);

        ss.setSpan(new TextAppearanceSpan(MainActivity.this, R.style.tv_style2), 0, cityName.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ss.setSpan(new TextAppearanceSpan(MainActivity.this, R.style.tv_style1), cityName.length(),
                cityName.length()+currentTemp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ss.setSpan(new TextAppearanceSpan(MainActivity.this, R.style.tv_style2), cityName.length()+currentTemp.length(),
                cityName.length()+currentTemp.length()+weather.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new TextAppearanceSpan(MainActivity.this, R.style.tv_style2), cityName.length()+currentTemp.length()+weather.length(),
                Info.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        currentWeather.setText(ss, TextView.BufferType.SPANNABLE);
        updateTime.setText("更新时间 "+lives.getReporttime());
        updateTime.setGravity(Gravity.CENTER);
       /* humidity.setText(lives.getHumidity());
        humidity.setTextSize(10);
        humidity.setGravity(Gravity.RIGHT);
        reportTime.setText(lives.getReporttime());
        humidity.setTextSize(10);
        humidity.setGravity(Gravity.LEFT);*/

    }

    void showFutureWeather(List<Casts> casts,String cityId,String city,String province) {
        futureWeather.setVisibility(View.VISIBLE);
        List<FutureWeather> weatherList= new ArrayList<>();
        for(int i=0;i<casts.size();i++) {
            Casts cast=casts.get(i);
            weatherList.add(new FutureWeather(cast.getDate(),cast.getDayweather(),cast.getDaytemp(),cast.getNighttemp()));
        }
        WeatherAdapter adapter=new WeatherAdapter(MainActivity.this,R.layout.weather_item,weatherList);


        futureWeather.setAdapter(adapter);

    }
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId()) {
            case R.id.city_item:
                Intent intent = new Intent(MainActivity.this, CityManager.class);
                startActivityForResult(intent,1);
                break;
            case R.id.refresh_item:
                refresh();
                break;
        }
        return true;
    }
    public void refresh()
    {
        getCurrentWeather(currentId);
        getFutureWeather(currentId);
    }

    //当城市管理页活动有点击活动返回时，显示所点击的城市天气信息
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    String id=data.getStringExtra("cityId");
                    currentId=id;
                    getCurrentWeather(id);
                    getFutureWeather(id);
                }
                break;
            default:break;

        }
    }


    private void getCurrentWeather(String cityId)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection;
                try{
                    URL url=new URL("https://restapi.amap.com/v3/weather/weatherInfo?key=7661143cb6047981f75d1d3756155c7a&output=JSON&city="+cityId);
                    connection=(HttpURLConnection)url.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    InputStream in=connection.getInputStream();
                    BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                    StringBuilder response=new StringBuilder();
                    String line;
                    while((line=reader.readLine())!=null)
                        response.append(line);

                    Message message=new Message();
                    message.what=CURRENT_INFO;
                    message.obj=response.toString();
                    handler.sendMessage(message);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void  getFutureWeather(String cityId)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection;
                try{
                    URL url=new URL("https://restapi.amap.com/v3/weather/weatherInfo?key=7661143cb6047981f75d1d3756155c7a&extensions=all&output=JSON&city="+cityId);
                    connection=(HttpURLConnection)url.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    InputStream in=connection.getInputStream();
                    BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                    StringBuilder response=new StringBuilder();
                    String line;
                    while((line=reader.readLine())!=null)
                        response.append(line);

                    Message message=new Message();
                    message.what=CAST_INFO;
                    message.obj=response.toString();
                    handler.sendMessage(message);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    @SuppressLint("HandlerLeak")
    private final Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch ((msg.what)){
                case CAST_INFO:
                    String response=(String)msg.obj;
                    //出现不存在的id或城市名，则什么也不干
                    if(response.endsWith("[[]}]}")||response.endsWith("[]}")) {
                        Toast.makeText(MainActivity.this,"不存在该城市",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    weather=gson.fromJson(response,Weather.class);


                        showFutureWeather(weather.getForecasts().get(0).getCasts(),weather.getForecasts().get(0).getAdcode(),weather.getForecasts().get(0).getCity(),weather.getForecasts().get(0).getProvince());
                    break;
                case CURRENT_INFO:
                    String response0=(String)msg.obj;
                    //出现不存在的id或城市名，则什么也不干
                    if(response0.endsWith("[[]]}")||response0.endsWith("[]}")){
                        Toast.makeText(MainActivity.this,"不存在该城市",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    livesWeather=gson.fromJson(response0,CWeather.class);

                    //如果搜索结果只有一个直接显示

                    showCurrentWeather(livesWeather.getLives().get(0));
                    break;
                default:break;

            }
        }
    };


}

