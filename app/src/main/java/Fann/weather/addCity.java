package Fann.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//问题： 只有市级才会显示未来的天气 某些省的县级不会显示 否则就会闪退 如何将json里的数据在转为object前就验证是否有未来天气 如何保证输入的是正确的id √
//在输入城市名时 可能会有多个城市 他只会显示第一个搜索结果 我想的是如果count>1的话则会先在listview显示各城市，点击后再显示各城市√
//将省、湿度、更新时间等信息补充 并在关注城市列表、搜索结果处显示省以区分√ 而湿度、更新时间信息则只在详细搜索结果显示
//点击关注后，会将关注的城市的城市名、省、id 写入关注的数据库 √
//点击关注后，关注按钮会显示为已关注 √
//如果是搜索后 城市已关注的话，会该城市的关注按钮将会是已关注，且不能点击 √
//搜索历史√

//最后的几个问题： 1：搜索历史显示在何时调用：a.展示天气结果的时候 b.再次点击搜索键时  c.再次进入搜索页面时（这样会导致当前显示的搜索记录被删除 点击会闪退  那就在退出时进行保存历史
//              2:下拉框选择城市后，区域名用于搜索仍然会和别的省的区域重名  如何在展示结果时  不再展示多个 而直接使用 省下拉表所选的那个省的城市

//click the cache textView the saved information will be showed above like what it looks like after searching
public class addCity extends AppCompatActivity {
    EditText city_input;
    public static final int CAST_INFO=0;
    public static final int CURRENT_INFO=1;
    private TextView result;
    private FloatingActionButton temp;
    private Gson gson=new Gson();
    private Weather weather=new Weather();
    private CWeather livesWeather=new CWeather();
    private ListView cities;
    private ListView resultCast;
    private FloatingActionButton followButton;
    private CityDB dbHelper;
    private TextView humidity;
    private CacheDB cdbHelper;
    private TextView reportTime;
    private TextView cache0;
    private TextView cache1;
    private TextView cache2;
    private Spinner pSpinner;
    private Spinner cSpinner;
    private Spinner aSpinner;
    private ArrayAdapter<String> pAdapter;
    private ArrayAdapter<String> cAdapter;
 //   private ArrayAdapter<String> aAdapter;
    private List<String> provinceList;
    private String provinceSelected="";
    private TextView noResult;
    private boolean cacheSet;

    //   省         市     区
    private Map<String,List<String>> p2c;//province : city
    private Map<String,List<String>>  c2a;//city : area
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
        try {
            getCity();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        city_input=findViewById(R.id.city_input);
        noResult=findViewById(R.id.noResult);
        temp=findViewById(R.id.temp);
        cities=findViewById(R.id.cityResult);
        result=findViewById(R.id.result);
        resultCast=findViewById(R.id.resultCast);
        followButton =findViewById(R.id.follow);

        reportTime=findViewById(R.id.reportTime);
        cache0=findViewById(R.id.cache0);
        cache1=findViewById(R.id.cache1);
        cache2=findViewById(R.id.cache2);
        pSpinner=findViewById(R.id.pSpinner);
        cSpinner=findViewById(R.id.cSpinner);
    //    aSpinner=findViewById(R.id.aSpinner);

        dbHelper=new CityDB(this,"City.db",null,1);
        cdbHelper=new CacheDB(this,"Cache.db",null,1);

        setCache();
        setSpinnerData();
        temp.setOnClickListener(v->{
            String cityInput=city_input.getText().toString();
            //Toast.makeText(addCity.this,cityInput.substring(cityInput.length()-2,cityInput.length()-1),Toast.LENGTH_SHORT).show();
            if(!cityInput.isEmpty()/*&& cityInput.endsWith("00")*/) {
                getCurrentWeather(cityInput);
                getWeatherInfo(cityInput);

                /*if(weather.getCount().equals("1")&&livesWeather.getCount().equals("1"))
                    saveCache(weather.getForecasts().get(0).getCasts(),livesWeather.getLives().get(0));//当查询结果只有一条时保存缓存 有多条时，则在展示多条时某条被点击时保存*/
            }

        });

    }


