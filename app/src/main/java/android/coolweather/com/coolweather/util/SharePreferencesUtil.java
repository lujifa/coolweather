package android.coolweather.com.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.coolweather.com.coolweather.gson.Weather;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharePreferencesUtil {
    private static String TAG="SharePreferencesUtil";
    public static boolean saveSharePreferences(List<Weather> weatherList
            , String key, Context context){
        boolean result=false;
        SharedPreferences.Editor editor= PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        Gson gson=new Gson();
        String weather_json=gson.toJson(weatherList);
        editor.putString(key,weather_json);
        result=editor.commit();
        return result;
    }
    public static List<Weather> getSharePreferences(String key,Context context){
        List<Weather> weatherList=new ArrayList<>();
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(context);
        String weather_json=preferences.getString(key,null);
        if(weather_json!=null){
            Gson gson=new Gson();
            Type type=new TypeToken<List<Weather>>(){}.getType();
            weatherList=gson.fromJson(weather_json,type);
            Log.i(TAG, "---方法getSharePreferences>>weatherList:" + weatherList.size());
        }
        return weatherList;
    }
}