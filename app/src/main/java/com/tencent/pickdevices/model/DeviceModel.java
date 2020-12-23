package com.tencent.pickdevices.model;

public class DeviceModel {
    
    private String goodsName;
    private String assetNumb;
    private String top;
    private String cloud;
    
    public DeviceModel() {
    }
    
    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getAssetNumb() {
        return assetNumb;
    }

    public void setAssetNumb(String assetNumb) {
        this.assetNumb = assetNumb;
    }

    public String getTop() {
        return top;
    }

    public void setTop(String top) {
        this.top = top;
    }

    public String getCloud() {
        return cloud;
    }

    public void setCloud(String cloud) {
        this.cloud = cloud;
    }

    @Override
    public String toString() {
        return "DeviceModel{" +
                "goodsName='" + goodsName + '\'' +
                ", assetNumb='" + assetNumb + '\'' +
                ", top=" + top +
                ", cloud='" + cloud + '\'' +
                '}';
    }
}