    //显示搜索记录的函数
    public void setCache()
    {
        SQLiteDatabase db=cdbHelper.getReadableDatabase();
        //搜索所有缓存
        Cursor cursor=db.query("Cache",null,null,null,null,null,null,null);
       // Toast.makeText(addCity.this,"......",Toast.LENGTH_SHORT).show();
        //如果没有缓存，则不进行任何操作
        if(cursor.getCount()==0)
        {
            Toast.makeText(addCity.this,"NO cache",Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }
        //否则将这三条缓存显示
        if(cursor.moveToFirst()){
            do{
                int pos=cursor.getPosition();//当前元组的下标（行数）
                @SuppressLint("Range")
                String name=cursor.getString(cursor.getColumnIndex("name"))+" ";
                @SuppressLint("Range")
                String province=" "+cursor.getString(cursor.getColumnIndex("province"))+",";
                if(pos==0)
                    cache0.setText(province+name);
                if(pos==1)
                    cache1.setText(province+name);
                if(pos==2)
                    cache2.setText(province+name);


            }while(cursor.moveToNext());
        }
        cursor.close();

        cache0.setOnClickListener(v->{
            String info=cache0.getText().toString();
            showCache(info);

        });
        cache1.setOnClickListener(v->{
            String info=cache1.getText().toString();
            showCache(info);

        });
        cache2.setOnClickListener(v->{
            String info=cache2.getText().toString();
            showCache(info);

        });
    }

    //显示Cache的函数
    @SuppressLint("Range")
    public  void showCache(String info)
    {
        //取得cache的省份和城市名
        String province="";
        String city="";
        //将二者count设为空，目的是调用显示函数时，不会去再次保存此缓存，而是在刷新的时候保存。
        weather.setCount("");
        livesWeather.setCount("");
        int i=0;
        for( ;i<info.length();i++) {
            if(info.charAt(i)!=' '&&info.charAt(i)!=',')
                province += info.charAt(i);
            if(info.charAt(i)==',')
                break;
        }
        for(;i<info.length();i++)
            if(info.charAt(i)!=' '&&info.charAt(i)!=',')
                city+=info.charAt(i);
    //    Toast.makeText(addCity.this,province+" "+city,Toast.LENGTH_SHORT).show();
        SQLiteDatabase db=cdbHelper.getReadableDatabase();

        @SuppressLint("Recycle")
        Cursor cursor=db.query("Cache",null,"province=? and name=?",new String[]{province,city},null,null,null);
        cursor.move(1);
        String id=cursor.getString(cursor.getColumnIndex("id"));
        String tem=cursor.getString(cursor.getColumnIndex("tem"));
        String livesWeather=cursor.getString(cursor.getColumnIndex("livesWeather"));
        String reportTime=cursor.getString(cursor.getColumnIndex("reportTime"));
        String humidity=cursor.getString(cursor.getColumnIndex("humidity"));
        String cast0=cursor.getString(cursor.getColumnIndex("cast0"));
        String cast1=cursor.getString(cursor.getColumnIndex("cast1"));
        String cast2=cursor.getString(cursor.getColumnIndex("cast2"));
        String cast3=cursor.getString(cursor.getColumnIndex("cast3"));
        Lives lives=new Lives();
        List<Casts> casts=new ArrayList<>();
        casts.add(toCasts(cast0));
        casts.add(toCasts(cast1));
        casts.add(toCasts(cast2));
        casts.add(toCasts(cast3));
        lives.setCity(city);
        lives.setProvince(province);
        lives.setAdcode(id);
        lives.setHumidity(humidity);
        lives.setReporttime(reportTime);
        lives.setWeather(livesWeather);
        lives.setTemperature(tem);


        showCurrentWeather(lives);
        showFutureWeather(casts,id,city,province);
    }
    public Casts toCasts(String casts)
    {
        String date="";
        String weather="";
        String dayTemp="";
        String nightTemp="";
        int i=0;
        for(;i<casts.length();i++)
        {
            char c=casts.charAt(i);
            if(c==' ')
                break;
            date+=c;
        }
        i++;//跳过空格
        for(;i<casts.length();i++)
        {
            char c=casts.charAt(i);
            if(c==' ')
                break;
            weather+=c;
        }
        i++;
        for(;i<casts.length();i++)
        {
            char c=casts.charAt(i);
            if(c=='/')
                break;
            dayTemp+=c;
        }
        i++;//跳过/
        for(;i<casts.length();i++)
        {
            char c=casts.charAt(i);
            if(c==' ')
                break;
            nightTemp+=c;
        }
        Casts cast=new Casts();
        cast.setDate(date);
        cast.setDayweather(weather);
        cast.setDaytemp(dayTemp);
        cast.setNighttemp(nightTemp);
        return cast;
    }

    //保存缓存的函数
    public void saveCache(List<Casts> casts,Lives lives)
    {
        SQLiteDatabase db=cdbHelper.getWritableDatabase();

        Cursor cursor=db.query("Cache",null,null,null,null,null,null,null);
        //Toast.makeText(addCity.this,String.valueOf(cursor.getCount()),Toast.LENGTH_SHORT).show();

        if(cursor.getCount()==3)//如果已经有三条缓存
        {
            cursor.move(1);//delete the first cache
            @SuppressLint("Range")
            String idToDel=cursor.getString(cursor.getColumnIndex("id"));

            db.delete("Cache","id=?",new String[]{idToDel});
        }
        cursor.close();
        //将当前搜索所得天气情况保存
        ContentValues values=new ContentValues();
        values.put("id",lives.getAdcode());
        values.put("name",lives.getCity());
        values.put("province",lives.getProvince());
        values.put("tem",lives.getTemperature());
        values.put("livesWeather",lives.getWeather());
        values.put("reportTime",lives.getReporttime());
        values.put("humidity",lives.getHumidity());

        String cast0=casts.get(0).getDate()+" "+casts.get(0).getDayweather()+" "+casts.get(0).getDaytemp()+"/"+casts.get(0).getNighttemp();
        String cast1=casts.get(1).getDate()+" "+casts.get(1).getDayweather()+" "+casts.get(1).getDaytemp()+"/"+casts.get(1).getNighttemp();
        String cast2=casts.get(2).getDate()+" "+casts.get(2).getDayweather()+" "+casts.get(2).getDaytemp()+"/"+casts.get(2).getNighttemp();
        String cast3=casts.get(3).getDate()+" "+casts.get(3).getDayweather()+" "+casts.get(3).getDaytemp()+"/"+casts.get(3).getNighttemp();
        values.put("cast0",cast0);
        values.put("cast1",cast1);
        values.put("cast2",cast2);
        values.put("cast3",cast3);

        db.insert("Cache",null,values);
    //    Toast.makeText(addCity.this,lives.getCity()+"saved",Toast.LENGTH_SHORT).show();
        values.clear();

    }

    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        public void handleMessage(Message msg){


            result=findViewById(R.id.result);
            city_input=findViewById(R.id.city_input);
            switch ((msg.what)){
                case CAST_INFO:
                    String response=(String)msg.obj;
                    //出现不存在的id或城市名，则什么也不干          [ ]
                    //    ]
                    //
                    //}
                   // Toast.makeText(addCity.this,response.substring(response.length()-5),Toast.LENGTH_LONG).show();
                    if(response.endsWith("[[]}]}")||response.endsWith("[]}]}")||response.endsWith("[]}")) {
                        noResult();
                        break;
                    }
                    weather=gson.fromJson(response,Weather.class);

                    //如果搜索结果只有一个城市才直接显示
                    if(weather.getCount().equals("1"))
                        showFutureWeather(weather.getForecasts().get(0).getCasts(),weather.getForecasts().get(0).getAdcode(),weather.getForecasts().get(0).getCity(),weather.getForecasts().get(0).getProvince());
                    break;
                case CURRENT_INFO:
                    String response0=(String)msg.obj;
                    //出现不存在的id或城市名，则什么也不干
                    if(response0.endsWith("[[]]}")||response0.endsWith("[]]}")||response0.endsWith("[]}")){
                        noResult();
                        break;
                    }
                    livesWeather=gson.fromJson(response0,CWeather.class);

                    //如果搜索结果只有一个直接显示
                    if(livesWeather.getCount().equals("1"))
                        showCurrentWeather(livesWeather.getLives().get(0));
                    else
                        showMultiCities(livesWeather);

                    break;
                default:break;

            }
        }
    };
    void noResult()
    {
        noResult.setVisibility(View.VISIBLE);
        noResult.setTextSize(40);
        noResult.setGravity(Gravity.CENTER);
        result.setVisibility(View.GONE);
        resultCast.setVisibility(View.GONE);
        followButton.setVisibility(View.GONE);
        reportTime.setVisibility(View.GONE);

    }
   /* public void show_data(String s)
    {

            Weather weather=gson.fromJson(s,Weather.class);
            String cityName=weather.getForecasts().get(0).getCity();
            Casts today=weather.getForecasts().get(0).getCasts().get(0);
            Casts next1=weather.getForecasts().get(0).getCasts().get(1);
            Casts next2=weather.getForecasts().get(0).getCasts().get(2);
            Casts next3=weather.getForecasts().get(0).getCasts().get(3);
          //  responseText.setText("明天"+tomorrow.toString());

            showCurrentWeather(today,cityName);
            showFutureWeather(next1,next2,next3);
    }*/

