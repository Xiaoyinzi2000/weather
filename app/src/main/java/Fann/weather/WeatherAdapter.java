package Fann.weather;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class WeatherAdapter extends ArrayAdapter {
    private int resourceID;
    private TextView weatherInfo;//天气信息
    private Context context;

    public WeatherAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        this.resourceID = resource;
        this.context = context;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        FutureWeather futureWeather=(FutureWeather)getItem(position);
        View view;

        if(convertView==null)
            view = LayoutInflater.from(getContext()).inflate(resourceID,null);
        else
            view=convertView;
        weatherInfo=view.findViewById(R.id.weatherInfo);
        String Info=futureWeather.getDate()+" "+futureWeather.getWeather()+" "+futureWeather.getMaxTem()+"/"+futureWeather.getMinTem();
        weatherInfo.setText(Info);
        weatherInfo.setTextSize(30);
        weatherInfo.setGravity(Gravity.CENTER);


        return view;



    }
}
