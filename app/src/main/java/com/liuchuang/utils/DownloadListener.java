package com.liuchuang.utils;

/**
 * Created by liuchuang on 2017/2/6.
 */

public interface DownloadListener {
    void onProgress(int progress);
    void onSuccess();
    void onError();
    void onPaused();
    void onCanceled();
}
