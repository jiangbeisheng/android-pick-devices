package com.tencent.pickdevices.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.pickdevices.R;
import com.tencent.pickdevices.model.DeviceModel;

import org.apache.log4j.chainsaw.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import jxl.Sheet;
import jxl.Workbook;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PickDevices";
    private static final int SELECT_FILE_CODE = 0x05;
    public static final String PREFS_NAME = MainActivity.class.getName();
    ArrayList<DeviceModel> devicesArray = null;
    TextView tv_info = null;
    TextView tv_top = null;
    TextView tv_cloud = null;
    TextView tv_assetNumb = null;
    TextView tv_assetName = null;
    TextView tv_excelName = null;
    Button bt_load = null;
    Button bt_clear = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        loadData();
        bt_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFile();
            }
        });
        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearScreen();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initView() {
        tv_info = findViewById(R.id.tv_info);
        tv_top = findViewById(R.id.tv_top);
        tv_cloud = findViewById(R.id.tv_cloud);
        tv_assetNumb = findViewById(R.id.tv_assetNumb);
        tv_assetName = findViewById(R.id.tv_assetName);
        tv_excelName = findViewById(R.id.tv_excelName);
        bt_load = findViewById(R.id.bt_load);
        bt_clear = findViewById(R.id.bt_clear);
    }

    public void selectFileDialog(int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher_foreground).setTitle("提示:")
                .setMessage(message)
                .setPositiveButton("选择文件", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectFile();
                    }
                }).show();
    }

    private void loadData() {
        SharedPreferences datas = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String excelUri = datas.getString("excelUri", null);
        if (excelUri != null) {
            Log.i(TAG, "excelUri: " + excelUri);
            Uri uri = Uri.parse(excelUri);
            devicesArray = getXlsFromSdcard(uri, 0);
            if (devicesArray.isEmpty()) {
                tv_excelName.setText(R.string.re_select_file);
                tv_info.setText(R.string.re_select_file);
                selectFileDialog(R.string.re_select_file);
            } else {
                Log.i(TAG, "devicesArray: " + devicesArray.toString());
                setExcelName(uri);
            }
        } else {
            tv_info.setText(R.string.please_load_excel);
        }
    }

    private void selectFile() {
        String[] mimeTypes =
                {"application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            String SHOW_ADVANCED;
            try {
                Field f = android.provider.DocumentsContract.class.getField("EXTRA_SHOW_ADVANCED");
                SHOW_ADVANCED = f.get(f.getName()).toString();
            } catch (NoSuchFieldException e) {
                Log.e(TAG, e.getMessage(), e);
                SHOW_ADVANCED = "android.content.extra.SHOW_ADVANCED";
            }
            intent.putExtra(SHOW_ADVANCED, true);
        } catch (Throwable e) {
            Log.e(TAG, "SET EXTRA_SHOW_ADVANCED", e);
        }

        try {
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, SELECT_FILE_CODE);
        } catch (Exception e) {
            Toast.makeText(this, R.string.file_select_error, Toast.LENGTH_LONG).show();
            Log.e(TAG, "START SELECT_FILE_ACTIVE FAIL", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_FILE_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
            devicesArray = getXlsFromSdcard(uri, 0);
            setExcelName(uri);
            SharedPreferences datas = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = datas.edit();
            editor.putString("excelUri", uri.toString());
            editor.apply();
        }
    }

    private void setExcelName(Uri uri) {
        String[] split = uri.getPath().split(":");
        String excelName = split[split.length - 1];
        tv_excelName.setText(excelName + " 表已加载");
        tv_info.setText(R.string.please_scan_code);
    }

    String mNumber = "";

    /**
     * 监听字符和回车输入，同时屏蔽ACTION_UP按键抬起事件
     * @param event
     * @return 返回true不再被后面的view监听，false反之
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (KeyEvent.KEYCODE_0 <= keyCode && keyCode <= KeyEvent.KEYCODE_9 && event.getAction() != KeyEvent.ACTION_UP) {
            mNumber += String.valueOf(event.getKeyCode() - KeyEvent.KEYCODE_0);
            return true;
        } else if (KeyEvent.KEYCODE_A <= keyCode && keyCode <= KeyEvent.KEYCODE_Z && event.getAction() != KeyEvent.ACTION_UP) {
            mNumber += String.valueOf((char)(event.getKeyCode() - KeyEvent.KEYCODE_A + 'A'));
            return true;
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() != KeyEvent.ACTION_UP) {
            if (devicesArray != null){
                if(devicesArray.isEmpty()){
                    selectFileDialog(R.string.re_select_file);
                }else {
                    show_devcie(mNumber);
                }
            }else {
                tv_info.setText(R.string.please_load_excel);
                selectFileDialog(R.string.please_load_excel);
            }
            mNumber = "";
        }
        return true;
    }

    private void show_devcie(String mNumber) {
        clearScreen();
        if (mNumber.equals("")){
            tv_info.setText("注意：输入为空，请正确扫码");
            return;
        }
        DeviceModel device = compareDevice(mNumber);
        if (device == null) {
            tv_info.setText("查不到: " + mNumber);
        } else {
            tv_info.setText("查询结果:");
            tv_top.setText("Top:" + device.getTop());
            tv_cloud.setText("所属云:" + device.getCloud());
            tv_assetNumb.setText("资产编号:" + device.getAssetNumb());
            tv_assetName.setText("资产名称:" + device.getGoodsName());
        }
    }

    private DeviceModel compareDevice(String assetNumb) {
        for (DeviceModel deviceModel : devicesArray) {
            if (assetNumb.equals(deviceModel.getAssetNumb())) {
                return deviceModel;
            }
        }
        return null;
    }

    private ArrayList<DeviceModel> getXlsFromSdcard(Uri uri, int index) {
        ArrayList<DeviceModel> deviceModels = new ArrayList<DeviceModel>();
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Workbook workbook = Workbook.getWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(index);
            int sheetRows = sheet.getRows();

            for (int i = 0; i < sheetRows; i++) {
                DeviceModel deviceModel = new DeviceModel();
                deviceModel.setGoodsName(sheet.getCell(0, i).getContents());
                deviceModel.setAssetNumb(sheet.getCell(1, i).getContents());
                deviceModel.setTop(sheet.getCell(2, i).getContents());
                deviceModel.setCloud(sheet.getCell(3, i).getContents());
                deviceModels.add(deviceModel);
            }

            workbook.close();

        } catch (Exception e) {
            Log.e(TAG, "read error=" + e, e);
        }

        return deviceModels;
    }

    private void clearScreen() {
        if (devicesArray !=null){
            tv_info.setText(R.string.please_scan_code);
        }else {
            tv_info.setText(R.string.please_load_excel);
        }
        tv_top.setText("Top:");
        tv_cloud.setText("所属云:");
        tv_assetNumb.setText("资产编号:");
        tv_assetName.setText("资产名称:");
    }
}