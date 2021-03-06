package com.coolweather.model;

import java.io.Serializable;

/**
 * Created by zzj on 2016/9/30.
 */

public class Province implements Serializable{
  private int id;
  private String provinceName;
  private String provinceCode;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getProvinceCode() {
    return provinceCode;
  }

  public void setProvinceCode(String provinceCode) {
    this.provinceCode = provinceCode;
  }

  public String getProvinceName() {
    return provinceName;
  }

  public void setProvinceName(String provinceName) {
    this.provinceName = provinceName;
  }
}
