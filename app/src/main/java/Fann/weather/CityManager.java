package Fann.weather;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


//长按显示菜单可以删除√ 点击可回到主活动显示
public class CityManager extends AppCompatActivity {
    private final List<City> cityList= new ArrayList<>();//关注城市的列表
    private final CityDB dbHelper=new CityDB(this,"City.db",null,1);
    private ListView cityListView;
    private CityAdapter adapter;
    public static final int CITY_INFO=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_manager);

        FloatingActionButton addButton=findViewById(R.id.addButton);
        cityListView=findViewById(R.id.cities);
        registerForContextMenu(cityListView);
        setCities();

        addButton.setOnClickListener(v->{
            Intent intent=new Intent(CityManager.this,addCity.class);
            startActivity(intent);
        });
        cityListView.setOnItemClickListener((parent, view, position, id) -> {
            City city=(City) adapter.getItem(position);
            Intent intent=new Intent();
            intent.putExtra("cityId",city.getId());
            setResult(RESULT_OK,intent);
            finish();
        });

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.add(0, Menu.FIRST+1,1,"取消关注");
        menu.add(0,Menu.FIRST+2,2,"设为常驻城市");

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        AdapterView.AdapterContextMenuInfo info =(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        int position=info.position;

        City city=(City)adapter.getItem(position);

        if(item.getItemId()==2)
            unfollow(city.getId());
        else
            setHome(city.getId());


        return super.onContextItemSelected(item);
    }
    void unfollow(String id)
    {
        SQLiteDatabase db=dbHelper.getWritableDatabase();
       // Toast.makeText(CityManager.this,id,Toast.LENGTH_SHORT).show();
        db.delete("City","id=?",new String[]{id});
        SharedPreferences prefs=getSharedPreferences("Home",MODE_PRIVATE);

        if(prefs.getString("Id","0").equals(id))
        {
            SharedPreferences.Editor editor=getSharedPreferences("Home",MODE_PRIVATE).edit();
            if(editor.clear().commit())
                Toast.makeText(CityManager.this,"常驻城市已删除",Toast.LENGTH_SHORT).show();
        }
        setCities();

    }
    public void setHome(String id)
    {

        SharedPreferences.Editor editor=getSharedPreferences("Home",MODE_PRIVATE).edit();
        editor.putString("Id",id);
        if(editor.commit())
            Toast.makeText(CityManager.this,"更改常驻城市成功",Toast.LENGTH_SHORT).show();

    }

    void setCities()
    {

        if(!cityList.isEmpty())
            cityList.clear();
         adapter=new CityAdapter(CityManager.this,R.layout.city_item,cityList);

        SQLiteDatabase db=dbHelper.getReadableDatabase();
        Cursor cursor=db.query("City",null,null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{

                @SuppressLint("Range")
                String name=cursor.getString(cursor.getColumnIndex("name"));
                @SuppressLint("Range")
                String province=cursor.getString(cursor.getColumnIndex("province"));
                @SuppressLint("Range")
                String id=cursor.getString(cursor.getColumnIndex("id"));
                City city=new City();
                city.setName(name);
                city.setProvince(province);
                city.setId(id);
                cityList.add(city);

            }while(cursor.moveToNext());

        }
        cursor.close();
        cityListView.setAdapter(adapter);
       


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setCities();
    }
}