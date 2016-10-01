package com.coolweather.util;

/**
 * Created by zzj on 2016/9/30.
 */
public interface HttpCallbackListener {
  void onFinish(String response);
  void onError(Exception e);
}
