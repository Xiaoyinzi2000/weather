package Fann.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class CityAdapter extends ArrayAdapter {
    private int resourceID;
    private TextView cityInfo;//城市的天气信息
    private Context context;

    public CityAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        this.resourceID = resource;
        this.context = context;
    }
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        City city=(City)getItem(position);
        View view;

        if(convertView==null)
            view = LayoutInflater.from(getContext()).inflate(resourceID,null);

        else
            view=convertView;

        cityInfo=view.findViewById(R.id.cityInfo);

        String Info=city.getProvince()+" "+city.getName()+" "+city.getPresentTem()+" "+city.getWeather();
        cityInfo.setText(Info);
        cityInfo.setTextSize(30);

        return view;



    }
}
