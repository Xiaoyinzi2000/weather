package Fann.weather;

public class City {
    private String name="";
    private String id="";
    private String weather="";
    private String presentTem="";
    private String maxTem="";
    private String minTem="";
    private FutureWeather futureWeather;
    private String province="";

    public City(String name, String id, String weather, String presentTem, String maxTem, String minTem) {
        this.name = name;
        this.id = id;
        this.weather = weather;
        this.presentTem = presentTem;
        this.maxTem = maxTem;
        this.minTem = minTem;
    }

    public City() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public void setPresentTem(String presentTem) {
        this.presentTem = presentTem;
    }

    public void setMaxTem(String maxTem) {
        this.maxTem = maxTem;
    }

    public void setMinTem(String minTem) {
        this.minTem = minTem;
    }

    public void setFutureWeather(FutureWeather futureWeather) {
        this.futureWeather = futureWeather;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getWeather() {
        return weather;
    }

    public String getPresentTem() {
        return presentTem;
    }

    public String getMaxTem() {
        return maxTem;
    }

    public String getMinTem() {
        return minTem;
    }

    public FutureWeather getFutureWeather() {
        return futureWeather;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
}
