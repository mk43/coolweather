package com.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.coolweather.db.CoolWeatherDB;
import com.coolweather.model.City;
import com.coolweather.model.County;
import com.coolweather.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zzj on 2016/9/30.
 */

public class Utility {

  /**
   * 解析和处理服务器返回的省级数据
   */
  public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB, String response) {
    if (!TextUtils.isEmpty(response)) {
      String[] allProvinces = response.split(",");
      if (allProvinces != null && allProvinces.length > 0) {
        for (String p : allProvinces) {
          Pattern pattern = Pattern.compile("\"(.*?)\"");
          Matcher matcher = pattern.matcher(p);
          Province province = new Province();
          int times = 0;
          while (matcher.find()) {
            String provinceInfo = matcher.group().replace("\"", "");
            if (times++ % 2 == 0) {
              province.setProvinceCode(provinceInfo);
            } else {
              province.setProvinceName(provinceInfo);
            }
          }
          coolWeatherDB.saveProvince(province);     // 存储解析出来的Province数据
        }
        return true;
      }
    }
    return false;
  }

  /**
   * 解析和处理服务器返回的市级数据
   */
  public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response, int provinceId) {
    if (!TextUtils.isEmpty(response)) {
      String[] allCities = response.split(",");
      if (allCities != null && allCities.length > 0) {
        for (String c : allCities) {
          Pattern pattern = Pattern.compile("\"(.*?)\"");
          Matcher matcher = pattern.matcher(c);
          City city = new City();
          int times = 0;
          while (matcher.find()) {
            String cityInfo = matcher.group().replace("\"", "");
            if (times++ % 2 == 0) {
              city.setCityCode(cityInfo);
            } else {
              city.setCityName(cityInfo);
            }
          }
          city.setProvinceId(provinceId);
          coolWeatherDB.saveCity(city);     // 存储解析出来的City数据
        }
        return true;
      }
    }
    return false;
  }

  /**
   * 解析和处理服务器返回的县级数据
   */
  public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB, String response, int cityId) {
    if (!TextUtils.isEmpty(response)) {
      String[] allCounties = response.split(",");
      if (allCounties != null && allCounties.length > 0) {
        for (String c : allCounties) {
          Pattern pattern = Pattern.compile("\"(.*?)\"");
          Matcher matcher = pattern.matcher(c);
          County county = new County();
          int times = 0;
          while (matcher.find()) {
            String countyInfo = matcher.group().replace("\"","");
            if (times++ % 2 == 0) {
              county.setCountyCode(countyInfo);
            } else {
              county.setCountyName(countyInfo);
            }
          }
          county.setCityId(cityId);
          coolWeatherDB.saveCounty(county);     // 存储解析出来的County数据
        }
        return true;
      }
    }
    return false;
  }

  /**
   * 解析服务器返回的JSON数据，并将解析出的数据存储到本地
   */
  public static void handleWeatherResponse(Context context, String response) {
    if (!TextUtils.isEmpty(response)) {
      try {
        JSONObject jsonObject = new JSONObject(response);
        JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
        String cityName = weatherInfo.getString("city");
        String weatherCode = weatherInfo.getString("cityid");
        String temp1 = weatherInfo.getString("temp1");
        String temp2 = weatherInfo.getString("temp2");
        String weatherDesp = weatherInfo.getString("weather");
        String publishTime = weatherInfo.getString("ptime");
        saveWeatherInfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 将服务器返回的所有天气信息存储到SharedPreference文件中
   */
  public static void saveWeatherInfo(Context context, String cityName, String weatherCode,
                                     String temp1, String temp2, String weatherDesp, String publishTime) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日",Locale.CHINA);
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.putBoolean("city_selected", true);
    editor.putString("city_name", cityName);
    editor.putString("weather_code", weatherCode);
    editor.putString("temp1", temp1);
    editor.putString("temp2", temp2);
    editor.putString("weather_desp", weatherDesp);
    editor.putString("publish_time", publishTime);
    editor.putString("current_data", sdf.format(new Date()));
    editor.commit();
  }
}
