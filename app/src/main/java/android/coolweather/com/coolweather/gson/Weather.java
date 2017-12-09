package android.coolweather.com.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Weather implements Serializable{
    public String status;
    public Basic basic;
    public Aqi aqi;
    public Now now;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")
    public List<Daily_Forecast> forecastList;
}