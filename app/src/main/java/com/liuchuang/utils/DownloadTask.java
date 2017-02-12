package com.liuchuang.utils;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by liuchuang on 2017/2/6.
 */

public class DownloadTask extends AsyncTask<String,Integer,Integer>{
    private static final int TYPY_SUCCESS = 0;
    private static final int TYPY_ERROR = 1;
    private static final int TYPY_PAUSED = 2;
    private static final int TYPY_CANCELED = 3;

    private DownloadListener listener;

    private boolean isCanceled = false;
    private boolean isPaused = false;
    private int lastProgress;

    public void setDownloadListener(DownloadListener listener){
        this.listener = listener;
    }
    public void pausedDownload(){
        isPaused = true;
    }
    public void canceledDownload(){
        isCanceled = true;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer){
            case TYPY_SUCCESS:
                listener.onSuccess();
                break;
            case TYPY_CANCELED:
                listener.onCanceled();
                break;
            case TYPY_PAUSED:
                listener.onPaused();
                break;
            case TYPY_ERROR:
                listener.onError();
                break;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if(progress>lastProgress){
            listener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected Integer doInBackground(String... params) {
        InputStream is = null;
        RandomAccessFile saveFile = null;
        File file = null;
        try {
            long downloadedLenght = 0;
            String downloadUrl = params[0];
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(dir+fileName);
            if(file.exists()){
                downloadedLenght = file.length();
            }
            long contentLenght = getContentLenght(downloadUrl);
            if(contentLenght == 0){
                return TYPY_ERROR;
            }else if(downloadedLenght == contentLenght){
                return TYPY_SUCCESS;
            }

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE","bytes="+downloadedLenght+"-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if(response!=null){
                is = response.body().byteStream();
                saveFile = new RandomAccessFile(file,"rw");
                saveFile.seek(downloadedLenght);
                byte[] b = new byte[1024];
                int total = 0;
                int len = 0;
                while((len=is.read(b))!=-1){
                    if(isCanceled){
                        return TYPY_CANCELED;
                    }else if(isPaused){
                        return TYPY_PAUSED;
                    }else{
                        total += len;
                        saveFile.write(b,0,len);
                        int progress = (int) ((total+downloadedLenght)*100/contentLenght);
                        publishProgress(progress);
                    }
                }
            }
            response.body().close();
            return TYPY_SUCCESS;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (is!=null){
                    is.close();
                }
                if(saveFile!=null){
                    saveFile.close();
                }
                if(isCanceled && file !=null){
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return TYPY_ERROR;
    }

    private long getContentLenght(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if(response!=null&&response.isSuccessful()){
            long contentLenght = response.body().contentLength();
            return contentLenght;
        }
        return 0;
    }
}
