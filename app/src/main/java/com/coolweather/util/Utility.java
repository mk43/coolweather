package com.coolweather.util;

import android.text.TextUtils;

import com.coolweather.db.CoolWeatherDB;
import com.coolweather.model.City;
import com.coolweather.model.County;
import com.coolweather.model.Province;

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
          String[] array = p.split("\\|");
          Province province = new Province();
          province.setProvinceName(array[0]);
          province.setProvinceCode(array[1]);
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
          String[] array = c.split("\\|");
          City city = new City();
          city.setCityCode(array[0]);
          city.setCityName(array[1]);
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
          String[] array = c.split("\\|");
          County county = new County();
          county.setCountyCode(array[0]);
          county.setCountyName(array[1]);
          county.setId(cityId);
          coolWeatherDB.saveCounty(county);     // 存储解析出来的County数据
        }
        return true;
      }
    }
    return false;
  }
}
