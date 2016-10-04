package com.coolweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.R;
import com.coolweather.model.City;
import com.coolweather.model.Province;
import com.coolweather.util.HttpCallbackListener;
import com.coolweather.util.HttpUtil;
import com.coolweather.util.Utility;

/**
 * Created by zzj on 2016/10/2.
 */

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener {

  private LinearLayout weatherInfoLayout;
  private TextView cityNameText;        // 显示城市名
  private TextView publishText;         // 显示发布时间
  private TextView weatherDespText;     // 显示天气信息
  private TextView temp1Text;           // 显示气温1
  private TextView temp2Text;           // 显示气温2
  private TextView currentDateText;     // 显示当前日期
  private Button switchCity;            // 切换城市按钮
  private Button refreshWeather;        // 更新天气按钮

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.weather_layout);
    // 初始化控件
    weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
    cityNameText = (TextView) findViewById(R.id.city_name);
    publishText = (TextView) findViewById(R.id.publish_text);
    weatherDespText = (TextView) findViewById(R.id.weather_desp);
    temp1Text = (TextView) findViewById(R.id.temp1);
    temp2Text = (TextView) findViewById(R.id.temp2);
    currentDateText = (TextView) findViewById(R.id.current_date);
    switchCity = (Button) findViewById(R.id.switch_city);
    refreshWeather = (Button) findViewById(R.id.refersh_weather);
    String countyCode = getIntent().getStringExtra("county_code");
    if (!TextUtils.isEmpty(countyCode)) {
      // 有县级代号就去查询
      Intent intent = getIntent();
      String selectedProvinceCode = ((Province) intent.getSerializableExtra("selectedProvince")).getProvinceCode();
      String selectedCityCode = ((City)intent.getSerializableExtra("selectedCity")).getCityCode();
      publishText.setText("同步中...");
      weatherInfoLayout.setVisibility(View.INVISIBLE);
      cityNameText.setVisibility(View.INVISIBLE);
      queryWeatherCode(selectedProvinceCode, selectedCityCode, countyCode);

    } else {
      // 没有县级代号就直接显示本地天气
      showWeather();
    }
    switchCity.setOnClickListener(this);
    refreshWeather.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.switch_city:
        Intent intent = new Intent(this,ChooseAreaActivity.class);
        intent.putExtra("from_weather_activity",true);
        startActivity(intent);
        finish();
        break;
      case R.id.refersh_weather:
        publishText.setText("同步中...");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode = prefs.getString("weather_code","");
        if (!TextUtils.isEmpty(weatherCode)) {
          queryWeatherInfo(weatherCode);
        }
        break;
      default:
        break;
    }
  }

  /**
   * 查询县级代号所对应的天气代号
   */
  private void queryWeatherCode(String selectedProvinceCode, String selectedCityCode, String countyCode) {
    if ("00".equals(selectedCityCode)) {
      queryWeatherInfo(selectedProvinceCode + countyCode + selectedCityCode);
    } else {
      queryWeatherInfo(selectedProvinceCode + selectedCityCode + countyCode);
    }
  }

  /**
   * 查询天气代号所对应的天气
   */
  private void queryWeatherInfo(String weatherCode) {
    String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
    queryFromServer(address);
  }

  /**
   * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
   */
  private void queryFromServer(final String address) {
    HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
      @Override
      public void onFinish(String response) {
        Utility.handleWeatherResponse(WeatherActivity.this, response);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            showWeather();
          }
        });
      }

      @Override
      public void onError(Exception e) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            publishText.setText("同步失败");
          }
        });
      }
    });
  }

  /**
   * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上
   */
  public void showWeather() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    cityNameText.setText(prefs.getString("city_name",""));
    temp1Text.setText(prefs.getString("temp1", ""));
    temp2Text.setText(prefs.getString("temp2", ""));
    weatherDespText.setText(prefs.getString("weather_desp", ""));
    publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
    currentDateText.setText(prefs.getString("current_data", ""));
    weatherInfoLayout.setVisibility(View.VISIBLE);
    cityNameText.setVisibility(View.VISIBLE);
  }
}
