# android-pick-devices
---
### 使用步骤
* 查看你的pc上连接的手机串号

  > adb devices
* 通过adb将apk安装到手机

  > adb -s [手机串号] install [apk路径] 
  > （只有一台手机设备可省了-s [手机串号]）
* 通过adb将Excel文件push到手机/sdcard目录下

  > adb -s [手机串号] push [文件路径] /sdcard/
* 打开app点击加载文件选择需要加载的Excel文件
* 将扫码抢无线USB端通过OTG转接头连接到手机上
* 扫码即可显示设备信息
