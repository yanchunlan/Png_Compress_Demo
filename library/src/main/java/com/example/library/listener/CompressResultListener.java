package com.example.library.listener;

/**
 * 单张
 */
public interface CompressResultListener {
    //成功
    void onCompressSuccess(String imgPath);

    //失败
    void onCompressFailed(String imgPath, String error);
}