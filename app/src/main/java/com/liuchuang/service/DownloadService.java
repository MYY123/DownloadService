package com.liuchuang.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.liuchuang.downloadservice.MainActivity;
import com.liuchuang.downloadservice.R;
import com.liuchuang.utils.DownloadListener;
import com.liuchuang.utils.DownloadTask;

import java.io.File;

public class DownloadService extends Service {
    private DownloadTask downloadTask;
    private DownloadBinder binder = new DownloadBinder();
    private String downloadUrl;

    public DownloadService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public class DownloadBinder extends Binder{
        public void startDownload(String url){
            if(downloadTask == null){
                downloadUrl = url;
                downloadTask = new DownloadTask();
                downloadTask.setDownloadListener(listener);
                downloadTask.execute(downloadUrl);
                startForeground(1,getNotification("开始下载",0));
            }
        }
        public void pausedDownload(){
            if(downloadTask !=null){
                downloadTask.pausedDownload();
            }
        }
        public void canceledDownload(){
            if(downloadTask !=null){
                downloadTask.canceledDownload();
            }else{
                if(downloadUrl != null){
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(dir+fileName);
                    if(file.exists()){
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                }
            }
        }
    }

    DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1,getNotification("开始下载",progress));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("下载成功",-1));

        }

        @Override
        public void onError() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("下载失败",-1));

        }

        @Override
        public void onPaused() {
            downloadTask = null;

        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
        }
    };

    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title,int progress){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if(progress>0) {
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);
        }
        return builder.build();
    }
}
