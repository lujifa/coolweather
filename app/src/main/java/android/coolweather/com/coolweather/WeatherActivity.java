package android.coolweather.com.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.coolweather.com.coolweather.gson.Daily_Forecast;
import android.coolweather.com.coolweather.gson.Weather;
import android.coolweather.com.coolweather.service.AutoUpdateService;
import android.coolweather.com.coolweather.util.HttpUtil;
import android.coolweather.com.coolweather.util.SharePreferencesUtil;
import android.coolweather.com.coolweather.util.Utility;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends FragmentActivity {
    private static String Tag = "WeatherActivity";
    private static ViewPager viewPager;
    public static PagerAdapter adapter;
    private List<Weather> list = new ArrayList<>();
    private int pageNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        viewPager = (ViewPager) findViewById(R.id.page);
        Log.i(Tag, "---WeatherActivity>>weather_list" + Weather_Fragment.weather_list.size());
        list = SharePreferencesUtil.getSharePreferences("weather_list", WeatherActivity.this);
        Log.i(Tag, "---WeatherActivity>>setCurrentItem" + (list.size() - 1));
        pageNum = list.size();
        adapter = new PagerAdapter(getSupportFragmentManager(), pageNum);
        viewPager.setAdapter(adapter);
        if (getIntent().getBooleanExtra("flag", false)) {

            viewPager.setCurrentItem(list.size() - 1);
        }
    }
    /**
     * 再请求
     *
     * @param weatherId
     */
    public void requestWeatherTwo(final String weatherId) {

        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId +
                "&key=c3924c2177ac47aab8b9624ea0f2aa09";
        Log.i(Tag, "---weatherId>>" + weatherId);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Log.i(Tag, "---two>>responseText:" + responseText);
                final Weather weather = Utility.handleWeatherResponse(responseText);
                list = SharePreferencesUtil.getSharePreferences("weather_list", WeatherActivity.this);
                Log.i(Tag, "---two>>list:" + list.size());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            Weather_Fragment.addWeather(list, weather);
                            boolean result = SharePreferencesUtil.saveSharePreferences(list,
                                    "weather_list", WeatherActivity.this);
                            Log.i(Tag, "---two>>list_add:" + list.size());
                            Log.i(Tag, "--->>result" + result);
                            Intent intent = new Intent(WeatherActivity.this, WeatherActivity.class);
                            intent.putExtra("flag", true);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });

    }

    public static class PagerAdapter extends FragmentStatePagerAdapter {
        private int pageCount;

        public PagerAdapter(FragmentManager fm, int pageCount) {
            super(fm);
            this.pageCount = pageCount;
        }

        @Override
        public Fragment getItem(int position) {
            Log.i(Tag, "---getItem>>position：" + position);
            return Weather_Fragment.newInstance(position);
        }


        @Override
        public int getCount() {
            return pageCount == 0 ? 1 : pageCount;
        }
    }

    public static class Weather_Fragment extends Fragment {
        private static boolean FLAG = true;
        private ScrollView weatherLayout;
        private TextView titleCity;
        private TextView titleUpdateTime;
        private TextView degreeText;
        private TextView weatherInfoText;
        private TextView aqiText;
        private TextView pm25Text;
        private TextView comfortText;
        private TextView carWashText;
        private TextView sportText;
        private LinearLayout forecastLayout;
        private ImageView bingPicIma;
        public SwipeRefreshLayout swipeRefreshLayout;
        public DrawerLayout drawerLayout;

        private Button navButton;
        public static List<Weather> weather_list = new ArrayList<>();

        public static Weather_Fragment newInstance(int position) {
            Bundle args = new Bundle();
            args.putInt("position", position);
            Weather_Fragment weatherFragment = new Weather_Fragment();
            weatherFragment.setArguments(args);
            return weatherFragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.weather_fragment, null);
            weatherLayout = (ScrollView) view.findViewById(R.id.weather_layout);
            titleCity = (TextView) view.findViewById(R.id.title_city);
            titleUpdateTime = (TextView) view.findViewById(R.id.title_update_time);
            degreeText = (TextView) view.findViewById(R.id.degree_text);
            weatherInfoText = (TextView) view.findViewById(R.id.weather_info_text);
            aqiText = (TextView) view.findViewById(R.id.aqi_text);
            pm25Text = (TextView) view.findViewById(R.id.pm25_text);
            comfortText = (TextView) view.findViewById(R.id.comfort_text);
            carWashText = (TextView) view.findViewById(R.id.car_wash_text);
            sportText = (TextView) view.findViewById(R.id.sport_text);
            forecastLayout = (LinearLayout) view.findViewById(R.id.forecast_layout);
            bingPicIma = (ImageView) view.findViewById(R.id.bing_pic_img);
            swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
            drawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
            navButton = (Button) view.findViewById(R.id.nav_button);

            navButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
            swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String bingPic = preferences.getString("bing_pic", null);
            if (bingPic != null) {
                Glide.with(this).load(bingPic).into(bingPicIma);
            } else {
                loadBingPic();
            }
            weather_list = SharePreferencesUtil.getSharePreferences("weather_list", getContext());
            Log.i(Tag, "---getSharePreferences>>weather_list:" + weather_list.size());
            final String weatherId;
            Log.i(Tag, "---position>>getArguments:" + getArguments().getInt("position"));
            if (weather_list.size() > 0) {
                Log.i(Tag, "---size>>weather_list" + Weather_Fragment.weather_list.size());

                weatherId = weather_list.get(getArguments().getInt("position")).basic.weatherId;
                showWeatherInfo(weather_list.get(getArguments().getInt("position")));
            } else {
                weatherId = getActivity().getIntent().getStringExtra("weather_id");
                weatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(weatherId);
            }
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    requestWeather(weatherId);
                    if (FLAG) {
                        Toast.makeText(getContext(), "更新成功", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return view;
        }

        /**
         * 向共享参数保存天气信息
         *
         * @param weather_list
         * @param weather
         * @return
         */
        public static void addWeather(List<Weather> weather_list, Weather weather) {

            for (int i = 0; i < weather_list.size(); i++) {
                if (weather_list.get(i).basic.cityName.equals(weather.basic.cityName)) {


                    return ;
                }
            }
            weather_list.add(weather);

            return ;
        }

        /**
         * 处理并展示weather实体类的数据
         *
         * @param weather
         */
        private void showWeatherInfo(Weather weather) {
            if (weather != null && "ok".equals(weather.status)) {
                String cityName = weather.basic.cityName;
                String updateTime = weather.basic.update.updateTime.split(" ")[1];
                String degree = weather.now.temperature + "℃";
                String weatherInfo = weather.now.more.info;
                titleCity.setText(cityName);
                titleUpdateTime.setText(updateTime);
                degreeText.setText(degree);
                weatherInfoText.setText(weatherInfo);
                forecastLayout.removeAllViews();
                for (Daily_Forecast forecast : weather.forecastList) {
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.forecast_item, forecastLayout, false);
                    TextView dateText = view.findViewById(R.id.date_text);
                    TextView infoText = view.findViewById(R.id.info_text);
                    TextView maxText = view.findViewById(R.id.max_text);
                    TextView minText = view.findViewById(R.id.min_text);
                    dateText.setText(forecast.date);
                    infoText.setText(forecast.more.info);
                    maxText.setText(forecast.temperature.max);
                    minText.setText(forecast.temperature.min);
                    forecastLayout.addView(view);
                }
                if (weather.aqi != null) {
                    aqiText.setText(weather.aqi.city.aqi);
                    pm25Text.setText(weather.aqi.city.pm25);
                }
                String comfort = "舒适度：" + weather.suggestion.comfort.info;
                String carwash = "洗车指数：" + weather.suggestion.carwash.info;
                String sport = "运动建议：" + weather.suggestion.sport.info;
                comfortText.setText(comfort);
                carWashText.setText(carwash);
                sportText.setText(sport);
                weatherLayout.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getContext(), AutoUpdateService.class);
                getActivity().startService(intent);
            } else {
                Toast.makeText(getContext(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * 根据id请求城市天气信息
         * 第一次请求
         * c3924c2177ac47aab8b9624ea0f2aa09
         * bc0418b57b2d4918819d3974ac1285d9
         *
         * @param weatherId
         */
        public void requestWeather(final String weatherId) {
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId +
                    "&key=c3924c2177ac47aab8b9624ea0f2aa09";
            Log.i(Tag, "---weatherId>>" + weatherId);
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "获取失败", Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseText = response.body().string();
                    Log.i(Tag, "--->>responseText:" + responseText);
                    final Weather weather = Utility.handleWeatherResponse(responseText);
                    weather_list = SharePreferencesUtil.getSharePreferences("weather_list", getContext());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (weather != null && "ok".equals(weather.status)) {
                                addWeather(weather_list, weather);
                                boolean result = SharePreferencesUtil.saveSharePreferences(weather_list,
                                        "weather_list", getContext());
                                Log.i(Tag, "--->>result" + result);
                                showWeatherInfo(weather);
                                FLAG = true;
                            } else {
                                FLAG = false;
                                Toast.makeText(getContext(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
                            }
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            });
            loadBingPic();
        }
        /**
         * 获取每日一图
         */
        private void loadBingPic() {
            String requestBingPic = "http://guolin.tech/api/bing_pic";
            HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String bingPic = response.body().string();
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                    editor.putString("bing_pic", bingPic);
                    editor.apply();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(getContext()).load(bingPic).into(bingPicIma);
                        }
                    });
                }
            });
        }

    }

}