    //搜索结果有多个城市时，用listview显示多个城市
    void showMultiCities(CWeather livesWeather)
    {
        //使得查询具体结果不可见
        result.setVisibility(View.GONE);
        resultCast.setVisibility(View.GONE);
        followButton.setVisibility(View.GONE);
        reportTime.setVisibility(View.GONE);
        noResult.setVisibility(View.GONE);
     //   humidity.setVisibility(View.GONE);
        int cityNum=Integer.parseInt(livesWeather.getCount());
        List<City> cityList=new ArrayList<>();

            for (int i = 0; i < cityNum; i++) {
                Lives lives = livesWeather.getLives().get(i);
                City city = new City();
                city.setName(lives.getCity());
                city.setId(lives.getAdcode());
                city.setPresentTem(lives.getTemperature());
                city.setWeather(lives.getWeather());
                city.setProvince(lives.getProvince());
                cityList.add(city);
            }


   //    cities=findViewById(R.id.cityResult);
        //使城市列表可见
        CityAdapter adapter=new CityAdapter(addCity.this,R.layout.city_item,cityList);
        cities.setVisibility(View.VISIBLE);

        cities.setAdapter(adapter);

        cities.setOnItemClickListener((parent, view, position, id) -> {
            City city=(City) adapter.getItem(position);

            //遍历已经获取的实况天气信息 对比所点击的城市的adcode和实况天气的adcode 将匹配到的显示
            for(int i=0;i<cityNum;i++)
            {
                Lives lives=livesWeather.getLives().get(i);
                if(city.getId().equals(lives.getAdcode())) {
                    List<Casts> casts=weather.getForecasts().get(i).getCasts();
                    saveCache(casts,lives);//casts为未来几天的天气情况 lives为当前天气情况
                    showCurrentWeather(lives);
                    showFutureWeather(casts,weather.getForecasts().get(i).getAdcode(),weather.getForecasts().get(i).getCity(),weather.getForecasts().get(i).getProvince());

                    break;
                }
            }
        });


    }
    @SuppressLint("SetTextI18n")
    void showCurrentWeather(Lives lives)
    {


        //使得多个城市列表不可见
        cities.setVisibility(View.GONE);
        noResult.setVisibility(View.GONE);
      //  result=findViewById(R.id.result);
        //使得实况天气可见
        result.setVisibility(View.VISIBLE);
        reportTime.setVisibility(View.VISIBLE);
        //humidity.setVisibility(View.VISIBLE);
        String cityName=lives.getCity()+"\n";
        String currentTemp= lives.getTemperature()+"℃\n";

        String weather=lives.getWeather()+"\n";
        String humidity="湿度 "+lives.getHumidity()+"%";


        String Info=cityName+currentTemp+weather+humidity;


        SpannableString ss = new SpannableString(Info);

        ss.setSpan(new TextAppearanceSpan(addCity.this, R.style.tv_style2), 0, cityName.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ss.setSpan(new TextAppearanceSpan(addCity.this, R.style.tv_style1), cityName.length(),
                cityName.length()+currentTemp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ss.setSpan(new TextAppearanceSpan(addCity.this, R.style.tv_style2), cityName.length()+currentTemp.length(),
                cityName.length()+currentTemp.length()+weather.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new TextAppearanceSpan(addCity.this, R.style.tv_style2), cityName.length()+currentTemp.length()+weather.length(),
                Info.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        result.setText(ss, TextView.BufferType.SPANNABLE);

        reportTime.setText("更新时间 "+lives.getReporttime());
        reportTime.setGravity(Gravity.CENTER);
        if(livesWeather.getCount().equals("1"))
        {
            //when weather is not null, save the lives and casts into cache
            if(!this.weather.getInfocode().isEmpty()) {
                saveCache(this.weather.getForecasts().get(0).getCasts(), livesWeather.getLives().get(0));


            }
        }
        setCache();




    }
    void showFutureWeather(List<Casts> casts,String cityId,String city,String province)
    {
        cacheSet=false;
      //  resultCast=findViewById(R.id.resultCast);
        //使得未来天气可见
        resultCast.setVisibility(View.VISIBLE);
        noResult.setVisibility(View.GONE);
        List<FutureWeather> weatherList= new ArrayList<>();
        for(int i=0;i<casts.size();i++) {
            Casts cast=casts.get(i);
            weatherList.add(new FutureWeather(cast.getDate(),cast.getDayweather(),cast.getDaytemp(),cast.getNighttemp()));
        }


        WeatherAdapter adapter=new WeatherAdapter(addCity.this,R.layout.weather_item,weatherList);


        resultCast.setAdapter(adapter);
        SQLiteDatabase db=dbHelper.getWritableDatabase();



        //如果该城市已在关注列表，则将关注按钮修改为已关注状态，并不允许按动
        if(isFollowed(cityId))
        {
          //  Toast.makeText(addCity.this,"已关注",Toast.LENGTH_SHORT).show();
          //  followButton.setText("已关注");
            followButton.setImageResource(R.drawable.submit_icon);
            followButton.setEnabled(false);
        }
        //否则改为可关注状态
        else {
            //followButton.setText("关注");
            followButton.setImageResource(R.drawable.plus_icon);
            followButton.setEnabled(true);
        }
        followButton.setVisibility(ListView.VISIBLE);

        followButton.setOnClickListener(v->{
            followButton.setImageResource(R.drawable.submit_icon);
            followButton.setEnabled(false);

            ContentValues values=new ContentValues();

            values.put("id",cityId);
            values.put("name",city);
            values.put("province",province);
            db.insert("City",null,values);//将关注的城市信息写入关注城市的数据库
            values.clear();
        });
        if(weather.getCount().equals("1"))
        {
            if(!livesWeather.getCount().isEmpty()) {
                saveCache(weather.getForecasts().get(0).getCasts(), livesWeather.getLives().get(0));
               // setCache();

            }
        }

        setCache();


    }
    public boolean isFollowed(String id)
    {
        SQLiteDatabase db=dbHelper.getWritableDatabase();

        Cursor cursor=db.query("City",null,"id=?",new String[]{id},null,null,null,null);
        if(cursor.getCount()==0)
            return false;
        cursor.close();
        return true;
        //如果没有对应id查询结果，则返回false，即该id的城市没有被关注
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

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void  getWeatherInfo(String cityId)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection=null;
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

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void setSpinnerData()
    {
        //给省下拉表赋值
        pAdapter=new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,provinceList);
        pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//设置按下的效果
        pSpinner.setAdapter(pAdapter);
       // pSpinner.setSelection(0);//设置默认选中的省

        //给市下拉表赋值
        cAdapter=new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        cAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//设置按下的效果
        cSpinner.setAdapter(cAdapter);

        //给区下拉列表赋值
       /* aAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        aAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//设置按下的效果
        aSpinner.setAdapter(aAdapter);*/
        setListener();//设置各spinner的监听

    }
    public void setListener()
    {
        pSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String province=pAdapter.getItem(position);
                updateCityAndArea(province);//当某省被选中时，将后两个下拉框的内容更新为该省的市以及对应的区
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                city_input.setText(cAdapter.getItem(position));
               // updateArea(city);//当某市被选中时，将后一个下拉框的内容更新为它的所有区
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
       /* aSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                city_input.setText(aAdapter.getItem(position).replace(" ",""));

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/
    }

    public void updateCityAndArea(String province)
    {
        if(!cAdapter.isEmpty())
            cAdapter.clear();//先将已有内容清除，即上一次更新加入的城市
        cAdapter.addAll(p2c.get(province));//再将对应的省的所有城市加入
       // updateArea(p2c.get(province).get(0));//并根据该省所有市中位列第一个的城市的区域去更新后一个下拉框

    }
    /*public void updateArea(String city)
    {
        if(!aAdapter.isEmpty())
            aAdapter.clear();
        aAdapter.addAll(c2a.get(city));
    }*/
    public void getCity() throws FileNotFoundException {
        //   省              市       区县
        // Gson gson = new Gson();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open("CityJson.txt")));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
        //Map<String, List<Map<String, List<String>>>> allCity = gson.fromJson(String.valueOf(stringBuilder), new TypeToken<Map<String, List<Map<String, List<String>>>>>() {
        //}.getType());
        //System.out.println("---------"+stringBuilder.length()+"----------");
        //Province provinces=gson.fromJson(String.valueOf(stringBuilder),Province.class);
        //JSONObject jsonObject = null;
        provinceList=new ArrayList<>();

        //   省         市     区
       p2c=new HashMap<>();//province : city
        c2a=new HashMap<>();//city : area


        JSONArray jsonArray= null;//将转化为string的json初始化为一个Json数组
        try {
            jsonArray = new JSONArray(String.valueOf(stringBuilder));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        for(int i=0;i<jsonArray.length();i++) {
            // System.out.println(i+"-------------------");
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);//取得对象数组其中某个json对象即某个省
                String provinceName=jsonObject.getString("name");//获得省对象的名字
                provinceList.add(provinceName);//将省名加入省列表
                //System.out.println("Province:"+provinceName);

                JSONArray city=jsonObject.getJSONArray("city");//取得当前省份的所有城市的数组
                List<String> cityList=new ArrayList<>();
                for(int j=0;j<city.length();j++)
                {

                    JSONObject jsonObject1=city.getJSONObject(j);//获得当前省份的城市数组中的某个城市
                    String cityName=jsonObject1.getString("name");//获得当前城市的名字
                    cityList.add(cityName);//将市名加入市列表
                    //System.out.println("City:"+cityName);
                    //每个城市的所有地区是一个字符串 将该字符串中的地区识别名存入字符串数组
                    String[] area = jsonObject1.getString("area").replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "").split(",");
                    List<String> areaList=new ArrayList<>();
                    for(String sa:area) {
                        areaList.add(sa);//将所有地区名加入地区列表
                        //System.out.println("Area:"+sa);
                    }
                    c2a.put(cityName,areaList);
                }
                p2c.put(provinceName,cityList);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
       // System.out.println(p2c);
      //  System.out.println(c2a);

    }
}