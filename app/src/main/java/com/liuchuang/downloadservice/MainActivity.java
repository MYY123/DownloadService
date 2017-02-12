package com.liuchuang.downloadservice;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.liuchuang.service.DownloadService;

public class MainActivity extends AppCompatActivity {
    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
        bindService(intent,conn,BIND_AUTO_CREATE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }

    public void start(View view){
        String url = "http://sw.bos.baidu.com/sw-search-sp/software/50045684f7da6/QQ_mac_5.4.1.dmg";
        downloadBinder.startDownload(url);
    }

    public void pause(View view){
        downloadBinder.pausedDownload();

    }

    public void cancel(View view){
        downloadBinder.canceledDownload();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }
}
