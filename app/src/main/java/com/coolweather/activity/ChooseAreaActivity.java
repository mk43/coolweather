package com.coolweather.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.R;
import com.coolweather.db.CoolWeatherDB;
import com.coolweather.model.City;
import com.coolweather.model.County;
import com.coolweather.model.Province;
import com.coolweather.util.HttpCallbackListener;
import com.coolweather.util.HttpUtil;
import com.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends AppCompatActivity {
  public static final int LEVEL_PROVINCE = 0;
  public static final int LEVEL_CITY = 1;
  public static final int LEVEL_COUNTY = 2;

  private ProgressDialog progressDialog;
  private TextView titleText;
  private ListView listView;
  private ArrayAdapter<String> adapter;
  private CoolWeatherDB coolWeatherDB;
  private List<String> dataList = new ArrayList<>();

  private List<Province> provinceList;        // 省列表
  private List<City> cityList;                // 市列表
  private List<County> countyList;            // 县列表
  private Province selectedProvince;          // 选中的省
  private City selectedCity;                  // 选中的市
  private int currentLevel;
  private boolean isFromWeatherActivity;      // 是否从WeatherActivity中跳转过来

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity",false);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    // 已经选择了城市且不是从WeatherActivity跳转过来，才会跳转到WeatherActivity
    if (prefs.getBoolean("city_selected",false) && !isFromWeatherActivity) {
      Intent intent = new Intent(this,WeatherActivity.class);
      intent.putExtra("selectedProvince", selectedProvince);
      intent.putExtra("selectedCity", selectedCity);
      startActivity(intent);
      finish();
      return;
    }
    setContentView(R.layout.choose_area);
    titleText = (TextView) findViewById(R.id.title_text);
    listView = (ListView) findViewById(R.id.list_view);
    adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
    listView.setAdapter(adapter);
    coolWeatherDB = CoolWeatherDB.getInstance(this);
    queryProvinces();
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> arg0,View view,int index,long arg3) {
        switch (currentLevel) {
          case LEVEL_PROVINCE:
            selectedProvince = provinceList.get(index);
            queryCities();
            break;
          case LEVEL_CITY:
            selectedCity = cityList.get(index);
            queryCounties();
            break;
          case LEVEL_COUNTY:
            String countyCode = countyList.get(index).getCountyCode();
            Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
            intent.putExtra("selectedProvince", selectedProvince);
            intent.putExtra("selectedCity", selectedCity);
            intent.putExtra("county_code",countyCode);
            startActivity(intent);
            finish();
            break;
        }
      }
    });
  }

  /**
   * 查询全国所有的省，优先重回数据库中查新，如果没有再去服务器上查询
   */
  private void queryProvinces() {
    provinceList = coolWeatherDB.loadProvinces();
    if (provinceList.size() > 0) {
      dataList.clear();
      for (Province province : provinceList) {
        dataList.add(province.getProvinceName());
      }
      adapter.notifyDataSetChanged();
      listView.setSelection(0);
      titleText.setText("中国");
      currentLevel = LEVEL_PROVINCE;
    } else {
      queryFormServer(null, "province");
    }
  }

  /**
   * 查询选中省内的所有市，优先重回数据库中查新，如果没有再去服务器上查询
   */
  private void queryCities() {
    cityList = coolWeatherDB.loadCities(selectedProvince.getId());
    if (cityList.size() > 0) {
      dataList.clear();
      for (City city : cityList) {
        dataList.add(city.getCityName());
      }
      adapter.notifyDataSetChanged();
      listView.setSelection(0);
      titleText.setText(selectedProvince.getProvinceName());
      currentLevel = LEVEL_CITY;
    } else {
      queryFormServer(selectedProvince.getProvinceCode(), "city");
    }
  }

  /**
   * 查询选中市内的所有县，优先重回数据库中查新，如果没有再去服务器上查询
   */
  private void queryCounties() {
    countyList = coolWeatherDB.loadCounties(selectedCity.getId());
    if (countyList.size() > 0) {
      dataList.clear();
      for (County county : countyList) {
        dataList.add(county.getCountyName());
      }
      adapter.notifyDataSetChanged();
      listView.setSelection(0);
      titleText.setText(selectedCity.getCityName());
      currentLevel = LEVEL_COUNTY;
    } else {
      queryFormServer(selectedProvince.getProvinceCode() + selectedCity.getCityCode(), "county");
    }
  }

  /**
   * 根据传入的代号和类型从服务器上查询省市县的数据
   */
  private void queryFormServer(final String code, final String type) {
    String address = "";
    switch (type) {
      case "province":
        address = "http://www.weather.com.cn/data/city3jdata/china.html";
        break;
      case "city":
        address = "http://www.weather.com.cn/data/city3jdata/provshi/" + code + ".html";
        break;
      case "county":
        address = "http://www.weather.com.cn/data/city3jdata/station/" + code + ".html";
        break;
      default:
        break;
    }
    showProgressDialog();
    HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
      @Override
      public void onFinish(String response) {
        boolean result = false;
        switch (type) {
          case "province":
            result = Utility.handleProvincesResponse(coolWeatherDB,response);
            break;
          case "city":
            result = Utility.handleCitiesResponse(coolWeatherDB,response,selectedProvince.getId());
            break;
          case "county":
            result = Utility.handleCountiesResponse(coolWeatherDB,response,selectedCity.getId());
            break;
        }
        if (result) {
          // 通过runOnUiThread()方法回到主线程处理逻辑
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              closeProgressDialog();
              switch (type) {
                case "province":
                  queryProvinces();
                  break;
                case "city":
                  queryCities();
                  break;
                case "county":
                  queryCounties();
                  break;
              }
            }
          });
        }
      }

      @Override
      public void onError(Exception e) {
        // 通过runOnUiThread()方法回到主线程处理逻辑
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            closeProgressDialog();
            Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
          }
        });
      }
    });
  }

  /**
   * 显示进度
   */
  private void showProgressDialog() {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(this);
      progressDialog .setMessage("正在加载...");
      progressDialog.setCanceledOnTouchOutside(false);
    }
    progressDialog.show();
  }

  /**
   * 关闭进度对话框
   */
  private void closeProgressDialog() {
    if (progressDialog != null) {
      progressDialog.dismiss();
    }
  }

  /**
   * 捕获Back按键，根据当前的级别来判断此时应该返回的市列表、省列表、还是直接退出。
   */
  @Override
  public void onBackPressed() {
    if (currentLevel == LEVEL_COUNTY) {
      queryCities();
    } else if (currentLevel == LEVEL_CITY) {
      queryProvinces();
    } else {
      if (isFromWeatherActivity) {
        Intent intent = new Intent(this, WeatherActivity.class);
        startActivity(intent);
      }
      finish();
    }
  }
}
