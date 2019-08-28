package com.example.library;

import android.content.Context;
import android.text.TextUtils;

import com.example.library.bean.Photo;
import com.example.library.config.CompressConfig;
import com.example.library.core.CompressImageUtil;
import com.example.library.listener.CompressImage;
import com.example.library.listener.CompressResultListener;

import java.io.File;
import java.util.ArrayList;

/**
 * author:  ycl
 * date:  2019/08/26 15:13
 * desc:
 */
public class CompressImageManager implements CompressImage {

    private CompressImageUtil compressImageUtil;
    private ArrayList<Photo> images;
    private CompressImage.CompressListener listener;
    private CompressConfig config;


    private CompressImageManager(Context context, ArrayList<Photo> images, CompressListener listener, CompressConfig config) {
        this.compressImageUtil = new CompressImageUtil(context, config);
        this.images = images;
        this.listener = listener;
        this.config = config;
    }

    public static CompressImage build(Context context, ArrayList<Photo> images, CompressListener listener, CompressConfig config) {
        return new CompressImageManager(context, images, listener, config);
    }

    @Override
    public void compress() {
        if (images == null || images.isEmpty()) {
            listener.onCompressFailed(images, "images isEmpty");
            return;
        }
        for (Photo image : images) {
            if (image == null) {
                listener.onCompressFailed(images, "has one images isEmpty");
                return;
            }
        }
        compress(images.get(0));
    }

    // index=0 从0开始遍历
    private void compress(final Photo image) {
        if (TextUtils.isEmpty(image.getOriginalPath())) {
            continueCompress(image, false);
            return;
        }
        File file = new File(image.getOriginalPath());
        if (!file.exists() || !file.isFile()) {
            continueCompress(image, false);
            return;
        }
        if (file.length() < config.getMaxSize()) {// 文件太小需要压缩
            continueCompress(image, true);
            return;
        }
        compressImageUtil.compress(image.getOriginalPath(), new CompressResultListener() {
            @Override
            public void onCompressSuccess(String imgPath) {
                image.setCompressPath(imgPath);
                continueCompress(image, true);
            }

            @Override
            public void onCompressFailed(String imgPath, String error) {
                continueCompress(image, false, error);
            }
        });

    }

    // 成功或失败的处理
    private void continueCompress(Photo image, boolean bool, String... error) {
        image.setCompressed(bool);
        int index = images.indexOf(image);
        if (index == images.size() - 1) {// 压缩到最后了
            callBack(error);
        } else {
            compress(images.get(index + 1));
        }
    }

    // 压缩结束
    private void callBack(String... error) {
        if (error.length > 0) { // 其实就一个异常消息，只是根据有无判断有无异常而已
            listener.onCompressFailed(images, error[0]);
            return;
        }
        for (Photo image : images) {
            if (!image.isCompressed()) {
                listener.onCompressFailed(images,"isCompressed == false");
                return;
            }
        }
        listener.onCompressSuccess(images);
    }
}
