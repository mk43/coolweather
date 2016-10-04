package com.coolweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.coolweather.service.AutoUpdateService;

/**
 * Created by zzj on 2016/10/4.
 */
public class AutoUpdateReceiver extends BroadcastReceiver{
  @Override
  public void onReceive(Context context,Intent intent) {
    Intent i = new Intent(context, AutoUpdateService.class);
    context.startActivity(i);
  }
}
