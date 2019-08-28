package com.example.png_compress_demo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.library.CompressImageManager;
import com.example.library.bean.Photo;
import com.example.library.config.CompressConfig;
import com.example.library.listener.CompressImage;
import com.example.library.utils.Constants;
import com.example.png_compress_demo.utils.CachePathUtils;
import com.example.png_compress_demo.utils.CommonUtils;
import com.example.png_compress_demo.utils.UriParseUtils;

import java.io.File;
import java.util.ArrayList;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 0x1001;

    private TextView tv;

    private ProgressDialog dialog;
    private CompressConfig compressConfig;
    private String cameraCachePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
        initCompress();

        PermissionUtils.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA},
                REQUEST_CODE_PERMISSIONS,
                okRunnable);
    }

    private Runnable okRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(MainActivity.this, "已经获得必要的权限", Toast.LENGTH_SHORT).show();
            addText("已经获得必要的权限");
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(requestCode == REQUEST_CODE_PERMISSIONS,
                grantResults,
                okRunnable,
                new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "没有获得必要的权限", Toast.LENGTH_SHORT).show();
                        addText("没有获得必要的权限");
                        finish();
                    }
                });
    }

    public void onTestLubanClick(View view) {
        String imgPath = "/storage/emulated/0/Pictures/camera_20190828_104421.jpg";
        String mCacheDir = Constants.BASE_CACHE_PATH + getPackageName() + "/cache/" + Constants.COMPRESS_CACHE;
        // 目录若不存在，需提前创建，/storage/emulated/0/Android/data/com.example.png_compress_demo/cache/compress_cache
        Log.i(TAG, "cacheDir "+mCacheDir);
        addText("");
        addText("cacheDir "+mCacheDir);

        Luban.with(this)
                .load(imgPath) //源文件
                .ignoreBy(100) //忽略100KB不压缩
                .setTargetDir(mCacheDir) //压缩后存放路径
                .filter(new CompressionPredicate() { //过滤
                    @Override
                    public boolean apply(String path) {
                        return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                    }
                })
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        Log.i(TAG, "onStart");
                        addText("onStart");
                    }

                    @Override
                    public void onSuccess(File file) {
                        Log.i(TAG, "onSuccess " + file.getAbsolutePath());
                        addText("onSuccess " + file.getAbsolutePath());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "onError " + e.toString());
                        addText("onError " + e.toString());
                    }
                }).launch();
    }

    public void onCameraClick(View view) {
        //Android 7.0 File路径的变更，需要使用FileProvider来做
        Uri outputUri;
        File file = CachePathUtils.getCameraCacheFile();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            outputUri = UriParseUtils.getCameraOutPutUri(this, file);
        } else {
            outputUri = Uri.fromFile(file);
        }
        cameraCachePath = file.getAbsolutePath();
        //启动拍照
        CommonUtils.hasCamera(this, CommonUtils.getCameraIntent(outputUri), Constants.CAMERA_CODE);
    }

    public void onAlbumClick(View view) {
        CommonUtils.openAlbum(this, Constants.ALBUM_CODE);
    }

    private void initCompress() {
        compressConfig = CompressConfig.builder()
                .setUnCompressMinPixel(1000)
                .setUnCompressNormalPixel(2000)
                .setMaxPixel(1000)
                .setMaxSize(100 * 1024)
                .enablePixelCompress(true)
                .enableQualityCompress(true)
                .enableReserveRaw(true)
                .setCacheDir("")
                .setShowCompressDialog(true)
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.CAMERA_CODE && resultCode == RESULT_OK) {// camera
            // 暂时是单张
            preCompress(cameraCachePath);
        } else if (requestCode == Constants.ALBUM_CODE && resultCode == RESULT_OK) { // album
            if (data != null && data.getData() != null) {
                String path = UriParseUtils.getPath(this, data.getData());
                preCompress(path);
            }
        }
    }

    private void preCompress(String path) {
        Log.i(TAG, "preCompress path " + path);
        addText("");
        addText("preCompress path " + path);

        ArrayList<Photo> photos = new ArrayList<>();
        photos.add(new Photo(path));
        if (!photos.isEmpty()) {
            compress(photos);
        }
    }


    private void compress(ArrayList<Photo> photos) {
        if (compressConfig.isShowCompressDialog()) {
            dialog = CommonUtils.showProgressDialog(this, "图片压缩中......");
        }
        CompressImageManager
                .build(this, photos, new CompressImage.CompressListener() {
                    @Override
                    public void onCompressSuccess(ArrayList<Photo> images) {
                        Log.i(TAG, "onCompressSuccess " + images.toString());
                        addText("onCompressSuccess " + images.toString());
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onCompressFailed(ArrayList<Photo> images, String error) {
                        Log.i(TAG, "onCompressFailed " + error);
                        addText("onCompressFailed " + error);
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                }, compressConfig)
                .compress();
    }

    private void addText(String path) {
        if (tv == null) {
            return;
        }
        tv.append(path);
        tv.append("\n");
    }
}
