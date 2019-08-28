package com.example.library.bean;

import java.io.Serializable;

/**
 * author:  ycl
 * date:  2019/08/26 11:12
 * desc:
 */
public class Photo implements Serializable {

    //图片原始路径
    private String originalPath;
    //是否压缩过
    private boolean compressed;
    //压缩后路径
    private String compressPath;

    public Photo(String originalPath) {
        this.originalPath = originalPath;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public String getCompressPath() {
        return compressPath;
    }

    public void setCompressPath(String compressPath) {
        this.compressPath = compressPath;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "originalPath='" + originalPath + '\'' +
                ", compressed=" + compressed +
                ", compressPath='" + compressPath + '\'' +
                '}';
    }
}
