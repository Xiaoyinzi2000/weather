package Fann.weather;

public class FutureWeather {
    private String date;
    private String dayweather;
    private String maxTem;
    private String minTem;

    public FutureWeather(String date, String weather, String maxTem, String minTem) {
        this.date = date;
        this.dayweather = weather;
        this.maxTem = maxTem;
        this.minTem = minTem;
    }

    public String getDate() {
        return date;
    }

    public String getWeather() {
        return dayweather;
    }

    public String getMaxTem() {
        return maxTem;
    }

    public String getMinTem() {
        return minTem;
    }
}
