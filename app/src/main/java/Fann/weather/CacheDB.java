package Fann.weather;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class CacheDB extends SQLiteOpenHelper {

    public static final String CREATE_CACHE="create table Cache("+"id text primary key,name text,province text,tem text,livesWeather text,reportTime text,humidity text,cast0 text,cast1 text,cast2 text,cast3 text)";
    private Context mContext;
    public CacheDB(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CACHE);
        Toast.makeText(mContext,"缓存创建成功",Toast.LENGTH_LONG).show();
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
