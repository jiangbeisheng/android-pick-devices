# android-pick-devices
___
### 使用步骤
* 通过adb将apk安装到手机
> adb install [你的apk路径] 
* 通过adb将Excel文件push到手机/sdcard目录下
> adb push 5st_all.xls /sdcard/
* 打开app点击加载文件选择需要加载到Excel文件
* 将扫码抢无线USB端通过OTG转接头连接到手机上
* 扫码即可显示设备信息
